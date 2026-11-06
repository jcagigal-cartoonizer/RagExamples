package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.viewModelScope
//import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import com.interfacom.sdk.taximeter.taximeter.models.taximeterstatus.StatusTaximeter
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.ConfigurationScreenPasswordUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.util.Logs
//import ifac.td.smarttd.framework.sdk.model.BluetoothInfo.Companion.BLUETOOTH_STRUCTAB
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val sessionUseCase: SessionUseCase,
    private val configurationScreenPasswordUseCase: ConfigurationScreenPasswordUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "SettingsViewModel"

    private val _bluetoothLocalFlow = MutableStateFlow<BluetoothInfo?>(null)
    val bluetoothLocalFlow = _bluetoothLocalFlow.asStateFlow()

    private val _conditionsLocalFlow = MutableStateFlow<Boolean?>(null)
    val conditionsLocalFlow = _conditionsLocalFlow.asStateFlow()

    private val _showSoundBrightButtonFlow = MutableStateFlow<Boolean>(false)
    val showSoundBrightButtonFlow = _showSoundBrightButtonFlow.asStateFlow()

    private val _statusFlow = MutableStateFlow<Int?>(null)
    val statusFlow = _statusFlow.asStateFlow()

    private val _userLogInFlow = MutableStateFlow(true)
    val userLogInFlow = _userLogInFlow.asStateFlow()

    private val _webViewUrlFlow = MutableStateFlow<String?>(null)
    val webViewUrlFlow = _webViewUrlFlow.asStateFlow()

    private val _correctPasswordFlow = MutableSharedFlow<Pair<Boolean, () -> Unit>>()
    val correctPasswordFlow = _correctPasswordFlow.asSharedFlow()

    private val _canSeeDriverRequirementsFlow = MutableStateFlow<Boolean>(false)
    val canSeeDriverRequirementsFlow = _canSeeDriverRequirementsFlow.asStateFlow()

    private val _configurationPasswordDialogFlow = MutableStateFlow<Boolean?>(null)
    val configurationPasswordDialogFlow = _configurationPasswordDialogFlow.asStateFlow()

//    private val _iTopFlow = MutableStateFlow<Boolean?>(null)
//    val iTopFlow = _iTopFlow.asStateFlow()

    fun clickBluetooth() {
        viewModelScope.launch {
            navigateTo(R.id.action_settingsFragment_to_discoveryChannelFragment)
        }
    }

    fun clickAbout() {
        viewModelScope.launch {
            navigateTo(R.id.action_settingsFragment_to_aboutFragment)
        }
    }

    fun clickGPS() {
        viewModelScope.launch {
            navigateTo(R.id.action_settingsFragment_to_GPSConfigFragment)
        }
    }

    fun clickLights() {
        viewModelScope.launch {
            navigateTo(R.id.action_settingsFragment_to_lightsTestFragment)
        }
    }

    fun clickRequirements() {
        viewModelScope.launch {
            navigateTo(R.id.action_settingsFragment_to_toolsFragment)
        }
    }

    /*
    fun checkStyleButton(button: ButtonModel) {
        when (button.type) {
            ButtonType.LUMINOSO -> {
                val device = _bluetoothLocalLiveData.value
                if (device != null) {
                    if (_conditionsLocalLiveData.value == true) {
                        button.style = CustomButtonComponent.StyleButton.ENABLE
                    } else {
                        button.style = CustomButtonComponent.StyleButton.DISABLE
                    }
                } else {
                    button.style = CustomButtonComponent.StyleButton.DISABLE
                }
            }

            else -> {}
        }
        _menuLiveData.value = Event(ArrayList())
    }*/

    fun getSavedDevice() {
        viewModelScope.launch {
            val bluetooth = bluetoothLocalUseCase.getLocalBluetooth()
            bluetooth?.let {
                _bluetoothLocalFlow.emit(it)
            }
        }
    }

    fun getStatus() {
        viewModelScope.launch {
            val status = Taximeter.getInstance().bluetoothState
            _statusFlow.emit(status)
        }
    }

    fun checkConditions(device: BluetoothInfo?, status: Int) {
//       val status = Taximeter.getInstance().bluetoothState
        viewModelScope.launch {
            if (device != null) {
                if ((device.name.startsWith("SKYG") || device.name.startsWith("SHER")) && status == StatusTaximeter.BLUETOOTH_CONNECTED_AND_TAXIMETER_CONNECTED) {
                    _conditionsLocalFlow.emit(true)
                } else {
                    _conditionsLocalFlow.emit(false)
                }
            }
        }
    }

    fun checkHasSettingsPassword() {
        viewModelScope.launch {
            val hasPassword = configurationScreenPasswordUseCase.hasSettingsPassword()
            _configurationPasswordDialogFlow.emit(hasPassword)
        }
    }

    fun checkSettingsPassword(pin: String, callback: () -> Unit) {
        viewModelScope.launch {
            val userPassword = configurationScreenPasswordUseCase.encryptSettingsPassword(pin)
            val savedPassword = configurationScreenPasswordUseCase.getSettingsPassword()
            _correctPasswordFlow.emit(Pair(userPassword == savedPassword, callback))
        }
    }

    fun checkUserLogIn() {
        viewModelScope.launch {
            val loggedIn = sessionUseCase.isUserLoggedIn()
            _userLogInFlow.emit(loggedIn)
        }
    }

    fun clickConfiguration() {
        viewModelScope.launch {
            navigateTo(R.id.action_settingsFragment_to_userLoginFragment)
        }
    }

    fun clickDeviceSettings() {
        viewModelScope.launch {
            if (hasAudioPermissions()) {
                navigateTo(R.id.action_settingsFragment_to_deviceSettingsFragment)
            } else {
                checkSystemWriteSettings()
            }
        }
    }

    private fun checkSystemWriteSettings() {
        if (!hasAudioPermissions()) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context?.packageName}"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(intent)
        } else {
            Logs.d(TAG, "checkSystemWriteSettings: System write settings already granted")
        }
    }

    fun hasAudioPermissions(): Boolean {
        return context != null && Settings.System.canWrite(context)
    }

    fun checkWebViewButton() {
        viewModelScope.launch {
            _webViewUrlFlow.emit(bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.webViewURL)
        }
    }

    fun checkDeviceSettingsButton() {
        viewModelScope.launch {
            _showSoundBrightButtonFlow.emit(bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.isSmartTDAccessSoundBright == true)
        }
    }

    fun checkRequirementsButton() {
        viewModelScope.launch {
            val hasRequirements = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.isDriverCanQueryRequirements ?: false
            Logs.d(TAG, "checkRequirementsButton hasRequirements: $hasRequirements")
            _canSeeDriverRequirementsFlow.emit(hasRequirements)
        }
    }

//    fun isItop() {
//        viewModelScope.launch {
//            val taximeterLocal = bluetoothLocalUseCase.getLocalBluetooth()
//            _iTopFlow.emit(taximeterLocal?.name?.startsWith(BLUETOOTH_STRUCTAB))
//        }
//    }
}