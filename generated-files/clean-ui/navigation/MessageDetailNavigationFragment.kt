package ifac.td.taxi.compose.navigation
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
