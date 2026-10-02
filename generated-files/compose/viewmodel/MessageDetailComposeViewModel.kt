package ifac.td.taxi.compose.viewmodel
import  android.app.Application
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
// # Block 87-2: import android.app.Application
class MessageDetailComposeViewModel(
    private val alfaMessageHandler: AlfaMessageHandler,
    private val messageUseCase: MessageUseCase,
    private val predefinedMessagesUseCase: PredefinedMessagesUseCase,
    private val printerUseCase: PrinterUseCase,
    private val countdownManagerUseCase: CountdownManagerUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    context: Application,
) : BaseViewModel(context) {
    private val TAG = "MessageDetailComposeVM"
    private val _uiState = MutableStateFlow(MessageDetailUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<MessageDetailUiEffect>(extraBufferCapacity = 64)
    val uiEffect = _uiEffect.asSharedFlow()
    private var countdownStartTime: Long = 0L
    init {
        viewModelScope.launch {
            val hasPredefined = predefinedMessagesUseCase.getPredefinedMessages().isNotEmpty()
            _uiState.update { it.copy(hasPredefinedMessages = hasPredefined) }
        }
    }
    fun initVM(msgId: Int, skipAutoClose: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = messageUseCase.getMessage(msgId)
            messageUseCase.setMessageRead(msgId)
            _uiState.update { it.copy(message = message) }
            if (!skipAutoClose) {
                val messageClosureTimes = mapOf(0 to null, 1 to 5, 2 to 10, 3 to 15, 4 to 20, 5 to 25, 6 to 30, 7 to 60)
                val selectedSeconds =
                    messageClosureTimes[userPreferencesUseCase.getUserPreferences()?.secondsToCloseMsgPosition]
                selectedSeconds?.let {
                    val existingCountdown = countdownManagerUseCase.getCountdownById(CountdownId.CLOSE_MESSAGE)
                    if (existingCountdown == null) {
                        createAndStartCountdown(selectedSeconds)
                    } else if (existingCountdown.state in listOf(CountdownState.FINISHED, CountdownState.STOPPED)) {
                        countdownManagerUseCase.deleteCountdownById(CountdownId.CLOSE_MESSAGE)
                        createAndStartCountdown(selectedSeconds)
                    }
                }
            }
        }
    }
    private suspend fun createAndStartCountdown(seconds: Int) {
        countdownStartTime = System.currentTimeMillis()
        val countdown = ifac.td.taxi.viewmodel.model.CountdownModel(
            idCountdown = CountdownId.CLOSE_MESSAGE,
            timeInSeconds = seconds.toLong(),
            idCallback = CountdownIdCallback.CLOSE_MESSAGE
        )
        _uiState.update {
            it.copy(
                acceptTimerState = ButtonTimerState(
                    maxSeconds = seconds.toLong(),
                    remainingSeconds = seconds.toLong(),
                    isActive = true,
                    startTime = countdownStartTime
                )
            )
        }
        countdownManagerUseCase.createOrUpdateCountdown(countdown)
        countdownManagerUseCase.startCountdown(countdown)
        countdownManagerUseCase.setCountdownFinishCallback(idCountdown = countdown.idCountdown) {
            CoroutineScope(Dispatchers.IO).launch {
                countdownManagerUseCase.deleteCountdownById(CountdownId.CLOSE_MESSAGE)
                _uiEffect.emit(MessageDetailUiEffect.NavigateBack)
            }
        }
        countdownManagerUseCase.setCountdownOnTickCallback(idCountdown = countdown.idCountdown) { tick ->
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update {
                    it.copy(
                        acceptTimerState = ButtonTimerState(
                            maxSeconds = seconds.toLong(),
                            remainingSeconds = tick.toLong(),
                            isActive = true,
                            startTime = countdownStartTime
                        )
                    )
                }
            }
        }
    }
    fun stopAutoCloseMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            if (countdownManagerUseCase.getCountdownById(CountdownId.CLOSE_MESSAGE) != null) {
                _uiState.update { it.copy(acceptTimerState = null) }
                countdownManagerUseCase.stopCountdownById(CountdownId.CLOSE_MESSAGE)
                countdownManagerUseCase.deleteCountdownById(CountdownId.CLOSE_MESSAGE)
            }
        }
    }
    fun onAnswerClicked() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnswerVisible = false,
                    isPrintVisible = false,
                    isPredefinedVisible = true,
                    isNewMessageVisible = true
                )
            }
        }
    }
    fun onPrintClicked(messageId: Int) {
        viewModelScope.launch {
            _uiEffect.emit(MessageDetailUiEffect.PrintMessage(messageId))
        }
    }
    fun onDeleteClicked(messageId: Int) {
        viewModelScope.launch {
            _uiEffect.emit(
                MessageDetailUiEffect.ShowDeleteConfirmDialog(
                    MessageDetailDialogState(
                        title = context.getString(R.string.dialog_delete_message_title),
                        description = context.getString(R.string.dialog_delete_message_desc),
                        buttons = listOf(MessageDetailComposeFragmentDialogButtonType.CANCEL, MessageDetailComposeFragmentDialogButtonType.ACCEPT)
                    )
                )
            )
        }
    }
    fun confirmDelete(messageId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            messageUseCase.getMessage(messageId)?.let { messageUseCase.deleteMessage(it) }
        }
    }
    fun onAcceptClicked() {
        viewModelScope.launch {
            _uiEffect.emit(MessageDetailUiEffect.NavigateBack)
        }
    }
    fun onPredefinedClicked() {
        viewModelScope.launch {
            _uiEffect.emit(MessageDetailUiEffect.OpenPredefinedMessages)
        }
    }
    fun onNewMessageClicked() {
        viewModelScope.launch {
            _uiEffect.emit(
                MessageDetailUiEffect.ShowWriteCustomMessageDialog(
                    MessageDetailDialogState(
                        title = context.getString(R.string.write_message),
                        hint = context.getString(R.string.message),
                        buttons = listOf(MessageDetailComposeFragmentDialogButtonType.CANCEL, MessageDetailComposeFragmentDialogButtonType.SEND)
                    )
                )
            )
        }
    }
    fun sendMessage(response: String, dispatchNumber: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = _uiState.value.message ?: return@launch
            val freeMessageRequest = FreeTextMessageRequest().apply {
                freeTextMessage = response
                dispatchNumber = dispatchNumber ?: ""
                operadora = message.provider
            }
            alfaMessageHandler.sendMessage(freeMessageRequest)
        }
    }
    fun onMessageLoaded(message: MessageEntity?) {
        if (message != null && !message.isUrgent) {
            _uiState.update { it.copy(answerEnabled = false) }
        }
    }
    fun buildTicketContent(message: MessageEntity?, context: android.content.Context): String {
        if (message == null) return ""
        val messageTypeText = when (message.messageType) {
            MessageType.MESSAGE -> context.getString(R.string.message_receive).uppercase()
            MessageType.DISPATCH -> context.getString(R.string.dispatch_receive)
            MessageType.PREDISPATCH -> context.getString(R.string.predispatch_receive)
            else -> null
        }
        return buildString {
            messageTypeText?.let { appendLine(it) }
            appendLine(message.defaultFormatted)
            appendLine(message.text)
        }
    }
}
