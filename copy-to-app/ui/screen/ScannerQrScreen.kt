package ifac.td.taxi.ui.screen
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
// # Block 216-4: import androidx.compose.foundation.layout.*
@Composable
fun ScannerQrScreen(
    uiState: ScannerQrUiState,
    onEvent: (ScannerQrUiEvent) -> Unit,
    uiEffects: kotlinx.coroutines.flow.SharedFlow<ScannerQrUiEffect>,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onOpenScanner: (cameraPosition: Int) -> Unit,
    onQrScanned: (String) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(uiEffects, lifecycleOwner) {
        uiEffects.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED).collect { effect ->
            when (effect) {
                is ScannerQrUiEffect.ShowCameraDialog -> {
                    // state already updated; no-op if you render from state
                }
                is ScannerQrUiEffect.OpenScanner -> onOpenScanner(effect.cameraPosition)
                ScannerQrUiEffect.NavigateBack -> onNavigateBack()
                ScannerQrUiEffect.NavigateHome -> onNavigateHome()
                ScannerQrUiEffect.EnableScannerButton -> Unit
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Scanner QR", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Use the scanner to read the voucher QR.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ScannerQrButton(
                    spec = uiState.buttonsState.scanner,
                    onClick = { onEvent(ScannerQrUiEvent.ScannerClicked) }
                )
                ScannerQrButton(
                    spec = uiState.buttonsState.cancel,
                    onClick = { onEvent(ScannerQrUiEvent.CancelClicked) }
                )
            }
        }
        uiState.dialogState?.let { dialogState ->
            ScannerQrButtonScannerQrCustomDialog(
                state = dialogState,
                onDismiss = { onEvent(ScannerQrUiEvent.DialogDismissed) },
                onButtonClicked = { btn ->
                    onEvent(ScannerQrUiEvent.DialogButtonClicked(btn))
                }
            )
        }
    }
}
