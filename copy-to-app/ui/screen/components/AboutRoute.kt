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
// # Block 179-3: import androidx.compose.foundation.clickable
@Composable
fun AboutRoute(
    viewModel: AboutComposeViewModel,
    onNavigateBack: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        viewModel.init()
    }
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        AboutUiEffect.NavigateBack -> onNavigateBack()
                        AboutUiEffect.ShowWarningDialog -> {
                            val dialog = uiState.dialog
                            if (dialog is AboutDialogState.Warning) {
                                onShowToast(dialog.message)
                            }
                        }
                        AboutUiEffect.TriggerSecretTracking -> {
                            com.interfacom.sdk.taximeter.bravocomm.W2CLocation.setTrackingState(
                                com.interfacom.sdk.taximeter.bravocomm.location.AlfaLocation.ESTADO_SEGUIMIENTO_ALARMA,
                                "1"
                            )
                        }
                        AboutUiEffect.OpenPrivacyPolicy -> Unit
                    }
                }
            }
        }
    }
    AboutScreen(
        uiState = uiState,
        onAcceptClick = viewModel::onAcceptClicked,
        onPrivacyClick = viewModel::onPrivacyClicked,
        onLogoClick = viewModel::onLogoClicked,
        onDismissDialog = viewModel::dismissDialog,
        modifier = modifier
    )
}
@Composable
fun AboutScreen(
    uiState: AboutUiState,
    onAcceptClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLogoClick: () -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = uiState.appInfo,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = uiState.privacyPolicyText,
            color = Color(0xFF1E88E5),
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onPrivacyClick() }
        )
        Spacer(Modifier.height(12.dp))
        if (uiState.isBluetoothInfoVisible) {
            Text(text = uiState.bluetoothInfoText)
        }
        Spacer(Modifier.height(24.dp))
        AboutButtons(
            state = uiState.buttons,
            onAcceptClick = onAcceptClick
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "LOGO",
            modifier = Modifier.clickable { onLogoClick() }
        )
    }
    when (val dialog = uiState.dialog) {
        AboutDialogState.Hidden -> Unit
        is AboutDialogState.Warning -> {
            AboutButtonStyle{AboutCustomDialog(
                title = "Warning",
                message = dialog.message,
                onConfirm = onDismissDialog,
                onDismiss = onDismissDialog
            )
        }
    }
}
