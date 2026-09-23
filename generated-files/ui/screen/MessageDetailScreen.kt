package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 365-4: import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
