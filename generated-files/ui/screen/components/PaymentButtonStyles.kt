package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 366-5: import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
object PaymentButtonStyles {
    val EnabledContainer = Color(0xFF1E88E5)
    val EnabledContent = Color.White
    val DisabledContainer = Color(0xFFE0E0E0)
    val DisabledContent = Color(0xFF9E9E9E)
    val LoadingContainer = Color(0xFF90CAF9)
    val LoadingContent = Color.White
}
@Composable
fun paymentButtonColors(style: PaymentButtonStyle): ButtonColors {
    return when (style) {
        PaymentButtonStyle.ENABLE -> ButtonDefaults.buttonColors(
            containerColor = PaymentButtonStyles.EnabledContainer,
            contentColor = PaymentButtonStyles.EnabledContent,
            disabledContainerColor = PaymentButtonStyles.DisabledContainer,
            disabledContentColor = PaymentButtonStyles.DisabledContent
        )
        PaymentButtonStyle.DISABLE -> ButtonDefaults.buttonColors(
            containerColor = PaymentButtonStyles.DisabledContainer,
            contentColor = PaymentButtonStyles.DisabledContent
        )
        PaymentButtonStyle.LOADING -> ButtonDefaults.buttonColors(
            containerColor = PaymentButtonStyles.LoadingContainer,
            contentColor = PaymentButtonStyles.LoadingContent
        )
    }
}
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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
        PaymentCustomDialog(
            dialog = dialogState!!,
            onDismiss = { dialogState = null },
            onButtonClick = { button, text ->
                viewModel.onEvent(PaymentUiEvent.OnDialogButtonClicked(button, text))
                dialogState = null
            }
        )
    }
}
