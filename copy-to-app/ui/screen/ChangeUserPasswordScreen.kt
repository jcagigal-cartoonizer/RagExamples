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
import ifac.td.taxi.ui.screen.ChangeUserPasswordScreen
// # Block 285-5: import androidx.compose.foundation.layout.Arrangement
@Composable
fun ChangeUserPasswordScreen(
    viewModel: ChangeUserPasswordComposeViewModel,
    navigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var dialogModel by remember { mutableStateOf<ChangeUserPasswordDialogModel?>(null) }
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner.lifecycle) {
        viewModel.uiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                when (effect) {
                    is ChangeUserPasswordUiEffect.ShowDialog -> dialogModel = effect.model
                    ChangeUserPasswordUiEffect.NavigateBack -> navigateBack()
                }
            }
    }
    Scaffold { padding ->
        val buttonsState = remember {
            ChangeUserPasswordButtonsState()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Change user password",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = uiState.actualPassword,
                onValueChange = viewModel::onActualPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Current password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.newPassword,
                onValueChange = viewModel::onNewPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.repeatPassword,
                onValueChange = viewModel::onRepeatPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Repeat new password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (buttonsState.acceptVisible) {
                Button(
                    onClick = viewModel::onAcceptClicked,
                    enabled = buttonsState.acceptEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ACCEPT")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (buttonsState.cancelVisible) {
                OutlinedButton(
                    onClick = viewModel::onCancelClicked,
                    enabled = buttonsState.cancelEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CANCEL")
                }
            }
        }
        dialogModel?.let { model ->
            ChangeUserPasswordScreenChangeUserPasswordCustomDialog(
                model = model,
                onDismissRequest = { dialogModel = null },
                onAccept = {
                    dialogModel = null
                    if (model.title == "Password changed successfully") {
                        navigateBack()
                    }
                }
            )
        }
    }
}
Your fragment had:
In Compose, preserve that behavior by:
Example:
class ChangeUserPasswordFragment : Fragment() {
    private val vModel: ChangeUserPasswordViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ChangeUserPasswordScreen(
                    viewModel = vModel,
                    navigateBack = { (activity as? MainActivity)?.navigateBack() }
                )
            }
        }
    }
}
Old fragment logic:
New Compose logic:
