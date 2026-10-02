package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ZoningServicesScreen
import ifac.td.taxi.compose.viewModel.ZoningServicesComposeViewModel
import ifac.td.taxi.viewModel.ZoningServicesViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class ZoningServicesNavigationFragment : androidx.fragment.app.Fragment() {
    private val args: ZoningServicesNavigationFragmentArgs by navArgs()
    private val viewModel: ZoningServicesComposeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize VM with XML navigation args
        viewModel.initViewModelData(args.idMacroZone, args.idZone)
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
                ZoningServicesScreen(
                    navController = findNavController(),
                    viewModel = viewModel,
                    onNavigateBack = { findNavController().navigateUp() },
                    onNavigateToHome = {
                        findNavController().navigate(
                            R.id.goToHomeFragment
                        )
                    },
                    onNavigateToOnTrip = {
                        findNavController().navigate(
                            R.id.goToOnTripFragment
                        )
                    }
                )
            }
        }
    }
}
