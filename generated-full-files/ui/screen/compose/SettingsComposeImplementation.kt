package ifac.td.taxi.ui.screen.compose
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Spacer
import ifac.td.taxi.framework.util.Logs
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import ifac.td.taxi.domain.usecase.SessionUseCase
import androidx.compose.runtime.LaunchedEffect
import ifac.td.taxi.domain.usecase.ConfigurationScreenPasswordUseCase
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.update
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import ifac.td.taxi.framework.PermissionRequest
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.setValue
import android.content.Intent
import com.interfacom.sdk.taximeter.taximeter.models.taximeterstatus.StatusTaximeter
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.navigation.NavDirections
import androidx.compose.foundation.background
import androidx.lifecycle.AndroidViewModel
import android.provider.Settings
import android.app.Application
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import androidx.navigation.NavDeepLinkRequest
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.ui.custom.button.CustomButton
import kotlinx.coroutines.flow.MutableSharedFlow
import ifac.td.taxi.R
import androidx.compose.foundation.layout.Column


@Immutable
data class SettingsButtonVisualState(
    val configurationStyle: CustomButton.StyleButton = CustomButton.StyleButton.ENABLE,
    val deviceSettingsVisible: Boolean = false,
    val bluetoothVisible: Boolean = true,
    val preferenciasVisible: Boolean = true,
    val gpsVisible: Boolean = true,
    val aboutVisible: Boolean = true,
    val lightVisible: Boolean = true,
    val webViewVisible: Boolean = false,
    val requirementsVisible: Boolean = false,
    val lightStyle: CustomButton.StyleButton = CustomButton.StyleButton.DISABLE,
)

@Immutable
data class SettingsDialogState(
    val showSettingsPasswordDialog: Boolean = false,
    val showBluetoothPermissionDialog: Boolean = false,
    val pendingPasswordCallback: (() -> Unit)? = null,
)

@Immutable
data class SettingsUiState(
    val buttons: SettingsButtonVisualState = SettingsButtonVisualState(),
    val dialog: SettingsDialogState = SettingsDialogState(),
    val webViewUrl: String? = null,
    val userLoggedIn: Boolean = true,
    val canSeeDriverRequirements: Boolean = false,
    val showSoundBrightButton: Boolean = false,
    val bluetoothConnected: Boolean = false,
    val conditionsValid: Boolean = false,
)

sealed interface SettingsUiEvent {
    data object ScreenStarted : SettingsUiEvent
    data object ConfigurationClicked : SettingsUiEvent
    data object DeviceSettingsClicked : SettingsUiEvent
    data object GPSClicked : SettingsUiEvent
    data object AboutClicked : SettingsUiEvent
    data object LightClicked : SettingsUiEvent
    data object RequirementsClicked : SettingsUiEvent
    data object PreferenciasClicked : SettingsUiEvent
    data object BluetoothClicked : SettingsUiEvent

    data class BluetoothLocalChanged(val hasDevice: Boolean) : SettingsUiEvent
    data class StatusChanged(val status: Int?) : SettingsUiEvent
    data class ConditionsChanged(val isValid: Boolean?) : SettingsUiEvent
    data class UserLoggedInChanged(val loggedIn: Boolean) : SettingsUiEvent
    data class WebViewUrlChanged(val url: String?) : SettingsUiEvent
    data class CanSeeDriverRequirementsChanged(val canSee: Boolean) : SettingsUiEvent
    data class ShowSoundBrightButtonChanged(val show: Boolean) : SettingsUiEvent
    data class ShiftStatusChanged(val currentStatus: Int?) : SettingsUiEvent
    data class SettingsPasswordSubmitted(val pin: String) : SettingsUiEvent
}

sealed interface SettingsUiEffect {
    data object NavigateToUserLogin : SettingsUiEffect
    data object NavigateToDiscoveryChannel : SettingsUiEffect
    data object NavigateToAbout : SettingsUiEffect
    data object NavigateToGpsConfig : SettingsUiEffect
    data object NavigateToLightsTest : SettingsUiEffect
    data object NavigateToTools : SettingsUiEffect
    data object NavigateToUserConfiguration : SettingsUiEffect
    data object NavigateToDeviceSettings : SettingsUiEffect
    data class NavigateToWebView(val url: String) : SettingsUiEffect

    data object RequestBluetoothPermission : SettingsUiEffect
    data object OpenSystemWriteSettings : SettingsUiEffect
    data object ShowSettingsPasswordDialog : SettingsUiEffect
    data object ShowIncorrectPinToast : SettingsUiEffect
}

