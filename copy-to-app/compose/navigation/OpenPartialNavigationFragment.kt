package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.OpenPartialScreen
import ifac.td.taxi.compose.viewModel.OpenPartialComposeViewModel
import ifac.td.taxi.viewModel.OpenPartialViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
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
