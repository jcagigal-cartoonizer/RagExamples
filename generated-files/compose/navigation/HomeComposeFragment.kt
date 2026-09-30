package ifac.td.taxi.compose.navigation
import ifac.td.taxi.compose.viewmodel.HomeComposeViewModel
import ifac.td.taxi.ui.screen.HomeScreen
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import android.view.View

class HomeComposeFragment : Fragment() {

    private val viewModel: HomeComposeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateBack = { parentFragmentManager.popBackStack() }
                )
            }
        }
    }
}

