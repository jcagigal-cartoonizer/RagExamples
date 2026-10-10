package ifac.td.taxi.compose.viewmodel
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.viewmodel.DashboardViewModel
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.DashboardScreen
// # Block 130-4: import android.app.Application
class DashboardComposeViewModel(
    private val pendingTripsUseCase: PendingTripsUseCase,
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    context: Application,
) : BaseViewModel(context) {
    fun fromViewModel(viewModel: DashboardViewModel): DashboardComposeViewModel {
        val composeViewModel = DashboardComposeViewModel(
            context = viewModel.context,
            licensingUseCase = viewModel.licensingUseCase,
            zoningUseCase = viewModel.zoningUseCase,
            pendingTripsUseCase = viewModel.pendingTripsUseCase,
        )
        return composeViewModel
    }

    private val handler = Handler(Looper.getMainLooper())
    private val pendingTripsHandler = Handler(Looper.getMainLooper())
    private var zonesRunnable: Runnable? = null
    private var pendingTripsRunnable: Runnable? = null
    var bravoConfiguration: BravoConfigurationVariableEntity? = null
    var isCustomersAtStand: Boolean = false
    lateinit var buttonsState : DashboardButtonsState
    lateinit var dialogState : DashboardDialogState
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffects = MutableSharedFlow<DashboardUiEffect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiEffects = _uiEffects.asSharedFlow()
    private val _zonesListFlow = MutableStateFlow<List<ZoneModel>?>(null)
    private val _nearbyZonesListSortedFlow = MutableStateFlow<List<ZoneModel>?>(null)
    private val _farZonesListSortedFlow = MutableStateFlow<List<ZoneModel>?>(null)
    private val _actualZoneListSortedFlow = MutableStateFlow<List<ZoneModel>?>(null)
    private val _zoningListDataTypes = MutableStateFlow<String?>(null)
    init {
        dialogState = _uiState.value.dialog
        viewModelScope.launch(Dispatchers.IO) {
            licensingUseCase.getLicensingParameters()?.isCustomersAtStand?.let {
                isCustomersAtStand = it
            }
        }
        viewModelScope.launch {
            combine(
                _nearbyZonesListSortedFlow,
                _farZonesListSortedFlow,
                _actualZoneListSortedFlow,
                _zoningListDataTypes
            ) { nearby, far, actual, dataTypes ->
                val header = buildHeaderState(dataTypes.orEmpty())
                val buttons = buildButtonsState()
                _uiState.value.copy(
                    nearbyZones = nearby.orEmpty(),
                    farZones = far.orEmpty(),
                    actualZones = actual.orEmpty(),
                    headerState = header,
                    buttonsState = buttons,
                    isLoadingZones = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
        viewModelScope.launch {
            _zonesListFlow.collect { zones ->
                zones?.let { filterData(it) }
            }
        }
    }
    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            DashboardUiEvent.OnResume -> {
                initViewModelData()
                loadTrips(sharedPendingTripsFlow())
            }
            DashboardUiEvent.OnPause -> removeHandlerCallbacks()
            DashboardUiEvent.OnBackPressed,
            DashboardUiEvent.OnCancelClicked -> {
viewModelScope.launch { emitEffect(DashboardUiEffect.NavigateBack)
}
}
            DashboardUiEvent.OnCloseClicked -> handleClose()
            DashboardUiEvent.OnShowAllTripsClicked -> {
                _uiState.update {
                    it.copy(buttonsState = it.buttonsState.copy(
                        showShowAll = false,
                        showShowRecent = true
                    ))
                }
            }
            DashboardUiEvent.OnShowRecentTripsClicked -> {
                _uiState.update {
                    it.copy(buttonsState = it.buttonsState.copy(
                        showShowAll = true,
                        showShowRecent = false
                    ))
                }
            }
            DashboardUiEvent.OnDismissDialog -> {
                _uiState.update { it.copy(dialogState = DashboardDialogState.Hidden) }
                emitEffect(DashboardUiEffect.CloseDialog)
            }
            DashboardUiEvent.OnConfirmDialog -> {
                _uiState.update { it.copy(dialogState = DashboardDialogState.Hidden) }
                emitEffect(DashboardUiEffect.NavigateToHome)
            }
            is DashboardUiEvent.OnZoneClicked -> Unit
            is DashboardUiEvent.OnZoneLongClicked -> Unit
        }
    }
    fun handleClose() {
        when (sharedShiftStatus()) {
            "STATE_HIRED",
            "STATE_DISPATCHED",
            "STATE_HIRED_DISPATCHED",
            "STATE_HIRED_NO_CENTRAL" -> {
                _uiState.update { it.copy(dialogState = DashboardDialogState.DelocateOnHired) }
                emitEffect(DashboardUiEffect.OpenDelocateDialog)
            }
            else -> {
viewModelScope.launch { emitEffect(DashboardUiEffect.NavigateToHome)
}
}
        }
    }
    fun sharedShiftStatus(): String? = null
    fun emitEffect(effect: DashboardUiEffect) {
        viewModelScope.launch { _uiEffects.emit(effect) }
    }
    fun buildHeaderState(dataTypes: String): DashboardHeaderState {
        val columns = ZoneUtils.parseStringDataTypes(dataTypes)
        return DashboardHeaderState(
            showStand = columns.contains(com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum.STAND_VEHICLES),
            showZone = columns.contains(com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum.ZONE_VEHICLES),
            showHired = columns.contains(com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum.HIRED_VEHICLES) ||
                    columns.contains(com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum.BOOKED_TRIPS),
            showTrips = columns.contains(com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum.PREBOOKED_TRIPS) ||
                    columns.contains(com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum.TOTAL_TRIPS) ||
                    (columns.contains(com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum.HIRED_VEHICLES) &&
                            columns.contains(com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum.BOOKED_TRIPS)),
            standText = "Stand",
            zoneText = "Zone",
            hiredText = if (columns.contains(com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum.HIRED_VEHICLES)) "Hired" else "Serv.",
            tripsText = if (columns.contains(com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum.PREBOOKED_TRIPS)) "Pre" else "Trips"
        )
    }
    fun buildButtonsState(): DashboardButtonsState {
        return DashboardButtonsState(
            showShowAll = true,
            showShowRecent = false,
            showCancel = true,
            showClose = true,
            showAllEnabled = false,
            showRecentEnabled = true,
            cancelEnabled = true,
            closeEnabled = true,
            showAllStyle = DashboardButtonStyles.DisabledBlue,
            showRecentStyle = DashboardButtonStyles.EnabledBlue,
            cancelStyle = DashboardButtonStyles.EnabledGray,
            closeStyle = DashboardButtonStyles.EnabledRed
        )
    }
    fun initViewModelData() {
        viewModelScope.launch {
            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion
            refreshRate?.let { seconds ->
                val millis = (seconds * 1000).toLong()
                var lastZoningUpdate = zoningUseCase.getLastZoningUpdate()
                if ((System.currentTimeMillis() - lastZoningUpdate) < millis) {
                    zoneCallback.invoke(W2CLocation.getZoning().macrozones[0].zones, W2CLocation.getZoning().availableColumns)
                }
                zonesRunnable = object : Runnable {
                    override fun run() {
                        viewModelScope.launch {
                            if ((System.currentTimeMillis() - lastZoningUpdate) > millis) {
                                zoningUseCase.getZonesInformation(
                                    W2CLocation.getZoning().macrozones[0],
                                    zoneCallback,
                                )
                                lastZoningUpdate = System.currentTimeMillis()
                                zoningUseCase.setLastZoningUpdate()
                            }
                        }
                        handler.postDelayed(this, 1000)
                    }
                }
                zonesRunnable?.let { handler.post(it) }
            }
        }
    }
    private val zoneCallback: (List<Zone>, String) -> Unit = { _, availableColumns ->
        viewModelScope.launch(Dispatchers.IO) {
            val zonesList = mutableListOf<ZoneModel>()
            W2CLocation.getZoning().macrozones.forEach { mz ->
                zonesList.addAll(mz.zones.map { zone -> ZoneModel(zone, false) })
            }
            _zonesListFlow.emit(zonesList)
            if (availableColumns.isNotBlank()) _zoningListDataTypes.emit(availableColumns)
        }
    }
    private suspend fun filterData(zonesList: List<ZoneModel>) {
        val zonesWithMoreTrips = zonesList.filter {
            ZoneUtils.checkDiff(it.zone, _zoningListDataTypes.value ?: "") > 0
        }
        val (farZones, nearbyZones) = zonesWithMoreTrips.partition {
            getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(), it.zone.centralCoord) > 5000
        }
        _nearbyZonesListSortedFlow.emit(
            nearbyZones.sortedWith(
                compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, _zoningListDataTypes.value ?: "") }
                    .thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(), it.zone.centralCoord) }
            )
        )
        _farZonesListSortedFlow.emit(
            farZones.sortedWith(
                compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, _zoningListDataTypes.value ?: "") }
                    .thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(), it.zone.centralCoord) }
            )
        )
        refreshActualZoneData()
    }
    fun refreshActualZoneData() {
        viewModelScope.launch {
            val lastZoneSent = W2CLocation.getZoning().getZoneById(
                W2CLocation.getLastIdMacrozoneSent(),
                W2CLocation.getLastIdZoneSent()
            )
            val lastZoneLocated = W2CLocation.getZoneFromLatLong(W2CLocation.getLastLatLong())
            val actualZoneList = mutableListOf<ZoneModel>()
            if (lastZoneSent != null) actualZoneList.add(ZoneModel(lastZoneSent, false))
            if (lastZoneLocated != null) actualZoneList.add(ZoneModel(lastZoneLocated, false))
            _actualZoneListSortedFlow.emit(actualZoneList)
        }
    }
    fun loadTrips(flow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        if (BravoCentral.isPendingTripsAllowed(false)) {
            viewModelScope.launch { pendingTripsUseCase.getPendingTripsZone(flow) }
        }
        startChronometer(flow)
    }
    fun startChronometer(mFlow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        viewModelScope.launch(Dispatchers.IO) {
            pendingTripsRunnable = object : Runnable {
                override fun run() {
                    viewModelScope.launch {
                        if (BravoCentral.isPendingTripsAllowed(true)) {
                            pendingTripsUseCase.getPendingTripsZone(mFlow)
                        }
                    }
                    pendingTripsHandler.postDelayed(this, 2000L)
                }
            }
            pendingTripsRunnable?.let { pendingTripsHandler.postDelayed(it, 500L) }
        }
    }
    fun sharedPendingTripsFlow(): MutableStateFlow<ArrayList<PendingTrip>?> {
        return MutableStateFlow(null)
    }
    fun getDistanceFromLatLonInMetersLocale(lastLatLong: LatLong?, centralCoord: LatLong?): Int {
        if (lastLatLong == null || lastLatLong.Lat.isNaN() || centralCoord == null || centralCoord.Lat.isNaN()) {
            return Int.MAX_VALUE
        }
        return W2CLocation.getDistanceFromLatLonInMeters(lastLatLong, centralCoord)
    }
    fun removeHandlerCallbacks() {
        pendingTripsRunnable?.let { pendingTripsHandler.removeCallbacks(it) }
        zonesRunnable?.let { handler.removeCallbacks(it) }
    }
}
