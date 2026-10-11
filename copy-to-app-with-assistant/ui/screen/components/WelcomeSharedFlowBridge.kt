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
// # Block 1164-9: import androidx.compose.runtime.*
@Composable
fun WelcomeSharedFlowBridge(
    welcomeViewModel: WelcomeComposeViewModel,
    sharedViewModel: WelcomeMainActivityViewModel,
    onButtonsChanged: (WelcomeButtonsAction) -> Unit,
) {
    LaunchedEffect(Unit) {
        sharedViewModel.uiState.collectLatest { sharedState ->
            onButtonsChanged(
                WelcomeButtonsAction.DownloadingBravoConfigurationChanged(
                    downloading = sharedState.downloadingBravoConfiguration,
                    hasITopMeterPaired = welcomeViewModel.hasITopMeterPaired()
                )
            )
            onButtonsChanged(
                WelcomeButtonsAction.LastITopStateReceived(
                    hasITopMeterPaired = welcomeViewModel.hasITopMeterPaired(),
                    taximeterState = sharedState.lastITopStateReceived?.taximeterState
                )
            )
            onButtonsChanged(
                WelcomeButtonsAction.CorrectLoginChanged(
                    success = sharedState.correctLogin.first,
                    hasITopMeterPaired = welcomeViewModel.hasITopMeterPaired(),
                    lastTaximeterState = sharedState.lastITopStateReceived?.taximeterState
                )
            )
        }
    }
}
@Composable
fun WelcomeRoute(
    welcomeViewModel: WelcomeComposeViewModel,
    sharedViewModel: WelcomeMainActivityViewModel,
    navController: NavController,
) {
    val welcomeState by welcomeViewModel.uiState.collectAsStateWithLifecycle()
    val sharedState by sharedViewModel.uiState.collectAsStateWithLifecycle()
    var buttonsState by remember { mutableStateOf(WelcomeButtonsState()) }
    LaunchedEffect(welcomeState, sharedState) {
        var next = welcomeState.buttons
        next = WelcomeButtonsReducer.reduce(
            next,
            WelcomeButtonsAction.HasShiftsChanged(
                hasShifts = welcomeViewModel.hasShiftsCachedValue()
            )
        )
        next = WelcomeButtonsReducer.reduce(
            next,
            WelcomeButtonsAction.PartialButtonChanged(
                enabled = welcomeViewModel.partialButtonCachedValue()
            )
        )
        next = WelcomeButtonsReducer.reduce(
            next,
            WelcomeButtonsAction.UserLoggedInChanged(
                loggedIn = welcomeViewModel.userLoggedInCachedValue()
            )
        )
        next = WelcomeButtonsReducer.reduce(
            next,
            WelcomeButtonsAction.DownloadingBravoConfigurationChanged(
                downloading = sharedState.downloadingBravoConfiguration,
                hasITopMeterPaired = welcomeViewModel.hasITopMeterPaired()
            )
        )
        next = WelcomeButtonsReducer.reduce(
            next,
            WelcomeButtonsAction.HasAllPermissionsChanged(
                hasAllPermissions = welcomeViewModel.hasAllPermissionsCachedValue()
            )
        )
        next = WelcomeButtonsReducer.reduce(
            next,
            WelcomeButtonsAction.LastITopStateReceived(
                hasITopMeterPaired = welcomeViewModel.hasITopMeterPaired(),
                taximeterState = sharedState.lastITopStateReceived?.taximeterState
            )
        )
        next = WelcomeButtonsReducer.reduce(
            next,
            WelcomeButtonsAction.CorrectLoginChanged(
                success = sharedState.correctLogin.first,
                hasITopMeterPaired = welcomeViewModel.hasITopMeterPaired(),
                lastTaximeterState = sharedState.lastITopStateReceived?.taximeterState
            )
        )
        buttonsState = next
    }
    WelcomeScreen(
        uiState = welcomeState.copy(buttons = buttonsState),
        onEvent = welcomeViewModel::onEvent,
        onInicioClick = { welcomeViewModel.onEvent(WelcomeUiEvent.OnInicioClicked) },
        onAjustesClick = { welcomeViewModel.onEvent(WelcomeUiEvent.OnAjustesClicked) },
        onTurnosClick = { welcomeViewModel.onEvent(WelcomeUiEvent.OnTurnosClicked) },
        onEstadisticasClick = { welcomeViewModel.onEvent(WelcomeUiEvent.OnEstadisticasClicked) },
        onConfiguracionClick = { welcomeViewModel.onEvent(WelcomeUiEvent.OnConfiguracionClicked) },
        onPermisosClick = { welcomeViewModel.onEvent(WelcomeUiEvent.OnPermisosClicked) },
        onParcialesClick = { welcomeViewModel.onEvent(WelcomeUiEvent.OnParcialesClicked) },
        onSalirClick = { welcomeViewModel.onEvent(WelcomeUiEvent.OnSalirClicked) },
        onPrivacyAccepted = { welcomeViewModel.onEvent(WelcomeUiEvent.OnPrivacyAccepted) },
        onPrivacyDeclined = { welcomeViewModel.onEvent(WelcomeUiEvent.OnPrivacyDeclined) },
        onSecurePinConfirmed = { pin -> welcomeViewModel.onEvent(WelcomeUiEvent.OnSecurePinConfirmed(pin)) },
        onSecurePinDismissed = { welcomeViewModel.onEvent(WelcomeUiEvent.OnSecurePinDismissed) },
        onExitConfirmed = { welcomeViewModel.onEvent(WelcomeUiEvent.OnExitConfirmed) },
        onShowPrivacyDialog = { welcomeViewModel.onPrivacyDialogNeeded() }
    )
}
In Compose, the exact equivalent is:
That is the Compose-native equivalent of the fragment’s `collect {}` branches.
1. a **fully compilable `WelcomeRoute + WelcomeScreen + WelcomeViewModel` set**, and  
2. a **`MainActivityViewModel` migration adapter** that maps your existing `downloadingBravoConfigurationFlow`, `lastITopStateReceived`, and `correctLoginFlow` into the new Compose bridge without changing the rest of the app.
