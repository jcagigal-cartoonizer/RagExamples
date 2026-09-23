package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PaymentButton
import ifac.td.taxi.ui.screen.components.PaymentButtons
import ifac.td.taxi.ui.screen.components.Payment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 496-7: import androidx.compose.foundation.layout.*
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
