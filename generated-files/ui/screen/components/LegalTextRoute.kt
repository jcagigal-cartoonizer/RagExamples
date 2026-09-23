package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.LegalTextComposeViewModel
import ifac.td.taxi.ui.screen.components.LegalTextScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 5-1: import androidx.compose.foundation.layout.*
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
