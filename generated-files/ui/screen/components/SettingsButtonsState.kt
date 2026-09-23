package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SettingsButtonState = SettingsButtonState
import ifac.td.taxi.ui.screen.components.SettingsButtonState
import ifac.td.taxi.ui.screen.components.Settings: SettingsButtonState = SettingsButtonState
import ifac.td.taxi.ui.screen.components.SettingsButtonsState
import ifac.td.taxi.ui.screen.components.Settings = SettingsButtonState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 430-4: import androidx.compose.runtime.Immutable
@Immutable
data class SettingsButtonsState(
    val configuration: SettingsButtonState = SettingsButtonState(),
    val deviceSettings: SettingsButtonState = SettingsButtonState(),
    val gps: SettingsButtonState = SettingsButtonState(),
    val about: SettingsButtonState = SettingsButtonState(),
    val light: SettingsButtonState = SettingsButtonState(),
    val requirements: SettingsButtonState = SettingsButtonState(),
    val preferencias: SettingsButtonState = SettingsButtonState(),
    val bluetooth: SettingsButtonState = SettingsButtonState(),
    val webView: SettingsButtonState = SettingsButtonState(),
) {
    companion object {
        fun from(
            bluetoothInfo: BluetoothInfo?,
            userLoggedIn: Boolean,
            showSoundBrightButton: Boolean,
            canSeeDriverRequirements: Boolean,
            webViewUrl: String?,
            shiftDisconnected: Boolean,
        ): SettingsButtonsState {
            val configurationEnabled = shiftDisconnected
            val deviceSettingsVisible = showSoundBrightButton
            val configurationVisible = !showSoundBrightButton
            val commonVisible = true
            val webVisible = !webViewUrl.isNullOrEmpty()
            val loggedOffStyle = SettingsButtonStyle.Enabled
            val loggedInStyle = SettingsButtonStyle.Enabled
            return SettingsButtonsState(
                configuration = SettingsButtonState(
                    visible = configurationVisible,
                    style = if (configurationEnabled) SettingsButtonStyle.Enabled else SettingsButtonStyle.Disabled
                ),
                deviceSettings = SettingsButtonState(
                    visible = deviceSettingsVisible,
                    style = SettingsButtonStyle.Enabled
                ),
                gps = SettingsButtonState(
                    visible = commonVisible,
                    style = SettingsButtonStyle.Enabled
                ),
                about = SettingsButtonState(
                    visible = commonVisible,
                    style = SettingsButtonStyle.Enabled
                ),
                light = SettingsButtonState(
                    visible = commonVisible,
                    style = if (bluetoothInfo != null && isValidBluetoothForLight(bluetoothInfo)) {
                        SettingsButtonStyle.Enabled
                    } else {
                        SettingsButtonStyle.Disabled
                    }
                ),
                requirements = SettingsButtonState(
                    visible = canSeeDriverRequirements,
                    style = SettingsButtonStyle.Enabled
                ),
                preferencias = SettingsButtonState(
                    visible = commonVisible,
                    style = if (userLoggedIn) loggedInStyle else loggedOffStyle
                ),
                bluetooth = SettingsButtonState(
                    visible = commonVisible,
                    style = SettingsButtonStyle.Enabled
                ),
                webView = SettingsButtonState(
                    visible = webVisible,
                    style = SettingsButtonStyle.Enabled
                ),
            )
        }
        fun isValidBluetoothForLight(device: BluetoothInfo): Boolean {
            return device.name.startsWith("SKYG") || device.name.startsWith("SHER")
        }
    }
}
@Immutable
data class SettingsButtonState(
    val visible: Boolean = true,
    val style: SettingsButtonStyle = SettingsButtonStyle.Enabled,
)
enum class SettingsButtonStyle {
    Enabled,
    Disabled
}
