package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 234-4: import androidx.compose.foundation.background
// // import androidx.compose.foundation.background
// // import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun ClosedPartialScreen(
    uiState: ClosedPartialUiState,
    onCancel: () -> Unit,
    onPrint: () -> Unit,
    onTotalizers: () -> Unit,
    onDialogDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TicketViewerReceipts(
                ticketContent = uiState.ticketContent
            )
            Spacer(modifier = Modifier.height(16.dp))
            ClosedPartialButtonsRow(
                buttonsState = uiState.buttonsState,
                onCancel = onCancel,
                onPrint = onPrint,
                onTotalizers = onTotalizers
            )
        }
        uiState.dialog?.let { dialog ->
            ClosedPartialCustomDialog(
                title = dialog.title,
                message = dialog.message,
                confirmText = dialog.confirmText,
                dismissText = dialog.dismissText,
                onConfirm = onDialogDismiss,
                onDismiss = onDialogDismiss
            )
        }
    }
}
// // # Block 286-5: import androidx.compose.foundation.layout.*
// // import androidx.compose.foundation.layout.*
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
