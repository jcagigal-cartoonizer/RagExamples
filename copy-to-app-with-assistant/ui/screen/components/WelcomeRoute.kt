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
// # Block 442-3: import android.content.Intent
@Composable
fun WelcomeRoute(
    navController: NavController,
    viewModel: WelcomeComposeViewModel,
    sharedViewModel: MainActivityViewModel,
    onShowHeader: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
    onConnectTaximeter: () -> Unit,
    onExitApp: () -> Unit,
    onRequestPermissions: (Array<String>) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        onShowHeader(true)
        viewModel.onEvent(WelcomeUiEvent.OnStart)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is WelcomeUiEffect.Navigate -> {
                    when (val d = effect.directions) {
                        is Int -> navController.navigate(d)
                        else -> {
                            // If using SafeArgs directions object, call generated navigate extension here.
                        }
                    }
                }
                is WelcomeUiEffect.ShowToast -> onShowToast(effect.messageRes)
                is WelcomeUiEffect.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, effect.url.toUri())
                    context.startActivity(intent)
                }
                WelcomeUiEffect.RequestPermissions -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        onRequestPermissions(
                            arrayOf(
                                ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.PERMISSION_LOCATION,
                                ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.PERMISSION_NOTIFICATION,
                            )
                        )
                    } else {
                        onRequestPermissions(
                            arrayOf(
                                ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.PERMISSION_LOCATION
                            )
                        )
                    }
                }
                WelcomeUiEffect.ConnectTaximeter -> onConnectTaximeter()
                WelcomeUiEffect.ExitApp -> onExitApp()
                is WelcomeUiEffect.ShowWelcomeSharedFlowBridgeCustomDialog -> Unit
            }
        }
    }
    WelcomeScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onInicioClick = {
            if (!viewModel.hasITopMeterPaired()) {
                checkBatteryOptimizationAndLogin(
                    context = context,
                    onContinue = { viewModel.clickMenuLogin() }
                )
            } else {
                viewModel.onEvent(WelcomeUiEvent.OnInicioClicked)
            }
        },
        onAjustesClick = { viewModel.onEvent(WelcomeUiEvent.OnAjustesClicked) },
        onTurnosClick = { viewModel.onEvent(WelcomeUiEvent.OnTurnosClicked) },
        onEstadisticasClick = { viewModel.onEvent(WelcomeUiEvent.OnEstadisticasClicked) },
        onConfiguracionClick = { viewModel.onEvent(WelcomeUiEvent.OnConfiguracionClicked) },
        onPermisosClick = { viewModel.onEvent(WelcomeUiEvent.OnPermisosClicked) },
        onParcialesClick = { viewModel.onEvent(WelcomeUiEvent.OnParcialesClicked) },
        onSalirClick = { viewModel.onEvent(WelcomeUiEvent.OnSalirClicked) },
        onPrivacyAccepted = { viewModel.onEvent(WelcomeUiEvent.OnPrivacyAccepted) },
        onPrivacyDeclined = { viewModel.onEvent(WelcomeUiEvent.OnPrivacyDeclined) },
        onSecurePinConfirmed = { pin -> viewModel.onEvent(WelcomeUiEvent.OnSecurePinConfirmed(pin)) },
        onSecurePinDismissed = { viewModel.onEvent(WelcomeUiEvent.OnSecurePinDismissed) },
        onExitConfirmed = { viewModel.onEvent(WelcomeUiEvent.OnExitConfirmed) },
        onShowPrivacyDialog = { viewModel.onPrivacyDialogNeeded() }
    )
}
