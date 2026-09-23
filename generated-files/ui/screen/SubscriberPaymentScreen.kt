package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.SubscriberPaymentUiEffect
import ifac.td.taxi.ui.screen.components.SubscriberPaymentScreen
import ifac.td.taxi.compose.viewmodel.SubscriberPaymentComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 4-1: import android.text.InputFilter
@Composable
fun SubscriberPaymentScreen(
    navController: NavController,
    viewModel: SubscriberPaymentComposeViewModel,
    onOpenScanner: (cameraPosition: Int, onResult: (String) -> Unit) -> Unit,
    onShowToast: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var dialogState by remember { mutableStateOf<SubscriberPaymentDialogState?>(null) }
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        is SubscriberPaymentUiEffect.ShowToast -> onShowToast(effect.messageRes)
                        is SubscriberPaymentUiEffect.ShowErrorDialog -> {
                            dialogState = SubscriberPaymentDialogState(
                                title = context.getString(R.string.dialog_error_title),
                                description = effect.description,
                                buttons = listOf(SubscriberPaymentDialogButton.Accept)
                            )
                        }
                        is SubscriberPaymentUiEffect.ShowOperationAuthorizedDialog -> {
                            dialogState = SubscriberPaymentDialogState(
                                title = context.getString(R.string.operation_authorized),
                                description = null,
                                buttons = listOf(SubscriberPaymentDialogButton.Accept),
                                cancellable = false
                            )
                        }
                        is SubscriberPaymentUiEffect.NavigateBack -> navController.popBackStack()
                        is SubscriberPaymentUiEffect.NavigateHome -> {
                            navController.navigate("home") {
                                popUpTo(0)
                            }
                        }
                        is SubscriberPaymentUiEffect.NavigateToSignature -> {
                            navController.navigate("signature/${effect.dispatchId}")
                        }
                        is SubscriberPaymentUiEffect.NavigateToVoucher -> {
                            navController.navigate("voucher/${effect.dispatchId}")
                        }
                        is SubscriberPaymentUiEffect.NavigateToQr -> {
                            navController.navigate("scannerQr/${effect.dispatchId}")
                        }
                    }
                }
            }
        }
    }
    SubscriberPaymentDialog(
        state = dialogState,
        onDismiss = { dialogState = null },
        onButtonClick = {
            dialogState = null
        }
    )
    SubscriberPaymentContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onOpenScanner = onOpenScanner,
        modifier = modifier
    )
}
