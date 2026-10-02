package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.LoginUserRedSysScreen
import ifac.td.taxi.compose.viewModel.LoginUserRedSysComposeViewModel
import ifac.td.taxi.viewModel.LoginUserRedSysViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 6-1: import android.os.Bundle
class LoginUserRedSysNavigationFragment : Fragment() {
    private val viewModel: LoginUserRedSysComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = findNavController()
                LoginUserRedSysScreen(
                    viewModel = viewModel,
                    navController = navController,
                    onBack = {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    },
                    onNavigateToChangePassword = {
                        navController.navigate(
                            R.id.action_loginUserRedSysFragment_to_changePasswordRedSysFragment
                        )
                    }
                )
            }
        }
    }
}
