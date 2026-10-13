package ifac.td.taxi.ui.screen.compose

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.interfacom.sdk.taximeter.bravocomm.BravoCentral
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import com.interfacom.sdk.taximeter.licensing.models.zoning.LatLong
import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
import ifac.td.taxi.HomeDirections
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.PendingTripsUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import ifac.td.taxi.repository.room.entities.BravoConfigurationVariableEntity
import ifac.td.taxi.ui.model.ZoneModel
import ifac.td.taxi.ui.util.ZoneUtils
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ------------------------------------------------------------
// SHARED VM ADAPTER
// ------------------------------------------------------------

class DashboardSharedVmAdapter(
    private val sharedViewModel: MainActivityViewModel
) {
    val pendingTripsListFlow = sharedViewModel.pendingTripsListFlow
    val zoningChangedFlow = sharedViewModel.zoningChangedFlow
    val bravoStateFlow = sharedViewModel.bravoStateFlow

    fun emitManualZoningNavigation(enabled: Boolean) {
        sharedViewModel.emitManualZoningNavigation(enabled)
    }

    fun saveHiredZone(zone: Any?) {
        sharedViewModel.saveHiredZone(zone)
    }

    fun launchIntent(intent: android.content.Intent) {
        sharedViewModel.launchIntent(intent)
    }

    fun navigateBack() {
        sharedViewModel.navigateBack()
    }
}

// ------------------------------------------------------------
// UI STATE
// ------------------------------------------------------------

@Immutable
data class DashboardUiState(
    val nearbyZones: List<ZoneModel> = emptyList(),
    val farZones: List<ZoneModel> = emptyList(),
    val actualZones: List<ZoneModel> = emptyList(),
    val pendingTripsCount: Int = 0,
    val headerState: DashboardHeaderState = DashboardHeaderState(),
    val buttonsState: DashboardButtonsState = DashboardButtonsState(),
    val customDialog: DashboardDialogState? = null
)

@Immutable
data class DashboardHeaderState(
    val showStand: Boolean = false,
    val showZone: Boolean = false,
    val showHired: Boolean = false,
    val showTrips: Boolean = false,
    @StringRes val hiredTextRes: Int? = null,
    @StringRes val tripsTextRes: Int? = null
)

@Immutable
data class DashboardButtonsState(
    val locateOnHired: DashboardButtonUiState = DashboardButtonUiState.Hidden,
    val showAllTrips: DashboardButtonUiState = DashboardButtonUiState.Hidden,
    val showRecentTrips: DashboardButtonUiState = DashboardButtonUiState.Hidden,
    val cancel: DashboardButtonUiState = DashboardButtonUiState.Visible(
        textRes = R.string.btn_cancel,
        iconRes = null,
        background = DashboardButtonBackground.BLUE,
        enabled = true
    ),
    val close: DashboardButtonUiState = DashboardButtonUiState.Visible(
        textRes = R.string.btn_close,
        iconRes = null,
        background = DashboardButtonBackground.BLUE,
        enabled = true
    )
)

@Immutable
sealed interface DashboardButtonUiState {
    data object Hidden : DashboardButtonUiState

    data class Visible(
        @StringRes val textRes: Int,
        @DrawableRes val iconRes: Int? = null,
        val background: DashboardButtonBackground,
        val enabled: Boolean = true
    ) : DashboardButtonUiState
}

enum class DashboardButtonBackground {
    BLUE, RED, DISABLE
}

@Immutable
data class DashboardDialogState(
    val title: String,
    val message: String,
    val confirmText: String,
    val dismissText: String
)

// ------------------------------------------------------------
// UI EVENTS / EFFECTS
// ------------------------------------------------------------

