package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.domain.usecase.CountdownManagerUseCase
import ifac.td.taxi.domain.usecase.MessageUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.PhoneCallUseCase
import ifac.td.taxi.framework.sdk.usecase.SoundManagerUseCase
import ifac.td.taxi.repository.room.dao.BravoConfigurationVariableDao
import ifac.td.taxi.repository.room.entities.BravoConfigurationVariableEntity
import ifac.td.taxi.ui.custom.button.ButtonTimerState
import ifac.td.taxi.ui.model.PreDispatchModel
import ifac.td.taxi.viewmodel.model.CountdownModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreDispatchReceivedViewModel(
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val soundManagerUseCase: SoundManagerUseCase,
    private val countdownManagerUseCase: CountdownManagerUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val phoneCallUseCase: PhoneCallUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    private val messagesUseCase: MessageUseCase,
    private val licensingUsecase: LicensingUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "PreDispatchReceivedViewModel"

    private val _bravoConfigurationLoginFlow =
        MutableStateFlow<BravoConfigurationVariableEntity?>(null)
    val bravoConfigurationLoginFlow = _bravoConfigurationLoginFlow.asStateFlow()

    private val _addressAcceptsDispatchFlow = MutableStateFlow<Boolean?>(null)
    val addressAcceptsDispatchFlow = _addressAcceptsDispatchFlow.asStateFlow()

    private val _buttonTimerState = MutableStateFlow<ButtonTimerState?>(null)
    val buttonTimerState = _buttonTimerState.asStateFlow()

    private var countdownCallCounter: CountdownModel? = null
    private var countdownAccept: CountdownModel? = null

    init {
        viewModelScope.launch {
            _bravoConfigurationLoginFlow.emit(bravoConfigurationVariableDao.getBravoConfigurationVariable())
        }
    }

    fun acceptDispatch(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteCountDownAcceptDispatch()

            bravoCentralUseCase.acceptPreDispatch(
                dispatchNumber = id,
                cause = ifConstants.DISPATCH_ACCEPTED_MANUAL
            )

            stopCallTimer()

            soundManagerUseCase.stopRingTone()
            soundManagerUseCase.stopVibration()
            navigateBack()
        }
    }

    fun savePreDispatchMessage(infoMessage: String) {
        viewModelScope.launch(Dispatchers.IO) {
            messagesUseCase.savePreDispatchMessage(infoMessage)
        }
    }
    fun rejectManualDispatch(id: String) {
        deleteCountDownAcceptDispatch()
        viewModelScope.launch {
            bravoCentralUseCase.rejectPreDispatch(
                dispatchNumber = id,
                cause = ifConstants.DISPATCH_REJECTED_MANUAL,
            )

            stopSounds()
            navigateBack()
        }
    }

    private fun deleteCountDownAcceptDispatch() {
        viewModelScope.launch {
            countdownAccept?.let {
                countdownManagerUseCase.stopCountdown(it)
                countdownManagerUseCase.deleteCountdownById(it.idCountdown)
                _buttonTimerState.emit(null)
            }
        }
    }

    fun stopCallTimer() {
        viewModelScope.launch {
            countdownCallCounter?.let { countdown ->
                countdownManagerUseCase.stopCountdown(countdown)
                countdownManagerUseCase.deleteCountdownById(countdown.idCountdown)
            }

            stopSounds()
        }
    }

    private fun stopSounds() {
        viewModelScope.launch {
            soundManagerUseCase.stopRingTone()
            soundManagerUseCase.stopVibration()
        }
    }

    fun checkIsDirRecogidaAceptaDespacho() {
        viewModelScope.launch {
            val preferences = licensingUsecase.getLicensingParameters()
            preferences?.isAddressTakenAcceptsDispatch?.let { _addressAcceptsDispatchFlow.emit(it) }
        }
    }

    fun showPickupAddress(): Boolean {
        return bravoConfigurationLoginFlow.value?.showPickupAddress == true
                && addressAcceptsDispatchFlow.value == true
    }
}
