package ifac.td.taxi.ui.screen.components
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
// # Block 429-4: import androidx.compose.runtime.Immutable
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
