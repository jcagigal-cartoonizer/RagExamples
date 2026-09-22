package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import ifac.td.taxi.ui.screen.state.*
import kotlinx.coroutines.launch
@Composable
fun DeviceSettingsScreen(
    navController: NavController,
    viewModel: DeviceSettingsComposeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var dialogState by remember { mutableStateOf<DeviceSettingsDialogState?>(null) }
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        DeviceSettingsUiEffect.NavigateBack -> navController.popBackStack()
                        DeviceSettingsUiEffect.OpenWriteSettings -> {
                            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                        is DeviceSettingsUiEffect.ShowToast -> {
                        }
                        is DeviceSettingsUiEffect.OpenDialog -> dialogState = effect.dialog
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.onEvent(DeviceSettingsUiEvent.OnScreenResumed)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            DeviceSettingsSliderCard(
                title = "Phone call volume",
                value = uiState.phoneCallVolume,
                max = 100,
                enabled = uiState.buttonsState.primaryEnabled,
                onValueChange = { viewModel.onEvent(DeviceSettingsUiEvent.OnPhoneCallVolumeChanged(it)) },
                onValueChangeFinished = { viewModel.onEvent(DeviceSettingsUiEvent.OnPhoneCallStopTracking) }
            )
            DeviceSettingsSliderCard(
                title = "System volume",
                value = uiState.systemVolume,
                max = 100,
                enabled = uiState.buttonsState.secondaryEnabled,
                onValueChange = { viewModel.onEvent(DeviceSettingsUiEvent.OnSystemVolumeChanged(it)) },
                onValueChangeFinished = { viewModel.onEvent(DeviceSettingsUiEvent.OnSystemStopTracking) }
            )
            DeviceSettingsSliderCard(
                title = "Notification volume",
                value = uiState.notificationVolume,
                max = 100,
                enabled = uiState.buttonsState.secondaryEnabled,
                onValueChange = { viewModel.onEvent(DeviceSettingsUiEvent.OnNotificationVolumeChanged(it)) },
                onValueChangeFinished = { viewModel.onEvent(DeviceSettingsUiEvent.OnNotificationStopTracking) }
            )
            DeviceSettingsSliderCard(
                title = "Ringtone volume",
                value = uiState.ringtoneVolume,
                max = 100,
                enabled = uiState.buttonsState.destructiveEnabled,
                onValueChange = { viewModel.onEvent(DeviceSettingsUiEvent.OnRingtoneVolumeChanged(it)) },
                onValueChangeFinished = { viewModel.onEvent(DeviceSettingsUiEvent.OnRingtoneStopTracking) }
            )
            DeviceSettingsSliderCard(
                title = "Brightness",
                value = uiState.brightness,
                max = 255,
                enabled = true,
                onValueChange = { viewModel.onEvent(DeviceSettingsUiEvent.OnBrightnessChanged(it)) },
                onValueChangeFinished = { viewModel.onEvent(DeviceSettingsUiEvent.OnBrightnessStopTracking) }
            )
            if (uiState.buttonsState.showWriteSettingsButton) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.onEvent(DeviceSettingsUiEvent.OnRequestWriteSettingsPermission) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = uiState.buttonsState.primaryContainerColor,
                        contentColor = uiState.buttonsState.primaryContentColor,
                        disabledContainerColor = uiState.buttonsState.disabledContainerColor,
                        disabledContentColor = uiState.buttonsState.disabledContentColor
                    )
                ) {
                    Text("Grant system write permission")
                }
            }
        }
        dialogState?.let { dialog ->
            DeviceSettingsCustomDialog(
                state = dialog,
                onConfirm = {
                    dialogState = null
                    viewModel.onEvent(DeviceSettingsUiEvent.OnDismissDialog)
                },
                onDismiss = {
                    dialogState = null
                    viewModel.onEvent(DeviceSettingsUiEvent.OnDismissDialog)
                }
            )
        }
    }
}
