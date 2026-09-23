package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.LoginDriverUiEffect
import ifac.td.taxi.ui.screen.components.LoginDriverCustomDialog
import ifac.td.taxi.ui.screen.components.LoginDriverUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 502-6: import androidx.compose.foundation.background
@Composable
fun LoginDriverCustomDialog(
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onConfirm) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
In your Fragment or Activity hosting Compose, wire navigation like this:
LoginDriverRoute(
    connectionMode = args.connectionMode,
    startTurnMode = args.startTurnMode,
    onNavigateBack = { navController.popBackStack() },
    onNavigateToChangePin = {
        navController.navigate(R.id.action_loginDriverFragment_to_changeDriverPinFragment)
    },
    onLoginWithoutCentralFinished = {
        // preserve original behavior
        sharedViewModel.connectTaximeter()
        navController.navigate(R.id.action_loginDriverFragment_to_homeFragment)
    }
)
A single `SharedFlow<LoginDriverUiEffect>` replaces:
To satisfy “use dialog state, lifecycle collection of state/events for dialog handling” more strictly, you can map the effect into UI state:
data class LoginDriverUiState(
    ...
    val dialog: LoginDriverDialogState? = null
)
sealed interface LoginDriverDialogState {
    data class IncorrectCredentials(val showIncorrect: Boolean) : LoginDriverDialogState
    data class Message(val title: String, val message: String) : LoginDriverDialogState
}
Then your effect collector updates `uiState.dialog`, and the composable renders it conditionally.
