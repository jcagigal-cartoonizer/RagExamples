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
// # Block 9-1: import android.Manifest
@Composable
fun PermissionsRoute(
    navController: NavController,
    viewModel: PermissionsComposeViewModel,
    safePermissionType: String? = null, // equivalent to navArgs()
    onRequestPermission: (String) -> Unit,
    onLaunchOverlayPermission: () -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // Lifecycle-aware effect collector
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collectLatest { effect ->
                when (effect) {
                    is PermissionsUiEffect.RequestPermission ->
                        onRequestPermission(effect.permission)
                    PermissionsUiEffect.RequestManageWriteSettings ->
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    PermissionsUiEffect.RequestIgnoreBatteryOptimization ->
                        context.startActivity(
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        )
                    PermissionsUiEffect.LaunchOverlayPermission ->
                        onLaunchOverlayPermission()
                    PermissionsUiEffect.ShowToastNotifDefault ->
                        onShowToast(R.string.toast_notif_activadas_por_defecto)
                    PermissionsUiEffect.ShowToastOldBackgroundLocation ->
                        onShowToast(R.string.toast_android_antiguo_ubicacion_seg_plano)
                    PermissionsUiEffect.NavigateToChangePassword ->
                        navController.navigate(R.id.action_permissionsFragment_to_changePasswordRedSysFragment3)
                    is PermissionsUiEffect.Navigate ->
                        navController.navigate(effect.resId)
                    is PermissionsUiEffect.ShowToast ->
                        onShowToast(effect.messageRes)
                    is PermissionsUiEffect.OpenRedSysDialog -> Unit // handled by Compose dialog state
                }
            }
        }
    }
    PermissionsScreen(
        uiState = uiState,
        safePermissionType = safePermissionType,
        onEvent = viewModel::onEvent
    )
}
@Composable
fun PermissionsScreen(
    uiState: PermissionsUiState,
    safePermissionType: String? = null,
    onEvent: (PermissionsUiEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        onEvent(PermissionsUiEvent.OnScreenStarted)
        safePermissionType?.let { onEvent(PermissionsUiEvent.HighlightPermission(it)) }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Permissions",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Bluetooth",
                    checked = uiState.buttons.bluetooth.checked,
                    enabled = uiState.buttons.bluetooth.enabled,
                    visible = uiState.buttons.bluetooth.visible,
                    highlighted = uiState.buttons.bluetooth.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickBluetooth) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Camera",
                    checked = uiState.buttons.camera.checked,
                    enabled = uiState.buttons.camera.enabled,
                    visible = uiState.buttons.camera.visible,
                    highlighted = uiState.buttons.camera.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickCamera) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Phone calls",
                    checked = uiState.buttons.phoneCalls.checked,
                    enabled = uiState.buttons.phoneCalls.enabled,
                    visible = uiState.buttons.phoneCalls.visible,
                    highlighted = uiState.buttons.phoneCalls.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickPhoneCalls) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Notifications",
                    checked = uiState.buttons.notifications.checked,
                    enabled = uiState.buttons.notifications.enabled,
                    visible = uiState.buttons.notifications.visible,
                    highlighted = uiState.buttons.notifications.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickNotifications) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Location",
                    checked = uiState.buttons.location.checked,
                    enabled = uiState.buttons.location.enabled,
                    visible = uiState.buttons.location.visible,
                    highlighted = uiState.buttons.location.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickLocation) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Background location",
                    checked = uiState.buttons.backgroundLocation.checked,
                    enabled = uiState.buttons.backgroundLocation.enabled,
                    visible = uiState.buttons.backgroundLocation.visible,
                    highlighted = uiState.buttons.backgroundLocation.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickBackgroundLocation) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Ignore battery optimizations",
                    checked = uiState.buttons.battery.checked,
                    enabled = uiState.buttons.battery.enabled,
                    visible = uiState.buttons.battery.visible,
                    highlighted = uiState.buttons.battery.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickBattery) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Microphone",
                    checked = uiState.buttons.microphone.checked,
                    enabled = uiState.buttons.microphone.enabled,
                    visible = uiState.buttons.microphone.visible,
                    highlighted = uiState.buttons.microphone.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickMicrophone) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Overlay system",
                    checked = uiState.buttons.overlay.checked,
                    enabled = uiState.buttons.overlay.enabled,
                    visible = uiState.buttons.overlay.visible,
                    highlighted = uiState.buttons.overlay.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickOverlay) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "System settings",
                    checked = uiState.buttons.settings.checked,
                    enabled = uiState.buttons.settings.enabled,
                    visible = uiState.buttons.settings.visible,
                    highlighted = uiState.buttons.settings.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickSystemSettings) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "RedSys password",
                    checked = uiState.buttons.redSysPassword.checked,
                    enabled = uiState.buttons.redSysPassword.enabled,
                    visible = uiState.buttons.redSysPassword.visible,
                    highlighted = uiState.buttons.redSysPassword.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickRedSysPassword) }
                )
            }
        }
        if (uiState.dialogState.visible) {
            PermissionsCustomDialog(
                state = uiState.dialogState,
                onDismiss = { onEvent(PermissionsUiEvent.DialogDismiss) },
                onUsernameChanged = { onEvent(PermissionsUiEvent.DialogUsernameChanged(it)) },
                onPasswordChanged = { onEvent(PermissionsUiEvent.DialogPasswordChanged(it)) },
                onAccept = { onEvent(PermissionsUiEvent.DialogAccept) },
                onChangePassword = { onEvent(PermissionsUiEvent.DialogChangePassword) }
            )
        }
    }
}
