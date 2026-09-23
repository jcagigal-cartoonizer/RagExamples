package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.MacroZoningCustomDialogState
import ifac.td.taxi.ui.screen.components.MacroZoningUiState
import ifac.td.taxi.ui.screen.components.MacroZoningButtonsState = MacroZoningButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 436-3: import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
data class MacroZoningUiState(
    val macroZones: List<MacroZoneModelUi> = emptyList(),
    val listOrder: OrderOptions = OrderOptions.NONE,
    val dataTypes: String? = null,
    val scrollMode: ScrollModeEnum = ScrollModeEnum.FOLLOW_SELECTED,
    val refreshProgress: Int = 0,
    val selectedZoneId: Int? = null,
    val shortBreakStatus: ShortBreakStatus? = null,
    val currentZone: String? = null,
    val hiredZone: String? = null,
    val buttonsState: MacroZoningButtonsState = MacroZoningButtonsState(),
    val dialogState: MacroZoningCustomDialogState? = null
)
sealed interface MacroZoningUiEvent {
    data object OnResume : MacroZoningUiEvent
    data object OnPause : MacroZoningUiEvent
    data object OnFilterClick : MacroZoningUiEvent
    data class OnOrderSelected(val order: OrderOptions) : MacroZoningUiEvent
    data object OnPendingClick : MacroZoningUiEvent
    data object OnPreReservationClick : MacroZoningUiEvent
    data class OnMacroZoneClick(val macroZoneId: Int) : MacroZoningUiEvent
    data object OnDialogDismiss : MacroZoningUiEvent
    data object OnDialogConfirm : MacroZoningUiEvent
}
sealed interface MacroZoningUiEffect {
    data class NavigateToZoning(val macroZoneId: Int) : MacroZoningUiEffect
    data object NavigateToPendingTrips : MacroZoningUiEffect
    data object NavigateToPreReservationTrips : MacroZoningUiEffect
    data class ShowToast(val message: String) : MacroZoningUiEffect
    data class ShowDialog(val dialogState: MacroZoningCustomDialogState) : MacroZoningUiEffect
}
