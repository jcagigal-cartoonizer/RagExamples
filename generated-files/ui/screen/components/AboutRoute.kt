package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.framework.util.ApkUtils
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.flow.collectLatest
@Composable
fun AboutRoute(
    viewModel: AboutComposeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
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
            dialogState = CustomDialogState(
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
