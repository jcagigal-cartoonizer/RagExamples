package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 380-4: import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.PendingTripsButtonsState
import ifac.td.taxi.viewmodel.PendingTripsDialogButtonsState
import ifac.td.taxi.viewmodel.PendingTripsScreenButtonAction
@Composable
fun PendingTripsButtonsRow(
    state: PendingTripsButtonsState,
    onButtonClick: (PendingTripsScreenButtonAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.cancelVisible) {
            PendingTripsButton(
                text = state.cancelText,
                enabled = state.cancelEnabled,
                containerColor = if (state.cancelEnabled) state.cancelContainerColor else state.cancelDisabledContainerColor,
                contentColor = if (state.cancelEnabled) state.cancelContentColor else state.cancelDisabledContentColor,
                onClick = { onButtonClick(PendingTripsScreenButtonAction.CANCEL) },
                modifier = Modifier.weight(1f)
            )
        }
        if (state.confirmVisible) {
            PendingTripsButton(
                text = state.confirmText,
                enabled = state.confirmEnabled,
                containerColor = if (state.confirmEnabled) state.confirmContainerColor else state.confirmDisabledContainerColor,
                contentColor = if (state.confirmEnabled) state.confirmContentColor else state.confirmDisabledContentColor,
                onClick = { onButtonClick(PendingTripsScreenButtonAction.ACCEPT) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
@Composable
fun PendingTripsButton(
    text: String,
    enabled: Boolean,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, containerColor)
    ) {
        Text(text)
    }
}
