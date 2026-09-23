package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.PortugalSettingsScreen
import ifac.td.taxi.ui.screen.components.PortugalSettingsUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 254-3: import androidx.compose.foundation.layout.*
@Composable
fun PortugalSettingsScreen(
    uiState: PortugalSettingsUiState,
    onEvent: (PortugalSettingsUiEvent) -> Unit,
    dialogState: PortugalDialogState?,
    onDismissDialog: () -> Unit
) {
    val buttonsState = remember(uiState.resetHashEnabled) {
        PortugalSettingsButtonsState.fromUiState(uiState)
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = uiState.atcud,
            onValueChange = { onEvent(PortugalSettingsUiEvent.AtcudChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("ATCUD") }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.sequenceNumber,
            onValueChange = { onEvent(PortugalSettingsUiEvent.SequenceNumberChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Serie") }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.document,
            onValueChange = { onEvent(PortugalSettingsUiEvent.DocumentChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Documento") }
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.resetHashEnabled,
                onCheckedChange = { onEvent(PortugalSettingsUiEvent.ResetHashChanged(it)) }
            )
            Spacer(Modifier.width(8.dp))
            Text("Reset hash")
        }
        Spacer(Modifier.height(20.dp))
        PortugalSettingsButtons(
            state = buttonsState,
            onAccept = { onEvent(PortugalSettingsUiEvent.ClickAccept) },
            onCancel = { onEvent(PortugalSettingsUiEvent.ClickCancel) },
            onChangePin = { onEvent(PortugalSettingsUiEvent.ClickChangePin) }
        )
    }
}
