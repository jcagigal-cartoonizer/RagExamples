package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
// import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
@Composable
fun LoginDriverButton(
    text: String,
    state: ButtonStyleState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (!state.visible) return
    val bg = when (state.background) {
        LoginButtonBackground.GREEN -> Color(0xFF2E7D32)
        LoginButtonBackground.BLUE -> Color(0xFF1565C0)
        LoginButtonBackground.ORANGE -> Color(0xFFEF6C00)
        LoginButtonBackground.GRAY -> Color(0xFF616161)
    }
    Box(
        modifier = modifier
            .height(52.dp)
            .background(bg, RoundedCornerShape(10.dp))
            .clickable(enabled = state.enabled && !state.isLoading) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = state.textColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = state.textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
// // # Block 502-6: import androidx.compose.foundation.background
// // import androidx.compose.foundation.background
// import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
@Composable
fun LoginDriverLoginDriverCustomDialog(
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
