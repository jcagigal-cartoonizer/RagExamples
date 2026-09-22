package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // import androidx.compose.runtime.Immutable
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
// // # Block 284-3: import androidx.compose.runtime.Immutable
// // import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
@Immutable
data class DeviceSettingsButtonsState(
    val showWriteSettingsButton: Boolean = false,
    val showLinkedVolumesInfoButton: Boolean = true,
    val showPhoneCallButton: Boolean = true,
    val showSystemButton: Boolean = true,
    val showNotificationButton: Boolean = true,
    val showRingtoneButton: Boolean = true,
    val showBrightnessButton: Boolean = true,
    val primaryEnabled: Boolean = true,
    val secondaryEnabled: Boolean = true,
    val destructiveEnabled: Boolean = true,
    val primaryContainerColor: Color = Color(0xFF1E88E5),
    val primaryContentColor: Color = Color.White,
    val secondaryContainerColor: Color = Color(0xFF263238),
    val secondaryContentColor: Color = Color.White,
    val destructiveContainerColor: Color = Color(0xFFD32F2F),
    val destructiveContentColor: Color = Color.White,
    val disabledContainerColor: Color = Color(0xFFB0BEC5),
    val disabledContentColor: Color = Color(0xFF607D8B),
) {
    companion object {
        fun from(
            canWriteSettings: Boolean,
            isDndEnabled: Boolean,
            isSilentModeEnabled: Boolean,
            linkedVolumes: Boolean
        ): DeviceSettingsButtonsState {
            val blocked = isDndEnabled || isSilentModeEnabled
            return DeviceSettingsButtonsState(
                showWriteSettingsButton = !canWriteSettings,
                primaryEnabled = !blocked,
                secondaryEnabled = !blocked,
                destructiveEnabled = !blocked,
                showLinkedVolumesInfoButton = linkedVolumes
            )
        }
    }
}
@Immutable
data class ButtonStyleTokens(
    val containerColor: Color,
    val contentColor: Color,
    val enabled: Boolean,
)
fun DeviceSettingsButtonsState.primaryStyle() = ButtonStyleTokens(
    containerColor = if (primaryEnabled) primaryContainerColor else disabledContainerColor,
    contentColor = if (primaryEnabled) primaryContentColor else disabledContentColor,
    enabled = primaryEnabled
)
fun DeviceSettingsButtonsState.secondaryStyle() = ButtonStyleTokens(
    containerColor = if (secondaryEnabled) secondaryContainerColor else disabledContainerColor,
    contentColor = if (secondaryEnabled) secondaryContentColor else disabledContentColor,
    enabled = secondaryEnabled
)
fun DeviceSettingsButtonsState.destructiveStyle() = ButtonStyleTokens(
    containerColor = if (destructiveEnabled) destructiveContainerColor else disabledContainerColor,
    contentColor = if (destructiveEnabled) destructiveContentColor else disabledContentColor,
    enabled = destructiveEnabled
)