sealed interface DashboardUiEvent {
    data object OnResume : DashboardUiEvent
    data object OnPause : DashboardUiEvent
    data object OnBackPressed : DashboardUiEvent
    data object OnLocateOnHiredClick : DashboardUiEvent
    data object OnShowAllTripsClick : DashboardUiEvent
    data object OnShowRecentTripsClick : DashboardUiEvent
    data object OnCancelClick : DashboardUiEvent
    data object OnCloseClick : DashboardUiEvent
    data class OnNearbyZoneClick(val item: ZoneModel) : DashboardUiEvent
    data class OnFarZoneClick(val item: ZoneModel) : DashboardUiEvent
    data class OnActualZoneClick(val item: ZoneModel) : DashboardUiEvent
    data object OnDialogConfirm : DashboardUiEvent
    data object OnDialogDismiss : DashboardUiEvent
}

sealed interface DashboardUiEffect {
    data object NavigateBack : DashboardUiEffect
    data object NavigateToHome : DashboardUiEffect
    data object NavigateToOnTrip : DashboardUiEffect
    data class ShowDialog(val dialog: DashboardDialogState) : DashboardUiEffect
    data class ShowMessage(val message: String) : DashboardUiEffect
}

// ------------------------------------------------------------
// SHARED STATE / EFFECTS
// ------------------------------------------------------------

@Immutable
data class SharedDashboardState(
    val pendingTrips: List<PendingTrip> = emptyList(),
    val pendingTripsCount: Int = 0,
    val isInSoonInZone: Boolean = false
)

sealed interface DashboardSharedEffect {
    data object RefreshActualZoneData : DashboardSharedEffect
    data object PendingTripsUpdated : DashboardSharedEffect
    data object SoonInZoneChanged : DashboardSharedEffect
}

// ------------------------------------------------------------
// REDUCER
// ------------------------------------------------------------

class DashboardButtonsReducer {
    fun reduceSoonInZonePresence(
        current: DashboardButtonsState,
        hasSoonInZone: Boolean
    ): DashboardButtonsState {
        return if (hasSoonInZone) {
            current.copy(
                locateOnHired = DashboardButtonUiState.Visible(
                    textRes = R.string.btn_delocate,
                    iconRes = R.drawable.ubdesact,
                    background = DashboardButtonBackground.RED,
                    enabled = true
                )
            )
        } else {
            current.copy(
                locateOnHired = DashboardButtonUiState.Visible(
                    textRes = R.string.btn_soon_to_clear,
                    iconRes = R.drawable.ubactivar,
                    background = DashboardButtonBackground.BLUE,
                    enabled = true
                )
            )
        }
    }
}

// ------------------------------------------------------------
// SHARED VM BRIDGE
// ------------------------------------------------------------

class DashboardMainActivityViewModel(
    private val sharedViewModel: MainActivityViewModel
) : ViewModel() {

    private val _sharedState = MutableStateFlow(SharedDashboardState())
    val sharedState: StateFlow<SharedDashboardState> = _sharedState.asStateFlow()

    private val _effects = MutableSharedFlow<DashboardSharedEffect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<DashboardSharedEffect> = _effects

    fun start() {
        viewModelScope.launch {
            sharedViewModel.pendingTripsListFlow.collectLatest { trips ->
                val list = trips ?: arrayListOf()
                _sharedState.update {
                    it.copy(
                        pendingTrips = list,
                        pendingTripsCount = list.size
                    )
                }
                _effects.tryEmit(DashboardSharedEffect.PendingTripsUpdated)
            }
        }

        viewModelScope.launch {
            sharedViewModel.zoningChangedFlow.collectLatest {
                _effects.tryEmit(DashboardSharedEffect.RefreshActualZoneData)
            }
        }

        viewModelScope.launch {
            sharedViewModel.bravoStateFlow.collectLatest { bravoState ->
                _sharedState.update {
                    it.copy(isInSoonInZone = bravoState.isInSoonInZone)
                }
                _effects.tryEmit(DashboardSharedEffect.SoonInZoneChanged)
            }
        }
    }

    fun emitManualZoningNavigation(enabled: Boolean) {
        sharedViewModel.emitManualZoningNavigation(enabled)
    }

    fun navigateBack() {
        sharedViewModel.navigateBack()
    }
}

// ------------------------------------------------------------
// DASHBOARD VIEWMODEL
// ------------------------------------------------------------

