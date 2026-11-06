package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.CountdownManagerUseCase
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.framework.sdk.usecase.GetButtonMinutesUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.PhoneCallUseCase
import ifac.td.taxi.framework.sdk.usecase.SoundManagerUseCase
import ifac.td.taxi.repository.room.dao.BravoConfigurationVariableDao
import ifac.td.taxi.repository.room.entities.BravoConfigurationVariableEntity
import ifac.td.taxi.ui.custom.button.ButtonTimerState
import ifac.td.taxi.viewmodel.model.CountdownModel
import ifac.td.taxi.viewmodel.model.DispatchReceivedModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toDispatchReceivedModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DispatchMinuteReceivedViewModel(
    private val dispatchUseCase: DispatchUseCase,
    private val getButtonMinutesUseCase: GetButtonMinutesUseCase,
    private val countdownManagerUseCase: CountdownManagerUseCase,
    private val soundManagerUseCase: SoundManagerUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val phoneCallUseCase: PhoneCallUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    context: Application,
) : BaseViewModel(context) {

    private val _minutesFlow = MutableStateFlow<ArrayList<Int>?>(null)
    val minutesFlow = _minutesFlow.asStateFlow()

    private val _showDialogOnCancelFlow = MutableSharedFlow<Boolean>()
    val showDialogOnCancelFlow = _showDialogOnCancelFlow.asSharedFlow()

    private val _dispatchFlow = MutableStateFlow<DispatchReceivedModel?>(null)
    val dispatchFlow = _dispatchFlow.asStateFlow()

    private val _bravoConfigurationLoginFlow =
        MutableStateFlow<BravoConfigurationVariableEntity?>(null)
    val bravoConfigurationLoginFlow = _bravoConfigurationLoginFlow.asStateFlow()

    private val _addressAcceptsDispatchFlow = MutableStateFlow<Boolean?>(null)
    val addressAcceptsDispatchFlow = _addressAcceptsDispatchFlow.asStateFlow()

    private val _buttonTimerState = MutableStateFlow<ButtonTimerState?>(null)
    val buttonTimerState = _buttonTimerState.asStateFlow()

    private var actualDispatch: DispatchReceivedModel? = null
    private var idDispatch: Long? = null
    private var countdownCounter: CountdownModel? = null
    private var countdownAccept: CountdownModel? = null

    init {
        viewModelScope.launch {
            _minutesFlow.emit(getButtonMinutesUseCase.getButtonMinutes())
            _bravoConfigurationLoginFlow.emit(bravoConfigurationVariableDao.getBravoConfigurationVariable())
        }
    }

    fun checkCancelDialogPermission() {
        viewModelScope.launch {
            val preferences = userPreferencesUseCase.getUserPreferences()
            preferences?.showConfRejectDispatch?.let { _showDialogOnCancelFlow.emit(it) }
        }
    }

    fun getDispatch(id: Long) {
        viewModelScope.launch {
            if (id != -1L) {
                idDispatch = id

                val dispatch = dispatchUseCase.getDispatchById(id)
                if (dispatch != null) {
                    actualDispatch = dispatch.toDispatchReceivedModel(id)
                    _dispatchFlow.emit(actualDispatch)
                }
            }
        }
    }

    fun checkIsDirRecogidaAceptaDespacho() {
        viewModelScope.launch {
            val preferences = licensingUseCase.getLicensingParameters()
            preferences?.isAddressTakenAcceptsDispatch?.let { _addressAcceptsDispatchFlow.emit(it) }
        }
    }

    fun showPickupAddress(): Boolean {
        return bravoConfigurationLoginFlow.value?.showPickupAddress == true
                && addressAcceptsDispatchFlow.value == true
    }
}