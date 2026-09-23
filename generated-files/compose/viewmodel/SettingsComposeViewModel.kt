package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.SettingsUiEvent
import ifac.td.taxi.ui.screen.components.SettingsUiEffect
import ifac.td.taxi.ui.screen.components.SettingsButtonsState
import ifac.td.taxi.ui.screen.components.SettingsUiState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 183-2: import android.app.Application
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
