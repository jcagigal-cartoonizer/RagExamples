package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PermissionsUiState
import ifac.td.taxi.ui.screen.components.PermissionsButtonsState = when 
import ifac.td.taxi.ui.screen.components.PermissionsButtonsState = PermissionsButtonsState
import ifac.td.taxi.ui.screen.components.PermissionsButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 507-3: import android.content.Context
@Immutable
data class PermissionsUiState(
    val buttons: PermissionsButtonsState = PermissionsButtonsState(),
    val showPhoneCallsSection: Boolean = false,
    val showRedSysSection: Boolean = false,
    val redSysUsername: String? = null,
    val dialogState: PermissionsDialogState = PermissionsDialogState.hidden(),
)
sealed interface PermissionsUiEvent {
    data object OnScreenStarted : PermissionsUiEvent
    data class HighlightPermission(val permissionString: String) : PermissionsUiEvent
    data object ClickBluetooth : PermissionsUiEvent
    data object ClickCamera : PermissionsUiEvent
    data object ClickPhoneCalls : PermissionsUiEvent
    data object ClickNotifications : PermissionsUiEvent
    data object ClickLocation : PermissionsUiEvent
    data object ClickBackgroundLocation : PermissionsUiEvent
    data object ClickBattery : PermissionsUiEvent
    data object ClickMicrophone : PermissionsUiEvent
    data object ClickOverlay : PermissionsUiEvent
    data object ClickSystemSettings : PermissionsUiEvent
    data object ClickRedSysPassword : PermissionsUiEvent
    data object DialogDismiss : PermissionsUiEvent
    data class DialogUsernameChanged(val value: String) : PermissionsUiEvent
    data class DialogPasswordChanged(val value: String) : PermissionsUiEvent
    data object DialogAccept : PermissionsUiEvent
    data object DialogChangePassword : PermissionsUiEvent
}
sealed interface PermissionsUiEffect {
    data class RequestPermission(val permission: String) : PermissionsUiEffect
    data object RequestManageWriteSettings : PermissionsUiEffect
    data object RequestIgnoreBatteryOptimization : PermissionsUiEffect
    data object LaunchOverlayPermission : PermissionsUiEffect
    data class ShowToast(val messageRes: Int) : PermissionsUiEffect
    data object ShowToastNotifDefault : PermissionsUiEffect
    data object ShowToastOldBackgroundLocation : PermissionsUiEffect
    data class Navigate(val resId: Int) : PermissionsUiEffect
}
@Immutable
data class PermissionsDialogState(
    val show: Boolean,
    val title: String = "",
    val description: String = "",
    val usernameHint: String = "",
    val passwordHint: String = "",
    val username: String? = null,
    val password: String? = null,
) {
    companion object {
        fun hidden() = PermissionsDialogState(show = false)
        fun visible(username: String?, password: String?) = PermissionsDialogState(
            show = true,
            title = "Guardar contraseña",
            description = "Guarda tus credenciales de RedSys",
            usernameHint = "Usuario",
            passwordHint = "Contraseña",
            username = username,
            password = password
        )
    }
}
@Immutable
data class PermissionsButtonsState(
    val bluetooth: PermissionButtonState = PermissionButtonState(),
    val camera: PermissionButtonState = PermissionButtonState(),
    val phoneCalls: PermissionButtonState = PermissionButtonState(visible = true),
    val notifications: PermissionButtonState = PermissionButtonState(),
    val location: PermissionButtonState = PermissionButtonState(),
    val backgroundLocation: PermissionButtonState = PermissionButtonState(),
    val battery: PermissionButtonState = PermissionButtonState(),
    val microphone: PermissionButtonState = PermissionButtonState(),
    val overlay: PermissionButtonState = PermissionButtonState(),
    val settings: PermissionButtonState = PermissionButtonState(),
    val redSysPassword: PermissionButtonState = PermissionButtonState(visible = true),
) {
    companion object {
        fun fromSystem(context: Context, showPhoneCalls: Boolean, showRedSys: Boolean): PermissionsButtonsState {
            fun perm(permission: String) = PermissionButtonState(
                checked = PermissionRequest.checkHavePermission(permission, context),
                enabled = !PermissionRequest.checkHavePermission(permission, context),
                visible = true
            )
            val batteryGranted = run {
                val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                pm.isIgnoringBatteryOptimizations(context.packageName)
            }
            val overlayGranted = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
            return PermissionsButtonsState(
                bluetooth = perm(PERMISSION_BLUETOOTH),
                camera = perm(PERMISSION_CAMERA),
                phoneCalls = PermissionButtonState(
                    checked = PermissionRequest.checkHavePermission(PERMISSION_PHONE_CALL, context),
                    enabled = !PermissionRequest.checkHavePermission(PERMISSION_PHONE_CALL, context),
                    visible = showPhoneCalls
                ),
                notifications = PermissionButtonState(
                    checked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        PermissionRequest.checkHavePermission(android.Manifest.permission.POST_NOTIFICATIONS, context)
                    else true,
                    enabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        !PermissionRequest.checkHavePermission(android.Manifest.permission.POST_NOTIFICATIONS, context)
                    else false,
                    visible = true
                ),
                location = perm(PERMISSION_LOCATION),
                backgroundLocation = PermissionButtonState(
                    checked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        PermissionRequest.checkHavePermission(PERMISSION_BACKGROUND_LOCATION, context)
                    else false,
                    enabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        !PermissionRequest.checkHavePermission(PERMISSION_BACKGROUND_LOCATION, context)
                    else false,
                    visible = true
                ),
                battery = PermissionButtonState(
                    checked = batteryGranted,
                    enabled = !batteryGranted,
                    visible = true
                ),
                microphone = perm(PERMISSION_MICROPHONE),
                overlay = PermissionButtonState(
                    checked = overlayGranted,
                    enabled = !overlayGranted,
                    visible = true
                ),
                settings = PermissionButtonState(
                    checked = Settings.System.canWrite(context),
                    enabled = !Settings.System.canWrite(context),
                    visible = true
                ),
                redSysPassword = PermissionButtonState(
                    checked = showRedSys,
                    enabled = true,
                    visible = showRedSys
                )
            )
        }
    }
    fun updatePermission(permission: String, granted: Boolean): PermissionsButtonsState {
        return when (permission) {
            android.Manifest.permission.BLUETOOTH -> copy(bluetooth = bluetooth.copy(checked = granted, enabled = !granted))
            android.Manifest.permission.CAMERA -> copy(camera = camera.copy(checked = granted, enabled = !granted))
            android.Manifest.permission.CALL_PHONE -> copy(phoneCalls = phoneCalls.copy(checked = granted, enabled = !granted))
            android.Manifest.permission.POST_NOTIFICATIONS -> copy(notifications = notifications.copy(checked = granted, enabled = !granted))
            android.Manifest.permission.ACCESS_FINE_LOCATION -> copy(location = location.copy(checked = granted, enabled = !granted))
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> copy(backgroundLocation = backgroundLocation.copy(checked = granted, enabled = !granted))
            android.Manifest.permission.RECORD_AUDIO -> copy(microphone = microphone.copy(checked = granted, enabled = !granted))
            android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> copy(battery = battery.copy(checked = granted, enabled = !granted))
            else -> this
        }
    }
    fun disable(which: PermissionButtonRef): PermissionsButtonsState = when (which) {
        NotificationButton -> copy(notifications = notifications.copy(enabled = false))
    }
    fun highlight(permissionString: String): PermissionsButtonsState = when (permissionString) {
        android.Manifest.permission.BLUETOOTH -> copy(bluetooth = bluetooth.copy(highlighted = true))
        android.Manifest.permission.CAMERA -> copy(camera = camera.copy(highlighted = true))
        android.Manifest.permission.CALL_PHONE -> copy(phoneCalls = phoneCalls.copy(highlighted = true))
        android.Manifest.permission.POST_NOTIFICATIONS -> copy(notifications = notifications.copy(highlighted = true))
        android.Manifest.permission.ACCESS_FINE_LOCATION -> copy(location = location.copy(highlighted = true))
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> copy(backgroundLocation = backgroundLocation.copy(highlighted = true))
        android.Manifest.permission.RECORD_AUDIO -> copy(microphone = microphone.copy(highlighted = true))
        android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> copy(battery = battery.copy(highlighted = true))
        else -> this
    }
}
enum class PermissionButtonRef { NotificationButton }
@Immutable
data class PermissionButtonState(
    val checked: Boolean = false,
    val enabled: Boolean = true,
    val visible: Boolean = true,
    val highlighted: Boolean = false,
)
