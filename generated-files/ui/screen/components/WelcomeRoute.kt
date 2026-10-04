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
// # Block 6-1: import android.content.ActivityNotFoundException
@Composable
fun WelcomeRoute(
    navController: NavController,
    viewModel: WelcomeComposeViewModel,
    showToast: (Int) -> Unit = {},
    showHeader: (Boolean) -> Unit = {},
    exitApp: () -> Unit = {},
    onBackPressed: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<WelcomeDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WelcomeUiEffect.Navigate -> {
                    navController.navigate(effect.directions)
                }
                WelcomeUiEffect.NavigateBack -> onBackPressed()
                WelcomeUiEffect.ExitApp -> exitApp()
                is WelcomeUiEffect.Toast -> showToast(effect.messageRes)
                WelcomeUiEffect.RequestPermissions -> {
                    // Hook this to your permissions launcher in the Activity/host
                }
                WelcomeUiEffect.OpenBatteryOptimizationSettings -> {
                    openBatteryOptimizationSettings(context)
                }
                WelcomeUiEffect.OpenPrivacyPolicy -> {
                    openUri(context, effect.url)
                }
                is WelcomeUiEffect.ShowSecurePinDialog -> {
                    dialogState = WelcomeDialogState.SecurePin(effect.destination)
                }
                WelcomeUiEffect.ShowExitDialog -> {
                    dialogState = WelcomeDialogState.Exit
                }
                WelcomeUiEffect.ShowBatteryOptimizationDialog -> {
                    dialogState = WelcomeDialogState.BatteryOptimization
                }
                WelcomeUiEffect.ClearDialog -> {
                    dialogState = null
                }
            }
        }
    }
    LifecycleStartEffect(Unit) {
        showHeader(true)
        viewModel.onScreenStarted()
        onStopOrDispose { }
    }
    WelcomeScreen(
        uiState = uiState,
        onAction = viewModel::onUiAction,
    )
    dialogState?.let { state ->
        WelcomeDialog(
            state = state,
            onDismiss = { dialogState = null },
            onAccept = { input ->
                when (state) {
                    is WelcomeDialogState.Exit -> viewModel.onUiAction(
                        ifac.td.taxi.viewmodel.WelcomeUiAction.ConfirmExit
                    )
                    is WelcomeDialogState.BatteryOptimization -> viewModel.onUiAction(
                        ifac.td.taxi.viewmodel.WelcomeUiAction.ConfirmBatteryOptimization
                    )
                    is WelcomeDialogState.SecurePin -> viewModel.onUiAction(
                        ifac.td.taxi.viewmodel.WelcomeUiAction.SubmitSecurePin(
                            pin = input.orEmpty(),
                            destination = state.destination
                        )
                    )
                }
                dialogState = null
            }
        )
    }
}
fun openUri(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // handle in host/toast
    }
}
fun openBatteryOptimizationSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
