package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 12-1: import androidx.annotation.DrawableRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
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
