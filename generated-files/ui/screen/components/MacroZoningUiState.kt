package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.ui.model.ScrollModeEnum
import ifac.td.taxi.viewmodel.model.OrderOptions
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
    val dialogState: CustomDialogState? = null
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
    data class ShowDialog(val dialogState: CustomDialogState) : MacroZoningUiEffect
}
import androidx.compose.ui.graphics.Color
data class MacroZoningButtonsState(
    val onStopVisible: Boolean = false,
    val onZoneVisible: Boolean = false,
    val hiredVisible: Boolean = false,
    val tripsVisible: Boolean = false,
    val hiredLabel: String = "Hired",
    val tripsLabel: String = "Trips",
    val placeholderText: String = "Sort by",
    val activeOrder: OrderOptions = OrderOptions.NONE,
    val pendingButton: MacroZoningActionButtonState = MacroZoningActionButtonState.Pending(),
    val preReservationButton: MacroZoningActionButtonState = MacroZoningActionButtonState.PreReservation()
)
sealed class MacroZoningActionButtonState(
    open val enabled: Boolean,
    open val visible: Boolean,
    open val backgroundColor: Color,
    open val contentColor: Color,
    open val disabledBackgroundColor: Color,
    open val disabledContentColor: Color
) {
    data class Pending(
        override val enabled: Boolean = false,
        override val visible: Boolean = true,
        override val backgroundColor: Color = Color(0xFF1976D2), // blue
        override val contentColor: Color = Color.White,
        override val disabledBackgroundColor: Color = Color(0xFFB0BEC5),
        override val disabledContentColor: Color = Color.White
    ) : MacroZoningActionButtonState(
        enabled, visible, backgroundColor, contentColor, disabledBackgroundColor, disabledContentColor
    )
    data class PreReservation(
        override val enabled: Boolean = false,
        override val visible: Boolean = false,
        override val backgroundColor: Color = Color(0xFF1976D2),
        override val contentColor: Color = Color.White,
        override val disabledBackgroundColor: Color = Color(0xFFB0BEC5),
        override val disabledContentColor: Color = Color.White
    ) : MacroZoningActionButtonState(
        enabled, visible, backgroundColor, contentColor, disabledBackgroundColor, disabledContentColor
    )
}
