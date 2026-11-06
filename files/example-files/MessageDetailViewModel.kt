package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.models.freetextmessage.FreeTextMessageRequest
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.CountdownManagerUseCase
import ifac.td.taxi.domain.usecase.MessageUseCase
import ifac.td.taxi.domain.usecase.PredefinedMessagesUseCase
import ifac.td.taxi.domain.usecase.PrinterUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.bravocentral.AlfaMessageHandler
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.entities.countdown.CountdownId
import ifac.td.taxi.repository.room.entities.countdown.CountdownIdCallback
import ifac.td.taxi.repository.room.entities.countdown.CountdownState
import ifac.td.taxi.repository.room.entities.message.MessageEntity
import ifac.td.taxi.ui.custom.button.ButtonTimerState
import ifac.td.taxi.viewmodel.model.CountdownModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessageDetailViewModel(
    private val alfaMessageHandler: AlfaMessageHandler,
    private val messageUseCase: MessageUseCase,
    private val predefinedMessagesUseCase: PredefinedMessagesUseCase,
    private val printerUseCase: PrinterUseCase,
    private val countdownManagerUseCase: CountdownManagerUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "MessageDetailViewModel"

    private val _messageFlow = MutableStateFlow<MessageEntity?>(null)
    val messageFlow = _messageFlow.asStateFlow()

    private val _hasPredefinedMessagesFlow = MutableStateFlow<Boolean?>(null)
    val hasPredefinedMessagesFlow = _hasPredefinedMessagesFlow.asStateFlow()

    private val _closeMessagesFlow = MutableStateFlow<Boolean?>(null)
    val closeMessagesFlow = _closeMessagesFlow.asStateFlow()

    private val _buttonTimerState = MutableStateFlow<ButtonTimerState?>(null)
    val buttonTimerState = _buttonTimerState.asStateFlow()

    init {
        viewModelScope.launch {
            _hasPredefinedMessagesFlow.emit(predefinedMessagesUseCase.getPredefinedMessages().size > 0)
        }
    }

    fun initVM(msgId: Int, skipAutoClose: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = messageUseCase.getMessage(msgId)
            messageUseCase.setMessageRead(msgId)
            Logs.d(TAG, "initVM message: $message ")
            _messageFlow.emit(message)

            if (!skipAutoClose) {
                //Check if has auto close
                val messageClosureTimes =
                    mapOf(0 to null, 1 to 5, 2 to 10, 3 to 15, 4 to 20, 5 to 25, 6 to 30, 7 to 60)

                val selectedSeconds =
                    messageClosureTimes[userPreferencesUseCase.getUserPreferences()?.secondsToCloseMsgPosition]
                selectedSeconds?.let {
                    val existingCountdown =
                        countdownManagerUseCase.getCountdownById(CountdownId.CLOSE_MESSAGE)
                    if (existingCountdown == null) {
                        createAndStartCountdown(selectedSeconds)
                    } else if (existingCountdown.state in listOf(CountdownState.FINISHED, CountdownState.STOPPED)) {
                        Logs.d(TAG, "checkMessageClosureTime() existing countdown is finished or stopped, recreating")
                        countdownManagerUseCase.deleteCountdownById(CountdownId.CLOSE_MESSAGE)
                        createAndStartCountdown(selectedSeconds)
                    } else {
                        Logs.d(TAG, "checkMessageClosureTime() countdown already running")
                    }
                }
            }
        }
    }

    private var countdownStartTime: Long = 0L

    private suspend fun createAndStartCountdown(seconds: Int) {
        countdownStartTime = System.currentTimeMillis()
        val countdown = CountdownModel(
            idCountdown = CountdownId.CLOSE_MESSAGE,
            timeInSeconds = seconds.toLong(),
            idCallback = CountdownIdCallback.CLOSE_MESSAGE
        )

        Logs.d(TAG, "createAndStartCountdown() countdown: $countdown")

        viewModelScope.launch(Dispatchers.IO) {
            _buttonTimerState.emit(
                ButtonTimerState(
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
                Logs.d(TAG, "handleCountdownFinished() triggered")
                countdownManagerUseCase.deleteCountdownById(CountdownId.CLOSE_MESSAGE)
                _closeMessagesFlow.emit(true)
                Logs.d(TAG, "handleCountdownFinished() message callback triggered")
            }
        }
        countdownManagerUseCase.setCountdownOnTickCallback(idCountdown = countdown.idCountdown) { tick ->
            viewModelScope.launch(Dispatchers.IO) {
                _buttonTimerState.emit(
                    ButtonTimerState(
                        maxSeconds = seconds.toLong(),
                        remainingSeconds = tick.toLong(),
                        isActive = true,
                        startTime = countdownStartTime
                    )
                )
            }
        }
    }

    fun stopAutoCloseMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            if (countdownManagerUseCase.getCountdownById(CountdownId.CLOSE_MESSAGE) != null) {
                _buttonTimerState.emit(null)
                countdownManagerUseCase.stopCountdownById(CountdownId.CLOSE_MESSAGE)
                countdownManagerUseCase.deleteCountdownById(CountdownId.CLOSE_MESSAGE)
            }
        }
    }

    fun sendMessage(response: String, dispatchNumber: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            messageFlow.value?.let { message ->
                val freeMessageRequest = FreeTextMessageRequest()
                freeMessageRequest.freeTextMessage = response
                freeMessageRequest.dispatchNumber = dispatchNumber ?: ""
                freeMessageRequest.operadora = message.provider
                alfaMessageHandler.sendMessage(freeMessageRequest)
            }
        }
    }

    fun deleteMessage(messageId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = messageUseCase.getMessage(messageId)
            if (message != null) {
                messageUseCase.deleteMessage(message)
            }
        }

    }

    fun printMessage(messageId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = messageUseCase.getMessage(messageId)
            if (message != null) {
                printerUseCase.printMessageEntity(header = context.getString(R.string.message_receive).uppercase(), message = message, paperFeed = true)
            }
        }
    }
}