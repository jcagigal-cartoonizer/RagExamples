package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.RequestStandReinforcementScreen
import ifac.td.taxi.compose.viewModel.RequestStandReinforcementComposeViewModel
import ifac.td.taxi.viewModel.RequestStandReinforcementViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 5-1: import android.os.Bundle
class RequestStandReinforcementNavigationFragment : Fragment() {
    private val viewModel: RequestStandReinforcementViewModel by viewModels()
    private val safeArgs: RequestStandReinforcementNavigationFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If your ViewModel needs the args to initialize state, do it here or in onViewCreated.
        viewModel.onScreenStarted(
            idMacrozone = safeArgs.idMacrozone,
            idZone = safeArgs.idZone,
            isInFavourites = safeArgs.isInFavourites
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.onScreenStopped()
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
                RequestStandReinforcementScreen(
                    uiState = viewModel.uiState,
                    onAction = ::handleAction
                )
            }
        }
    }
    fun handleAction(action: RequestStandReinforcementAction) {
        when (action) {
            RequestStandReinforcementAction.CancelClicked -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            RequestStandReinforcementAction.AddFavouriteClicked -> {
                viewModel.addZoneToFavourites(safeArgs.idMacrozone, safeArgs.idZone)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            RequestStandReinforcementAction.RemoveFavouriteClicked -> {
                viewModel.removeZoneFromFavourites(safeArgs.idMacrozone, safeArgs.idZone)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            is RequestStandReinforcementAction.ReinforcementClicked -> {
                viewModel.sendReinforcementRequest(
                    safeArgs.idMacrozone,
                    safeArgs.idZone,
                    action.number
                )
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            RequestStandReinforcementAction.DialogConfirmed -> {
                viewModel.onDialogConfirmed()
            }
            RequestStandReinforcementAction.DialogDismissed -> {
                viewModel.onDialogDismissed()
            }
        }
    }
}
`by viewModels()` creates the ViewModel with the default `Fragment` factory.
So this works if your `RequestStandReinforcementViewModel`:
If `uiState` is not a plain property, use Compose collection inside `setContent`, for example:
setContent {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    RequestStandReinforcementScreen(
        uiState = uiState,
        onAction = ::handleAction
    )
}
