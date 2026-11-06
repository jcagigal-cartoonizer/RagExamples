package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import com.interfacom.sdk.taximeter.taximeter.TaximeterConstants
import ifac.td.taxi.BuildConfig
import ifac.td.taxi.domain.model.UserPreferences
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.IngenicoUseCase
import ifac.td.taxi.domain.usecase.PortugalUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.ShiftUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.SendLogsUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.screen.UserPreferencesFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserPreferencesViewModel(
    private val externalBridgeInterface: ExternalBridgeInterface,
    private val shiftUseCase: ShiftUseCase,
    private val sessionUseCase: SessionUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val sendLogsUseCase: SendLogsUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val portugalUseCase: PortugalUseCase,
    private val ingenicoUseCase: IngenicoUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val _userInfoFlow = MutableStateFlow<Boolean?>(null)
    val userInfoFlow = _userInfoFlow.asStateFlow()

    private val _userPreferencesFlow = MutableStateFlow<UserPreferences?>(null)
    val userPreferencesFlow = _userPreferencesFlow.asStateFlow()

    private val _shiftsFlow = MutableSharedFlow<Boolean>()
    val shiftsFlow = _shiftsFlow.asSharedFlow()

    private val _taximeterSkyGlassFlow = MutableStateFlow<Boolean>(false)
    val taximeterSkyGlassFlow = _taximeterSkyGlassFlow.asStateFlow()

    private val _cbLightOffOnDispatchedFlow = MutableStateFlow(false)
    val cbLightOffOnDispatchedFlow = _cbLightOffOnDispatchedFlow.asStateFlow()

    private val _licensingFiscalFlow = MutableStateFlow(false)
    val licensingFiscalFlow = _licensingFiscalFlow.asStateFlow()

    private val _ingenicoInstalledFlow = MutableStateFlow(false)
    val ingenicoInstalledFlow = _ingenicoInstalledFlow.asStateFlow()

    private val _driverTrunModeFlow = MutableStateFlow<Int?>(null)
    val driverTrunModeFlow = _driverTrunModeFlow.asStateFlow()

    // Se emite cuando ya existe un pin y hay que pedir el pin actual antes de modificarlo
    private val _requestCurrentSecurePinFlow = MutableSharedFlow<Boolean>()
    val requestCurrentSecurePinFlow = _requestCurrentSecurePinFlow.asSharedFlow()

    // Se emite cuando el pin actual introducido no coincide con el guardado
    private val _wrongSecurePinFlow = MutableSharedFlow<Boolean>()
    val wrongSecurePinFlow = _wrongSecurePinFlow.asSharedFlow()

    private val TAG = "UserPreferencesViewModel"

    init {
        isUserLogIn()
        getUserPreferences()
    }

    fun deleteShiftByDate(year: Int, month: Int, day: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                shiftUseCase.deleteShiftByDate(year = year, month = month, day = day)
            } catch (e: Exception) {
                Logs.e(TAG, "deleteShiftByDate: $e")
            }
        }
    }

    private fun isUserLogIn() {
        viewModelScope.launch(Dispatchers.IO) {
            _userInfoFlow.emit(sessionUseCase.isUserLoggedIn())
        }
    }

    fun insertUserPreferences(userPreferences: UserPreferences) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesUseCase.insertUserPreferences(userPreferences)
        }
    }

    fun getUserPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            _userPreferencesFlow.emit(userPreferencesUseCase.getUserPreferences())
        }
    }

    fun sendLogs(description: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            sendLogsUseCase.prepareLogSending(description)
        }
    }

    fun checkShifts() {
        viewModelScope.launch {
            val allShifts = shiftUseCase.getAll()
            _shiftsFlow.emit(allShifts.isNullOrEmpty())
        }
    }

    fun checkTaximeter() {
        viewModelScope.launch {
            Logs.d(TAG, "checkTaximeter: Checking taximeter status")

            //val isSkyGlass = isSkyGlass()
            val taximeterInstance = Taximeter.getInstance().isSkyGlass //Is Skyglass, urba, urba1, sherp
            val isTimeControlAvailable = externalBridgeInterface.isSkyGlassWithTimeControlAvailable

            //Logs.d(TAG, "checkTaximeter: isSkyGlass = $isSkyGlass")
            Logs.d(TAG, "checkTaximeter: taximeterInstance.isSkyGlass = $taximeterInstance")
            Logs.d(TAG, "checkTaximeter: isTimeControlAvailable = $isTimeControlAvailable")

            val isSkyGlassAvailable = taximeterInstance && isTimeControlAvailable /*&& isSkyGlass */
            Logs.d(TAG, "checkTaximeter: isSkyGlassAvailable = $isSkyGlassAvailable")

            _taximeterSkyGlassFlow.emit(isSkyGlassAvailable)
        }
    }

    private suspend fun isSkyGlass(): Boolean {
        val btName = bluetoothLocalUseCase.getLocalBluetooth()?.name
        return btName != null && (btName.startsWith("SK"))
    }

    fun checkLightOffOnDispatched() {
        viewModelScope.launch {
            val txmInstance = Taximeter.getInstance()
            var show = false
            // adapted from BravoService.showLuminoso() from v2
            if (txmInstance.isConnected && txmInstance.isRoofLightAvailable) {
                if (txmInstance.internalMeterType == TaximeterConstants.METER_SOFTWARE ||
                    txmInstance.internalMeterType == TaximeterConstants.METER_DUAL_SOFTWARE &&
                    Taximeter.getExternalInterface().isUseTaxitronicPrinter
                ) {
                    show = true
                } else if (!txmInstance.answerK62.contains("TXD30") &&
                    !txmInstance.isTaximeterWithoutProtocol
                ) {
                    show = licensingUseCase.getLicensingParameters()?.isDisableLuminous ?: false
                } else {
                    show = licensingUseCase.getLicensingParameters()?.isDisableLuminous ?: false
                }
            }
            _cbLightOffOnDispatchedFlow.emit(show)
        }
    }

    fun checkFiscalLicensing() {
        viewModelScope.launch {
            val fiscal: Boolean = licensingUseCase.getLicensingParameters()?.isFiscalService ?: false
            Logs.d(TAG, "checkLicensing isFiscalService ${fiscal}")
            _licensingFiscalFlow.emit(fiscal)
        }
    }

    fun checkPinPortugal(editTextString: String) {
        viewModelScope.launch {
            val portugalCode = BuildConfig.portugal_p
            val portugalPassword = portugalUseCase.getPortugalData()
            if (editTextString == portugalCode || (portugalPassword != null && portugalPassword.portugalPassword == portugalPassword.encryptPortugalPassword(editTextString))) {
                navigateTo(UserPreferencesFragmentDirections.actionUserConfigurationFragmentToPortugalSettingsFragment())
            }
        }

    }

    fun hasIngenicoInstalled() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasIngenico = ingenicoUseCase.hasIngenicoInstalled()
            Logs.d(TAG, "hasIngenicoInsttalled: $hasIngenico")
            _ingenicoInstalledFlow.emit(hasIngenico)
        }
    }

    fun checkDriverTrunMode() {
        viewModelScope.launch(Dispatchers.IO) {
            val driverTrunMode = licensingUseCase.getStartTurnMode()
            _driverTrunModeFlow.emit(driverTrunMode)
        }
    }

    fun navigateToSecurePin() {
        viewModelScope.launch(Dispatchers.IO) {
            val actualPin: String? = userPreferencesUseCase.getSecurePin()

            if (actualPin.isNullOrEmpty()) {
                // No hay pin -> modo crear, navegamos directamente
                navigateTo(UserPreferencesFragmentDirections.actionUserConfigurationFragmentToSecurePinFragment())
            } else {
                // Ya existe un pin -> pedimos el pin actual antes de permitir modificarlo/eliminarlo
                Logs.d(TAG, "checkSecure: ${encodeSecurePin(actualPin)}")
                _requestCurrentSecurePinFlow.emit(true)
            }
        }
    }

    fun verifyCurrentSecurePin(currentPin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val actualPin: String? = userPreferencesUseCase.getSecurePin()
            if (currentPin == actualPin) {
                navigateTo(UserPreferencesFragmentDirections.actionUserConfigurationFragmentToSecurePinFragment())
            } else {
                _wrongSecurePinFlow.emit(true)
            }
        }
    }


    /* private void sendLog() {
        Log.d(TAG, "sendLog() from UserConfig");
        RemoteLog.flush();
        com.interfacom.sdk.taximeter.log.log_manager.FTPLogManager.getInstance().sendAllLogs();
    }
    private void sendText(final String text) {
        Log.d(TAG, "sendText() from UserConfig");
        RemoteLog.flush();
        FTPLogManager.getInstance().sendText(text);
    }
    */


}