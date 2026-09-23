package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PredefinedMessageButtonsState
import ifac.td.taxi.ui.screen.components.PredefinedMessageUiEvent
import ifac.td.taxi.ui.screen.components.PredefinedMessageUiState
import ifac.td.taxi.ui.screen.components.PredefinedMessageUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 196-2: import android.app.Application
class PredefinedMessageComposeViewModelCompose(
    private val alfaMessageHandler: AlfaMessageHandler,
    private val messageUseCase: MessageUseCase,
    private val predefinedMessageUseCase: PredefinedMessagesUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PredefinedMessageUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<PredefinedMessageUiEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()
    fun onEvent(event: PredefinedMessageUiEvent) {
        when (event) {
            is PredefinedMessageUiEvent.Initialize -> load(event.messageId)
            PredefinedMessageUiEvent.CancelClicked -> emitEffect(PredefinedMessageUiEffect.NavigateBack)
            PredefinedMessageUiEvent.NewMessageClicked -> {
                emitEffect(
                    PredefinedMessageUiEffect.OpenEditableDialog(
                        initialText = null,
                        dispatchNumber = _uiState.value.dispatchNumber
                    )
                )
            }
            is PredefinedMessageUiEvent.PredefinedMessageClicked -> {
                emitEffect(
                    PredefinedMessageUiEffect.OpenPredefinedDialog(
                        id = event.id,
                        message = event.message
                    )
                )
            }
            is PredefinedMessageUiEvent.SendPredefined -> sendPredefinedMessage(event.id)
            is PredefinedMessageUiEvent.SendTypedMessage -> sendTypedMessage(
                response = event.text,
                dispatchNumber = event.dispatchNumber
            )
        }
    }
    fun load(messageId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = messageUseCase.getMessage(messageId)
            val predefinedMessages = predefinedMessageUseCase.getPredefinedMessages().toList()
            _uiState.emit(
                PredefinedMessageUiState(
                    message = message,
                    predefinedMessages = predefinedMessages,
                    dispatchNumber = null,
                    selectedPredefinedMessage = null,
                    buttonsState = PredefinedMessageButtonsState.default(
                        hasMessages = predefinedMessages.isNotEmpty()
                    )
                )
            )
        }
    }
    fun sendPredefinedMessage(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val provider = _uiState.value.message?.provider
            alfaMessageHandler.sendPredefinedMessage(id, 0, 0, provider)
            emitEffect(PredefinedMessageUiEffect.NavigateBack)
        }
    }
    fun sendTypedMessage(response: String, dispatchNumber: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val freeMessageRequest = FreeTextMessageRequest().apply {
                freeTextMessage = response
                this.dispatchNumber = dispatchNumber.orEmpty()
                operadora = _uiState.value.message?.provider
            }
            alfaMessageHandler.sendMessage(freeMessageRequest)
            emitEffect(PredefinedMessageUiEffect.NavigateBack)
        }
    }
    fun emitEffect(effect: PredefinedMessageUiEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
