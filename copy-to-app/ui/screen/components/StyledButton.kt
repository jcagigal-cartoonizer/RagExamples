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
// # Block 372-6: import androidx.compose.foundation.BorderStroke
@Composable
fun StyledButton(
    text: String,
    state: ButtonUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = if (state.style == ButtonStyle.ENABLE) {
        ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E88E5),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF90A4AE),
            disabledContentColor = Color.White
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color(0xFFB0BEC5),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFB0BEC5),
            disabledContentColor = Color.White
        )
    }
    OutlinedButton(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.height(48.dp),
        colors = colors,
        border = BorderStroke(
            width = 1.dp,
            color = if (state.enabled) Color(0xFF1565C0) else Color(0xFF90A4AE)
        )
    ) {
        Text(text = text)
    }
}
@Composable
fun OpenPartialCustomDialogCustomDialog(
    state: OpenPartialDialogState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Alert",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Are you sure you want to close partials?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onAccept) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}
@Composable
fun OpenPartialCustomDialogCustomDialog(
    state: OpenPartialDialogState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
) {
    val title = androidx.compose.ui.res.stringResource(state.titleRes)
    val message = androidx.compose.ui.res.stringResource(state.messageRes)
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onAccept) { Text("Accept") }
                }
            }
        }
    }
}
Your fragment logic had a few conditions:
To fully preserve the original fragment logic, add a shared state source for:
Then compute button state like this:
val canShowTotalizers =
    !canClose &&
    shiftStatus?.currentStatus != ifConstants.STATE_DISCONNECTED &&
    isTaximeterConnected
and
val totalizersEnabled = isTaximeterConnected && hasTotalizers
Then store it in `OpenPartialButtonsState`.
1. a full `OpenPartialComposable` wired into `NavHost`
2. a Koin module for the Compose `ViewModel`
3. a more exact Material-style clone of your `CustomButton` and `OpenPartialCustomDialogCustomDialog` XML visuals
