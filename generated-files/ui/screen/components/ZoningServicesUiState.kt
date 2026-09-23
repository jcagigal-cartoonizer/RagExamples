package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ZoningServicesCustomDialogState
import ifac.td.taxi.ui.screen.components.ZoningServicesCustomDialogState = ZoningServicesCustomDialogState
import ifac.td.taxi.ui.screen.components.ZoningServicesButtonsState
import ifac.td.taxi.ui.screen.components.ZoningServicesButtonsState = ZoningServicesButtonsState
import ifac.td.taxi.ui.screen.components.ZoningServicesUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 320-3: import androidx.compose.runtime.Immutable
@Immutable
data class ZoningServicesUiState(
    val zoneName: String = "",
    val zoneTrips: List<ZoneTripModel> = emptyList(),
    val refreshProgress: Int = 0,
    val showLoading: Boolean = true,
    val currentShiftStatus: Int? = null,
    val tripId: Long? = null,
    val dialogState: ZoningServicesCustomDialogState = ZoningServicesCustomDialogState(),
    val buttonsState: ZoningServicesButtonsState = ZoningServicesButtonsState(),
    val zoneLoading: Boolean = false,
)
sealed interface ZoningServicesUiEvent {
    data class Init(val idMacroZone: Int, val idZone: Int) : ZoningServicesUiEvent
    data object ShowAllClicked : ZoningServicesUiEvent
    data object ShowRecentClicked : ZoningServicesUiEvent
    data object CancelClicked : ZoningServicesUiEvent
    data object CloseClicked : ZoningServicesUiEvent
    data object ShowCloseDialog : ZoningServicesUiEvent
    data object HideCloseDialog : ZoningServicesUiEvent
    data object DismissDialog : ZoningServicesUiEvent
    data object ConfirmDialog : ZoningServicesUiEvent
}
sealed interface ZoningServicesUiEffect {
    data object NavigateBack : ZoningServicesUiEffect
    data object NavigateToHome : ZoningServicesUiEffect
    data object NavigateToOnTrip : ZoningServicesUiEffect
}
@Immutable
data class ZoningServicesCustomDialogState(
    val visible: Boolean = false,
    val title: String = "Confirm",
    val message: String = "Do you want to continue?",
    val confirmText: String = "OK",
    val dismissText: String = "Cancel",
)
@Immutable
data class ZoningServicesButtonsState(
    val showAllVisible: Boolean = false,
    val showRecentVisible: Boolean = true,
    val showAllEnabled: Boolean = true,
    val showRecentEnabled: Boolean = true,
    val cancelVisible: Boolean = true,
    val closeVisible: Boolean = true,
    val showAllSelected: Boolean = false,
    val showRecentSelected: Boolean = false,
) {
    fun withShowAllHidden() = copy(
        showAllVisible = false,
        showRecentVisible = true,
        showAllSelected = true,
        showRecentSelected = false
    )
    fun withShowRecentHidden() = copy(
        showAllVisible = true,
        showRecentVisible = false,
        showAllSelected = false,
        showRecentSelected = true
    )
}
