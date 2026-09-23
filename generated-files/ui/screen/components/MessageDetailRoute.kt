package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.MessageDetailUiEffect
import ifac.td.taxi.compose.viewmodel.MessageDetailComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 294-3: import androidx.compose.foundation.layout.*
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
