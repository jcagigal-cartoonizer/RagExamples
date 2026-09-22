package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 11-1: import ifac.td.taxi.repository.room.entities.message.MessageEntity
// import ifac.td.taxi.repository.room.entities.message.MessageEntity
data class MessagesUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedMessageIds: Set<Long> = emptySet(),
    val showDeleteButton: Boolean = false,
    val showSelectButton: Boolean = true,
    val showMarkReadAction: Boolean = true,
    val dialogState: MessageDialogState? = null
)
data class MessageDialogState(
    val title: String,
    val description: String,
    val buttons: List<DialogButtonConfig>,
)
sealed class DialogButtonConfig {
    data object Cancel : DialogButtonConfig()
    data object Accept : DialogButtonConfig()
}
sealed interface MessagesUiEvent {
    data object Back : MessagesUiEvent
    data object DeleteSelected : MessagesUiEvent
    data object ToggleSelectionMode : MessagesUiEvent
    data object MarkAllReadRequest : MessagesUiEvent
    data object MarkAllReadConfirmed : MessagesUiEvent
    data object DismissDialog : MessagesUiEvent
    data class MessageClicked(val message: MessageEntity) : MessagesUiEvent
    data class ToggleMessageSelection(val message: MessageEntity) : MessagesUiEvent
}
sealed interface MessagesUiEffect {
    data object NavigateBack : MessagesUiEffect
    data object NavigateToMessageDetail : MessagesUiEffect
    data class NavigateToMessageDetailWithArgs(
        val messageId: Long,
        val skipAutoClose: Boolean = true
    ) : MessagesUiEffect
    data object ShowMarkAllReadDialog : MessagesUiEffect
}
