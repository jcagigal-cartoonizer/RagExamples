package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 4-1: import androidx.annotation.StringRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
@Immutable
data class InfoDispatchUiState(
    val isHeaderVisible: Boolean = true,
    val isBottomBarVisible: Boolean = true,
    val dispatch: InfoDispatchUiModel? = null,
    val extraData: DispatchExtraUiModel? = null,
    val multiDispatch: List<InfoDispatchUiModel> = emptyList(),
    val bravoConfigurationShowTripAmount: Boolean = false,
    val meetingSignColors: Pair<Int, Int> = 0 to 0,
    val hasITopConnected: Boolean = false,
    val isManualShift: Boolean = false,
    val customerCallAvailable: Boolean = false,
    val canMakeCalls: Boolean = false,
    val bridgeCallState: Int = 0,
    val noClientButtonEnabled: Boolean = false,
    val showReturnDispatchButton: Boolean = false,
    val clientButtonGreen: Boolean = true,
    val bridgeCallMaxTries: Int = 0,
    val timerText: String = "",
    val showConcertedPriceDialog: Boolean = false,
    val buttons: InfoDispatchButtonsState = InfoDispatchButtonsState(),
    val tabs: List<InfoDispatchTabUiModel> = emptyList(),
    val selectedTabIndex: Int = 0,
    val flightCode: String? = null,
    val dialog: InfoDispatchDialogState? = null
)
@Immutable
data class InfoDispatchTabUiModel(
    val title: String,
    val iconRes: Int,
    val isSelected: Boolean = false
)
@Immutable
data class InfoDispatchDialogState(
    val type: DialogType,
    val title: String,
    val description: String = "",
    val iconRes: Int? = null,
    val buttons: List<DialogButton> = emptyList()
)
enum class DialogType {
    SELECT_NOTIFICATION,
    CONFIRM_NO_CLIENT,
    CONFIRM_RETURN_DISPATCH,
    CONFIRM_HIRED_MANUAL,
    CONCERTED_PRICE_WARNING,
    MULTI_CALL_CONFIRM
}
@Immutable
data class DialogButton(
    val type: InfoDispatchDialogButtonType,
    val text: String
)
enum class InfoDispatchDialogButtonType {
    CANCEL,
    ACCEPT,
    AT_DOOR,
    RIDER_IN_CAB
}
sealed interface InfoDispatchUiEvent {
    data object OnBackPressed : InfoDispatchUiEvent
    data object OnNavigateDirections : InfoDispatchUiEvent
    data object OnNavigateMeetingSign : InfoDispatchUiEvent
    data object OnClickNotifications : InfoDispatchUiEvent
    data object OnClickVoiceCall : InfoDispatchUiEvent
    data object OnClickPrint : InfoDispatchUiEvent
    data object OnClickNoClient : InfoDispatchUiEvent
    data object OnClickReturn : InfoDispatchUiEvent
    data object OnClickFlightCode : InfoDispatchUiEvent
    data class OnTabSelected(val index: Int) : InfoDispatchUiEvent
    data class OnDialogButtonClicked(val button: InfoDispatchDialogButtonType) : InfoDispatchUiEvent
}
sealed interface InfoDispatchUiEffect {
    data object NavigateToDirections : InfoDispatchUiEffect
    data object NavigateToHome : InfoDispatchUiEffect
    data class NavigateToMeetingSign(val textColor: Int, val backgroundColor: Int) : InfoDispatchUiEffect
    data object RequestPhonePermission : InfoDispatchUiEffect
    data class ShowToast(@StringRes val messageRes: Int, val durationLong: Boolean = false) : InfoDispatchUiEffect
    data class OpenExternalPhoneCall(val phoneNumber: String) : InfoDispatchUiEffect
    data class OpenDialog(val dialog: InfoDispatchDialogState) : InfoDispatchUiEffect
    data object CloseDialog : InfoDispatchUiEffect
}