object SettingsButtonsReducer {
    fun reduce(
        current: SettingsUiState,
        event: SettingsUiEvent
    ): Pair<SettingsUiState, SettingsUiEffect?> {
        return when (event) {
            is SettingsUiEvent.ShiftStatusChanged -> {
                val configurationStyle =
                    if (event.currentStatus != ifConstants.STATE_DISCONNECTED && event.currentStatus != null) {
                        CustomButton.StyleButton.DISABLE
                    } else {
                        CustomButton.StyleButton.ENABLE
                    }
                current.copy(buttons = current.buttons.copy(configurationStyle = configurationStyle)) to null
            }

            is SettingsUiEvent.BluetoothLocalChanged -> {
                if (event.hasDevice) current to null
                else current.copy(buttons = current.buttons.copy(lightStyle = CustomButton.StyleButton.DISABLE)) to null
            }

            is SettingsUiEvent.StatusChanged -> current to null

            is SettingsUiEvent.ConditionsChanged -> {
                val newStyle =
                    if (event.isValid == true) CustomButton.StyleButton.ENABLE
                    else CustomButton.StyleButton.DISABLE
                current.copy(buttons = current.buttons.copy(lightStyle = newStyle)) to null
            }

            is SettingsUiEvent.UserLoggedInChanged -> current.copy(userLoggedIn = event.loggedIn) to null

            is SettingsUiEvent.WebViewUrlChanged -> {
                current.copy(
                    webViewUrl = event.url,
                    buttons = current.buttons.copy(webViewVisible = !event.url.isNullOrEmpty())
                ) to null
            }

            is SettingsUiEvent.CanSeeDriverRequirementsChanged -> {
                current.copy(
                    canSeeDriverRequirements = event.canSee,
                    buttons = current.buttons.copy(requirementsVisible = event.canSee)
                ) to null
            }

            is SettingsUiEvent.ShowSoundBrightButtonChanged -> {
                current.copy(
                    showSoundBrightButton = event.show,
                    buttons = current.buttons.copy(deviceSettingsVisible = event.show)
                ) to null
            }

            is SettingsUiEvent.ScreenStarted,
            is SettingsUiEvent.ConfigurationClicked,
            is SettingsUiEvent.DeviceSettingsClicked,
            is SettingsUiEvent.GPSClicked,
            is SettingsUiEvent.AboutClicked,
            is SettingsUiEvent.LightClicked,
            is SettingsUiEvent.RequirementsClicked,
            is SettingsUiEvent.PreferenciasClicked,
            is SettingsUiEvent.BluetoothClicked,
            is SettingsUiEvent.SettingsPasswordSubmitted -> current to null
        }
    }
}

