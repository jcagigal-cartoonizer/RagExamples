package ifac.td.taxi.compose.navigation
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
// # Block 54-2: import androidx.compose.runtime.*
@Composable
fun LoginDriverNavigationRoute(
    navController: NavController,
    viewModel: LoginDriverViewModel,
    connectionMode: Int,
    startTurnMode: Int,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // If your ViewModel needs args, pass them here as needed:
    LaunchedEffect(connectionMode, startTurnMode) {
        viewModel.setNavigationArgs(
            connectionMode = connectionMode,
            startTurnMode = startTurnMode
        )
    }
    LoginDriverScreen(
        uiState = uiState,
        onDriverChange = { viewModel.onDriverChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onCancel = onNavigateBack,
        onLoginCentral = { viewModel.onLoginCentral() },
        onLoginRefuerzo = { viewModel.onLoginRefuerzo() },
        onLoginWithoutCentral = { viewModel.onLoginWithoutCentral() },
        onChangePin = {
            navController.navigate(R.id.changeDriverPinFragment)
        },
        onDismissDialog = { viewModel.onDismissDialog() }
    )
}
Your current `LoginDriverScreen` has local dialog state:
var showIncorrectDialog by remember { mutableStateOf(false) }
var dialogMessage by remember { mutableStateOf("") }
But these are never updated, so the dialog will never appear.
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
    if (uiState.showErrorDialog) {
        LoginDriverCustomDialogCustomDialog(
            title = "Error",
            message = uiState.dialogMessage,
            confirmText = "OK",
            onConfirm = onDismissDialog,
            onDismiss = onDismissDialog
        )
    }
}
