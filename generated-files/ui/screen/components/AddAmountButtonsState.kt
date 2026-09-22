package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.collectLatest
@Composable
fun AddAmountRoute(
    viewModel: AddAmountComposeViewModel,
    onNavigateBack: () -> Unit,
    onShowDialog: (AddAmountDialogState) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collectLatest { effect ->
                when (effect) {
                    AddAmountUiEffect.NavigateBack -> onNavigateBack()
                    is AddAmountUiEffect.ShowDialog -> onShowDialog(effect.dialogState)
                }
            }
        }
    }
    AddAmountScreen(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent
    )
}
@Composable
fun AddAmountScreen(
    uiState: AddAmountUiState,
    dialogState: AddAmountDialogState?,
    onEvent: (AddAmountUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add Amount") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AmountField(
                label = "Service amount",
                value = uiState.serviceAmountText,
                enabled = uiState.buttonsState.serviceEnabled,
                placeholder = uiState.serviceAmountHint,
                onValueChange = { onEvent(AddAmountUiEvent.ServiceAmountChanged(it)) }
            )
            if (uiState.buttonsState.extraVisible) {
                Spacer(Modifier.height(12.dp))
                AmountField(
                    label = "Extras",
                    value = uiState.extraAmountText,
                    enabled = uiState.buttonsState.extraEnabled,
                    placeholder = uiState.extraAmountHint,
                    onValueChange = { onEvent(AddAmountUiEvent.ExtraAmountChanged(it)) }
                )
            }
            if (uiState.buttonsState.tollsVisible) {
                Spacer(Modifier.height(12.dp))
                AmountField(
                    label = "Tolls",
                    value = uiState.tollAmountText,
                    enabled = uiState.buttonsState.tollsEnabled,
                    placeholder = uiState.tollAmountHint,
                    onValueChange = { onEvent(AddAmountUiEvent.TollAmountChanged(it)) }
                )
            }
            if (uiState.buttonsState.tipsVisible) {
                Spacer(Modifier.height(12.dp))
                AmountField(
                    label = "Tips",
                    value = uiState.tipAmountText,
                    enabled = uiState.buttonsState.tipsEnabled,
                    placeholder = uiState.tipAmountHint,
                    onValueChange = { onEvent(AddAmountUiEvent.TipAmountChanged(it)) }
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Total: ${uiState.totalAmountText}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(24.dp))
            AddAmountButtons(
                state = uiState.buttonsState,
                onAccept = { onEvent(AddAmountUiEvent.AcceptClicked) },
                onCancel = { onEvent(AddAmountUiEvent.CancelClicked) }
            )
        }
    }
    if (dialogState != null && dialogState.visible) {
        AddAmountCustomDialog(
            dialog = dialogState,
            onDismiss = { onEvent(AddAmountUiEvent.DismissDialog) },
            onAccept = { onEvent(AddAmountUiEvent.DialogAccept) }
        )
    }
}
@Composable
fun AmountField(
    label: String,
    value: String,
    enabled: Boolean,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
            keyboardType = KeyboardType.Number
        )
    )
}
