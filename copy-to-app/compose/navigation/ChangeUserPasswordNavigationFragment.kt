package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ChangeUserPasswordScreen
import ifac.td.taxi.compose.viewModel.ChangeUserPasswordComposeViewModel
import ifac.td.taxi.viewModel.ChangeUserPasswordViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 5-1: import android.os.Bundle
class ChangeUserPasswordNavigationFragment : Fragment() {
    private val viewModel: ChangeUserPasswordComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ChangeUserPasswordScreen(
                    viewModel = viewModel,
                    navigateBack = {
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }
}
