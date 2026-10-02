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
// # Block 182-2: import android.app.Application
class SettingsComposeViewModel(
    application: Application,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val sessionUseCase: SessionUseCase,
    private val configurationScreenPasswordUseCase: ConfigurationScreenPasswordUseCase,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<SettingsUiEffect>()
    val uiEffect: SharedFlow<SettingsUiEffect> = _uiEffect.asSharedFlow()
    fun onScreenShown() {
        loadInitialState()
    }
    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            SettingsUiEvent.ConfigurationClicked -> handleConfigurationClick()
            SettingsUiEvent.DeviceSettingsClicked -> handleDeviceSettingsClick()
            SettingsUiEvent.GpsClicked -> emitEffect(SettingsUiEffect.NavigateToGPS)
            SettingsUiEvent.AboutClicked -> emitEffect(SettingsUiEffect.NavigateToAbout)
            SettingsUiEvent.LightClicked -> handleLightClick()
            SettingsUiEvent.RequirementsClicked -> emitEffect(SettingsUiEffect.NavigateToRequirements)
            SettingsUiEvent.PreferenciasClicked -> handlePreferenciasClick()
            SettingsUiEvent.BluetoothClicked -> handleBluetoothClick()
            SettingsUiEvent.WebViewClicked -> {
                uiState.value.webViewUrl?.let { emitEffect(SettingsUiEffect.NavigateToWebView(it)) }
            }
            SettingsUiEvent.DialogDismissed -> hideDialog()
            is SettingsUiEvent.DialogPasswordChanged -> updateDialogPassword(event.value)
            SettingsUiEvent.DialogPasswordConfirmed -> confirmDialogPassword()
        }
    }
    fun loadInitialState() {
        viewModelScope.launch {
            val loggedIn = sessionUseCase.isUserLoggedIn()
            val hasSettingsPassword = configurationScreenPasswordUseCase.hasSettingsPassword()
            val showSoundBright = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.isSmartTDAccessSoundBright == true
            val canSeeRequirements = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.isDriverCanQueryRequirements == true
            val webUrl = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.webViewURL
            val bluetooth = bluetoothLocalUseCase.getLocalBluetooth()
            _uiState.value = _uiState.value.copy(
                userLoggedIn = loggedIn,
                showSoundBrightButton = showSoundBright,
                canSeeDriverRequirements = canSeeRequirements,
                webViewUrl = webUrl,
                bluetoothInfo = bluetooth,
                buttons = SettingsButtonsState.from(
                    bluetoothInfo = bluetooth,
                    userLoggedIn = loggedIn,
                    showSoundBrightButton = showSoundBright,
                    canSeeDriverRequirements = canSeeRequirements,
                    webViewUrl = webUrl,
                    shiftDisconnected = isShiftDisconnected()
                ),
                dialog = if (hasSettingsPassword) {
                    SettingsDialogState.Password(
                        title = "PIN actual",
                        description = "Ingrese el PIN actual",
                        value = ""
                    )
                } else null
            )
            if (bluetooth != null) {
                updateTaximeterStatus()
            }
        }
    }
    fun updateTaximeterStatus() {
        viewModelScope.launch {
            val status = Taximeter.getInstance().bluetoothState
            val device = _uiState.value.bluetoothInfo
            val isValid = device != null &&
                (device.name.startsWith("SKYG") || device.name.startsWith("SHER")) &&
                status == StatusTaximeter.BLUETOOTH_CONNECTED_AND_TAXIMETER_CONNECTED
            _uiState.value = _uiState.value.copy(
                taximeterStatus = status,
                conditionsValid = isValid,
                buttons = _uiState.value.buttons.copy(
                    light = _uiState.value.buttons.light.copy(
                        style = if (isValid) SettingsButtonStyle.Enabled else SettingsButtonStyle.Disabled
                    )
                )
            )
        }
    }
    fun handleConfigurationClick() {
        if (isShiftDisconnected()) {
            emitEffect(SettingsUiEffect.NavigateToUserLogin)
        }
    }
    fun handleDeviceSettingsClick() {
        if (hasAudioPermissions()) {
            emitEffect(SettingsUiEffect.NavigateToDeviceSettings)
        } else {
            emitEffect(SettingsUiEffect.RequestWriteSettingsPermission)
        }
    }
    fun handleLightClick() {
        if (_uiState.value.conditionsValid) {
            emitEffect(SettingsUiEffect.NavigateToLights)
        }
    }
    fun handlePreferenciasClick() {
        val dialogEnabled = _uiState.value.settingsPasswordEnabled
        if (dialogEnabled) showPasswordDialog(
            onSuccess = { emitEffect(SettingsUiEffect.NavigateToUserConfiguration) }
        ) else {
            emitEffect(SettingsUiEffect.NavigateToUserConfiguration)
        }
    }
    fun handleBluetoothClick() {
        val dialogEnabled = _uiState.value.settingsPasswordEnabled
        if (dialogEnabled) showPasswordDialog(
            onSuccess = { emitEffect(SettingsUiEffect.NavigateToDiscoveryChannel) }
        ) else {
            emitEffect(SettingsUiEffect.NavigateToDiscoveryChannel)
        }
    }
    fun showPasswordDialog(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(
            pendingPasswordCallback = onSuccess,
            dialog = SettingsDialogState.Password(
                title = "PIN actual",
                description = "Ingrese el PIN actual",
                value = ""
            )
        )
    }
    fun updateDialogPassword(value: String) {
        val dialog = _uiState.value.dialog
        if (dialog is SettingsDialogState.Password) {
            _uiState.value = _uiState.value.copy(
                dialog = dialog.copy(value = value)
            )
        }
    }
    fun confirmDialogPassword() {
        val dialog = _uiState.value.dialog
        val current = dialog as? SettingsDialogState.Password ?: return
        val pin = current.value
        viewModelScope.launch {
            val encrypted = configurationScreenPasswordUseCase.encryptSettingsPassword(pin)
            val saved = configurationScreenPasswordUseCase.getSettingsPassword()
            if (encrypted == saved) {
                hideDialog()
                _uiState.value.pendingPasswordCallback?.invoke()
                _uiState.value = _uiState.value.copy(pendingPasswordCallback = null)
            } else {
                emitEffect(SettingsUiEffect.ShowToastIncorrectPin)
            }
        }
    }
    fun hideDialog() {
        _uiState.value = _uiState.value.copy(dialog = null)
    }
    fun hasAudioPermissions(): Boolean {
        val context = getApplication<Application>()
        return Settings.System.canWrite(context)
    }
    fun isShiftDisconnected(): Boolean {
        return true
    }
    fun emitEffect(effect: SettingsUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}
