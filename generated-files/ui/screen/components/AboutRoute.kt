package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.AboutCustomDialogState
import ifac.td.taxi.compose.viewmodel.AboutComposeViewModel
import ifac.td.taxi.ui.screen.components.AboutUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 6-1: import androidx.compose.foundation.clickable
@Composable
fun AboutRoute(
    viewModel: AboutComposeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Dialog state holder
    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    // Lifecycle-safe effect collection
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                AboutUiEffect.NavigateBack -> onNavigateBack()
                AboutUiEffect.ShowWarningDialog -> showWarningDialog = true
            }
        }
    }
    if (showWarningDialog) {
        AboutCustomDialog(
            dialogState = AboutCustomDialogState(
                title = "Warning",
                message = "No browser available to open this link.",
                confirmText = "OK",
                dismissText = null,
                showDismiss = false
            ),
            onConfirm = {
                showWarningDialog = false
            },
            onDismiss = {
                showWarningDialog = false
            }
        )
    }
    AboutScreen(
        uiState = uiState,
        onPrivacyClick = { viewModel.onEvent(AboutUiEvent.ClickPrivacy) },
        onAcceptClick = { viewModel.onEvent(AboutUiEvent.ClickAccept) },
        onLogoClick = { viewModel.onEvent(AboutUiEvent.LogoTapped) },
        modifier = modifier
    )
}
