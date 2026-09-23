package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 234-4: import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
