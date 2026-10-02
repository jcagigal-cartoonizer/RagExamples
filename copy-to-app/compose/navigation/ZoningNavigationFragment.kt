package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ZoningScreen
import ifac.td.taxi.compose.viewModel.ZoningComposeViewModel
import ifac.td.taxi.viewModel.ZoningViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 535-4: import android.os.Bundle
class ZoningNavigationFragment : Fragment() {
    private val viewModel: ZoningComposeViewModel by viewModels()
    private val args: ZoningFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ZoningScreen(
                    navController = findNavController(),
                    viewModel = viewModel
                )
            }
        }
    }
}
