package ifac.td.taxi.ui.screen.components
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
