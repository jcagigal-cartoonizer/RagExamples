package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ClosedPartialScreen
import ifac.td.taxi.compose.viewModel.ClosedPartialComposeViewModel
import ifac.td.taxi.viewModel.ClosedPartialViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class ClosedPartialNavigationFragment : Fragment() {
    private val viewModel: ClosedPartialViewModel by viewModels()
    private val args: ClosedPartialNavigationFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If your ViewModel needs the argument, pass it here if you expose an init method.
        // Example:
        // viewModel.setJustClosed(args.justClosed)
        //
        // Or trigger initial loading if your ViewModel supports it:
        // viewModel.onEvent(ClosedPartialUiEvent.Init(args.justClosed))
    }
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
                val uiState by viewModel.uiState.collectAsState()
                ClosedPartialScreen(
                    uiState = uiState,
                    onEvent = { event ->
                        when (event) {
                            ClosedPartialUiEvent.CancelClicked -> {
                                viewModel.onEvent(event)
                                findNavController().navigateUp()
                            }
                            ClosedPartialUiEvent.PrintClicked -> {
                                viewModel.onEvent(event)
                            }
                            ClosedPartialUiEvent.TotalizersClicked -> {
                                viewModel.onEvent(event)
                                findNavController().navigate(
                                    R.id.action_closedPartialFragment_to_totalizersFragment
                                )
                            }
                            ClosedPartialUiEvent.DialogConfirmed -> {
                                viewModel.onEvent(event)
                            }
                            ClosedPartialUiEvent.DialogDismissed -> {
                                viewModel.onEvent(event)
                            }
                        }
                    }
                )
            }
        }
    }
}
Your old fragment was doing:
With Compose, the recommended approach is:
If `justClosed` affects logic, you can pass it to the ViewModel in one of these ways:
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.setJustClosed(args.justClosed)
}
viewModel.onEvent(ClosedPartialUiEvent.Init(args.justClosed))
ClosedPartialUiEvent.DialogConfirmed -> {
    viewModel.onEvent(event)
    findNavController().navigateUp()
}