class SettingsViewModel(
    val bluetoothLocalUseCase: BluetoothLocalUseCase,
    val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    val sessionUseCase: SessionUseCase,
    val configurationScreenPasswordUseCase: ConfigurationScreenPasswordUseCase,
    application: Application,
) : AndroidViewModel(application) {

    val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    val _effects = MutableSharedFlow<SettingsUiEffect>()
    val effects = _effects.asSharedFlow()

    fun onEvent(event: SettingsUiEvent) {
        val (newState, effect) = SettingsButtonsReducer.reduce(_uiState.value, event)
        _uiState.value = newState

        when (event) {
            SettingsUiEvent.ScreenStarted -> {
                checkDeviceSettingsButton()
                getSavedDevice()
                checkUserLogIn()
                checkWebViewButton()
                checkHasSettingsPassword()
                checkRequirementsButton()
            }

            is SettingsUiEvent.BluetoothLocalChanged -> if (event.hasDevice) getStatus()
            is SettingsUiEvent.StatusChanged -> event.status?.let { checkConditionsFromState(it) }
            SettingsUiEvent.ConfigurationClicked -> clickConfiguration()
            SettingsUiEvent.DeviceSettingsClicked -> clickDeviceSettings()
            SettingsUiEvent.GPSClicked -> clickGPS()
            SettingsUiEvent.AboutClicked -> clickAbout()
            SettingsUiEvent.LightClicked -> clickLights()
            SettingsUiEvent.RequirementsClicked -> clickRequirements()
            SettingsUiEvent.PreferenciasClicked -> emitEffect(SettingsUiEffect.NavigateToUserConfiguration)
            SettingsUiEvent.BluetoothClicked -> clickBluetooth()
            is SettingsUiEvent.SettingsPasswordSubmitted -> Unit
            else -> Unit
        }

        effect?.let { emitEffect(it) }
    }

    private fun emitEffect(effect: SettingsUiEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    fun getSavedDevice() {
        viewModelScope.launch {
            val bluetooth = bluetoothLocalUseCase.getLocalBluetooth()
            _uiState.update { it.copy(bluetoothConnected = bluetooth != null) }
            onEvent(SettingsUiEvent.BluetoothLocalChanged(bluetooth != null))
        }
    }

    fun getStatus() {
        viewModelScope.launch {
            val status = Taximeter.getInstance().bluetoothState
            onEvent(SettingsUiEvent.StatusChanged(status))
        }
    }

    private fun checkConditionsFromState(status: Int) {
        viewModelScope.launch {
            val device = bluetoothLocalUseCase.getLocalBluetooth()
            val valid =
                device != null &&
                    (device.name.startsWith("SKYG") || device.name.startsWith("SHER")) &&
                    status == StatusTaximeter.BLUETOOTH_CONNECTED_AND_TAXIMETER_CONNECTED

            _uiState.update { it.copy(conditionsValid = valid) }
            onEvent(SettingsUiEvent.ConditionsChanged(valid))
        }
    }

    fun checkHasSettingsPassword() {
        viewModelScope.launch {
            val hasPassword = configurationScreenPasswordUseCase.hasSettingsPassword()
            if (hasPassword == true) {
                emitEffect(SettingsUiEffect.ShowSettingsPasswordDialog)
            }
        }
    }

    fun checkSettingsPassword(pin: String, callback: () -> Unit) {
        viewModelScope.launch {
            val userPassword = configurationScreenPasswordUseCase.encryptSettingsPassword(pin)
            val savedPassword = configurationScreenPasswordUseCase.getSettingsPassword()
            if (userPassword == savedPassword) callback.invoke()
            else emitEffect(SettingsUiEffect.ShowIncorrectPinToast)
        }
    }

    fun checkUserLogIn() {
        viewModelScope.launch {
            val loggedIn = sessionUseCase.isUserLoggedIn()
            _uiState.update { it.copy(userLoggedIn = loggedIn) }
            onEvent(SettingsUiEvent.UserLoggedInChanged(loggedIn))
        }
    }

    fun checkWebViewButton() {
        viewModelScope.launch {
            val url = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.webViewURL
            _uiState.update { it.copy(webViewUrl = url) }
            onEvent(SettingsUiEvent.WebViewUrlChanged(url))
        }
    }

    fun checkDeviceSettingsButton() {
        viewModelScope.launch {
            val show = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.isSmartTDAccessSoundBright == true
            _uiState.update { it.copy(showSoundBrightButton = show) }
            onEvent(SettingsUiEvent.ShowSoundBrightButtonChanged(show))
        }
    }

    fun checkRequirementsButton() {
        viewModelScope.launch {
            val hasRequirements = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.isDriverCanQueryRequirements ?: false
            Logs.d("SettingsViewModel", "checkRequirementsButton hasRequirements: $hasRequirements")
            _uiState.update { it.copy(canSeeDriverRequirements = hasRequirements) }
            onEvent(SettingsUiEvent.CanSeeDriverRequirementsChanged(hasRequirements))
        }
    }

    fun clickConfiguration() = emitEffect(SettingsUiEffect.NavigateToUserLogin)
    fun clickBluetooth() = emitEffect(SettingsUiEffect.NavigateToDiscoveryChannel)
    fun clickAbout() = emitEffect(SettingsUiEffect.NavigateToAbout)
    fun clickGPS() = emitEffect(SettingsUiEffect.NavigateToGpsConfig)
    fun clickLights() = emitEffect(SettingsUiEffect.NavigateToLightsTest)
    fun clickRequirements() = emitEffect(SettingsUiEffect.NavigateToTools)

    fun clickDeviceSettings() {
        if (hasAudioPermissions()) emitEffect(SettingsUiEffect.NavigateToDeviceSettings)
        else emitEffect(SettingsUiEffect.OpenSystemWriteSettings)
    }

    fun hasAudioPermissions(): Boolean = Settings.System.canWrite(getApplication())

    fun openSystemWriteSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:${getApplication<Application>().packageName}")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        getApplication<Application>().startActivity(intent)
    }
}

data class SettingsShiftStatus(val currentStatus: Int? = null)

class SettingsMainActivityViewModel : ViewModel() {
    val _shiftStatusFlow = MutableStateFlow(SettingsShiftStatus())
    val shiftStatusFlow = _shiftStatusFlow.asStateFlow()

    fun updateShiftStatus(currentStatus: Int?) {
        _shiftStatusFlow.value = SettingsShiftStatus(currentStatus)
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    sharedViewModel: SettingsMainActivityViewModel,
    onNavigateToUserLogin: () -> Unit,
    onNavigateToDiscoveryChannel: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToGpsConfig: () -> Unit,
    onNavigateToLightsTest: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToUserConfiguration: () -> Unit,
    onNavigateToWebView: (String) -> Unit,
    onNavigateToDeviceSettings: () -> Unit,
    onRequestBluetoothPermission: () -> Unit,
    onShowToast: (Int) -> Unit,
    onShowHeader: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shiftStatus by sharedViewModel.shiftStatusFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onShowHeader(true)
        viewModel.onEvent(SettingsUiEvent.ScreenStarted)
    }

