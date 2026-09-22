package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 273-6: import androidx.compose.foundation.layout.*
// // // import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun ChangeUserPasswordScreen(
    state: ChangeUserPasswordUiState,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onRepeatPasswordChanged: (String) -> Unit,
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDialogAccepted: () -> Unit,
    dialogState: ChangeUserPasswordDialogState?,
    onDismissDialog: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.currentPassword,
                onValueChange = onCurrentPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Current password") }
            )
            OutlinedTextField(
                value = state.newPassword,
                onValueChange = onNewPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New password") }
            )
            OutlinedTextField(
                value = state.repeatPassword,
                onValueChange = onRepeatPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Repeat password") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ChangeUserPasswordButtons(
                state = state.buttonsState,
                onAcceptClick = onAcceptClick,
                onCancelClick = onCancelClick
            )
        }
        dialogState?.let { dialog ->
            ChangeUserPasswordDialog(
                state = dialog,
                onAccept = {
                    onDialogAccepted()
                    onDismissDialog()
                },
                onDismiss = onDismissDialog
            )
        }
    }
}
