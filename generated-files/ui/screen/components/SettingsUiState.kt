package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SettingsButtonsState = SettingsButtonsState
import ifac.td.taxi.ui.screen.components.SettingsUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 375-3: import ifac.td.taxi.framework.sdk.model.BluetoothInfo
data class SettingsUiState(
    val bluetoothInfo: BluetoothInfo? = null,
    val taximeterStatus: Int? = null,
    val conditionsValid: Boolean = false,
    val userLoggedIn: Boolean = true,
    val webViewUrl: String? = null,
    val settingsPasswordEnabled: Boolean = true,
    val canSeeDriverRequirements: Boolean = false,
    val showSoundBrightButton: Boolean = false,
    val buttons: SettingsButtonsState = SettingsButtonsState(),
    val dialog: SettingsDialogState? = null,
    val pendingPasswordCallback: (() -> Unit)? = null,
)
sealed interface SettingsUiEvent {
    data object ConfigurationClicked : SettingsUiEvent
    data object DeviceSettingsClicked : SettingsUiEvent
    data object GpsClicked : SettingsUiEvent
    data object AboutClicked : SettingsUiEvent
    data object LightClicked : SettingsUiEvent
    data object RequirementsClicked : SettingsUiEvent
    data object PreferenciasClicked : SettingsUiEvent
    data object BluetoothClicked : SettingsUiEvent
    data object WebViewClicked : SettingsUiEvent
    data object DialogDismissed : SettingsUiEvent
    data object DialogPasswordConfirmed : SettingsUiEvent
    data class DialogPasswordChanged(val value: String) : SettingsUiEvent
}
sealed interface SettingsUiEffect {
    data object NavigateToUserLogin : SettingsUiEffect
    data object NavigateToDeviceSettings : SettingsUiEffect
    data object NavigateToDiscoveryChannel : SettingsUiEffect
    data object NavigateToAbout : SettingsUiEffect
    data object NavigateToGPS : SettingsUiEffect
    data object NavigateToLights : SettingsUiEffect
    data object NavigateToRequirements : SettingsUiEffect
    data object NavigateToUserConfiguration : SettingsUiEffect
    data class NavigateToWebView(val url: String) : SettingsUiEffect
    data object RequestWriteSettingsPermission : SettingsUiEffect
    data object ShowToastIncorrectPin : SettingsUiEffect
}
sealed class SettingsDialogState {
    data class Password(
        val title: String,
        val description: String,
        val value: String,
    ) : SettingsDialogState()
}