class DashboardComposeViewModel(
    private val pendingTripsUseCase: PendingTripsUseCase,
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val sharedBridge: DashboardMainActivityViewModel,
    application: Application
) : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())
    private var zonesRunnable: Runnable? = null

    private val pendingTripsHandler = Handler(Looper.getMainLooper())
    private var pendingTripsRunnable: Runnable? = null

    private val reducer = DashboardButtonsReducer()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<DashboardUiEffect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiEffect: SharedFlow<DashboardUiEffect> = _uiEffect

    var bravoConfiguration: BravoConfigurationVariableEntity? = null
    var isCustomersAtStand: Boolean = false

    init {
        viewModelScope.launch {
            sharedBridge.sharedState.collectLatest { shared ->
                _uiState.update { current ->
                    current.copy(
                        pendingTripsCount = shared.pendingTripsCount,
                        buttonsState = reducer.reduceSoonInZonePresence(
                            current = current.buttonsState,
                            hasSoonInZone = shared.isInSoonInZone
                        )
                    )
                }
            }
        }
    }

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            DashboardUiEvent.OnResume -> {
                initViewModelData()
                loadTrips()
            }

            DashboardUiEvent.OnPause -> removeHandlerCallbacks()

            DashboardUiEvent.OnBackPressed -> {
                viewModelScope.launch { _uiEffect.emit(DashboardUiEffect.NavigateBack) }
            }

            DashboardUiEvent.OnLocateOnHiredClick -> Unit
            DashboardUiEvent.OnShowAllTripsClick -> Unit
            DashboardUiEvent.OnShowRecentTripsClick -> Unit

            DashboardUiEvent.OnCancelClick -> {
                viewModelScope.launch { _uiEffect.emit(DashboardUiEffect.NavigateBack) }
            }

            DashboardUiEvent.OnCloseClick -> {
                viewModelScope.launch { _uiEffect.emit(DashboardUiEffect.NavigateToHome) }
            }

            is DashboardUiEvent.OnNearbyZoneClick -> Unit
            is DashboardUiEvent.OnFarZoneClick -> Unit
            is DashboardUiEvent.OnActualZoneClick -> Unit

            DashboardUiEvent.OnDialogConfirm -> _uiState.update { it.copy(customDialog = null) }
            DashboardUiEvent.OnDialogDismiss -> _uiState.update { it.copy(customDialog = null) }
        }
    }

    fun loadTrips() {
        if (BravoCentral.isPendingTripsAllowed(false)) {
            refreshPendingTripsManual()
        }
        startChronometer()
    }

    private fun refreshPendingTripsManual() {
        viewModelScope.launch {
            pendingTripsUseCase.getPendingTripsZone(
                flow = MutableStateFlow<ArrayList<PendingTrip>?>(null)
            )
        }
    }

    private fun startChronometer() {
        viewModelScope.launch {
            val refreshRate = 2000L
            val initRefreshRate = 500L

            pendingTripsRunnable = object : Runnable {
                override fun run() {
                    viewModelScope.launch {
                        if (BravoCentral.isPendingTripsAllowed(true)) {
                            pendingTripsUseCase.getPendingTripsZone(
                                flow = MutableStateFlow<ArrayList<PendingTrip>?>(null)
                            )
                        }
                    }
                    pendingTripsHandler.postDelayed(this, refreshRate)
                }
            }
            pendingTripsRunnable?.let {
                pendingTripsHandler.postDelayed(it, initRefreshRate)
            }
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
                    zoneCallback(
                        W2CLocation.getZoning().macrozones[0].zones,
                        W2CLocation.getZoning().availableColumns
                    )
                }

                zonesRunnable = object : Runnable {
                    override fun run() {
                        viewModelScope.launch {
                            val newCall = (System.currentTimeMillis() - lastZoningUpdate) > millis
                            if (newCall) {
                                zoningUseCase.getZonesInformation(
                                    W2CLocation.getZoning().macrozones[0],
                                    zoneCallback
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
        viewModelScope.launch {
            val zonesList = mutableListOf<ZoneModel>()
            W2CLocation.getZoning().macrozones.forEach { mz ->
                zonesList.addAll(mz.zones.map { zone -> ZoneModel(zone, false) })
            }

            filterData(zonesList)
            updateHeader(availableColumns)
        }
    }

    private fun updateHeader(dataTypes: String) {
        val columns = ZoneUtils.parseStringDataTypes(dataTypes)

        val showStand = columns.contains(AvailableColumnsEnum.STAND_VEHICLES)
        val showZone = columns.contains(AvailableColumnsEnum.ZONE_VEHICLES)
        val showHired = columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) ||
                columns.contains(AvailableColumnsEnum.BOOKED_TRIPS)
        val showTrips = columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS) ||
                columns.contains(AvailableColumnsEnum.TOTAL_TRIPS) ||
                (columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) &&
                        columns.contains(AvailableColumnsEnum.BOOKED_TRIPS))

        val hiredTextRes = if (!columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) &&
            columns.contains(AvailableColumnsEnum.BOOKED_TRIPS)
        ) R.string.abrevUbServiciosWithoutDots else null

        val tripsTextRes = when {
            columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS) -> R.string.abbrevPreBookedTrips
            columns.contains(AvailableColumnsEnum.TOTAL_TRIPS) -> R.string.abbrevTotalTrips
            else -> null
        }

        _uiState.update {
            it.copy(
                headerState = DashboardHeaderState(
                    showStand = showStand,
                    showZone = showZone,
                    showHired = showHired,
                    showTrips = showTrips,
                    hiredTextRes = hiredTextRes,
                    tripsTextRes = tripsTextRes
                )
            )
        }
    }

    private suspend fun filterData(zonesList: List<ZoneModel>) {
        val zonesWithMoreTrips = zonesList.filter {
            ZoneUtils.checkDiff(it.zone, "") > 0
        }

        val (farZones, nearbyZones) = zonesWithMoreTrips.partition {
            getDistanceFromLatLonInMetersLocale(
                W2CLocation.getLastLatLong(),
                it.zone.centralCoord
            ) > 5000
        }

        val nearbyZonesSorted = nearbyZones.sortedWith(
            compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, "") }
                .thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(), it.zone.centralCoord) }
        )

        val farZonesSorted = farZones.sortedWith(
            compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, "") }
                .thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(), it.zone.centralCoord) }
        )

        _uiState.update {
            it.copy(
                nearbyZones = nearbyZonesSorted,
                farZones = farZonesSorted
            )
        }

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

            _uiState.update { it.copy(actualZones = actualZoneList) }
        }
    }

    private fun getDistanceFromLatLonInMetersLocale(
        lastLatLong: LatLong?,
        centralCoord: LatLong?
    ): Int {
        if (lastLatLong == null || lastLatLong.Lat.isNaN() || centralCoord == null || centralCoord.Lat.isNaN()) {
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

// ------------------------------------------------------------
// COMPOSE SCREEN
// ------------------------------------------------------------

@Composable
fun DashboardRoute(
    navController: NavController,
    viewModel: DashboardComposeViewModel,
    sharedViewModel: DashboardMainActivityViewModel,
    onShowHeader: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sharedState by sharedViewModel.sharedState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        onShowHeader(true)
        sharedViewModel.start()
        viewModel.onEvent(DashboardUiEvent.OnResume)
    }

    LaunchedEffect(viewModel) {
        lifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            viewModel.uiEffect.collectLatest { effect ->
                when (effect) {
                    DashboardUiEffect.NavigateBack -> navController.popBackStack()
                    DashboardUiEffect.NavigateToHome -> {
                        navController.navigate(HomeDirections.goToOnTripFragment())
                    }
                    DashboardUiEffect.NavigateToOnTrip -> Unit
                    is DashboardUiEffect.ShowDialog -> Unit
                    is DashboardUiEffect.ShowMessage -> Unit
                }
            }
        }
    }

    LaunchedEffect(sharedViewModel) {
        lifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            sharedViewModel.effects.collectLatest { effect ->
                when (effect) {
                    DashboardSharedEffect.RefreshActualZoneData -> viewModel.refreshActualZoneData()
                    DashboardSharedEffect.PendingTripsUpdated -> Unit
                    DashboardSharedEffect.SoonInZoneChanged -> Unit
                }
            }
        }
    }

    DashboardScreen(
        state = uiState.copy(
            pendingTripsCount = sharedState.pendingTripsCount,
            buttonsState = if (sharedState.isInSoonInZone) {
                uiState.buttonsState.copy(
                    locateOnHired = DashboardButtonUiState.Visible(
                        textRes = R.string.btn_delocate,
                        iconRes = R.drawable.ubdesact,
                        background = DashboardButtonBackground.RED,
                        enabled = true
                    )
                )
            } else {
                uiState.buttonsState.copy(
                    locateOnHired = DashboardButtonUiState.Visible(
                        textRes = R.string.btn_soon_to_clear,
                        iconRes = R.drawable.ubactivar,
                        background = DashboardButtonBackground.BLUE,
                        enabled = true
                    )
                )
            }
        ),
        onEvent = viewModel::onEvent
    )
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        DashboardHeader(state.headerState)

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Pending trips: ${state.pendingTripsCount}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        DashboardButton(state.buttonsState.locateOnHired, onClick = {
            onEvent(DashboardUiEvent.OnLocateOnHiredClick)
        })

        Spacer(Modifier.height(12.dp))

        DashboardButton(state.buttonsState.cancel, onClick = {
            onEvent(DashboardUiEvent.OnCancelClick)
        })

        Spacer(Modifier.height(12.dp))

        DashboardButton(state.buttonsState.close, onClick = {
            onEvent(DashboardUiEvent.OnCloseClick)
        })

        Spacer(Modifier.height(16.dp))

        Text("Nearby zones", style = MaterialTheme.typography.titleMedium)
        ZoneList(state.nearbyZones)

        Spacer(Modifier.height(16.dp))

        Text("Far zones", style = MaterialTheme.typography.titleMedium)
        ZoneList(state.farZones)

        Spacer(Modifier.height(16.dp))

        Text("Actual zones", style = MaterialTheme.typography.titleMedium)
        ZoneList(state.actualZones)
    }

    state.customDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { onEvent(DashboardUiEvent.OnDialogDismiss) },
            title = { Text(dialog.title) },
            text = { Text(dialog.message) },
            confirmButton = {
                TextButton(onClick = { onEvent(DashboardUiEvent.OnDialogConfirm) }) {
                    Text(dialog.confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(DashboardUiEvent.OnDialogDismiss) }) {
                    Text(dialog.dismissText)
                }
            }
        )
    }
}

