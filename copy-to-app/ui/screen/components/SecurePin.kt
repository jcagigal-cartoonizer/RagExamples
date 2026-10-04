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
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.WelcomeScreen
// # Block 706-6: import androidx.compose.foundation.background
sealed interface WelcomeDialogState {
    data object Exit : WelcomeDialogState
    data object BatteryOptimization : WelcomeDialogState
    data class SecurePin(val destination: WelcomeUiEffect.SecurePinDestination) : WelcomeDialogState
}
@Composable
fun WelcomeDialog(
    state: WelcomeDialogState,
    onDismiss: () -> Unit,
    onAccept: (String?) -> Unit,
) {
    var pin by remember(state) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (state) {
                    WelcomeDialogState.Exit -> "Close app"
                    WelcomeDialogState.BatteryOptimization -> "Battery optimization"
                    is WelcomeDialogState.SecurePin -> "Secure PIN"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (state) {
                    WelcomeDialogState.Exit -> {
                        Text("Do you want to exit the app?")
                    }
                    WelcomeDialogState.BatteryOptimization -> {
                        Text("To continue, disable battery optimizations.")
                    }
                    is WelcomeDialogState.SecurePin -> {
                        Text("Enter the current PIN to continue.")
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { pin = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            label = { Text("PIN") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAccept(pin.ifBlank { null }) }) {
                Text("ACCEPT")
            }
        },
        dismissButton = {
            if (state != WelcomeDialogState.BatteryOptimization) {
                TextButton(onClick = onDismiss) { Text("CANCEL") }
            }
        }
    )
}
In Compose, you can handle that in your host with either:
Example:
when (effect) {
    is WelcomeUiEffect.Navigate -> {
        when (val target = effect.directions) {
            is Int -> navController.navigate(target)
            else -> navController.navigate(target)
        }
    }
}
To fully match the fragment behavior, your host should still provide:
For example:
WelcomeRoute(
    navController = navController,
    viewModel = welcomeViewModel,
    showToast = { resId -> Toast.makeText(context, resId, Toast.LENGTH_SHORT).show() },
    showHeader = { visible -> /* activity.showHeader(visible) */ },
    exitApp = { activity.finishAffinity() },
    onBackPressed = { activity.onBackPressedDispatcher.onBackPressed() }
)
A few things from the original Fragment are hard to reproduce 1:1 without the XML/custom view code:
But the code above gives you a **complete Compose architecture** matching the original behavior, and the state holders are structured so you can plug in the remaining style details easily.
1. a more exact **Material3 design system** for `CustomButton`,
2. a **full Compose version of the top bar**,
3. a **permission launcher implementation** using `rememberLauncherForActivityResult`,
4. and a **one-to-one WelcomeSecurePin(valdestinationCustomDialog layout** using Compose `Surface` + `Checkbox` + link text + PIN field.
