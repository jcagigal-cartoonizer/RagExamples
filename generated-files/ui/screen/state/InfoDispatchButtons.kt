package ifac.td.taxi.ui.screen.state
import ifac.td.taxi.ui.screen.components.InfoDispatchCustomDialog
import ifac.td.taxi.ui.screen.state.InfoDispatchDialogState
import ifac.td.taxi.ui.screen.state.InfoDispatchButtonsState
import ifac.td.taxi.ui.screen.state.InfoDispatchButtons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // ## `InfoDispatchButtons.kt`


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun InfoDispatchButtons(
    state: InfoDispatchButtonsState,
    onNavigate: () -> Unit,
    onVoiceCall: () -> Unit,
    onPrint: () -> Unit,
    onNoClient: () -> Unit,
    onReturn: () -> Unit,
    onNotifications: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DispatchButton(state.notifications, onNotifications)
        DispatchButton(state.navigate, onNavigate)
        DispatchButton(state.voiceCall, onVoiceCall)
        DispatchButton(state.print, onPrint)
        DispatchButton(state.noClient, onNoClient)
        DispatchButton(state.returnDispatch, onReturn)
    }
}

@Composable
private fun DispatchButton(
    state: ButtonUiState,
    onClick: () -> Unit,
) {
    if (!state.visible) return

    val style = infoDispatchButtonStyle(state.background)

    val isEnabled = state.enabled && state.style != ButtonStyleUi.Loading

    Button(
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(style.shapeRadius.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = style.containerColor,
            contentColor = style.contentColor,
            disabledContainerColor = style.disabledContainerColor,
            disabledContentColor = style.disabledContentColor,
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.style == ButtonStyleUi.Loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp.value,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text = state.text)
    }
}


// # 7) Compose dialog implementation based on custom dialog behavior

// This is a dialog state-driven Compose replacement.

// ## `InfoDispatchDialog.kt`


import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Immutable
data class InfoDispatchDialogState(
    val visible: Boolean = false,
    val title: String = "",
    val description: String = "",
    val iconRes: Int? = null,
    val buttons: List<InfoDispatchDialogAction> = emptyList()
)

enum class InfoDispatchDialogAction {
    CANCEL,
    ACCEPT,
    AT_DOOR,
    RIDER_IN_CAB
}

@Composable
fun InfoDispatchCustomDialog(
    state: InfoDispatchDialogState,
    onDismiss: () -> Unit,
    onAction: (InfoDispatchDialogAction) -> Unit,
) {
    if (!state.visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = state.title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(
                text = state.description,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.buttons.forEach { action ->
                    when (action) {
                        InfoDispatchDialogAction.CANCEL -> TextButton(onClick = { onAction(action) }) { Text("CANCEL") }
                        InfoDispatchDialogAction.ACCEPT -> Button(onClick = { onAction(action) }) { Text("ACCEPT") }
                        InfoDispatchDialogAction.AT_DOOR -> Button(onClick = { onAction(action) }) { Text("AT DOOR") }
                        InfoDispatchDialogAction.RIDER_IN_CAB -> Button(onClick = { onAction(action) }) { Text("RIDER IN CAB") }
                    }
                }
            }
        }
    )
}


// # 8) Compose ViewModel with UiState + UiEffect

// This wraps your existing domain logic and keeps one shared effect stream.

