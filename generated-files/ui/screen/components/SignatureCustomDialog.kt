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
// # Block 457-6: import androidx.compose.material3.*
@Composable
fun SignatureButtonDefaultsSignatureCustomDialog(
    state: SignatureDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = state.titleResId)) },
        text = { Text(text = stringResource(id = state.messageResId)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = state.confirmResId))
            }
        },
        dismissButton = {
            if (state.cancelResId != null) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = state.cancelResId))
                }
            }
        }
    )
}
@Composable
fun signatureButtonDefaults(
    enabled: Boolean,
    state: SignatureButtonState,
) = ButtonDefaults.buttonColors(
    containerColor = if (enabled) state.containerColor else state.disabledContainerColor,
    contentColor = if (enabled) state.contentColor else state.disabledContentColor,
    disabledContainerColor = state.disabledContainerColor,
    disabledContentColor = state.disabledContentColor
)
val SignatureButtonShape = RoundedCornerShape(12.dp)
fun signatureButtonHeight() = 52.dp
Example:
navController.navigate(PaymentDirections.goToScannerQRFragment(dispatchId))
That preserves your existing navigation action classes.
composable("signature/{serviceId}") { backStackEntry ->
    val serviceId = backStackEntry.arguments?.getString("serviceId")
    SignatureScreen(
        navController = navController,
        viewModel = signatureComposeViewModel,
        sharedViewModel = mainActivityViewModel,
        serviceId = serviceId,
        onShowHeader = { show -> mainActivityViewModel.showHeader(show) },
        onShowToast = { resId -> /* host activity toast */ }
    )
}
1. a `SignatureRoute(...)` wrapper,
2. Koin `viewModel()` injection in Compose,
3. exact `NavArgs` extraction in Compose,
4. and a more complete `MainActivityViewModel` interop example.
