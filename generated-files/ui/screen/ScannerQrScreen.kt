package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.ScannerQrUiEvent
import ifac.td.taxi.ui.screen.components.ScannerQrScreen
import ifac.td.taxi.ui.screen.components.ScannerQrUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
            ScannerQrCustomDialog(
                state = dialogState,
                onDismiss = { onEvent(ScannerQrUiEvent.DialogDismissed) },
                onButtonClicked = { btn ->
                    onEvent(ScannerQrUiEvent.DialogButtonClicked(btn))
                }
            )
        }
    }
}