@Composable
fun DashboardHeader(state: DashboardHeaderState) {
    Column {
        if (state.showStand) Text(text = stringResource(R.string.tvOnStop))
        if (state.showZone) Text(text = stringResource(R.string.tvOnZone))
        if (state.showHired) {
            Text(
                text = state.hiredTextRes?.let { stringResource(it) }
                    ?: stringResource(R.string.tvHired)
            )
        }
        if (state.showTrips) {
            Text(
                text = state.tripsTextRes?.let { stringResource(it) }
                    ?: stringResource(R.string.tvTrips)
            )
        }
    }
}

@Composable
fun DashboardButton(
    state: DashboardButtonUiState,
    onClick: () -> Unit
) {
    when (state) {
        DashboardButtonUiState.Hidden -> Unit

        is DashboardButtonUiState.Visible -> {
            val containerColor = when (state.background) {
                DashboardButtonBackground.BLUE -> Color(0xFF1976D2)
                DashboardButtonBackground.RED -> Color(0xFFD32F2F)
                DashboardButtonBackground.DISABLE -> Color(0xFF9E9E9E)
            }

            Button(
                onClick = onClick,
                enabled = state.enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = Color.White
                )
            ) {
                Text(text = stringResource(state.textRes))
            }
        }
    }
}

@Composable
fun ZoneList(zones: List<ZoneModel>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(zones) { zone ->
            Card {
                Text(
                    text = zone.zone.name ?: "Zone",
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}


