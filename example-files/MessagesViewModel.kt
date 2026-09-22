package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.MessageUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.entities.message.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagesViewModel(
    private val messageUseCase: MessageUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "MessagesViewModel"

    private val _messagesFlow = MutableStateFlow<List<MessageEntity>?>(null)
    val messagesFlow = _messagesFlow.asStateFlow()

    fun deleteMessages(deleteSelected: MutableList<MessageEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            messageUseCase.deleteMessages(deleteSelected)
        }
    }

    fun loadMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            _messagesFlow.emit(messageUseCase.getMessages())
        }
    }

    fun loadMessagesByDriverId() {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "loadMessagesByDriverId: getMessageByDriverId(): ${messageUseCase.getMessageByDriverId()}")
            _messagesFlow.emit(messageUseCase.getMessageByDriverId())
        }
    }

    fun markAllMessagesRead() {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "loadMessagesByDriverId: getMessageByDriverId(): ${messageUseCase.getMessageByDriverId()}")
            messageUseCase.setAllMessagesRead()
            _messagesFlow.emit(messageUseCase.getMessageByDriverId())
        }
    }
}