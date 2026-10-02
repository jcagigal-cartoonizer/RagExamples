package ifac.td.taxi.ui.screen.components
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// # Block 447-5: import androidx.compose.foundation.background
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
@Composable
fun LoginDriverCustomDialogCustomDialog(
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
