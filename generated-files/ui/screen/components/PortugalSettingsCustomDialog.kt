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
// # Block 429-6: import androidx.compose.foundation.layout.*
@Composable
fun PortugalSettingsCustomDialogCustomDialog(
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
