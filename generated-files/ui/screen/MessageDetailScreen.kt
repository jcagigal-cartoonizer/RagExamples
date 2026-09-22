package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // // import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
// import ifac.td.taxi.repository.room.entities.message.MessageEntity
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.MessageDetailComposeViewModel
import kotlinx.coroutines.flow.collectLatest
@Composable
fun MessageDetailRoute(
    navController: NavController,
    viewModel: MessageDetailComposeViewModel,
    sharedViewModel: MainActivityViewModel,
    messageId: Int,
    skipAutoClose: Boolean,
    onNavigateToPredefinedMessages: (messageId: Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(messageId, skipAutoClose) {
        viewModel.initVM(messageId, skipAutoClose)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is MessageDetailUiEffect.NavigateBack -> navController.popBackStack()
                is MessageDetailUiEffect.OpenPredefinedMessages -> {
                    uiState.message?.let { onNavigateToPredefinedMessages(it.id) }
                }
                is MessageDetailUiEffect.ShowDeleteConfirmDialog -> {}
                is MessageDetailUiEffect.ShowWriteCustomMessageDialog -> {}
                is MessageDetailUiEffect.PrintMessage -> {}
                is MessageDetailUiEffect.DeleteMessage -> {}
                is MessageDetailUiEffect.StopAutoClose -> viewModel.stopAutoCloseMessage()
                is MessageDetailUiEffect.SendMessage -> viewModel.sendMessage(effect.response, effect.dispatchNumber)
            }
        }
    }
    MessageDetailScreen(
        uiState = uiState,
        ticketContent = viewModel.buildTicketContent(uiState.message, context),
        onAnswer = { viewModel.stopAutoCloseMessage(); viewModel.onAnswerClicked() },
        onPrint = { viewModel.stopAutoCloseMessage(); viewModel.onPrintClicked(messageId) },
        onDelete = { viewModel.stopAutoCloseMessage(); viewModel.onDeleteClicked(messageId) },
        onAccept = { viewModel.stopAutoCloseMessage(); viewModel.onAcceptClicked() },
        onPredefined = { viewModel.stopAutoCloseMessage(); viewModel.onPredefinedClicked() },
        onNewMessage = { viewModel.stopAutoCloseMessage(); viewModel.onNewMessageClicked() },
        onDialogResult = { dialogResult ->
            when (dialogResult) {
                is MessageDetailDialogResult.DeleteAccepted -> {
                    viewModel.confirmDelete(messageId)
                    navController.popBackStack()
                }
                is MessageDetailDialogResult.SendCustomMessage -> {
                    viewModel.sendMessage(dialogResult.text, sharedViewModel.dispatchFlow.value?.dispatchNumber)
                }
                else -> Unit
            }
        }
    )
}
// // # Block 365-4: import androidx.compose.foundation.layout.*
// // // import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.style.TextAlign
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
            MessageDetailCustomDialog(
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
// // // import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.TextFieldValue
@Composable
fun MessageDetailMessageDetailCustomDialog(
    state: MessageDetailDialogState,
    onDismiss: () -> Unit,
    onResult: (MessageDetailDialogResult) -> Unit
) {
    var input by remember(state) { mutableStateOf(TextFieldValue("")) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.description?.let { Text(it) }
                state.message?.let { Text(it) }
                state.messageOptions?.let { options ->
                    Column {
                        options.forEach { option ->
                            TextButton(
                                onClick = {
                                    input = TextFieldValue(option)
                                }
                            ) {
                                Text(option)
                            }
                        }
                    }
                }
                if (state.hint != null) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text(state.hint) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            when {
                state.buttons.contains(MessageDetailDialogButtonType.ACCEPT) -> {
                    TextButton(onClick = {
                        onResult(MessageDetailDialogResult.Accept(input.text))
                    }) { Text("Accept") }
                }
                state.buttons.contains(MessageDetailDialogButtonType.SEND) -> {
                    TextButton(onClick = {
                        onResult(MessageDetailDialogResult.SendCustomMessage(input.text))
                    }) { Text("Send") }
                }
            }
        },
        dismissButton = {
            if (state.buttons.contains(MessageDetailDialogButtonType.CANCEL)) {
                TextButton(onClick = { onDismiss() }) { Text("Cancel") }
            }
        }
    )
}
sealed interface MessageDetailDialogResult {
    data object Dismiss : MessageDetailDialogResult
    data class Accept(val text: String) : MessageDetailDialogResult
    data class SendCustomMessage(val text: String) : MessageDetailDialogResult
    data object DeleteAccepted : MessageDetailDialogResult
}
