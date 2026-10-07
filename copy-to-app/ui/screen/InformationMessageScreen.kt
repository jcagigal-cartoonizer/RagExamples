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
import ifac.td.taxi.ui.screen.InformationMessageScreen
// # Block 14-1: import androidx.activity.compose.BackHandler
@Composable
fun InformationMessageScreen(
    viewModel: InformationMessageComposeViewModel,
    onBack: () -> Unit,
    onNavigateBackAfterSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // One-off effects: show dialog, navigate, toast, etc.
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                InformationMessageUiEffect.NavigateBack -> onBack()
                InformationMessageUiEffect.NavigateBackAfterSend -> onNavigateBackAfterSend()
                is InformationMessageUiEffect.ShowToast -> {
                    // Hook your own toast mechanism here if needed
                }
            }
        }
    }
    val buttonsState = remember(uiState) {
        InformationMessageButtonsState.from(uiState)
    }
    BackHandler(enabled = true) {
        viewModel.onCancelPressed()
    }
    // Initial load, equivalent to initVM()
    LaunchedEffect(Unit) {
        viewModel.initVM()
    }
    // Observe dialog state via state holder
    uiState.selectedInformationMessage?.let { selected ->
        if (uiState.dialogVisible) {
            InformationMessageDialog(
                title = uiState.dialogTitle,
                description = selected.second,
                buttonsState = buttonsState.dialogButtons,
                onDismiss = viewModel::onDialogDismiss,
                onButtonClick = { type ->
                    viewModel.onDialogButtonPressed(type, selected.first)
                }
            )
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // XML had hidden header, so typically no top bar here.
        },
        bottomBar = {
            InformationMessageBottomBar(
                buttonsState = buttonsState,
                onCancel = viewModel::onCancelPressed
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(uiState.informationMessages) { index, message ->
                InformationMessageRow(
                    index = index,
                    message = message,
                    onClick = { viewModel.onMessageSelected(index, message) }
                )
            }
        }
    }
}
@Composable
fun InformationMessageRow(
    index: Int,
    message: String,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
