package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.OpenPartialScreen
import ifac.td.taxi.ui.screen.components.OpenPartialUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 260-4: import androidx.compose.foundation.layout.*
@Composable
fun OpenPartialScreen(
    state: OpenPartialUiState,
    onEvent: (OpenPartialUiEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            } else {
                TicketViewer(
                    content = state.ticketContent,
                    modifier = Modifier.weight(1f)
                )
            }
            OpenPartialButtons(
                buttons = state.buttons,
                onEvent = onEvent,
                modifier = Modifier.fillMaxWidth()
            )
        }
        state.dialog?.let { dialog ->
            OpenPartialCustomDialog(
                state = dialog,
                onDismiss = { onEvent(OpenPartialUiEvent.OnDialogDismissed) },
                onAccept = { onEvent(OpenPartialUiEvent.OnDialogAccepted) },
            )
        }
    }
}
@Composable
fun TicketViewer(
    content: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = content)
    }
}
