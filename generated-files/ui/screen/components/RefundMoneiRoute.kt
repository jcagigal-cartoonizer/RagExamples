package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.RefundMoneiComposeViewModel
import ifac.td.taxi.ui.screen.components.RefundMoneiUiEvent
import ifac.td.taxi.ui.screen.components.RefundMoneiScreen
import ifac.td.taxi.ui.screen.components.RefundMoneiUiEffect
import ifac.td.taxi.ui.screen.components.RefundMoneiButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 328-5: import androidx.compose.foundation.layout.Arrangement
@Composable
fun RefundMoneiRoute(
    tripId: Long,
    viewModel: RefundMoneiComposeViewModel,
    onNavigateBack: () -> Unit,
    onShowSnackbar: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(tripId) {
        viewModel.start(tripId)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                RefundMoneiUiEffect.NavigateBack -> onNavigateBack()
                is RefundMoneiUiEffect.ShowSnackbar -> onShowSnackbar(effect.message)
                RefundMoneiUiEffect.StartPolling -> Unit
                RefundMoneiUiEffect.StopPolling -> Unit
            }
        }
    }
    RefundMoneiScreen(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}
@Composable
fun RefundMoneiScreen(
    state: RefundMoneiUiState,
    onEvent: (RefundMoneiUiEvent) -> Unit
) {
    val buttonsState = remember(state) {
        RefundMoneiButtonsState(
            showCancel = true,
            showRefund = state.status != PaymentMoneiViewModel.StatusPayments.REFUNDED,
            cancelEnabled = true,
            refundEnabled = state.canRefund && state.status != PaymentMoneiViewModel.StatusPayments.REFUNDED,
            cancelText = "Cancel",
            refundText = "Refund",
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Amount")
        Spacer(Modifier.height(8.dp))
        Text(text = state.amountText.toCurrency())
        Spacer(Modifier.height(12.dp))
        Text(text = state.status.name)
        if (state.isLoading) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            if (buttonsState.showCancel) {
                RefundMoneiButton(
                    text = buttonsState.cancelText,
                    enabled = buttonsState.cancelEnabled,
                    background = RefundMoneiButtonStyle.secondary,
                    textColor = RefundMoneiButtonStyle.textOnSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(RefundMoneiUiEvent.OnCancelClicked) }
                )
            }
            if (buttonsState.showRefund) {
                RefundMoneiButton(
                    text = buttonsState.refundText,
                    enabled = buttonsState.refundEnabled,
                    background = RefundMoneiButtonStyle.primary,
                    textColor = RefundMoneiButtonStyle.textOnPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(RefundMoneiUiEvent.OnRefundClicked) }
                )
            }
        }
        if (state.showConfirmDialog) {
            RefundMoneiCustomDialog(
                title = "Refund",
                message = "Do you want to refund this payment?",
                confirmText = "Refund",
                dismissText = "Cancel",
                onConfirm = { onEvent(RefundMoneiUiEvent.OnConfirmRefund) },
                onDismiss = { onEvent(RefundMoneiUiEvent.OnDismissDialog) }
            )
        }
        if (state.showErrorDialog) {
            RefundMoneiCustomDialog(
                title = "Error",
                message = state.errorMessage ?: "Something went wrong",
                confirmText = "OK",
                dismissText = "Close",
                onConfirm = { onEvent(RefundMoneiUiEvent.OnDismissDialog) },
                onDismiss = { onEvent(RefundMoneiUiEvent.OnDismissDialog) }
            )
        }
    }
}
class RefundMoneiComposeFragment : Fragment() {
    private val args: RefundMoneiFragmentArgs by navArgs()
    private val viewModel: RefundMoneiComposeViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Scaffold {
                    RefundMoneiRoute(
                        tripId = args.tripId,
                        viewModel = viewModel,
                        onNavigateBack = { findNavController().navigateUp() },
                        onShowSnackbar = { /* hook into your snackbar host if needed */ }
                    )
                }
            }
        }
    }
}
Your original fragment logic effectively did:
The Compose version above preserves that behavior with:
then the `RefundMoneiButton` can be upgraded to a `Surface`-based implementation. Example pattern:
@Composable
fun RefundMoneiButton(
    text: String,
    enabled: Boolean,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        color = if (enabled) background else RefundMoneiButtonStyle.disabled,
        shape = RefundMoneiButtonStyle.shape,
        modifier = modifier
            .heightIn(min = 48.dp)
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
    ) {
        androidx.compose.foundation.layout.Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = text, color = textColor)
        }
    }
}
1. a **full Koin module setup** for the Compose ViewModel  
2. a **Compose version of the status badge** matching your `tvStatus` behavior exactly  
3. a **more exact translation of `custom_dialog.xml`** if you share that XML or `RefundMoneiCustomDialog.kt` source
