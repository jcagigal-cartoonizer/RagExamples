package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 349-4: import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
@Composable
fun LoginDriverScreen(
    uiState: LoginDriverUiState,
    onDriverChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCancel: () -> Unit,
    onLoginCentral: () -> Unit,
    onLoginRefuerzo: () -> Unit,
    onLoginWithoutCentral: () -> Unit,
    onChangePin: () -> Unit,
    onDismissDialog: () -> Unit,
) {
    var showIncorrectDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (uiState.canShowDriverContainer) {
            OutlinedTextField(
                value = uiState.driverId,
                onValueChange = onDriverChange,
                label = { Text("Driver") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LoginDriverButton(
                text = "Cancel",
                state = uiState.buttons.cancel,
                modifier = Modifier.weight(1f),
                onClick = onCancel
            )
        }
        Spacer(Modifier.height(12.dp))
        if (uiState.buttons.conCentral.visible) {
            LoginDriverButton(
                text = "Con central",
                state = uiState.buttons.conCentral,
                modifier = Modifier.fillMaxWidth(),
                onClick = onLoginCentral
            )
            Spacer(Modifier.height(8.dp))
        }
        if (uiState.buttons.refuerzo.visible) {
            LoginDriverButton(
                text = "Refuerzo",
                state = uiState.buttons.refuerzo,
                modifier = Modifier.fillMaxWidth(),
                onClick = onLoginRefuerzo
            )
            Spacer(Modifier.height(8.dp))
        }
        if (uiState.buttons.sinCentral.visible) {
            LoginDriverButton(
                text = "Sin central",
                state = uiState.buttons.sinCentral,
                modifier = Modifier.fillMaxWidth(),
                onClick = onLoginWithoutCentral
            )
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onChangePin) { Text("Change PIN") }
    }
    if (showIncorrectDialog) {
        LoginDriverCustomDialog(
            title = "Error",
            message = dialogMessage,
            confirmText = "OK",
            onConfirm = {
                showIncorrectDialog = false
                onDismissDialog()
            },
            onDismiss = {
                showIncorrectDialog = false
                onDismissDialog()
            }
        )
    }
}
