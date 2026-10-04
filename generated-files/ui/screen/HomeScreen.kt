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
import ifac.td.taxi.ui.screen.HomeScreen
// # Block 387-3: import android.media.ToneGenerator
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeComposeViewModel,
    sharedVm: MainActivityComposeStateHolder,
    onShowToast: (Int) -> Unit,
    onBeep: (Int) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val runtime by viewModel.runtime.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.onScreenStarted()
        sharedVm.resetDispatchFlow()
        sharedVm.resetCurrentAmountFlow()
    }
    LaunchedEffect(runtime) {
        viewModel.reduceButtons()
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToRes -> navController.navigate(effect.resId)
                is HomeUiEffect.NavigateDeepLink -> navController.navigate(
                    NavDeepLinkRequest.Builder.fromUri(effect.uri.toUri()).build()
                )
                is HomeUiEffect.ShowToast -> onShowToast(effect.resId)
                is HomeUiEffect.Beep -> onBeep(effect.tone)
                HomeUiEffect.ResetMainActivityFlows -> Unit
                HomeUiEffect.OpenPendingTrips -> navController.navigate(R.id.action_homeFragment_to_pendingTripsFragment)
                HomeUiEffect.OpenMessages -> navController.navigate(R.id.action_homeFragment_to_messageFragment)
                HomeUiEffect.OpenReceiptHistory -> navController.navigate(R.id.action_homeFragment_to_receiptHistoryFragment)
                HomeUiEffect.OpenContactCentral -> navController.navigate(R.id.action_homeFragment_to_contactCentralFragment)
                HomeUiEffect.OpenDashboard -> navController.navigate(R.id.action_homeFragment_to_dashboardFragment)
                HomeUiEffect.OpenFixedPrice -> navController.navigate(R.id.action_homeFragment_to_FixedPriceMapFragment)
                HomeUiEffect.LogoffAndGoToWelcome -> {
                    navController.navigate("android-app://ifac.td.taxi/welcome".toUri())
                }
            }
        }
    }
    LaunchedEffect(uiState.isKeepScreenOn) {
        onKeepScreenOn(uiState.isKeepScreenOn)
    }
    Box(Modifier.fillMaxSize()) {
        HomeButtonsContent(
            buttons = uiState.buttons,
            onLocationClick = viewModel::onLocationClicked,
            onZoningClick = viewModel::onZoningClicked,
            onPendingClick = viewModel::onPendingClicked,
            onLocateStandClick = viewModel::onLocateStandClicked,
            onCentralClick = viewModel::onCentralClicked,
            onReceiptsClick = viewModel::onReceiptsClicked,
            onMessagesClick = viewModel::onMessagesClicked,
            onRoofLightClick = viewModel::onRoofLightClicked,
            onDashboardClick = viewModel::onDashboardClicked,
            onFixedPriceClick = viewModel::onFixedPriceClicked,
        )
        uiState.dialog?.let { dialog ->
            HomeMainActivityComposeStateHolderHomeCustomDialog(
                state = dialog,
                onDismiss = viewModel::onDialogDismissed,
                onAccept = { viewModel.onDialogAccepted(dialog.type) }
            )
        }
    }
}
