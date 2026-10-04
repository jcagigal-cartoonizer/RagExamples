package ifac.td.taxi.compose.navigation
import ifac.td.taxi.compose.viewmodel.ChangeDriverPinComposeViewModel
import ifac.td.taxi.ui.screen.ChangeDriverPinScreen
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.parentFragmentManager

class ChangeDriverPinComposeFragment : Fragment() {

    private val viewModel: ChangeDriverPinComposeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ChangeDriverPinScreen(
                    viewModel = viewModel,
                    onNavigateBack = { parentFragmentManager.popBackStack() }
                )
            }
        }
    }
}
