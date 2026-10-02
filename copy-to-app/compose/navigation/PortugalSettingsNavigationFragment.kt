package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.PortugalSettingsScreen
import ifac.td.taxi.compose.viewModel.PortugalSettingsComposeViewModel
import ifac.td.taxi.viewModel.PortugalSettingsViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class PortugalSettingsNavigationFragment : Fragment() {
    private val viewModel: PortugalSettingsViewModel by viewModels()
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
                var dialogState by androidx.compose.runtime.remember {
                    mutableStateOf<PortugalDialogState?>(null)
                }
                PortugalSettingsScreen(
                    uiState = viewModel.uiState,
                    onEvent = { event ->
                        viewModel.onEvent(event)
                    },
                    dialogState = dialogState,
                    onDismissDialog = {
                        dialogState = null
                    }
                )
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getPortugalData()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.backDataFlow.collect {
                        if (isAdded) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
                launch {
                    viewModel.pinCallbackFlow.collect { pinOk ->
                        if (pinOk) {
                            // If your Compose UI handles the dialog internally,
                            // you can expose a state in the ViewModel instead.
                            // For now this is just a hook point.
                        } else {
                            // Optional: show toast/dialog using Android APIs if needed
                        }
                    }
                }
            }
        }
    }
}
