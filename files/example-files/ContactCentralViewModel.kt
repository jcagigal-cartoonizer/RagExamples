package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.PredefinedMessagesUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.framework.sdk.usecase.VoicePetitionUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.screen.ContactCentralFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactCentralViewModel(
    private val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val predefinedMessagesUseCase: PredefinedMessagesUseCase,
    private val voicePetitionUseCase: VoicePetitionUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "ContactCentralViewModel"

    private val _hasPredefinedMessagesFlow = MutableStateFlow(false)
    val hasPredefinedMessagesFlow = _hasPredefinedMessagesFlow.asStateFlow()

    private val _hasShortBreak = MutableStateFlow(false)
    val hasShortBreak = _hasShortBreak.asStateFlow()

    private val _isITopTaximeter = MutableStateFlow(false)
    val isITopTaximeter = _isITopTaximeter.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            //TODO: Check information message, post on livedata and change the button
            _hasPredefinedMessagesFlow.emit(predefinedMessagesUseCase.getPredefinedMessages().size > 0)
            _hasShortBreak.emit(bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.hasShortBreak ?: false)
        }
    }

    fun navigateToPredefinedMessages() {
        viewModelScope.launch {
            navigateTo(
                ContactCentralFragmentDirections.actionContactCentralFragmentToPredefinedMessageFragment(
                    -1, true
                )
            )
        }
    }

    fun navigateToInformationMessages() {
        viewModelScope.launch {
            navigateTo(R.id.action_contactCentralFragment_to_informationMessageFragment)
        }
    }

    fun sendVoiceRequest(value: Boolean?) {
        voicePetitionUseCase.voiceRequest(value ?: true) //Porque en V2 está true or defecto
    }

    fun isHired(currentStatus: Int): Boolean {
        return shiftStatusUseCase.isHired(currentStatus)
    }

    fun checkITopTaximeter() {
        viewModelScope.launch {
            val isITop = bluetoothLocalUseCase.isCurrentBluetoothItop()
            Logs.d(TAG, "isITopTaximeter: $isITop")
            _isITopTaximeter.emit(isITop)
        }
    }
}