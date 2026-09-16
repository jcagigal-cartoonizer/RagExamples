package ifac.td.taxi.ui.screen.state
import ifac.td.taxi.ui.screen.state.DashboardDialogState
import ifac.td.taxi.ui.screen.state.DashboardButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // ## 1) Dashboard UI state and effects

// This replaces fragment-driven view updates and button visibility logic with state holders.


import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
import ifac.td.taxi.ui.model.ZoneModel

data class DashboardUiState(
    val nearbyZones: List<ZoneModel> = emptyList(),
    val farZones: List<ZoneModel> = emptyList(),
    val actualZones: List<ZoneModel> = emptyList(),
    val pendingTripsCount: Int = 0,
    val zoningDataTypes: String = "",
    val buttonsState: DashboardButtonsState = DashboardButtonsState(),
    val isLoadingZones: Boolean = false,
    val isLoadingTrips: Boolean = false,
    val dialog: DashboardDialogState? = null
)

data class DashboardDialogState(
    val title: String,
    val message: String,
    val confirmText: String = "OK",
    val dismissText: String? = null
)

sealed interface DashboardUiEvent {
    data object OnResume : DashboardUiEvent
    data object OnPause : DashboardUiEvent

    data class OnZoneClick(val zone: ZoneModel) : DashboardUiEvent
    data class OnZoneLongClick(val macrozoneId: Int, val zoneId: Int) : DashboardUiEvent

    data object OnLocateOnHiredClick : DashboardUiEvent
    data object OnChangeButtonClick : DashboardUiEvent
    data object OnDialogConfirm : DashboardUiEvent
    data object OnDialogDismiss : DashboardUiEvent
}

sealed interface DashboardUiEffect {
    data object NavigateBack : DashboardUiEffect
    data class NavigateToZoneDetail(val zone: ZoneModel) : DashboardUiEffect
    data class ShowToast(val message: String) : DashboardUiEffect
    data class OpenDialog(val dialog: DashboardDialogState) : DashboardUiEffect
    data object CloseDialog : DashboardUiEffect
}


