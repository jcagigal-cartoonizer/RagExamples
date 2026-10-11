package ifac.td.taxi.ui.screen
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
@Composable
fun WelcomeScreen(
    uiState: WelcomeUiState,
    onEvent: (WelcomeUiEvent) -> Unit,
    onInicioClick: () -> Unit,
    onAjustesClick: () -> Unit,
    onTurnosClick: () -> Unit,
    onEstadisticasClick: () -> Unit,
    onConfiguracionClick: () -> Unit,
    onPermisosClick: () -> Unit,
    onParcialesClick: () -> Unit,
    onSalirClick: () -> Unit,
    onPrivacyAccepted: () -> Unit,
    onPrivacyDeclined: () -> Unit,
    onSecurePinConfirmed: (String) -> Unit,
    onSecurePinDismissed: () -> Unit,
    onExitConfirmed: () -> Unit,
    onShowPrivacyDialog: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WelcomeButton(
                text = stringResource(R.string.inicio),
                state = uiState.buttons.inicio,
                onClick = onInicioClick
            )
            WelcomeButton(
                text = stringResource(R.string.ajustes),
                state = uiState.buttons.ajustes,
                onClick = onAjustesClick
            )
            WelcomeButton(
                text = stringResource(R.string.turnos),
                state = uiState.buttons.turnos,
                onClick = onTurnosClick
            )
            WelcomeButton(
                text = stringResource(R.string.estadisticas),
                state = uiState.buttons.estadisticas,
                onClick = onEstadisticasClick
            )
            WelcomeButton(
                text = stringResource(R.string.configuracion),
                state = uiState.buttons.configuracion,
                onClick = onConfiguracionClick
            )
            if (uiState.buttons.permisos.visible) {
                WelcomeButton(
                    text = stringResource(R.string.permisos),
                    state = uiState.buttons.permisos,
                    onClick = onPermisosClick
                )
            }
            WelcomeButton(
                text = stringResource(R.string.parciales),
                state = uiState.buttons.parciales,
                onClick = onParcialesClick
            )
            WelcomeButton(
                text = stringResource(R.string.salir),
                state = uiState.buttons.salir,
                onClick = onSalirClick
            )
        }
        if (uiState.showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = onPrivacyDeclined,
                title = { Text(stringResource(R.string.dialog_privacy_title)) },
                text = {
                    Column {
                        Text(stringResource(R.string.dialog_privacy_desc))
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.dialog_privacy_href))
                    }
                },
                confirmButton = {
                    TextButton(onClick = onPrivacyAccepted) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            )
        }
        if (uiState.showExitDialog) {
            AlertDialog(
                onDismissRequest = onEvent,
                title = { Text(stringResource(R.string.close_app)) },
                confirmButton = {
                    TextButton(onClick = onExitConfirmed) {
                        Text(stringResource(android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(WelcomeUiEvent.OnPrivacyDeclined) }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        }
        if (uiState.showSecurePinDialog) {
            SecurePinDialog(
                onConfirm = onSecurePinConfirmed,
                onDismiss = onSecurePinDismissed
            )
        }
    }
}
@Composable
fun WelcomeButton(
    text: String,
    state: WelcomeButtonState,
    onClick: () -> Unit,
) {
    if (!state.visible) return
    val backgroundColor = when (state.background) {
        WelcomeButtonBackground.BLUE -> MaterialTheme.colorScheme.primary
        WelcomeButtonBackground.ORANGE -> MaterialTheme.colorScheme.tertiary
    }
    Button(
        onClick = onClick,
        enabled = state.enabled && !state.loading,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}
@Composable
fun SecurePinDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.user_preferences_btn_secure_pin)) },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text(stringResource(R.string.pin_actual_hint)) }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pin) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
fun checkBatteryOptimizationAndLogin(
    context: android.content.Context,
    onContinue: () -> Unit
) {
    val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as? PowerManager
    val disabled = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
    if (disabled) {
        onContinue()
    } else {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
        }
        onContinue()
    }
}
To preserve the old navigation behavior:
should be handled in the Compose host using `NavController.navigate(...)` or generated SafeArgs directions.
So the reducer should be able to apply each branch independently, without mixing concerns.
// # Block 753-4: import androidx.annotation.StringRes
@Immutable
data class WelcomeUiState(
    val buttons: WelcomeButtonsState = WelcomeButtonsState(),
    val privacyDialog: WelcomeDialogState? = null,
    val exitDialog: WelcomeDialogState? = null,
    val securePinDialog: WelcomeSecurePinWelcomeDialogState? = null,
)
@Immutable
data class WelcomeButtonsState(
    val inicio: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.BLUE,
    ),
    val ajustes: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.BLUE,
    ),
    val turnos: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = false,
        loading = false,
        background = WelcomeButtonBackground.ORANGE,
    ),
    val estadisticas: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.BLUE,
    ),
    val configuracion: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.BLUE,
    ),
    val permisos: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.ORANGE,
    ),
    val parciales: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = false,
        loading = false,
        background = WelcomeButtonBackground.ORANGE,
    ),
    val salir: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.BLUE,
    ),
)
@Immutable
data class WelcomeButtonState(
    val visible: Boolean,
    val enabled: Boolean,
    val loading: Boolean,
    val background: WelcomeButtonBackground,
)
enum class WelcomeButtonBackground {
    BLUE,
    ORANGE
}
