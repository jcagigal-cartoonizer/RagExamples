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
// # Block 361-4: import androidx.compose.foundation.layout.*
@Composable
fun InfoDispatchScreen(
    navController: NavController,
    viewModel: InfoDispatchComposeViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<InfoDispatchDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                InfoDispatchUiEffect.NavigateToDirections -> {
                    navController.navigate("directions")
                }
                InfoDispatchUiEffect.NavigateToHome -> {
                    navController.navigate(HomeDirections.goToOnTripFragment().actionId.toString())
                }
                is InfoDispatchUiEffect.NavigateToMeetingSign -> {
                    navController.navigate("meeting_sign/${effect.textColor}/${effect.backgroundColor}")
                }
                InfoDispatchUiEffect.RequestPhonePermission -> {
                    // Host should handle permission request
                }
                is InfoDispatchUiEffect.ShowToast -> {
                    // Host can show Snackbar/Toast
                }
                is InfoDispatchUiEffect.OpenExternalPhoneCall -> {
                    // Host can start ACTION_DIAL/ACTION_CALL
                }
                is InfoDispatchUiEffect.OpenDialog -> dialogState = effect.dialog
                InfoDispatchUiEffect.CloseDialog -> dialogState = null
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            InfoDispatchTopSection(
                state = state,
                onNavigate = viewModel::onNavigateClicked,
                onVoiceCall = viewModel::onVoiceCallClicked,
                onPrint = viewModel::onPrintClicked,
                onNoClient = viewModel::onNoClientClicked,
                onNotification = viewModel::onNotificationClicked
            )
        }
        dialogState?.let { dialog ->
            InfoDispatchCustomDialog(
                dialog = dialog,
                onDismissRequest = { dialogState = null },
                onButtonClicked = { button ->
                    viewModel.onDialogResult(button)
                    dialogState = null
                }
            )
        }
    }
}
@Composable
fun InfoDispatchTopSection(
    state: InfoDispatchUiState,
    onNavigate: () -> Unit,
    onVoiceCall: () -> Unit,
    onPrint: () -> Unit,
    onNoClient: () -> Unit,
    onNotification: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.buttons.notification.let {
            if (it is InfoDispatchButtonUiState.Visible) {
                InfoDispatchActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    state = it
                ) { onNotification() }
            }
        }
        state.buttons.voiceCall.let {
            if (it is InfoDispatchButtonUiState.Visible) {
                InfoDispatchActionButton(Modifier.fillMaxWidth(), it) { onVoiceCall() }
            }
        }
        state.buttons.print.let {
            if (it is InfoDispatchButtonUiState.Visible) {
                InfoDispatchActionButton(Modifier.fillMaxWidth(), it) { onPrint() }
            }
        }
        state.buttons.noClient.let {
            if (it is InfoDispatchButtonUiState.Visible) {
                InfoDispatchActionButton(Modifier.fillMaxWidth(), it) { onNoClient() }
            }
        }
        state.buttons.returnTrip.let {
            if (it is InfoDispatchButtonUiState.Visible) {
                InfoDispatchActionButton(Modifier.fillMaxWidth(), it) { }
            }
        }
        state.buttons.navigate.let {
            if (it is InfoDispatchButtonUiState.Visible) {
                InfoDispatchActionButton(Modifier.fillMaxWidth(), it) { onNavigate() }
            }
        }
    }
}
