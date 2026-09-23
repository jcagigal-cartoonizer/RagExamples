package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 228-2: import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Immutable
@Immutable
data class DeviceSettingsUiState(
    val canWriteSettings: Boolean = false,
    val isDndEnabled: Boolean = false,
    val isSilentModeEnabled: Boolean = false,
    val linkedVolumes: Boolean = false,
    val phoneCallVolume: Int = 0,
    val systemVolume: Int = 0,
    val notificationVolume: Int = 0,
    val ringtoneVolume: Int = 0,
    val brightness: Int = 0,
    val dialog: DeviceSettingsDialogState? = null,
    val buttonsState: DeviceSettingsButtonsState = DeviceSettingsButtonsState()
)
sealed interface DeviceSettingsUiEvent {
    data object OnScreenResumed : DeviceSettingsUiEvent
    data object OnDismissDialog : DeviceSettingsUiEvent
    data object OnBackPressed : DeviceSettingsUiEvent
    data object OnRequestWriteSettingsPermission : DeviceSettingsUiEvent
    data object OnOpenSystemWriteSettings : DeviceSettingsUiEvent
    data class OnPhoneCallVolumeChanged(val volume: Int) : DeviceSettingsUiEvent
    data class OnSystemVolumeChanged(val volume: Int) : DeviceSettingsUiEvent
    data class OnNotificationVolumeChanged(val volume: Int) : DeviceSettingsUiEvent
    data class OnRingtoneVolumeChanged(val volume: Int) : DeviceSettingsUiEvent
    data class OnBrightnessChanged(val brightness: Int) : DeviceSettingsUiEvent
    data object OnPhoneCallStopTracking : DeviceSettingsUiEvent
    data object OnSystemStopTracking : DeviceSettingsUiEvent
    data object OnNotificationStopTracking : DeviceSettingsUiEvent
    data object OnRingtoneStopTracking : DeviceSettingsUiEvent
    data object OnBrightnessStopTracking : DeviceSettingsUiEvent
}
sealed interface DeviceSettingsUiEffect {
    data object NavigateBack : DeviceSettingsUiEffect
    data object OpenWriteSettings : DeviceSettingsUiEffect
    data class ShowToast(val toast: DeviceSettingsToast) : DeviceSettingsUiEffect
    data class OpenDialog(val dialog: DeviceSettingsDialogState) : DeviceSettingsUiEffect
}
sealed interface DeviceSettingsToast {
    data object ErrorPermissionDenied : DeviceSettingsToast
    data object DndOrSilentModeEnabled : DeviceSettingsToast
    data object GenericError : DeviceSettingsToast
}
@Immutable
data class DeviceSettingsDialogState(
    val title: String,
    val message: String,
    val confirmText: String,
    val dismissText: String? = null,
)
