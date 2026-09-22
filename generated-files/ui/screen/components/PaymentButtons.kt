package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
@Composable
fun PaymentScreen(
    navController: NavController,
    viewModel: PaymentComposeViewModel,
    onSendAppPaymentAuthRequest: (Int) -> Unit,
    onBackToDispatched: () -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<PaymentDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is PaymentUiEffect.NavigateTo -> {
                    navController.navigate(effect.routeId)
                }
                is PaymentUiEffect.NavigateToDirections -> {
                    // if using generated SafeArgs/Directions
                }
                is PaymentUiEffect.OpenDialog -> dialogState = effect.dialog
                is PaymentUiEffect.ShowToast -> onShowToast(effect.messageRes)
                is PaymentUiEffect.RequestAppPaymentAuth -> onSendAppPaymentAuthRequest(effect.amount)
                PaymentUiEffect.BackToDispatched -> onBackToDispatched()
                PaymentUiEffect.StopCountdown -> Unit
                PaymentUiEffect.ResetBottomBarText -> Unit
                is PaymentUiEffect.ShowRedSysPopup -> Unit
                is PaymentUiEffect.StartActivityForResult -> Unit
                is PaymentUiEffect.OpenExternalPayment -> Unit
            }
        }
    }
    BackHandler(enabled = true) {
        // preserve your fragment back behavior if needed
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Payment") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = uiState.amountText, style = MaterialTheme.typography.headlineMedium)
            PaymentButtons(
                state = uiState.buttons,
                onCard = { viewModel.onEvent(PaymentUiEvent.OnCardClicked) },
                onCash = { viewModel.onEvent(PaymentUiEvent.OnCashClicked) },
                onSubscriber = { viewModel.onEvent(PaymentUiEvent.OnSubscriberClicked) },
                onBizum = { viewModel.onEvent(PaymentUiEvent.OnBizumClicked) },
                onAddAmount = { viewModel.onEvent(PaymentUiEvent.OnAddAmountClicked) },
                onAppPayment = { viewModel.onEvent(PaymentUiEvent.OnAppPaymentClicked) },
                onOthersPayment = { viewModel.onEvent(PaymentUiEvent.OnOthersPaymentClicked) },
                onCancel = { viewModel.onEvent(PaymentUiEvent.OnCancelAppPaymentClicked) },
                onBackToDispatch = { viewModel.onEvent(PaymentUiEvent.OnBackToDispatchClicked) },
                onLocution = { viewModel.onEvent(PaymentUiEvent.OnLocutionClicked) },
                onCourtesy = { viewModel.onEvent(PaymentUiEvent.OnCourtesyLightClicked) },
                onUv = { viewModel.onEvent(PaymentUiEvent.OnUvLightClicked) },
            )
        }
    }
    if (dialogState != null) {
        PaymentPaymentCustomDialog(
            dialog = dialogState!!,
            onDismiss = { dialogState = null },
            onButtonClick = { button, text ->
                viewModel.onEvent(PaymentUiEvent.OnDialogButtonClicked(button, text))
                dialogState = null
            }
        )
    }
}
// // # Block 496-7: import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
@Composable
fun PaymentButtons(
    state: PaymentButtonsState,
    onCard: () -> Unit,
    onCash: () -> Unit,
    onSubscriber: () -> Unit,
    onBizum: () -> Unit,
    onAddAmount: () -> Unit,
    onAppPayment: () -> Unit,
    onOthersPayment: () -> Unit,
    onCancel: () -> Unit,
    onBackToDispatch: () -> Unit,
    onLocution: () -> Unit,
    onCourtesy: () -> Unit,
    onUv: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentButton(state.cash, onCash)
            PaymentButton(state.card, onCard)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentButton(state.subscriber, onSubscriber)
            if (state.bizum.visible) PaymentButton(state.bizum, onBizum)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.appPayment.visible) PaymentButton(state.appPayment, onAppPayment)
            if (state.othersPayment.visible) PaymentButton(state.othersPayment, onOthersPayment)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.addAmount.visible) PaymentButton(state.addAmount, onAddAmount)
            if (state.backToDispatch.visible) PaymentButton(state.backToDispatch, onBackToDispatch)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.coutesyLight.visible) PaymentButton(state.coutesyLight, onCourtesy)
            if (state.uvLight.visible) PaymentButton(state.uvLight, onUv)
            if (state.locution.visible) PaymentButton(state.locution, onLocution)
        }
    }
}
@Composable
fun PaymentButton(
    state: PaymentButtonUiState,
    onClick: () -> Unit
) {
    if (!state.visible) return
    Button(
        onClick = onClick,
        enabled = state.enabled && state.style != PaymentButtonStyle.LOADING,
        colors = paymentButtonColors(state.style),
        modifier = Modifier.height(48.dp)
    ) {
        Text(text = state.text)
    }
}
