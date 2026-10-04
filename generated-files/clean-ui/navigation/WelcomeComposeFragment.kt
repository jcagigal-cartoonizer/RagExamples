package ifac.td.taxi.compose.navigation
import ifac.td.taxi.compose.viewmodel.WelcomeComposeViewModel
import ifac.td.taxi.ui.screen.WelcomeScreen
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.parentFragmentManager

class WelcomeComposeFragment : Fragment() {

    private val viewModel: WelcomeComposeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                WelcomeScreen(
                    viewModel = viewModel,
                    onNavigateBack = { parentFragmentManager.popBackStack() }
                )
            }
        }
    }
}
