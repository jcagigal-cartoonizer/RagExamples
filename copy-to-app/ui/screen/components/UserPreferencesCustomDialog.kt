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
// # Block 710-4: import androidx.compose.foundation.background
@Composable
fun UserPreferencesButtonStylesUserPreferencesCustomDialog(
    dialog: UserPreferencesDialogModel,
    onDismiss: () -> Unit,
    onAction: (UserPreferencesUiEffect.DialogAction) -> Unit,
) {
    var text by remember(dialog.text) { mutableStateOf(dialog.text) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = dialog.title)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                dialog.description?.let { Text(it) }
                if (dialog.showTextField) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(dialog.hint ?: "") },
                        visualTransformation = if (dialog.isPin) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
                    )
                }
            }
        },
        confirmButton = {
            if (dialog.buttons.contains(UserPreferencesDialogButton.Accept)) {
                TextButton(onClick = {
                    onAction(UserPreferencesUiEffect.DialogAction.Accept(text))
                }) {
                    Text("Accept")
                }
            }
        },
        dismissButton = {
            if (dialog.buttons.contains(UserPreferencesDialogButton.Cancel)) {
                TextButton(onClick = {
                    onAction(UserPreferencesUiEffect.DialogAction.Cancel)
                }) {
                    Text("Cancel")
                }
            }
        }
    )
}
