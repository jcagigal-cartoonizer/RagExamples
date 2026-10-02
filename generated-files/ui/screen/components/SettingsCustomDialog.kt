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
import  androidx.compose.ui.window.Dialog
// # Block 578-6: import androidx.compose.foundation.background
@Composable
fun SettingsCustomDialogCustomDialog(
    dialog: SettingsDialogState,
    onDismiss: () -> Unit,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    when (dialog) {
        is SettingsDialogState.Password -> {
            Dialog(onDismissRequest = onDismiss) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 280.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = dialog.title, color = Color.Black)
                    Text(text = dialog.description, color = Color.DarkGray)
                    OutlinedTextField(
                        value = dialog.value,
                        onValueChange = { onEvent(SettingsUiEvent.DialogPasswordChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("CANCEL")
                        }
                        TextButton(
                            onClick = { onEvent(SettingsUiEvent.DialogPasswordConfirmed) }
                        ) {
                            Text("ACCEPT")
                        }
                    }
                }
            }
        }
    }
}
A few behaviors from the legacy fragment are preserved conceptually, but you’ll likely want to adapt them slightly depending on your app architecture:
In the sample above, I used `"PIN actual"` and `"Ingrese el PIN actual"` directly to keep the code self-contained. In your app, switch them to `stringResource(...)` or pass them from the ViewModel as resource IDs to keep localization clean.
1. a fully working **`@HiltViewModel` / Koin** implementation,
2. a **proper permission launcher for Bluetooth and write-settings permissions**,
3. and a **more exact Material 2 / Material 3 styled recreation of `CustomButton` and `SettingsCustomDialogCustomDialog`**.
