package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 4-1: import android.graphics.Bitmap
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toCurrency
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.framework.util.tickets.Tickets
import kotlinx.coroutines.flow.collectLatest
@Composable
fun PaymentMoneiScreen(
    viewModel: PaymentMoneiComposeViewModel,
    sharedViewModel: MainActivitySharedUiModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onPrintTicket: (String) -> Unit,
    showHeader: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonState by viewModel.buttonsState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        showHeader(false)
        viewModel.init(
            totalAmount = sharedViewModel.trip?.totalAmount ?: 0,
            isoMoneda = NumberUtils.currencySymbol(),
            orderId = "${System.currentTimeMillis()}_${TicketsPrinter.get_numRecibo(10000000)}"
        )
    }
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collectLatest { effect ->
                when (effect) {
                    PaymentMoneiUiEffect.NavigateBack -> onNavigateBack()
                    PaymentMoneiUiEffect.NavigateHome -> onNavigateHome()
                    is PaymentMoneiUiEffect.ShowToast -> {
                        android.widget.Toast.makeText(context, effect.messageRes, android.widget.Toast.LENGTH_SHORT).show()
                    }
                    is PaymentMoneiUiEffect.PrintTicket -> onPrintTicket(effect.ticketBody)
                    is PaymentMoneiUiEffect.ShowDialog -> Unit
                    PaymentMoneiUiEffect.HideDialog -> Unit
                }
            }
        }
    }
    if (uiState.showDialog) {
        PaymentMoneiCustomDialog(
            title = uiState.dialogTitle,
            message = uiState.dialogMessage,
            confirmText = uiState.dialogConfirmText,
            dismissText = uiState.dialogDismissText,
            onConfirm = { viewModel.onEvent(PaymentMoneiUiEvent.DialogConfirm) },
            onDismiss = { viewModel.onEvent(PaymentMoneiUiEvent.DialogDismiss) }
        )
    }
    PaymentMoneiContent(
        uiState = uiState,
        buttonsState = buttonState,
        onEvent = viewModel::onEvent
    )
}
@Composable
fun PaymentMoneiContent(
    uiState: PaymentMoneiUiState,
    buttonsState: PaymentMoneiButtonsState,
    onEvent: (PaymentMoneiUiEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.amountText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            PaymentStatusChip(
                status = uiState.statusText,
                backgroundColor = uiState.statusBackgroundColor
            )
            Spacer(modifier = Modifier.height(20.dp))
            when {
                uiState.showQrLoading -> {
                    CircularProgressIndicator()
                }
                uiState.qrBitmap != null -> {
                    Image(
                        bitmap = uiState.qrBitmap,
                        contentDescription = "Payment QR",
                        modifier = Modifier.size(260.dp)
                    )
                }
                else -> {
                    Text(
                        text = stringResource(R.string.loading),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.showInfoQr) {
                Text(
                    text = stringResource(R.string.scan_qr_to_pay),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (uiState.showSuccessMessage) {
                Text(
                    text = stringResource(R.string.payment_success),
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            PaymentMoneiButtons(
                state = buttonsState,
                onCancelClick = { onEvent(PaymentMoneiUiEvent.CancelClicked) },
                onPrintClick = { onEvent(PaymentMoneiUiEvent.PrintClicked) }
            )
        }
    }
}
@Composable
fun PaymentMoneiButtons(
    state: PaymentMoneiButtonsState,
    onCancelClick: () -> Unit,
    onPrintClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.cancel.visible) {
            CustomStyledButton(
                text = state.cancel.text,
                modifier = Modifier.fillMaxWidth(),
                colors = state.cancel.colors,
                contentColor = state.cancel.contentColor,
                enabled = state.cancel.enabled,
                onClick = onCancelClick
            )
        }
        if (state.print.visible) {
            CustomStyledButton(
                text = state.print.text,
                modifier = Modifier.fillMaxWidth(),
                colors = state.print.colors,
                contentColor = state.print.contentColor,
                enabled = state.print.enabled,
                onClick = onPrintClick
            )
        }
    }
}
@Composable
fun CustomStyledButton(
    text: String,
    modifier: Modifier = Modifier,
    colors: ButtonColors,
    contentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        colors = colors,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun PaymentStatusChip(
    status: String,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = status,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
