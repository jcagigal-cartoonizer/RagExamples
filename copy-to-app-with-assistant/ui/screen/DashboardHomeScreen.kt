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
import ifac.td.taxi.ui.screen.DashboardScreen
// # Block 1056-12: import androidx.compose.foundation.layout.*
@Composable
fun HomeScreen(
    state: HomeButtonsState,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onOnTrip: () -> Unit,
    onDismissDialog: () -> Unit,
    onTopBarRightClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    when (val right = state.topBarRight) {
                        is TopBarRightState.Next -> {
                            TextButton(onClick = onTopBarRightClick) {
                                Text("Next")
                            }
                        }
                        TopBarRightState.Hidden -> Unit
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Zone: ${state.zoneText ?: "-"}")
            Text("Roof light: ${state.isRoofLightOn ?: false}")
            Text("Location enabled: ${state.locationEnabled.first} / ${state.locationEnabled.second}")
            Text("Short break: ${state.shortBreakStatus ?: "-"}")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onHome) { Text("Go Home") }
            Button(onClick = onOnTrip) { Text("Go On Trip") }
        }
    }
    when (val dialog = state.notificationDialog) {
        is NotificationDashboardDialogState.Hidden -> Unit
        is NotificationDashboardDialogState.SelectNotification -> {
            CustomNotificationDialog(
                buttons = dialog.buttons,
                onDismiss = onDismissDialog,
                onButtonClick = { button ->
                    when (button) {
                        NotificationButtonType.AT_DOOR -> {
                            // sendAtTheDoorNotification(...)
                        }
                        NotificationButtonType.RIDER_IN_CAB -> {
                            // sendInCabNotification(...)
                        }
                        else -> Unit
                    }
                }
            )
        }
    }
}