    LaunchedEffect(shiftStatus.currentStatus) {
        viewModel.onEvent(SettingsUiEvent.ShiftStatusChanged(shiftStatus.currentStatus))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SettingsUiEffect.NavigateToUserLogin -> onNavigateToUserLogin()
                SettingsUiEffect.NavigateToDiscoveryChannel -> onNavigateToDiscoveryChannel()
                SettingsUiEffect.NavigateToAbout -> onNavigateToAbout()
                SettingsUiEffect.NavigateToGpsConfig -> onNavigateToGpsConfig()
                SettingsUiEffect.NavigateToLightsTest -> onNavigateToLightsTest()
                SettingsUiEffect.NavigateToTools -> onNavigateToTools()
                SettingsUiEffect.NavigateToUserConfiguration -> onNavigateToUserConfiguration()
                SettingsUiEffect.NavigateToDeviceSettings -> onNavigateToDeviceSettings()
                is SettingsUiEffect.NavigateToWebView -> onNavigateToWebView(effect.url)
                SettingsUiEffect.RequestBluetoothPermission -> onRequestBluetoothPermission()
                SettingsUiEffect.OpenSystemWriteSettings -> viewModel.openSystemWriteSettings()
                SettingsUiEffect.ShowIncorrectPinToast -> onShowToast(R.string.pin_incorrecto)
                SettingsUiEffect.ShowSettingsPasswordDialog -> Unit
            }
        }
    }

    SettingsGrid(
        uiState = uiState,
        onConfigurationClick = { viewModel.clickConfiguration() },
        onDeviceSettingsClick = { viewModel.clickDeviceSettings() },
        onBluetoothClick = { viewModel.clickBluetooth() },
        onPreferenciasClick = { onNavigateToUserConfiguration() },
        onGpsClick = { viewModel.clickGPS() },
        onAboutClick = { viewModel.clickAbout() },
        onLightClick = { viewModel.clickLights() },
        onRequirementsClick = { viewModel.clickRequirements() },
        onWebClick = { uiState.webViewUrl?.let(onNavigateToWebView) }
    )
}

@Composable
private fun SettingsGrid(
    uiState: SettingsUiState,
    onConfigurationClick: () -> Unit,
    onDeviceSettingsClick: () -> Unit,
    onBluetoothClick: () -> Unit,
    onPreferenciasClick: () -> Unit,
    onGpsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLightClick: () -> Unit,
    onRequirementsClick: () -> Unit,
    onWebClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(vertical = 1.dp)
    ) {
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SettingsButtonCell(Modifier.weight(1f), true, uiState.buttons.configurationStyle, ButtonType.CONFIGURACION, onConfigurationClick)
            SettingsButtonCell(Modifier.weight(1f), uiState.buttons.deviceSettingsVisible, CustomButton.StyleButton.ENABLE, ButtonType.DEVICE_SETTINGS, onDeviceSettingsClick)
        }
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SettingsButtonCell(Modifier.weight(1f), true, CustomButton.StyleButton.ENABLE, ButtonType.PREFERENCIAS, onPreferenciasClick)
            SettingsButtonCell(Modifier.weight(1f), true, CustomButton.StyleButton.ENABLE, ButtonType.GPS, onGpsClick)
        }
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SettingsButtonCell(Modifier.weight(1f), true, CustomButton.StyleButton.ENABLE, ButtonType.ACERCADE, onAboutClick)
            SettingsButtonCell(Modifier.weight(1f), true, uiState.buttons.lightStyle, ButtonType.LUMINOSO, onLightClick)
        }
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SettingsButtonCell(Modifier.weight(1f), uiState.buttons.webViewVisible, CustomButton.StyleButton.ENABLE, ButtonType.WEB, onWebClick)
            SettingsButtonCell(Modifier.weight(1f), uiState.buttons.requirementsVisible, CustomButton.StyleButton.ENABLE, ButtonType.TOOLS, onRequirementsClick)
        }
    }
}

@Composable
private fun SettingsButtonCell(
    modifier: Modifier,
    visible: Boolean,
    style: CustomButton.StyleButton,
    buttonType: ButtonType,
    onClick: () -> Unit,
) {
    if (!visible) return

    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier,
        factory = { context ->
            CustomButton(context).apply {
                setButtonType(buttonType.value)
                setButtonStyle(style)
                setAction(onClick)
            }
        },
        update = { view ->
            view.setButtonType(buttonType.value)
            view.setButtonStyle(style)
            view.setAction(onClick)
        }
    )
}

@Composable
private fun SettingsDialogs(
    uiState: SettingsUiState,
    onPasswordSubmit: (String) -> Unit,
    onBluetoothPermissionAccept: () -> Unit,
) {
    // Hook for CustomDialog-based handling if you want to mirror the fragment dialogs exactly.
    // Kept intentionally empty in this single-file compile-safe version.
}
