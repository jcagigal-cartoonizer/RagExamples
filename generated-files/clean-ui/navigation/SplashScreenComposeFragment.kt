package ifac.td.taxi.compose.navigation
import ifac.td.taxi.compose.viewmodel.SplashScreenComposeViewModel
import ifac.td.taxi.ui.screen.SplashScreenScreen
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.parentFragmentManager

class SplashScreenComposeFragment : Fragment() {

    private val viewModel: SplashScreenComposeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SplashScreenScreen(
                    viewModel = viewModel,
                    onNavigateBack = { parentFragmentManager.popBackStack() }
                )
            }
        }
    }
}
