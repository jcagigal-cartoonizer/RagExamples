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
import ifac.td.taxi.ui.screen.ContactCentralScreen
// # Block 377-4: import android.widget.Toast
@Composable
fun ContactCentralScreen(
    navController: NavController,
    viewModel: ContactCentralComposeViewModel,
    onShortBreakPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var showVoiceDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        viewModel.onEvent(ContactCentralUiEvent.ScreenStarted)
    }
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        ContactCentralUiEffect.NavigateBack -> navController.popBackStack()
                        ContactCentralUiEffect.NavigateToPredefinedMessages -> {
                            navController.navigate(R.id.action_contactCentralFragment_to_predefinedMessageFragment)
                        }
                        ContactCentralUiEffect.NavigateToInformationMessages -> {
                            navController.navigate(R.id.action_contactCentralFragment_to_informationMessageFragment)
                        }
                        ContactCentralUiEffect.ShowVoiceRequestDialog -> {
                            showVoiceDialog = true
                            dialogTitle = context.getString(R.string.btn_voice_request)
                        }
                        ContactCentralUiEffect.HideVoiceRequestDialog -> {
                            showVoiceDialog = false
                        }
                        is ContactCentralUiEffect.ShowToast -> {
                            Toast.makeText(context, effect.messageRes, Toast.LENGTH_SHORT).show()
                        }
                        ContactCentralUiEffect.StartShortBreakLoading -> {
                            // You can reflect loading in state if desired; kept here for parity.
                        }
                        ContactCentralUiEffect.RequestShortBreakAction -> {
                            onShortBreakPressed()
                        }
                        is ContactCentralUiEffect.SendVoiceRequest -> Unit
                    }
                }
            }
        }
    }
    val buttons = remember(uiState) { viewModel.buildButtonsState() }
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        ContactCentralButton(
            state = buttons.cancel,
            onClick = { viewModel.onEvent(ContactCentralUiEvent.CancelClicked) }
        )
        Spacer(Modifier.height(12.dp))
        if (buttons.shortBreak.visible) {
            ContactCentralButton(
                state = buttons.shortBreak,
                onClick = { viewModel.onEvent(ContactCentralUiEvent.ShortBreakClicked) }
            )
            Spacer(Modifier.height(12.dp))
        }
        ContactCentralButton(
            state = buttons.voiceCall,
            onClick = { viewModel.onEvent(ContactCentralUiEvent.VoiceCallClicked) }
        )
        Spacer(Modifier.height(12.dp))
        ContactCentralButton(
            state = buttons.messages,
            onClick = { viewModel.onEvent(ContactCentralUiEvent.MessagesClicked) }
        )
        Spacer(Modifier.height(12.dp))
        ContactCentralButton(
            state = buttons.information,
            onClick = { viewModel.onEvent(ContactCentralUiEvent.InformationClicked) }
        )
    }
    if (showVoiceDialog) {
        ContactCentralCustomDialogCustomDialog(
            title = dialogTitle,
            onDismiss = { viewModel.onEvent(ContactCentralUiEvent.DialogCancelled) },
            onCancel = { viewModel.onEvent(ContactCentralUiEvent.DialogCancelled) },
            onAccept = { viewModel.onEvent(ContactCentralUiEvent.DialogAccepted) }
        )
    }
}
