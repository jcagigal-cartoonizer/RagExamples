package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import androidx.compose.runtime.Composable
@Composable
fun OpenPartialButtons(
    buttons: OpenPartialButtonsState,
    onEvent: (OpenPartialUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (buttons.back.visible) {
            StyledButton(
                text = "Back",
                state = buttons.back,
                onClick = { onEvent(OpenPartialUiEvent.OnBackClicked) },
                modifier = Modifier.weight(1f)
            )
        }
        if (buttons.print.visible) {
            StyledButton(
                text = "Print",
                state = buttons.print,
                onClick = { onEvent(OpenPartialUiEvent.OnPrintClicked) },
                modifier = Modifier.weight(1f)
            )
        }
        if (buttons.close.visible) {
            StyledButton(
                text = "Close",
                state = buttons.close,
                onClick = { onEvent(OpenPartialUiEvent.OnCloseClicked) },
                modifier = Modifier.weight(1f)
            )
        }
        if (buttons.totalizers.visible) {
            StyledButton(
                text = "Totalizers",
                state = buttons.totalizers,
                onClick = { onEvent(OpenPartialUiEvent.OnTotalizersClicked) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
