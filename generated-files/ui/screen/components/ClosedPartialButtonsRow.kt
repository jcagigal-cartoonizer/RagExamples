package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ClosedPartialButtonsRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 286-5: import androidx.compose.foundation.layout.*
@Composable
fun ClosedPartialButtonsRow(
    buttonsState: ClosedPartialButtonsState,
    onCancel: () -> Unit,
    onPrint: () -> Unit,
    onTotalizers: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StyledActionButton(
            text = "Cancel",
            style = buttonsState.cancel,
            onClick = onCancel
        )
        StyledActionButton(
            text = "Print",
            style = buttonsState.print,
            onClick = onPrint
        )
        if (buttonsState.showTotalizers) {
            StyledActionButton(
                text = "Totalizers",
                style = buttonsState.totalizers,
                onClick = onTotalizers
            )
        }
    }
}
@Composable
fun StyledActionButton(
    text: String,
    style: ClosedPartialButtonStyle,
    onClick: () -> Unit
) {
    when (style) {
        ClosedPartialButtonStyle.Hidden -> Unit
        ClosedPartialButtonStyle.Disabled -> {
            Button(
                onClick = onClick,
                enabled = false,
                colors = closedPartialButtonColors(disabled = true),
                modifier = Modifier.fillMaxWidth()
            ) { Text(text) }
        }
        ClosedPartialButtonStyle.Enabled -> {
            Button(
                onClick = onClick,
                enabled = true,
                colors = closedPartialButtonColors(disabled = false),
                modifier = Modifier.fillMaxWidth()
            ) { Text(text) }
        }
    }
}
@Composable
fun closedPartialButtonColors(disabled: Boolean): ButtonColors {
    return if (disabled) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}
@Composable
fun TicketViewerReceipts(
    ticketContent: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = ticketContent,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
