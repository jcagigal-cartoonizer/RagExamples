package ifac.td.taxi.ui.screen.components
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
// # Block 220-2: import android.app.Application
data class DashboardUiState(
    val buttonsState: DashboardButtonsState = DashboardButtonsState(),
    val nearbyZones: List<ZoneModel> = emptyList(),
    val farZones: List<ZoneModel> = emptyList(),
    val actualZones: List<ZoneModel> = emptyList(),
    val pendingTrips: List<PendingTrip> = emptyList(),
    val dialogState: DashboardDialogState = DashboardDialogState(),
    val loading: Boolean = false,
)
sealed interface DashboardUiEvent {
    data object Init : DashboardUiEvent
    data object OnResume : DashboardUiEvent
    data object OnPause : DashboardUiEvent
    data class ZoneDataTypesChanged(val dataTypes: String) : DashboardUiEvent
    data class NearbyZonesChanged(val zones: List<ZoneModel>) : DashboardUiEvent
    data class FarZonesChanged(val zones: List<ZoneModel>) : DashboardUiEvent
    data class ActualZonesChanged(val zones: List<ZoneModel>) : DashboardUiEvent
    data class PendingTripsChanged(val trips: List<PendingTrip>) : DashboardUiEvent
    data class HeaderClicked(val action: DashboardHeaderAction) : DashboardUiEvent
    data class ZoneClicked(val zone: ZoneModel) : DashboardUiEvent
    data class ZoneLongClicked(val zone: ZoneModel) : DashboardUiEvent
    data object DismissDialog : DashboardUiEvent
    data object ConfirmDialog : DashboardUiEvent
    data object CancelDialog : DashboardUiEvent
}
sealed interface DashboardUiEffect {
    data object NavigateBack : DashboardUiEffect
    data class NavigateToZoneDetails(val zone: ZoneModel) : DashboardUiEffect
    data object ShowDialog : DashboardUiEffect
    data object HideDialog : DashboardUiEffect
}
data class DashboardDialogState(
    val visible: Boolean = false,
    val title: String = "",
    val message: String = "",
    val primaryText: String = "OK",
    val secondaryText: String = "Cancel",
)
enum class DashboardHeaderAction {
    ON_STOP, ON_ZONE, HIRED, TRIPS
}
class DashboardComposeViewModel(
    private val pendingTripsUseCase: PendingTripsUseCase,
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    application: Application,
) : BaseViewModel(application) {
    private val handler = Handler(Looper.getMainLooper())
    private val pendingTripsHandler = Handler(Looper.getMainLooper())
    private var zonesRunnable: Runnable? = null
    private var pendingTripsRunnable: Runnable? = null
    var bravoConfiguration: BravoConfigurationVariableEntity? = null
    var isCustomersAtStand: Boolean = false
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<DashboardUiEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()
    init {
        viewModelScope.launch(Dispatchers.IO) {
            licensingUseCase.getLicensingParameters()?.isCustomersAtStand?.let {
                isCustomersAtStand = it
            }
        }
    }
    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            DashboardUiEvent.Init -> initViewModelData()
            DashboardUiEvent.OnResume -> loadTrips()
            DashboardUiEvent.OnPause -> removeHandlerCallbacks()
            is DashboardUiEvent.ZoneDataTypesChanged -> {
                val buttonsState = DashboardButtonsState.fromDataTypes(event.dataTypes)
                _uiState.update { it.copy(buttonsState = buttonsState) }
            }
            is DashboardUiEvent.NearbyZonesChanged -> {
                _uiState.update { it.copy(nearbyZones = event.zones) }
            }
            is DashboardUiEvent.FarZonesChanged -> {
                _uiState.update { it.copy(farZones = event.zones) }
            }
            is DashboardUiEvent.ActualZonesChanged -> {
                _uiState.update { it.copy(actualZones = event.zones) }
            }
            is DashboardUiEvent.PendingTripsChanged -> {
                _uiState.update { it.copy(pendingTrips = event.trips) }
            }
            is DashboardUiEvent.HeaderClicked -> {
                // Replace with your header-sort logic if needed
            }
            is DashboardUiEvent.ZoneClicked -> {
                viewModelScope.launch {
                    _effects.emit(DashboardUiEffect.NavigateToZoneDetails(event.zone))
                }
            }
            is DashboardUiEvent.ZoneLongClicked -> {
                _uiState.update {
                    it.copy(
                        dialogState = DashboardDialogState(
                            visible = true,
                            title = "Zone options",
                            message = "Long press action for zone",
                            primaryText = "Accept",
                            secondaryText = "Cancel"
                        )
                    )
                }
                viewModelScope.launch {
                    _effects.emit(DashboardUiEffect.ShowDialog)
                }
            }
            DashboardUiEvent.DismissDialog,
            DashboardUiEvent.CancelDialog -> {
                _uiState.update { it.copy(dialogState = it.dialogState.copy(visible = false)) }
                viewModelScope.launch { _effects.emit(DashboardUiEffect.HideDialog) }
            }
            DashboardUiEvent.ConfirmDialog -> {
                _uiState.update { it.copy(dialogState = it.dialogState.copy(visible = false)) }
                viewModelScope.launch { _effects.emit(DashboardUiEffect.HideDialog) }
            }
        }
    }
    fun loadTrips() {
        if (BravoCentral.isPendingTripsAllowed(false)) {
            refreshPendingTripsManual()
        }
        startChronometer()
    }
    fun refreshPendingTripsManual() {
        viewModelScope.launch {
            pendingTripsUseCase.getPendingTripsZone(
                flow = MutableStateFlow<ArrayList<PendingTrip>?>(null)
            )
        }
    }
    fun startChronometer() {
        viewModelScope.launch(Dispatchers.IO) {
            val refreshRate = 2000L
            val initRefreshRate = 500L
            pendingTripsRunnable = Runnable {
                viewModelScope.launch {
                    if (BravoCentral.isPendingTripsAllowed(true)) {
                        pendingTripsUseCase.getPendingTripsZone(
                            flow = MutableStateFlow<ArrayList<PendingTrip>?>(null)
                        )
                    }
                }
                pendingTripsHandler.postDelayed(pendingTripsRunnable!!, refreshRate)
            }
            pendingTripsRunnable?.let { pendingTripsHandler.postDelayed(it, initRefreshRate) }
        }
    }
    fun initViewModelData() {
        viewModelScope.launch {
            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion
            refreshRate?.let { seconds ->
                val millis = (seconds * 1000).toLong()
                var lastZoningUpdate = zoningUseCase.getLastZoningUpdate()
                val shouldLoadData = (System.currentTimeMillis() - lastZoningUpdate) < millis
                if (shouldLoadData) {
                    updateFromZoneSource()
                }
                zonesRunnable = Runnable {
                    viewModelScope.launch {
                        val newCall = (System.currentTimeMillis() - lastZoningUpdate) > millis
                        if (newCall) {
                            zoningUseCase.getZonesInformation(
                                W2CLocation.getZoning().macrozones[0],
                                { _, availableColumns ->
                                    updateFromZoneSource()
                                    onEvent(DashboardUiEvent.ZoneDataTypesChanged(availableColumns))
                                }
                            )
                            lastZoningUpdate = System.currentTimeMillis()
                            zoningUseCase.setLastZoningUpdate()
                        }
                    }
                    handler.postDelayed(zonesRunnable!!, 1000)
                }
                handler.post(zonesRunnable!!)
            }
        }
    }
    fun updateFromZoneSource() {
        val zonesList = mutableListOf<ZoneModel>()
        W2CLocation.getZoning().macrozones.forEach { mz ->
            zonesList.addAll(mz.zones.map { zone -> ZoneModel(zone, false) })
        }
        viewModelScope.launch {
            filterData(zonesList)
            _uiState.update {
                it.copy(
                    buttonsState = DashboardButtonsState.fromDataTypes(
                        W2CLocation.getZoning().availableColumns
                    )
                )
            }
        }
    }
    private suspend fun filterData(zonesList: List<ZoneModel>) {
        val zonesWithMoreTrips = zonesList.filter {
            ZoneUtils.checkDiff(it.zone, W2CLocation.getZoning().availableColumns) > 0
        }
        val (farZones, nearbyZones) = zonesWithMoreTrips.partition {
            getDistanceFromLatLonInMetersLocale(
                W2CLocation.getLastLatLong(),
                it.zone.centralCoord
            ) > 5000
        }
        val nearbyZonesSorted = nearbyZones.sortedWith(
            compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, W2CLocation.getZoning().availableColumns) }
                .thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(), it.zone.centralCoord) }
        )
        val farZonesSorted = farZones.sortedWith(
            compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, W2CLocation.getZoning().availableColumns) }
                .thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(), it.zone.centralCoord) }
        )
        _uiState.update {
            it.copy(
                nearbyZones = nearbyZonesSorted,
                farZones = farZonesSorted,
                actualZones = buildActualZones()
            )
        }
    }
    fun buildActualZones(): List<ZoneModel> {
        val actualZoneList = mutableListOf<ZoneModel>()
        val lastZoneSent = W2CLocation.getZoning()
            .getZoneById(W2CLocation.getLastIdMacrozoneSent(), W2CLocation.getLastIdZoneSent())
        val lastZoneLocated = W2CLocation.getZoneFromLatLong(W2CLocation.getLastLatLong())
        if (lastZoneSent != null) actualZoneList.add(ZoneModel(lastZoneSent, false))
        if (lastZoneLocated != null) actualZoneList.add(ZoneModel(lastZoneLocated, false))
        return actualZoneList
    }
    fun getDistanceFromLatLonInMetersLocale(
        lastLatLong: LatLong?,
        centralCoord: LatLong?
    ): Int {
        if (lastLatLong == null || centralCoord == null || lastLatLong.Lat.isNaN() || centralCoord.Lat.isNaN()) {
            return Int.MAX_VALUE
        }
        return W2CLocation.getDistanceFromLatLonInMeters(lastLatLong, centralCoord)
    }
    fun removeHandlerCallbacks() {
        pendingTripsRunnable?.let { pendingTripsHandler.removeCallbacks(it) }
        zonesRunnable?.let { handler.removeCallbacks(it) }
    }
    override fun onCleared() {
        removeHandlerCallbacks()
        super.onCleared()
    }
}
