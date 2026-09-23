package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.ReceiptRedSysScreen
import ifac.td.taxi.compose.viewmodel.ReceiptRedSysComposeViewModel
import ifac.td.taxi.ui.screen.components.ReceiptRedSysUiEvent
import ifac.td.taxi.ui.screen.components.ReceiptRedSysUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 310-5: import androidx.compose.foundation.layout.*
@Composable
fun ReceiptRedSysScreen(
    navController: NavController,
    viewModel: ReceiptRedSysComposeViewModel,
    operations: List<RedSysOperation>
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(operations) {
        // If you pass navArgs list here, keep original fragment behavior
        viewModel.onEvent(ReceiptRedSysUiEvent.ScreenOpened)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ReceiptRedSysUiEffect.ShowDialog -> {
                    // handled by state in this version
                }
                is ReceiptRedSysUiEffect.ShowMessage -> {
                    // You can show Snackbar if desired
                }
                is ReceiptRedSysUiEffect.PrintServiceTicket -> {
                    viewModel.printServiceTicket(effect.operation)
                }
                is ReceiptRedSysUiEffect.PrintRefundTicket -> {
                    viewModel.printRefundTicket(effect.operation)
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("RedSys Receipts") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.operations.isEmpty()) {
                Text(
                    text = "No operations available",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.operations) { operation ->
                        ReceiptRedSysItem(
                            operation = operation,
                            onClick = { viewModel.onEvent(ReceiptRedSysUiEvent.OperationClicked(operation)) }
                        )
                    }
                }
            }
            if (uiState.dialogState != null) {
                ReceiptRedSysDialog(
                    state = uiState.dialogState!!,
                    onDismiss = { viewModel.onEvent(ReceiptRedSysUiEvent.DialogDismissed) },
                    onButtonClick = { button ->
                        viewModel.onEvent(ReceiptRedSysUiEvent.DialogButtonClicked(button))
                    }
                )
            }
        }
    }
}
