package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SubscriberPaymentUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 96-2: import androidx.compose.foundation.layout.*
@Composable
fun SubscriberPaymentContent(
    state: SubscriberPaymentUiState,
    onEvent: (SubscriberPaymentUiEvent) -> Unit,
    onOpenScanner: (cameraPosition: Int, onResult: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SubscriberTextFields(
            state = state,
            onEvent = onEvent,
            onOpenScanner = onOpenScanner
        )
        SubscriberPaymentButtons(
            buttonsState = state.buttonsState,
            onAccept = { onEvent(SubscriberPaymentUiEvent.AcceptClicked) },
            onCancel = { onEvent(SubscriberPaymentUiEvent.CancelClicked) },
            onQrClick = { onEvent(SubscriberPaymentUiEvent.QrClicked) }
        )
    }
}
