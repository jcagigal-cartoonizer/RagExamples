package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.OnTripScreen
import ifac.td.taxi.compose.viewModel.OnTripComposeViewModel
import ifac.td.taxi.viewModel.OnTripViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class OnTripNavigationFragment : Fragment() {
    private val viewModel: OnTripComposeViewModel by viewModels()
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
                val navController = findNavController()
                OnTripScreen(
                    navController = navController,
                    viewModel = viewModel,
                    locationAllowedByCentral = requireArguments().getBoolean("location_allowed", true),
                    shiftStatusCurrentStatus = requireArguments().getInt("shiftStatusCurrentStatus", 0).takeIf {
                        it != 0
                    },
                    shiftIsManual = requireArguments().getBoolean("shiftIsManual", false),
                    tripFromDispatch = requireArguments().getBoolean("tripFromDispatch", false),
                    dispatchId = requireArguments().getLong("dispatchId", 0L).takeIf { it != 0L },
                    dispatch = null,
                    roofLight = requireArguments().getBoolean("roofLight", false),
                    hiredZoneExists = requireArguments().getBoolean("hiredZoneExists", false),
                    hasTaximeterConnection = requireArguments().getBoolean("hasTaximeterConnection", false),
                    canDoManualTrips = requireArguments().getBoolean("canDoManualTrips", false),
                    onShowHeader = { /* handled by parent activity if needed */ },
                    onTopBarNextVisible = { /* handled by parent activity if needed */ },
                    onTopBarNextClick = null
                )
            }
        }
    }
}
