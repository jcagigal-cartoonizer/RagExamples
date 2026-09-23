package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.MessagesButtonsState
import ifac.td.taxi.ui.screen.components.MessagesUiEvent
import ifac.td.taxi.ui.screen.components.MessagesUiEffect
import ifac.td.taxi.compose.viewmodel.MessagesComposeViewModel
import ifac.td.taxi.ui.screen.components.MessagesScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
