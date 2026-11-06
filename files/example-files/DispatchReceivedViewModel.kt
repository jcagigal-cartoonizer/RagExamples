package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.dao.BravoConfigurationVariableDao
import ifac.td.taxi.repository.room.entities.BravoConfigurationVariableEntity
import ifac.td.taxi.viewmodel.model.DispatchReceivedModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toDispatchReceivedModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DispatchReceivedViewModel(
    private val dispatchUseCase: DispatchUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    private val licensingUsecase: LicensingUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "DispatchReceivedViewModel"

    private val _dispatchFlow = MutableStateFlow<DispatchReceivedModel?>(null)
    val dispatchFlow = _dispatchFlow.asStateFlow()

    private val _bravoConfigurationLoginFlow = MutableStateFlow<BravoConfigurationVariableEntity?>(null)
    val bravoConfigurationLoginFlow = _bravoConfigurationLoginFlow.asStateFlow()

    private val _showDialogOnCancelFlow = MutableSharedFlow<Boolean>()
    val showDialogOnCancelFlow = _showDialogOnCancelFlow.asSharedFlow()

    private val _addressAcceptsDispatchFlow = MutableStateFlow<Boolean?>(null)
    val addressAcceptsDispatchFlow = _addressAcceptsDispatchFlow.asStateFlow()

    private var actualDispatch: DispatchReceivedModel? = null
    private var idDispatch: Long? = null


    init {
        viewModelScope.launch {
            _bravoConfigurationLoginFlow.emit(bravoConfigurationVariableDao.getBravoConfigurationVariable())
        }
    }

    fun getDispatch(id: Long) {
        viewModelScope.launch {
            idDispatch = id

            val dispatch = dispatchUseCase.getDispatchById(id)
            if (dispatch != null) {
                actualDispatch = dispatch.toDispatchReceivedModel(id)
                _dispatchFlow.emit(actualDispatch)
            }
        }
    }
    fun checkCancelDialogPermission() {
        viewModelScope.launch {
            val preferences = userPreferencesUseCase.getUserPreferences()
            preferences?.showConfRejectDispatch?.let { _showDialogOnCancelFlow.emit(it) }
        }
    }

    fun getDispatchProvider(idDispatch: Long, dataToString: List<String>) {
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

    fun checkIsDirRecogidaAceptaDespacho() {
        viewModelScope.launch {
            val preferences = licensingUsecase.getLicensingParameters()
            preferences?.isAddressTakenAcceptsDispatch?.let { _addressAcceptsDispatchFlow.emit(it) }
        }
    }

    fun showPickupAddress(): Boolean {
        val addressParamLogin = bravoConfigurationLoginFlow.value?.showPickupAddress
        val addressParamLicensing = addressAcceptsDispatchFlow.value
        Logs.d(
            TAG,
            "showPickup: from login $addressParamLogin, from licensing $addressParamLicensing"
        )
        return addressParamLogin == true
                && addressParamLicensing == true
    }
}
