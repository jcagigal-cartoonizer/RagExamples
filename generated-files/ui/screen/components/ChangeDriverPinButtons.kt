package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 389-4: import androidx.compose.foundation.BorderStroke
// import androidx.compose.foundation.BorderStroke
// import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun ChangeDriverPinButtons(
    state: ChangeDriverPinButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.accept.visible) {
            CustomStyledButton(
                visualState = state.accept,
                onClick = onAccept
            )
        }
        if (state.cancel.visible) {
            CustomStyledButton(
                visualState = state.cancel,
                onClick = onCancel
            )
        }
    }
}
@Composable
fun CustomStyledButton(
    visualState: ChangeDriverPinButtonVisualState,
    onClick: () -> Unit
) {
    val bg = Color(visualState.backgroundColor)
    val fg = Color(visualState.contentColor)
    if (visualState.borderColor != null) {
        OutlinedButton(
            onClick = onClick,
            enabled = visualState.enabled,
            border = BorderStroke(1.dp, Color(visualState.borderColor)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = fg
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(visualState.text)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = visualState.enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = bg,
                contentColor = fg
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(visualState.text)
        }
    }
}
// // # Block 467-5: import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
@Composable
fun ChangeDriverPinChangeDriverPinCustomDialog(
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
