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
import ifac.td.taxi.ui.screen.MessagesScreen
// # Block 197-3: import androidx.compose.foundation.layout.*
@Composable
fun MessagesScreen(
    viewModel: MessagesComposeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMessageDetail: (messageId: Long, skipAutoClose: Boolean) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.init()
    }
    LaunchedEffect(viewModel) {
        viewModel.uiEffects.collectLatest { effect ->
            when (effect) {
                MessagesUiEffect.NavigateBack -> onNavigateBack()
                is MessagesUiEffect.NavigateToMessageDetailWithArgs ->
                    onNavigateToMessageDetail(effect.messageId, effect.skipAutoClose)
                MessagesUiEffect.ShowMarkAllReadDialog -> Unit
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            MessagesButtons(
                state = MessagesButtonsState.from(uiState),
                onBack = { viewModel.onEvent(MessagesUiEvent.Back) },
                onDelete = { viewModel.onEvent(MessagesUiEvent.DeleteSelected) },
                onSelect = { viewModel.onEvent(MessagesUiEvent.ToggleSelectionMode) },
                onMarkRead = { viewModel.onEvent(MessagesUiEvent.MarkAllReadRequest) }
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp)
            ) {
                items(
                    items = uiState.messages,
                    key = { it.id }
                ) { message ->
                    MessageRow(
                        message = message,
                        isSelected = uiState.selectedMessageIds.contains(message.id),
                        selectionMode = uiState.isSelectionMode,
                        onClick = { viewModel.onEvent(MessagesUiEvent.MessageClicked(message)) },
                        onLongClick = { viewModel.onEvent(MessagesUiEvent.ToggleMessageSelection(message)) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        uiState.dialogState?.let { dialogState ->
            MessagesCustomDialog(
                title = dialogState.title,
                description = dialogState.description,
                buttons = dialogState.buttons,
                onCancel = { viewModel.onEvent(MessagesUiEvent.DismissDialog) },
                onAccept = {
                    viewModel.onEvent(MessagesUiEvent.MarkAllReadConfirmed)
                },
                onDismiss = { viewModel.onEvent(MessagesUiEvent.DismissDialog) }
            )
        }
    }
}
@Composable
fun MessageRow(
    message: MessageEntity,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Message #${message.id}")
            Text(text = message.dateFormatted)
            if (selectionMode) {
                Text(text = if (isSelected) "Selected" else "Not selected")
            }
        }
    }
}
