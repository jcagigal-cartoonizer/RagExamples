package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.PredefinedMessageScreen
import ifac.td.taxi.ui.screen.components.PredefinedMessageCustomDialog
import ifac.td.taxi.ui.screen.components.PredefinedMessageUiEvent
import ifac.td.taxi.ui.screen.components.PredefinedMessageScreenContent
import ifac.td.taxi.compose.viewmodel.PredefinedMessageComposeViewModel
import ifac.td.taxi.ui.screen.components.PredefinedMessageUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
        ComposePredefinedMessageCustomDialog(
            title = "Write message",
            description = null,
            editText = dialogState.initialText,
            hint = "Message",
            buttons = dialogState.buttonsState,
            onDismiss = { editableDialogState = null },
            onButtonClick = { buttonType, text ->
                when (buttonType) {
                    ComposePredefinedMessageDialogButtonType.CANCEL -> editableDialogState = null
                    ComposePredefinedMessageDialogButtonType.SEND -> {
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
        ComposePredefinedMessageCustomDialog(
            title = "Predefined message",
            description = dialogState.message,
            editText = null,
            hint = null,
            buttons = listOf(
                ComposePredefinedMessageDialogButtonType.EDIT,
                ComposePredefinedMessageDialogButtonType.SEND
            ),
            onDismiss = { predefinedDialogState = null },
            onButtonClick = { buttonType, _ ->
                when (buttonType) {
                    ComposePredefinedMessageDialogButtonType.SEND -> {
                        viewModel.onEvent(
                            PredefinedMessageUiEvent.SendPredefined(dialogState.id)
                        )
                        predefinedDialogState = null
                    }
                    ComposePredefinedMessageDialogButtonType.EDIT -> {
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
    val buttonsState: List<ComposePredefinedMessageDialogButtonType> = listOf(
        ComposePredefinedMessageDialogButtonType.CANCEL,
        ComposePredefinedMessageDialogButtonType.SEND
    )
)
data class PredefinedDialogState(
    val id: Int,
    val message: String
)
