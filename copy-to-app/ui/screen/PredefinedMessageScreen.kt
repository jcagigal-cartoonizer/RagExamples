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
// # Block 14-1: import androidx.compose.foundation.layout.Arrangement
@Composable
fun PredefinedMessageScreen(
    navController: NavController,
    viewModel: PredefinedMessageComposeViewModel,
    messageId: Int,
    onHeaderVisibleChange: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editableDialogState by remember { mutableStateOf<EditableDialogState?>(null) }
    var predefinedDialogState by remember { mutableStateOf<PredefinedDialogState?>(null) }
    LaunchedEffect(Unit) {
        onHeaderVisibleChange(false)
        viewModel.onEvent(PredefinedMessageUiEvent.Initialize(messageId))
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PredefinedMessageUiEffect.NavigateBack -> navController.popBackStack()
                is PredefinedMessageUiEffect.OpenEditableDialog -> {
                    editableDialogState = EditableDialogState(
                        initialText = effect.initialText,
                        dispatchNumber = effect.dispatchNumber
                    )
                }
                is PredefinedMessageUiEffect.OpenPredefinedDialog -> {
                    predefinedDialogState = PredefinedDialogState(
                        id = effect.id,
                        message = effect.message
                    )
                }
            }
        }
    }
    PredefinedMessageScreenContent(
        state = uiState,
        buttonsState = uiState.buttonsState,
        onEvent = viewModel::onEvent,
        onPredefinedMessageClick = { id, msg ->
            viewModel.onEvent(PredefinedMessageUiEvent.PredefinedMessageClicked(id, msg))
        }
    )
    editableDialogState?.let { dialogState ->
        ComposePredefinedMessageComposeDialogButtonType{PredefinedMessageCustomDialog(
            title = "Write message",
            description = null,
            editText = dialogState.initialText,
            hint = "Message",
            buttons = dialogState.buttonsState,
            onDismiss = { editableDialogState = null },
            onButtonClick = { buttonType, text ->
                when (buttonType) {
                    ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.CANCEL -> editableDialogState = null
                    ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.SEND -> {
                        viewModel.onEvent(
                            PredefinedMessageUiEvent.SendTypedMessage(
                                text.orEmpty(),
                                dialogState.dispatchNumber
                            )
                        )
                        editableDialogState = null
                    }
                    else -> Unit
                }
            }
        )
    }
    predefinedDialogState?.let { dialogState ->
        ComposePredefinedMessageComposeDialogButtonType{PredefinedMessageCustomDialog(
            title = "Predefined message",
            description = dialogState.message,
            editText = null,
            hint = null,
            buttons = listOf(
                ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.EDIT,
                ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.SEND
            ),
            onDismiss = { predefinedDialogState = null },
            onButtonClick = { buttonType, _ ->
                when (buttonType) {
                    ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.SEND -> {
                        viewModel.onEvent(
                            PredefinedMessageUiEvent.SendPredefined(dialogState.id)
                        )
                        predefinedDialogState = null
                    }
                    ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.EDIT -> {
                        editableDialogState = EditableDialogState(
                            initialText = dialogState.message,
                            dispatchNumber = uiState.dispatchNumber
                        )
                        predefinedDialogState = null
                    }
                    else -> Unit
                }
            }
        )
    }
}
@Composable
fun PredefinedMessageScreenContent(
    state: PredefinedMessageUiState,
    buttonsState: PredefinedMessageButtonsState,
    onEvent: (PredefinedMessageUiEvent) -> Unit,
    onPredefinedMessageClick: (Int, String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        PredefinedMessageButtons(
            state = buttonsState,
            onNewMessageClick = { onEvent(PredefinedMessageUiEvent.NewMessageClicked) },
            onCancelClick = { onEvent(PredefinedMessageUiEvent.CancelClicked) }
        )
        if (state.predefinedMessages.isEmpty()) {
            Text(text = "No predefined messages available")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(state.predefinedMessages) { index, item ->
                    PredefinedMessageRow(
                        index = index,
                        message = item,
                        onClick = { onPredefinedMessageClick(index, item) }
                    )
                }
            }
        }
    }
}
@Composable
fun PredefinedMessageRow(
    index: Int,
    message: String,
    onClick: () -> Unit,
) {
    // Replace with your custom row style if needed
    Text(
        text = "${index + 1}. $message",
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
data class EditableDialogState(
    val initialText: String?,
    val dispatchNumber: String?,
    val buttonsState: List<ComposePredefinedMessageComposeDialogButtonType{DialogButtonType> = listOf(
        ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.CANCEL,
        ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.SEND
    )
)
data class PredefinedDialogState(
    val id: Int,
    val message: String
)
