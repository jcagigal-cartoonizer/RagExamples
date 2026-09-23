package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.PortugalSettingsComposeViewModel
import ifac.td.taxi.ui.screen.components.PortugalSettingsCustomDialog
import ifac.td.taxi.ui.screen.components.PortugalSettingsButtons
import ifac.td.taxi.ui.screen.components.PortugalSettingsUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 367-5: import androidx.compose.foundation.layout.*
@Composable
fun PortugalSettingsButtons(
    state: PortugalSettingsButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    onChangePin: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (state.acceptVisible) {
            Button(
                onClick = onAccept,
                enabled = state.acceptEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = state.acceptContainerColor,
                    contentColor = state.acceptContentColor
                )
            ) {
                Text("ACEPTAR")
            }
        }
        Spacer(Modifier.height(8.dp))
        if (state.cancelVisible) {
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = state.cancelContainerColor,
                    contentColor = state.cancelContentColor
                )
            ) {
                Text("CANCELAR")
            }
        }
        Spacer(Modifier.height(8.dp))
        if (state.changePinVisible) {
            Button(
                onClick = onChangePin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = state.changePinContainerColor,
                    contentColor = state.changePinContentColor
                )
            ) {
                Text("CAMBIAR PIN")
            }
        }
    }
}
@Composable
fun PortugalSettingsCustomDialog(
    title: String,
    description: String,
    editTextTypePin: Boolean,
    editTextMaxLength: Int = 4,
    onCancel: () -> Unit,
    onAccept: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = {
            Column {
                Text(description)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { newValue ->
                        text = if (newValue.length <= editTextMaxLength) newValue else newValue.take(editTextMaxLength)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = if (editTextTypePin) {
                        KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    } else {
                        KeyboardOptions.Default
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAccept(text) }) {
                Text("ACEPTAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("CANCELAR")
            }
        }
    )
}
The `PortugalSettingsRoute` above already does this by using:
That is the Compose equivalent of your `repeatOnLifecycle(STARTED)` collectors.
In your original fragment, `checkResetHashData()` shows a toast if `numTicket > 1`. In the sample view model above I left a placeholder effect comment because the effect should ideally be:
data class ShowToast(val messageRes: Int) : PortugalSettingsUiEffect
That is the cleanest Compose approach.
sealed interface PortugalSettingsUiEffect {
    data object NavigateBack : PortugalSettingsUiEffect
    data class ShowToast(val messageRes: Int) : PortugalSettingsUiEffect
    data object OpenCurrentPinDialog : PortugalSettingsUiEffect
    data object OpenNewPinDialog : PortugalSettingsUiEffect
}
Then in your Composable route:
when (effect) {
    PortugalSettingsUiEffect.NavigateBack -> onNavigateBack()
    is PortugalSettingsUiEffect.ShowToast -> onShowToast(effect.messageRes)
    PortugalSettingsUiEffect.OpenCurrentPinDialog -> dialogState = PortugalDialogState.CurrentPin
    PortugalSettingsUiEffect.OpenNewPinDialog -> dialogState = PortugalDialogState.NewPin
}
Example usage:
@Composable
fun PortugalSettingsHost(
    viewModel: PortugalSettingsComposeViewModel,
    navigateBack: () -> Unit,
    showToast: (Int) -> Unit
) {
    PortugalSettingsRoute(
        viewModel = viewModel,
        onNavigateBack = navigateBack,
        onShowToast = showToast
    )
}
1. a version using **Material2** instead of Material3,
2. an implementation that matches your **exact XML colors/paddings/corner radii** if you share the XML files,
3. a **fully hoisted dialog state** version with no `remember` inside the route,
4. a **Koin Compose** injection example for this ViewModel.
