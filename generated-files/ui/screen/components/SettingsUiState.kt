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
// # Block 374-3: import ifac.td.taxi.framework.sdk.model.BluetoothInfo
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
