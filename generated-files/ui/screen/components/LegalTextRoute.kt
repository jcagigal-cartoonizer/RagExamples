package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 5-1: import androidx.compose.foundation.layout.*
// // import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import ifac.td.taxi.ui.screen.components.CustomDialog
import ifac.td.taxi.ui.screen.components.LegalTextButtons
import ifac.td.taxi.viewmodel.LegalTextUiEffect
import ifac.td.taxi.viewmodel.LegalTextComposeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
@Composable
fun LegalTextRoute(
    navController: NavController,
    viewModel: LegalTextComposeViewModel,
    showHeader: (Boolean) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Preserve fragment behavior: hide header on screen entry
    LaunchedEffect(Unit) {
        showHeader(false)
        viewModel.loadLegalText()
    }
    // Collect effects lifecycle-aware
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.effects.collectLatest { effect ->
                    when (effect) {
                        LegalTextUiEffect.NavigateToWelcome -> {
                            navController.navigate(R.id.action_legalTextFragment_to_welcomeFragment)
                        }
                        LegalTextUiEffect.ShowAcceptDialog -> {
                            viewModel.setDialogVisible(true)
                        }
                        LegalTextUiEffect.HideAcceptDialog -> {
                            viewModel.setDialogVisible(false)
                        }
                    }
                }
            }
        }
    }
    LegalTextScreen(
        uiState = uiState,
        onAcceptClick = viewModel::onAcceptClick,
        onDismissDialog = viewModel::dismissDialog,
        onConfirmDialog = viewModel::confirmDialog,
    )
}
@Composable
fun LegalTextScreen(
    uiState: ifac.td.taxi.viewmodel.LegalTextUiState,
    onAcceptClick: () -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDialog: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = uiState.legalText ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            )
            Spacer(modifier = Modifier.height(16.dp))
            LegalTextButtons(
                state = uiState.buttonsState,
                onAccept = onAcceptClick
            )
        }
        if (uiState.isDialogVisible) {
            LegalTextCustomDialog(
                title = uiState.dialog.title,
                message = uiState.dialog.message,
                confirmText = uiState.dialog.confirmText,
                dismissText = uiState.dialog.dismissText,
                onConfirm = onConfirmDialog,
                onDismiss = onDismissDialog
            )
        }
    }
}
