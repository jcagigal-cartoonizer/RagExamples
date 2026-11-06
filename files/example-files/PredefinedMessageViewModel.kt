package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.models.freetextmessage.FreeTextMessageRequest
import ifac.td.taxi.domain.usecase.MessageUseCase
import ifac.td.taxi.domain.usecase.PredefinedMessagesUseCase
import ifac.td.taxi.framework.sdk.bravocentral.AlfaMessageHandler
import ifac.td.taxi.repository.room.entities.message.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PredefinedMessageViewModel(
    private val alfaMessageHandler: AlfaMessageHandler,
    private val messageUseCase: MessageUseCase,
    private val predefinedMessageUseCase: PredefinedMessagesUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val _messageFlow = MutableStateFlow<MessageEntity?>(null)
    val messageFlow = _messageFlow.asStateFlow()

    private val _predefinedMessagesFlow = MutableStateFlow<List<String>?>(null)
    val predefinedMessagesFlow = _predefinedMessagesFlow.asStateFlow()

    private val _selectedPredefinedMessageFlow = MutableStateFlow<Pair<Int, String>?>(null)
    val selectedPredefinedMessageFlow = _selectedPredefinedMessageFlow.asStateFlow()


    fun initVM(msgId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = messageUseCase.getMessage(msgId)
            message?.let {
                _messageFlow.emit(it)
            }
            _predefinedMessagesFlow.emit(predefinedMessageUseCase.getPredefinedMessages().toList())
        }
    }

    fun sendPredefinedMessage(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            alfaMessageHandler.sendPredefinedMessage(id, 0, 0,  messageFlow.value?.provider)
        }
    }

    fun sendMessage(response: String, dispatchNumber: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val freeMessageRequest = FreeTextMessageRequest()
            freeMessageRequest.freeTextMessage = response
            freeMessageRequest.dispatchNumber = dispatchNumber ?: ""
            freeMessageRequest.operadora = messageFlow.value?.provider
            alfaMessageHandler.sendMessage(freeMessageRequest)
        }
    }

    fun changeSelectedPredefinedMessage(msg: Pair<Int, String>) {
        viewModelScope.launch {
            _selectedPredefinedMessageFlow.emit(msg)
        }

    }
}