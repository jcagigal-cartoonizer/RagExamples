package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PredefinedMessageButtonsState
import ifac.td.taxi.ui.screen.components.PredefinedMessageClicked
import ifac.td.taxi.ui.screen.components.PredefinedMessageUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 294-3: import ifac.td.taxi.repository.room.entities.message.MessageEntity
data class PredefinedMessageUiState(
    val message: MessageEntity? = null,
    val predefinedMessages: List<String> = emptyList(),
    val selectedPredefinedMessage: Pair<Int, String>? = null,
    val dispatchNumber: String? = null,
    val buttonsState: PredefinedMessageButtonsState = PredefinedMessageButtonsState.default(false)
)
sealed interface PredefinedMessageUiEvent {
    data class Initialize(val messageId: Int) : PredefinedMessageUiEvent
    data object CancelClicked : PredefinedMessageUiEvent
    data object NewMessageClicked : PredefinedMessageUiEvent
    data class PredefinedMessageClicked(val id: Int, val message: String) : PredefinedMessageUiEvent
    data class SendPredefined(val id: Int) : PredefinedMessageUiEvent
    data class SendTypedMessage(val text: String, val dispatchNumber: String?) : PredefinedMessageUiEvent
}
sealed interface PredefinedMessageUiEffect {
    data object NavigateBack : PredefinedMessageUiEffect
    data class OpenEditableDialog(
        val initialText: String?,
        val dispatchNumber: String?
    ) : PredefinedMessageUiEffect
    data class OpenPredefinedDialog(
        val id: Int,
        val message: String
    ) : PredefinedMessageUiEffect
}
