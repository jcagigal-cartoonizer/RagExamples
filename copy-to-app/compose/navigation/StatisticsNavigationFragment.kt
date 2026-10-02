package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.StatisticsScreen
import ifac.td.taxi.compose.viewModel.StatisticsComposeViewModel
import ifac.td.taxi.viewModel.StatisticsViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class StatisticsNavigationFragment : BaseFragment<View, StatisticsComposeViewModel>() {
    private val viewModel: StatisticsComposeViewModel by viewModels()
    override fun getViewModel(): StatisticsComposeViewModel = viewModel
    override fun getViewBinding() = null
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
                StatisticsScreen(
                    navController = findNavController(),
                    viewModel = viewModel
                )
            }
        }
    }
}
Your existing `StatisticsFragment` extends a custom `BaseFragment<FragmentStatisticsBinding, StatisticsViewModel>`.  
If that base class is mandatory and expects a binding, then for a Compose-only screen it is usually better to **not use that base class** and instead use a plain `Fragment`.
So the recommended version is this simpler one:
// # Block 46-2: import android.os.Bundle
class StatisticsNavigationFragment : Fragment() {
    private val viewModel: StatisticsComposeViewModel by viewModels()
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
                StatisticsScreen(
                    navController = findNavController(),
                    viewModel = viewModel
                )
            }
        }
    }
}
