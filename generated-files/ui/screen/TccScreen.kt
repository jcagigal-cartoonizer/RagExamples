package ifac.td.taxi.ui.screen
import ifac.td.taxi.compose.viewmodel.TccComposeViewModel
import ifac.td.taxi.ui.screen.components.TccUiEffect
import ifac.td.taxi.ui.screen.components.TccButtonStyle
import ifac.td.taxi.ui.screen.components.TccScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 350-3: import androidx.compose.foundation.layout.*
@Composable
fun TccScreen(
    tripId: Long?,
    navController: NavController,
    viewModel: TccComposeViewModel,
    onToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(tripId) {
        viewModel.loadDispatch(tripId)
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is TccUiEffect.ToastRes -> onToast(effect.resId)
                is TccUiEffect.NavigateToSignature -> {
                    navController.navigate(
                        ifac.td.taxi.PaymentDirections.goToSignatureFragment(effect.dispatchId)
                    )
                }
                is TccUiEffect.NavigateToVoucher -> {
                    navController.navigate(
                        ifac.td.taxi.PaymentDirections.goToCropImageViewFragment(effect.dispatchId)
                    )
                }
                is TccUiEffect.NavigateToQr -> {
                    navController.navigate(
                        ifac.td.taxi.PaymentDirections.goToScannerQRFragment(effect.dispatchId)
                    )
                }
                is TccUiEffect.FinishTcc -> Unit
                is TccUiEffect.NavigateBack -> navController.popBackStack()
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        } else {
            TccContent(
                dispatch = uiState.dispatch,
                buttonsState = uiState.buttonsState,
                onAccept = { att1, att2, att3, att4 ->
                    uiState.dispatch?.let { dispatch ->
                        viewModel.onAcceptClicked(
                            tripId = tripId,
                            dispatch = dispatch,
                            att1 = att1,
                            att2 = att2,
                            att3 = att3,
                            att4 = att4
                        )
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
        when (val dialog = uiState.dialogState) {
            is TccDialogState.Hidden -> Unit
            is TccDialogState.Error -> {
                TccCustomDialog(
                    visible = true,
                    title = stringResource(R.string.dialog_error_title),
                    message = dialog.message,
                    confirmText = stringResource(android.R.string.ok),
                    onConfirm = viewModel::dismissDialog,
                    onDismiss = viewModel::dismissDialog
                )
            }
            is TccDialogState.ConfirmAccept -> {
                TccCustomDialog(
                    visible = true,
                    title = stringResource(R.string.dialog_error_title),
                    message = dialog.dispatchTitle ?: "",
                    confirmText = stringResource(android.R.string.ok),
                    onConfirm = viewModel::dismissDialog,
                    onDismiss = viewModel::dismissDialog
                )
            }
        }
    }
}
@Composable
fun TccContent(
    dispatch: Dispatch?,
    buttonsState: TccButtonsState,
    onAccept: (Int?, Int?, Int?, Int?) -> Unit,
    onCancel: () -> Unit
) {
    val zeroToNine = remember { (0..9).toList() }
    var att1 by remember { mutableIntStateOf(1) }
    var att2 by remember { mutableIntStateOf(1) }
    var att3 by remember { mutableIntStateOf(1) }
    var att4 by remember { mutableIntStateOf(1) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        dispatch?.let {
            TccAttributeRow(
                state = buttonsState.attributes1,
                value = att1,
                onValueChange = { att1 = it },
                items = zeroToNine
            )
            TccAttributeRow(
                state = buttonsState.attributes2,
                value = att2,
                onValueChange = { att2 = it },
                items = zeroToNine
            )
            TccAttributeRow(
                state = buttonsState.attributes3,
                value = att3,
                onValueChange = { att3 = it },
                items = zeroToNine
            )
            TccAttributeRow(
                state = buttonsState.attributes4,
                value = att4,
                onValueChange = { att4 = it },
                items = zeroToNine
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            if (buttonsState.cancel.visible) {
                TccCustomButton(
                    text = "Cancel",
                    modifier = Modifier.weight(1f),
                    style = TccButtonStyle.cancel(),
                    enabled = buttonsState.cancel.enabled,
                    loading = buttonsState.cancel.loading,
                    onClick = onCancel
                )
            }
            Spacer(Modifier.width(12.dp))
            if (buttonsState.accept.visible) {
                TccCustomButton(
                    text = "Accept",
                    modifier = Modifier.weight(1f),
                    style = TccButtonStyle.accept(),
                    enabled = buttonsState.accept.enabled,
                    loading = buttonsState.accept.loading,
                    onClick = { onAccept(att1, att2, att3, att4) }
                )
            }
        }
    }
}
@Composable
fun TccAttributeRow(
    state: AttributeSpinnerState,
    value: Int,
    onValueChange: (Int) -> Unit,
    items: List<Int>
) {
    if (!state.visible) return
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = state.title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        TccDropdown(
            value = value,
            items = items,
            onValueChange = onValueChange
        )
    }
}
