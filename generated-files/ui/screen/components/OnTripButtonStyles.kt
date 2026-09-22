package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.graphics.Color
@Immutable
data class OnTripUiState(
    val buttons: OnTripButtonsState = OnTripButtonsState(),
    val showDialog: OnTripDialogState? = null,
    val topBarRightVisible: Boolean = false,
    val topBarRightEnabled: Boolean = false,
)
@Immutable
data class OnTripDialogState(
    val title: String,
    val description: String? = null,
    val buttons: List<OnTripDialogButton> = emptyList()
)
enum class OnTripDialogButton {
    CANCEL,
    ACCEPT,
    AT_DOOR,
    RIDER_IN_CAB
}
sealed interface OnTripUiEffect {
    data object NavigateToEstimateFixedPrice : OnTripUiEffect
    data object NavigateToReceiptHistory : OnTripUiEffect
    data object NavigateToMessage : OnTripUiEffect
    data object NavigateToContactCentral : OnTripUiEffect
    data object NavigateToDispatchInfo : OnTripUiEffect
    data object NavigateToDirections : OnTripUiEffect
    data object NavigateToMacroZoning : OnTripUiEffect
    data class NavigateToDeepLink(val deepLinkUri: String) : OnTripUiEffect
    data class OpenExternalIntent(val intentAction: String) : OnTripUiEffect
    data object ShowSelectNotificationDialog : OnTripUiEffect
    data object ShowConfirmNoClientDialog : OnTripUiEffect
    data object ShowConfirmHiredManualDialog : OnTripUiEffect
    data object ShowTopBarNext : OnTripUiEffect
    data object HideTopBarNext : OnTripUiEffect
    data object ShowToastItopRestricted : OnTripUiEffect
    data object NoEffect : OnTripUiEffect
}
@Immutable
data class OnTripButtonsState(
    val fixedPrice: OnTripButtonState = OnTripButtonState.Hidden,
    val notifications: OnTripButtonState = OnTripButtonState.Hidden,
    val zoning: OnTripButtonState = OnTripButtonState.Hidden,
    val navigate: OnTripButtonState = OnTripButtonState.Hidden,
    val dispatchInfo: OnTripButtonState = OnTripButtonState.Hidden,
    val receipts: OnTripButtonState = OnTripButtonState.Hidden,
    val messages: OnTripButtonState = OnTripButtonState.Hidden,
    val central: OnTripButtonState = OnTripButtonState.Hidden,
    val client: OnTripButtonState = OnTripButtonState.Hidden,
    val roofLight: OnTripButtonState = OnTripButtonState.Hidden,
)
@Immutable
data class OnTripButtonState(
    val visible: Boolean = false,
    val enabled: Boolean = false,
    val text: String = "",
    val iconRes: Int? = null,
    val background: ButtonBackground = ButtonBackground.Default,
    val style: ButtonStyle = ButtonStyle.Disabled,
    val type: ButtonTypeUi = ButtonTypeUi.Default,
)
enum class ButtonStyle { Enabled, Disabled, Loading }
enum class ButtonTypeUi { Default, Empty, AtDoor, RiderInCab }
enum class ButtonBackground { Default, Red, Green, Blue, Orange, Gray }
object OnTripButtonStyles {
    val red = Color(0xFFD32F2F)
    val green = Color(0xFF2E7D32)
    val blue = Color(0xFF1976D2)
    val orange = Color(0xFFF57C00)
    val gray = Color(0xFF9E9E9E)
    val white = Color.White
    val disabledTint = Color(0xFFBDBDBD)
    fun backgroundColor(background: ButtonBackground): Color = when (background) {
        ButtonBackground.Red -> red
        ButtonBackground.Green -> green
        ButtonBackground.Blue -> blue
        ButtonBackground.Orange -> orange
        ButtonBackground.Gray -> gray
        ButtonBackground.Default -> green
    }
    fun textColor(style: ButtonStyle): Color = when (style) {
        ButtonStyle.Enabled -> white
        ButtonStyle.Loading -> white
        ButtonStyle.Disabled -> disabledTint
    }
    fun alpha(style: ButtonStyle): Float = when (style) {
        ButtonStyle.Enabled -> 1f
        ButtonStyle.Loading -> 0.75f
        ButtonStyle.Disabled -> 0.38f
    }
}
