package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.MessageDetailScreen
import ifac.td.taxi.compose.viewModel.MessageDetailComposeViewModel
import ifac.td.taxi.viewModel.MessageDetailViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class MessageDetailNavigationFragment : Fragment() {
    private val args: MessageDetailNavigationFragmentArgs by navArgs()
    private val viewModel: MessageDetailViewModel by viewModels()
    private val sharedViewModel: MainActivityViewModel by viewModels(ownerProducer = { requireActivity() })
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MaterialTheme {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    MessageDetailScreen(
                        uiState = uiState,
                        ticketContent = viewModel.ticketContent, // or from your state/flow
                        onAnswer = {
                            viewModel.stopAutoCloseMessage()
                            viewModel.onAnswerClicked()
                        },
                        onPrint = {
                            viewModel.stopAutoCloseMessage()
                            viewModel.printMessage(args.messageId)
                        },
                        onDelete = {
                            viewModel.stopAutoCloseMessage()
                            viewModel.requestDeleteMessage()
                        },
                        onAccept = {
                            viewModel.stopAutoCloseMessage()
                            viewModel.requestNavigateBack()
                        },
                        onPredefined = {
                            viewModel.stopAutoCloseMessage()
                            viewModel.openPredefinedMessages()
                        },
                        onNewMessage = {
                            viewModel.stopAutoCloseMessage()
                            viewModel.openNewMessageDialog()
                        },
                        onDialogResult = { result ->
                            viewModel.onDialogResult(result)
                        }
                    )
                }
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initVM(args.messageId, args.skipAutoClose)
        lifecycleScope.launch {
            viewModel.closeMessagesFlow.collectLatest { shouldClose ->
                if (shouldClose == true) {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }
}
