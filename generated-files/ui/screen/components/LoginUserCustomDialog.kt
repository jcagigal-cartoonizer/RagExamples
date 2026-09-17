package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// // // # Block 5: import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
@Composable
fun LoginUserLoginUserCustomDialog(
    title: String,
    description: String,
    pinMode: Boolean,
    maxLength: Int,
    onDismiss: () -> Unit,
    onAccept: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(description)
                OutlinedTextField(
                    value = text,
                    onValueChange = { newValue ->
                        text = newValue.take(maxLength)
                    },
                    singleLine = true,
                    visualTransformation = if (pinMode) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = if (pinMode) {
                        androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        )
                    } else {
                        androidx.compose.foundation.text.KeyboardOptions.Default
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAccept(text) }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
> If your original `custom_dialog.xml` has more fields/styling, you can extend this with icons, custom spacing, and error text in the same pattern.
// # 6) How to preserve Jetpack Navigation behavior
In the fragment you had:
navigateBack()
navigateToChangePasswordFragment()
In Compose, preserve that with `NavController`:
If you are using the same navigation graph, keep the same action id.
// # 7) Lifecycle-safe collection pattern for dialog/state/effects
