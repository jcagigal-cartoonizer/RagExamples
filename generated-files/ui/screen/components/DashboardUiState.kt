package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.DashboardButtonsState
import ifac.td.taxi.compose.viewmodel.DashboardComposeViewModel
import ifac.td.taxi.ui.screen.components.DashboardUiEffect
import ifac.td.taxi.ui.screen.components.DashboardUiState
import ifac.td.taxi.ui.screen.components.DashboardButtonsState = DashboardButtonsState
import ifac.td.taxi.ui.screen.components.DashboardUiState = DashboardUiState
import ifac.td.taxi.ui.screen.components.DashboardUiEvent
import ifac.td.taxi.ui.screen.components.DashboardDialogState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 132-2: import android.app.Application
data class DashboardUiState(
    val buttonsState: DashboardButtonsState = DashboardButtonsState(),
    val nearbyZones: List<ZoneModel> = emptyList(),
    val farZones: List<ZoneModel> = emptyList(),
    val actualZones: List<ZoneModel> = emptyList(),
    val pendingTrips: List<PendingTrip> = emptyList(),
    val zoningListDataTypes: String = "",
    val isLoadingZones: Boolean = true,
    val isLoadingPendingTrips: Boolean = true,
)
sealed interface DashboardUiEvent {
    data object ScreenShown : DashboardUiEvent
    data object OnResume : DashboardUiEvent
    data object OnPause : DashboardUiEvent
    data object PendingTripsClicked : DashboardUiEvent
    data object LocateOnHiredClicked : DashboardUiEvent
    data object DismissDialog : DashboardUiEvent
    data object ConfirmDialog : DashboardUiEvent
}
sealed interface DashboardUiEffect {
    data object NavigateBack : DashboardUiEffect
    data class ShowDialog(val dialog: DashboardDialogState) : DashboardUiEffect
    data class ShowToast(val message: String) : DashboardUiEffect
}
data class DashboardDialogState(
    val title: String,
    val message: String,
    val confirmText: String = "OK",
    val dismissText: String = "Cancel",
    val onConfirm: (() -> Unit)? = null
)
data class DashboardViewState(
    val uiState: DashboardUiState = DashboardUiState()
)
class DashboardComposeViewModel(
    private val pendingTripsUseCase: PendingTripsUseCase,
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<DashboardUiEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()
    var bravoConfiguration: BravoConfigurationVariableEntity? = null
    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            DashboardUiEvent.ScreenShown -> {
                initDashboard()
            }
            DashboardUiEvent.OnResume -> {
                initDashboard()
                loadTrips()
            }
            DashboardUiEvent.OnPause -> {
                removeHandlerCallbacks()
            }
            DashboardUiEvent.PendingTripsClicked -> {
                // preserve navigation behavior as an effect
                emitEffect(DashboardUiEffect.NavigateBack)
            }
            DashboardUiEvent.LocateOnHiredClicked -> {
                emitEffect(
                    DashboardUiEffect.ShowDialog(
                        DashboardDialogState(
                            title = "Locate on hired",
                            message = "Do you want to continue?",
                            confirmText = "Accept",
                            dismissText = "Cancel"
                        )
                    )
                )
            }
            DashboardUiEvent.DismissDialog -> Unit
            DashboardUiEvent.ConfirmDialog -> Unit
        }
    }
    fun initDashboard() {
        viewModelScope.launch {
            val dataTypes = W2CLocation.getZoning().availableColumns
            val zones = W2CLocation.getZoning().macrozones
                .flatMap { it.zones }
                .map { ZoneModel(it, false) }
            val filtered = filterZones(zones, dataTypes)
            _uiState.update {
                it.copy(
                    zoningListDataTypes = dataTypes,
                    buttonsState = DashboardButtonsState.fromAvailableColumns(dataTypes),
                    nearbyZones = filtered.nearby,
                    farZones = filtered.far,
                    actualZones = filtered.actual,
                    isLoadingZones = false
                )
            }
        }
    }
    fun loadTrips() {
        viewModelScope.launch {
            if (BravoCentral.isPendingTripsAllowed(false)) {
                pendingTripsUseCase.getPendingTripsZone(
                    flow = MutableStateFlow(arrayListOf())
                )
            }
        }
    }
    data class FilteredZones(
        val nearby: List<ZoneModel>,
        val far: List<ZoneModel>,
        val actual: List<ZoneModel>
    )
    fun filterZones(
        zones: List<ZoneModel>,
        dataTypes: String
    ): FilteredZones {
        val zonesWithMoreTrips = zones.filter {
            ZoneUtils.checkDiff(it.zone, dataTypes) > 0
        }
        val (far, nearby) = zonesWithMoreTrips.partition {
            getDistanceFromLatLonInMetersLocale(
                W2CLocation.getLastLatLong(),
                it.zone.centralCoord
            ) > 5000
        }
        val nearbySorted = nearby.sortedWith(
            compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, dataTypes) }
                .thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(), it.zone.centralCoord) }
        )
        val farSorted = far.sortedWith(
            compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, dataTypes) }
                .thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(), it.zone.centralCoord) }
        )
        val actualList = buildList {
            W2CLocation.getZoning().getZoneById(
                W2CLocation.getLastIdMacrozoneSent(),
                W2CLocation.getLastIdZoneSent()
            )?.let { add(ZoneModel(it, false)) }
            W2CLocation.getZoneFromLatLong(W2CLocation.getLastLatLong())?.let { add(ZoneModel(it, false)) }
        }
        return FilteredZones(nearbySorted, farSorted, actualList)
    }
    fun getDistanceFromLatLonInMetersLocale(
        lastLatLong: LatLong?,
        centralCoord: LatLong?
    ): Int {
        if (lastLatLong == null || lastLatLong.Lat.isNaN() || centralCoord == null || centralCoord.Lat.isNaN()) {
            return Int.MAX_VALUE
        }
        return W2CLocation.getDistanceFromLatLonInMeters(lastLatLong, centralCoord)
    }
    fun emitEffect(effect: DashboardUiEffect) {
        _effects.tryEmit(effect)
    }
    fun removeHandlerCallbacks() {
        // no-op in Compose version unless you still use external handlers
    }
}
