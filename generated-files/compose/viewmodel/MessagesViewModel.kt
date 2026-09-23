package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 56-2: import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.MessageUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.entities.message.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class MessagesComposeViewModel(
    private val messageUseCase: MessageUseCase,
    context: Application,
) : BaseViewModel(context) {
    private val TAG = "MessagesViewModel"
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffects = MutableSharedFlow<MessagesUiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()
    fun init() {
        loadMessagesByDriverId()
    }
    fun onEvent(event: MessagesUiEvent) {
        when (event) {
            MessagesUiEvent.Back -> emitEffect(MessagesUiEffect.NavigateBack)
            MessagesUiEvent.DeleteSelected -> deleteSelectedMessages()
            MessagesUiEvent.ToggleSelectionMode -> {
                _uiState.update { state ->
                    val newMode = !state.isSelectionMode
                    state.copy(
                        isSelectionMode = newMode,
                        selectedMessageIds = if (newMode) state.selectedMessageIds else emptySet(),
                        showDeleteButton = newMode,
                        showSelectButton = true
                    )
                }
            }
            MessagesUiEvent.MarkAllReadRequest -> {
                _uiState.update {
                    it.copy(
                        dialogState = MessageDialogState(
                            title = "Warning",
                            description = "Do you want to mark all messages as read?",
                            buttons = listOf(DialogButtonConfig.Cancel, DialogButtonConfig.Accept)
                        )
                    )
                }
                emitEffect(MessagesUiEffect.ShowMarkAllReadDialog)
            }
            MessagesUiEvent.MarkAllReadConfirmed -> {
                _uiState.update { it.copy(dialogState = null) }
                markAllMessagesRead()
            }
            MessagesUiEvent.DismissDialog -> {
                _uiState.update { it.copy(dialogState = null) }
            }
            is MessagesUiEvent.MessageClicked -> {
                emitEffect(
                    MessagesUiEffect.NavigateToMessageDetailWithArgs(
                        messageId = event.message.id,
                        skipAutoClose = true
                    )
                )
            }
            is MessagesUiEvent.ToggleMessageSelection -> {
                _uiState.update { state ->
                    val id = event.message.id
                    val newSelection = state.selectedMessageIds.toMutableSet().apply {
                        if (!add(id)) remove(id)
                    }
                    state.copy(
                        selectedMessageIds = newSelection,
                        showDeleteButton = newSelection.isNotEmpty()
                    )
                }
            }
        }
    }
    fun deleteSelectedMessages() {
        val selectedIds = _uiState.value.selectedMessageIds
        if (selectedIds.isEmpty()) return
        val selectedMessages = _uiState.value.messages.filter { it.id in selectedIds }.toMutableList()
        deleteMessages(selectedMessages)
        _uiState.update {
            it.copy(
                selectedMessageIds = emptySet(),
                isSelectionMode = false,
                showDeleteButton = false
            )
        }
    }
    fun emitEffect(effect: MessagesUiEffect) {
        viewModelScope.launch { _uiEffects.emit(effect) }
    }
    fun deleteMessages(deleteSelected: MutableList<MessageEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            messageUseCase.deleteMessages(deleteSelected)
            loadMessagesByDriverId()
        }
    }
    fun loadMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            _uiState.update {
                it.copy(
                    messages = messageUseCase.getMessages(),
                    isLoading = false
                )
            }
        }
    }
    fun loadMessagesByDriverId() {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "loadMessagesByDriverId: getMessageByDriverId(): ${messageUseCase.getMessageByDriverId()}")
            _uiState.update { it.copy(isLoading = true) }
            _uiState.update {
                it.copy(
                    messages = messageUseCase.getMessageByDriverId(),
                    isLoading = false
                )
            }
        }
    }
    fun markAllMessagesRead() {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "markAllMessagesRead")
            messageUseCase.setAllMessagesRead()
            _uiState.update {
                it.copy(messages = messageUseCase.getMessageByDriverId())
            }
        }
    }
}
