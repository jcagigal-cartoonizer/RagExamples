package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.InformationMessageUiEffect
import ifac.td.taxi.ui.screen.components.InformationMessageScreen
import ifac.td.taxi.compose.viewmodel.InformationMessageComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 14-1: import androidx.activity.compose.BackHandler
@Composable
fun InformationMessageScreen(
    viewModel: InformationMessageComposeViewModel,
    onBack: () -> Unit,
    onNavigateBackAfterSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // One-off effects: show dialog, navigate, toast, etc.
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                InformationMessageUiEffect.NavigateBack -> onBack()
                InformationMessageUiEffect.NavigateBackAfterSend -> onNavigateBackAfterSend()
                is InformationMessageUiEffect.ShowToast -> {
                    // Hook your own toast mechanism here if needed
                }
            }
        }
    }
    val buttonsState = remember(uiState) {
        InformationMessageButtonsState.from(uiState)
    }
    BackHandler(enabled = true) {
        viewModel.onCancelPressed()
    }
    // Initial load, equivalent to initVM()
    LaunchedEffect(Unit) {
        viewModel.initVM()
    }
    // Observe dialog state via state holder
    uiState.selectedInformationMessage?.let { selected ->
        if (uiState.dialogVisible) {
            InformationMessageDialog(
                title = uiState.dialogTitle,
                description = selected.second,
                buttonsState = buttonsState.dialogButtons,
                onDismiss = viewModel::onDialogDismiss,
                onButtonClick = { type ->
                    viewModel.onDialogButtonPressed(type, selected.first)
                }
            )
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // XML had hidden header, so typically no top bar here.
        },
        bottomBar = {
            InformationMessageBottomBar(
                buttonsState = buttonsState,
                onCancel = viewModel::onCancelPressed
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(uiState.informationMessages) { index, message ->
                InformationMessageRow(
                    index = index,
                    message = message,
                    onClick = { viewModel.onMessageSelected(index, message) }
                )
            }
        }
    }
}
@Composable
fun InformationMessageRow(
    index: Int,
    message: String,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
with a Compose-friendly state/effect model.
