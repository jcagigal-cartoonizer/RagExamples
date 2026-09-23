package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 284-3: import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Immutable
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
