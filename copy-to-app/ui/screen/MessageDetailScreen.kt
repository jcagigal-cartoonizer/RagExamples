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
import ifac.td.taxi.ui.screen.MessageDetailScreen
// # Block 365-4: import androidx.compose.foundation.layout.*
@Composable
fun MessageDetailScreen(
    uiState: MessageDetailUiState,
    ticketContent: String,
    onAnswer: () -> Unit,
    onPrint: () -> Unit,
    onDelete: () -> Unit,
    onAccept: () -> Unit,
    onPredefined: () -> Unit,
    onNewMessage: () -> Unit,
    onDialogResult: (MessageDetailDialogResult) -> Unit,
) {
    val buttons = remember(uiState) { uiState.toButtonsState() }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = ticketContent,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(24.dp))
            MessageDetailButtons(
                state = buttons,
                onAnswer = onAnswer,
                onPrint = onPrint,
                onDelete = onDelete,
                onAccept = onAccept,
                onPredefined = onPredefined,
                onNewMessage = onNewMessage
            )
        }
        uiState.dialogState?.let { dialogState ->
            MessageDetailComposeFragmentMessageDetailCustomDialog(
                state = dialogState,
                onDismiss = { onDialogResult(MessageDetailDialogResult.Dismiss) },
                onResult = onDialogResult
            )
        }
    }
}
@Composable
fun MessageDetailButtons(
    state: MessageDetailButtonsState,
    onAnswer: () -> Unit,
    onPrint: () -> Unit,
    onDelete: () -> Unit,
    onAccept: () -> Unit,
    onPredefined: () -> Unit,
    onNewMessage: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.answerVisible) StyledMessageButton("Answer", state.answer, onAnswer)
        if (state.printVisible) StyledMessageButton("Print", state.print, onPrint)
        if (state.deleteVisible) StyledMessageButton("Delete", state.delete, onDelete)
        if (state.acceptVisible) StyledMessageButton("Accept", state.accept, onAccept)
        if (state.predefinedVisible) StyledMessageButton("Predefined", state.predefined, onPredefined)
        if (state.newMessageVisible) StyledMessageButton("New message", state.newMessage, onNewMessage)
    }
}
@Composable
fun StyledMessageButton(
    text: String,
    style: MessageDetailButtonStyle,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = style.enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.containerColor,
            contentColor = style.contentColor,
            disabledContainerColor = style.containerColor.copy(alpha = 0.5f),
            disabledContentColor = style.contentColor.copy(alpha = 0.7f),
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
fun MessageDetailUiState.toButtonsState(): MessageDetailButtonsState {
    val answerStyle = if (answerEnabled) {
        MessageDetailButtonStyle.raisedEnabled()
    } else {
        MessageDetailButtonStyle.raisedDisabled()
    }
    return MessageDetailButtonsState(
        answer = answerStyle,
        print = MessageDetailButtonStyle.raisedEnabled(),
        delete = MessageDetailButtonStyle.raisedEnabled(),
        accept = MessageDetailButtonStyle.raisedEnabled(),
        predefined = if (hasPredefinedMessages) MessageDetailButtonStyle.raisedEnabled() else MessageDetailButtonStyle.raisedDisabled(),
        newMessage = MessageDetailButtonStyle.raisedEnabled(),
        answerVisible = isAnswerVisible,
        printVisible = isPrintVisible,
        deleteVisible = true,
        acceptVisible = true,
        predefinedVisible = isPredefinedVisible,
        newMessageVisible = isNewMessageVisible
    )
}
fun MessageDetailButtonStyle.Companion.raisedEnabled() = MessageDetailButtonStyle(
    enabled = true,
    containerColor = MessageDetailButtonColors.enabledContainer,
    contentColor = MessageDetailButtonColors.enabledContent
)
fun MessageDetailButtonStyle.Companion.raisedDisabled() = MessageDetailButtonStyle(
    enabled = false,
    containerColor = MessageDetailButtonColors.disabledContainer,
    contentColor = MessageDetailButtonColors.disabledContent
)
