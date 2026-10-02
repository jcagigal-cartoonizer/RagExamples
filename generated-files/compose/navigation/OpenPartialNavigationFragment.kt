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
// # Block 6-1: import android.os.Bundle
class OpenPartialNavigationFragment : Fragment() {
    private val args: OpenPartialNavigationFragmentArgs by navArgs()
    private val viewModel: OpenPartialViewModel by viewModels()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()
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
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    // Equivalent to setupComponents()
                    viewModel.checkClosuresPermission()
                    viewModel.getPartial()
                }
                OpenPartialScreen(
                    state = uiState,
                    onEvent = { event ->
                        when (event) {
                            OpenPartialUiEvent.OnBackClicked -> {
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                            OpenPartialUiEvent.OnPrintClicked -> {
                                val content = uiState.ticketContent
                                if (content.isNotBlank()) {
                                    sharedViewModel.printTicket(content.addTicketLines())
                                }
                            }
                            OpenPartialUiEvent.OnCloseClicked -> {
                                viewModel.closePartials()
                            }
                            OpenPartialUiEvent.OnTotalizersClicked -> {
                                // You can navigate here using your NavController if needed
                                // e.g. findNavController().navigate(...)
                            }
                            OpenPartialUiEvent.OnDialogDismissed -> {
                                viewModel.onDialogDismissed()
                            }
                            OpenPartialUiEvent.OnDialogAccepted -> {
                                viewModel.onDialogAccepted()
                            }
                        }
                    }
                )
            }
        }
    }
}
val uiState: StateFlow<OpenPartialUiState>
If it does not yet, you should adapt it so the screen can observe a single state object.
Your `OpenPartialScreen` can stay mostly the same, but it should work with a state object like this:
@Composable
fun OpenPartialScreen(
    state: OpenPartialUiState,
    onEvent: (OpenPartialUiEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            } else {
                TicketViewer(
                    content = state.ticketContent,
                    modifier = Modifier.weight(1f)
                )
            }
            OpenPartialButtons(
                buttons = state.buttons,
                onEvent = onEvent,
                modifier = Modifier.fillMaxWidth()
            )
        }
        state.dialog?.let { dialog ->
            OpenPartialCustomDialogCustomDialog(
                state = dialog,
                onDismiss = { onEvent(OpenPartialUiEvent.OnDialogDismissed) },
                onAccept = { onEvent(OpenPartialUiEvent.OnDialogAccepted) },
            )
        }
    }
}
@Composable
fun TicketViewer(
    content: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = content)
    }
}
