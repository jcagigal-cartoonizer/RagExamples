package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.WelcomeScreen
import ifac.td.taxi.compose.viewModel.WelcomeComposeViewModel
import ifac.td.taxi.viewModel.WelcomeViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class WelcomeNavigationFragment : Fragment() {
    private val viewModel: WelcomeViewModel by viewModels()
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
                val uiState by viewModel.uiState.collectAsState()
                WelcomeScreen(
                    uiState = uiState,
                    onAction = { action ->
                        when (action) {
                            WelcomeUiAction.StartPressed -> {
                                viewModel.onStartPressed()
                            }
                            WelcomeUiAction.SettingsPressed -> {
                                viewModel.onSettingsPressed()
                                findNavController().navigate(
                                    R.id.action_welcomeFragment_to_settingsFragment
                                )
                            }
                            WelcomeUiAction.ShiftsPressed -> {
                                viewModel.onShiftsPressed()
                                findNavController().navigate(
                                    R.id.action_welcomeFragment_to_shiftsFragment
                                )
                            }
                            WelcomeUiAction.StatisticsPressed -> {
                                viewModel.onStatisticsPressed()
                                findNavController().navigate(
                                    R.id.action_welcomeFragment_to_statisticsFragment
                                )
                            }
                            WelcomeUiAction.ConfigurationPressed -> {
                                viewModel.onConfigurationPressed()
                                findNavController().navigate(
                                    R.id.action_welcomeFragment_to_loginUserFragment
                                )
                            }
                            WelcomeUiAction.PermissionsPressed -> {
                                viewModel.onPermissionsPressed()
                                findNavController().navigate(
                                    R.id.action_welcomeFragment_to_permissionsFragment
                                )
                            }
                            WelcomeUiAction.PartialsPressed -> {
                                viewModel.onPartialsPressed()
                                findNavController().navigate(
                                    R.id.action_welcomeFragment_to_partials
                                )
                            }
                            WelcomeUiAction.ExitPressed -> {
                                viewModel.onExitPressed()
                                requireActivity().finish()
                            }
                        }
                    }
                )
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onScreenResumed()
    }
}
