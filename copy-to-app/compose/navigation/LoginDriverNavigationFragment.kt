package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.LoginDriverScreen
import ifac.td.taxi.compose.viewModel.LoginDriverComposeViewModel
import ifac.td.taxi.viewModel.LoginDriverViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class LoginDriverNavigationFragment : Fragment() {
    private val viewModel: LoginDriverViewModel by viewModels()
    private val args: LoginDriverNavigationFragmentArgs by navArgs()
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
                LoginDriverNavigationRoute(
                    navController = findNavController(),
                    viewModel = viewModel,
                    connectionMode = args.connectionMode,
                    startTurnMode = args.startTurnMode,
                    onNavigateBack = { findNavController().navigateUp() },
                    onNavigateHome = {
                        findNavController().navigate(R.id.home)
                    }
                )
            }
        }
    }
}
