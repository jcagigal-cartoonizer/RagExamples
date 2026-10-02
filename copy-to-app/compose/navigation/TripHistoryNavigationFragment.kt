package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.TripHistoryScreen
import ifac.td.taxi.compose.viewModel.TripHistoryComposeViewModel
import ifac.td.taxi.viewModel.TripHistoryViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 3-1: import android.os.Bundle
class TripHistoryNavigationFragment : Fragment() {
    private val viewModel: TripHistoryViewModel by viewModels()
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
                TripHistoryRoute(
                    viewModel = viewModel,
                    onBackClick = {
                        findNavController().navigate(
                            TripHistoryNavigationFragmentDirections
                                .actionTripHistoryNavigationFragmentToReceiptHistoryFragment(-1)
                        )
                    }
                )
            }
        }
    }
}
@Composable
fun TripHistoryRoute(
    viewModel: TripHistoryViewModel,
    onBackClick: () -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val buttonsState = viewModel.buttonsState.collectAsStateWithLifecycle().value
    TripHistoryScreen(
        uiState = uiState,
        buttonsState = buttonsState,
        onBackClick = onBackClick,
        onAllClick = { viewModel.onAllClicked() },
        onDeleteClick = { viewModel.onDeleteClicked() },
        onTripClick = { trip: Trip -> viewModel.onTripClicked(trip) },
        onDialogDismiss = { viewModel.onDialogDismiss() },
        onDialogAccept = { viewModel.onDialogAccept() },
        onLoadMore = { viewModel.onLoadMore() },
    )
}
