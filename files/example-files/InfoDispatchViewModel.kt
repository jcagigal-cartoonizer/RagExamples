package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.BridgeCallUseCase
import ifac.td.taxi.domain.usecase.CountdownManagerUseCase
import ifac.td.taxi.domain.usecase.DispatchNotificationUseCase
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.TTSUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.PhoneCallUseCase
import ifac.td.taxi.framework.sdk.usecase.PhoneCallUseCaseImpl
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.dao.BravoConfigurationVariableDao
import ifac.td.taxi.repository.room.entities.BravoConfigurationVariableEntity
import ifac.td.taxi.repository.room.entities.DispatchExtraDataEntity
import ifac.td.taxi.repository.room.entities.countdown.CountdownId
import ifac.td.taxi.repository.room.entities.countdown.CountdownIdCallback
import ifac.td.taxi.viewmodel.model.CountdownModel
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class InfoDispatchViewModel(
    private val dispatchUseCase: DispatchUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val phoneCallUseCase: PhoneCallUseCase,
    private val dispatchNotificationUseCase: DispatchNotificationUseCase,
    private val bridgeCallUseCase: BridgeCallUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val ttsUseCase: TTSUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    private val countdownManagerUseCase: CountdownManagerUseCase,
    context: Application,
) : BaseViewModel(context) {

    var hasShowedConcertedPriceDialog: Boolean = false

    private val _timerFlow = MutableStateFlow<Int?>(null)
    val timerFlow = _timerFlow.asStateFlow()

    private val _extraDataFlow = MutableStateFlow<DispatchExtraDataEntity?>(null)
    val extraDataFlow = _extraDataFlow.asStateFlow()

    private val _noClientButtonFlow = MutableSharedFlow<Boolean?>()
    val noClientButtonFlow = _noClientButtonFlow.asSharedFlow()

    private val _bravoConfigurationLogin = MutableStateFlow<BravoConfigurationVariableEntity?>(null)
    val bravoConfigurationLogin = _bravoConfigurationLogin.asStateFlow()

    private val _phoneCallErrorFlow = MutableSharedFlow<PhoneCallUseCaseImpl.PhoneCallError?>()
    val phoneCallErrorFlow = _phoneCallErrorFlow.asSharedFlow()

    private val _bridgeCallMaxTriesFlow = MutableSharedFlow<Int>()
    val bridgeCallMaxTriesFlow = _bridgeCallMaxTriesFlow.asSharedFlow()

    private val _meetingSignColors = MutableStateFlow<Pair<Int, Int>>(Pair(0,0))
    val meetingSignColors = _meetingSignColors.asStateFlow()

    private val _bridgeCallTimeoutFlow = MutableSharedFlow<Unit>()
    val bridgeCallTimeoutFlow = _bridgeCallTimeoutFlow.asSharedFlow()

    private val _bridgeCallStateFlow = MutableStateFlow<Int?>(null)
    val bridgeCallStateFlow = _bridgeCallStateFlow.asStateFlow()

    private companion object {
        private const val BRIDGE_CALL_TIMEOUT_SECONDS = 10L
    }

    private val TAG = "InfoDispatchViewModel"

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _bravoConfigurationLogin.emit(bravoConfigurationVariableDao.getBravoConfigurationVariable())
        }
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                _meetingSignColors.emit(Pair(it.meetingSignTextColor, it.meetingSignBackgroundColor))
            }
        }
    }

    fun sendAtTheDoorNotification(dispatch: InfoDispatchModel?) {
        viewModelScope.launch {
            dispatch?.let {
                dispatchNotificationUseCase.atTheDoor(it.id)
            }
        }
    }

    fun sendNoClientNotification(currentDispatchId: Long?) {
        viewModelScope.launch {
            currentDispatchId?.let {
                dispatchNotificationUseCase.noClient(it)
            }
        }
    }

    fun sendInCabNotification(dispatch: InfoDispatchModel?) {
        viewModelScope.launch {
            dispatch?.let {
                dispatchNotificationUseCase.inCab(it.id)
            }
        }
    }

    fun canGoToHiredManual(taximeterConnected: Boolean): Boolean {
        return licensingUseCase.canDoManualTrips(taximeterConnected)
    }

    fun goToHiredManual(tripId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            shiftStatusUseCase.goToHiredManualFromDispatch(tripId = tripId)
        }
    }

    fun printInfoDispatch(dispatch: InfoDispatchModel) {
        viewModelScope.launch {
            dispatchUseCase.getCurrentDispatchInfo(dispatch)
        }
    }

    fun checkTTSOnDispatch(dispatch: InfoDispatchModel) {
        viewModelScope.launch {
            if (userPreferencesUseCase.getUserPreferences()?.ttsDispatch == true) {
                if (!ttsUseCase.isTTSSpeaking()) {
                    ttsUseCase.createTextFromDispatch(dispatch)
                }
            }
        }
    }

    fun stopTTS() {
        viewModelScope.launch {
            if (!ttsUseCase.isSpeechCompleted()) {
                ttsUseCase.cancelCurrentSpeech()
            }
        }
    }

    fun retrieveExtraData(idDispatch: Long) {
        viewModelScope.launch {
            dispatchUseCase.getTripExtraData(idDispatch, _extraDataFlow)
        }
    }

    fun checkNoClientButton() {
        viewModelScope.launch(Dispatchers.IO) {
            val parameters = licensingUseCase.getLicensingParameters()
            Logs.d(TAG, "isNoClientHired: ${parameters?.isNoClientHired}")
            _noClientButtonFlow.emit(parameters?.isNoClientHired)
        }
    }

    fun getProvider(idDispatch: Long, dataToString: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            var provider: String? = null
            for (str in dataToString.reversed()) {
                val trimmed = str.trim()
                if (trimmed.startsWith("OPERADOR:")) {
                    provider = trimmed.substringAfter("OPERADOR:").trim()
                    break
                }
            }

            provider?.let {
                dispatchUseCase.updateDispatchProvider(idDispatch = idDispatch, provider = it)
            }
        }
    }

    fun makeBridgeCall(dispatchNumber: String) {
        viewModelScope.launch {
            Logs.d(TAG, "makeBridgeCall: starting bridge call to dispatch: $dispatchNumber")

            val countdown = CountdownModel(
                idCountdown = CountdownId.CLIENT_CALL_BUTTON,
                timeInSeconds = BRIDGE_CALL_TIMEOUT_SECONDS,
                idCallback = CountdownIdCallback.CLIENT_CALL_BUTTON
            )

            countdownManagerUseCase.stopCountdownById(CountdownId.CLIENT_CALL_BUTTON)
            countdownManagerUseCase.deleteCountdownById(CountdownId.CLIENT_CALL_BUTTON)
            countdownManagerUseCase.createOrUpdateCountdown(countdown)

            val initialState = bridgeCallStateFlow.value ?: 0

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    bridgeCallStateFlow.first { it != initialState }
                    Logs.d(TAG, "makeBridgeCall: bridgeCallState changed from $initialState, cancelling countdown")
                    countdownManagerUseCase.stopCountdownById(CountdownId.CLIENT_CALL_BUTTON)
                    countdownManagerUseCase.deleteCountdownById(CountdownId.CLIENT_CALL_BUTTON)
                } catch (e: Exception) {
                    Logs.d(TAG, "makeBridgeCall: observer cancelled or completed")
                }
            }

            countdownManagerUseCase.startCountdown(countdown)
            countdownManagerUseCase.setCountdownFinishCallback(CountdownId.CLIENT_CALL_BUTTON) {
                viewModelScope.launch {
                    Logs.e(TAG, "makeBridgeCall: timeout reached, emitting bridgeCallTimeoutFlow")
                    _bridgeCallTimeoutFlow.emit(Unit)
                }
            }

            bridgeCallUseCase.startBridgeCall(dispatchNumber)
        }
    }

    fun cancelBridgeCall(dispatchNumber: String) {
        Logs.d(TAG, "cancelBridgeCall: cancelling bridge call for dispatch: $dispatchNumber")
        viewModelScope.launch {
            countdownManagerUseCase.stopCountdownById(CountdownId.CLIENT_CALL_BUTTON)
            countdownManagerUseCase.deleteCountdownById(CountdownId.CLIENT_CALL_BUTTON)
            bridgeCallUseCase.cancelBridgeCall(dispatchNumber)
        }
    }

    fun startCall(phoneNumber: String) {
        viewModelScope.launch {
            val response = phoneCallUseCase.makePhoneDial(phoneNumber)
            _phoneCallErrorFlow.emit(response)
        }
    }

    fun changeStateToPaymentManual() {
        viewModelScope.launch {
            shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_PAYMENT)
                ?.let {
                    shiftStatusUseCase.setStatus(it, true)
                }
        }
    }

    fun hasITopTaximeterConnected(): Boolean {
        return bluetoothLocalUseCase.isCurrentBluetoothItop()
    }

    fun updateSelectedDispatch(dispathId: String, routeId: String) {
        viewModelScope.launch {
            dispatchUseCase.updateSelectedDispatchId(dispathId, routeId)
        }
    }

    fun isCurrentBluetoothITop(): Boolean {
        return bluetoothLocalUseCase.isCurrentBluetoothItop()
    }

    fun getMaxBridgeCalls() {
        viewModelScope.launch(Dispatchers.IO) {
            val maxBridgeCalls = bravoCentralUseCase.getBravoConfiguration()?.maxCalls
            Logs.d(TAG, "getMaxBridgeCalls: Max Bridge Calls: $maxBridgeCalls")
            if (maxBridgeCalls != null) {
                _bridgeCallMaxTriesFlow.emit(maxBridgeCalls)
            } else {
                Logs.e(TAG, "getMaxBridgeCalls: Failed to retrieve max bridge calls from Bravo Central")
            }
        }
    }

    fun saveBridgeCallState(bridgeCallState: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _bridgeCallStateFlow.value = bridgeCallState
        }
    }
}
