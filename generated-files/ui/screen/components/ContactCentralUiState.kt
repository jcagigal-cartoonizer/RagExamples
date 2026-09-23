package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 12-1: import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
@Immutable
data class ContactCentralUiState(
    val buttons: ContactCentralButtonsState = ContactCentralButtonsState(),
    val isLoadingShortBreakAction: Boolean = false,
    val dialog: ContactCentralDialogState? = null,
)
@Immutable
data class ContactCentralButtonsState(
    val cancel: ContactCentralButtonStyle = ContactCentralButtonStyle(),
    val shortBreak: ContactCentralButtonStyle = ContactCentralButtonStyle(),
    val voiceCall: ContactCentralButtonStyle = ContactCentralButtonStyle(),
    val messages: ContactCentralButtonStyle = ContactCentralButtonStyle(),
    val information: ContactCentralButtonStyle = ContactCentralButtonStyle(),
) {
    companion object {
        fun initial() = ContactCentralButtonsState(
            cancel = ContactCentralButtonStyle(
                visible = true,
                enabled = true,
                text = "CANCEL",
                background = ContactCentralButtonBackground.DEFAULT,
            ),
            shortBreak = ContactCentralButtonStyle(
                visible = true,
                enabled = false,
                text = "SHORT BREAK",
                background = ContactCentralButtonBackground.GREEN,
            ),
            voiceCall = ContactCentralButtonStyle(
                visible = true,
                enabled = true,
                text = "VOICE CALL",
                background = ContactCentralButtonBackground.RED,
            ),
            messages = ContactCentralButtonStyle(
                visible = true,
                enabled = false,
                text = "MESSAGES",
                background = ContactCentralButtonBackground.DEFAULT,
            ),
            information = ContactCentralButtonStyle(
                visible = true,
                enabled = true,
                text = "INFORMATION",
                background = ContactCentralButtonBackground.DEFAULT,
            ),
        )
    }
}
@Immutable
data class ContactCentralButtonStyle(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val text: String = "",
    val background: ContactCentralButtonBackground = ContactCentralButtonBackground.DEFAULT,
)
enum class ContactCentralButtonBackground {
    DEFAULT,
    RED,
    GREEN,
}
@Immutable
data class ContactCentralDialogState(
    val title: String,
    val message: String? = null,
    val acceptText: String = "ACCEPT",
    val cancelText: String = "CANCEL",
)
sealed interface ContactCentralUiEffect {
    data object NavigateBack : ContactCentralUiEffect
    data object NavigateToPredefinedMessages : ContactCentralUiEffect
    data object NavigateToInformationMessages : ContactCentralUiEffect
    data object ShowShortBreakForcedToast : ContactCentralUiEffect
    data class ShowVoiceRequestDialog(val title: String) : ContactCentralUiEffect
}
