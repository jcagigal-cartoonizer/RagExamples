package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 467-5: import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun ChangeDriverPinCustomDialog(
    state: CustomDialogState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(text = state.title)
        },
        text = {
            Spacer(modifier = Modifier.height(1.dp))
        },
        confirmButton = {
            if (ChangeDriverPinDialogButtonType.Accept in state.buttons) {
                TextButton(onClick = onAccept) {
                    Text("ACEPTAR")
                }
            }
        },
        dismissButton = {
            if (ChangeDriverPinDialogButtonType.Cancel in state.buttons) {
                TextButton(onClick = onDismiss) {
                    Text("CANCELAR")
                }
            }
        }
    )
}
fun submitChangePin() {
    val presenterView = object : ChangePasswordPinView {
        override fun updateSuccess() {
            onPinChangeSuccess()
        }
        override fun updateFailure() {
            onPinChangeFailure()
        }
    }
    viewModelScope.launch {
        val presenter = UserModule.provideUserPresenter(presenterView, app)
        val state = _uiState.value
        presenter.changeDriverPin(
            state.currentDriverNumber.trim(),
            state.currentPin.trim(),
            state.newPin.trim(),
            app
        )
    }
}
Then `AcceptClicked` simply validates and calls `submitChangePin()`.
In Compose Navigation, replace:
iMainActivity.navigateBack()
with:
navController.popBackStack()
