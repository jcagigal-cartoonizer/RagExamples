package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.SecurePinScreen
import ifac.td.taxi.compose.viewModel.SecurePinComposeViewModel
import ifac.td.taxi.viewModel.SecurePinViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class SecurePinNavigationFragment : BaseFragment<View, SecurePinComposeViewModel>(0) {
    private val viewModel: SecurePinComposeViewModel by viewModels()
    override fun getViewModel(): SecurePinComposeViewModel = viewModel
    override fun getViewBinding(): View {
        // Not used because we override onCreateView below
        throw UnsupportedOperationException("ViewBinding is not used in SecurePinNavigationFragment")
    }
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
                SecurePinScreen(
                    navController = findNavController(),
                    viewModel = viewModel,
                    onShowBottomBar = { show ->
                        iMainActivity.showBottomBar(show)
                    },
                    onShowHeader = { show ->
                        iMainActivity.showHeader(show)
                    }
                )
            }
        }
    }
}
Your current `BaseFragment` seems to be built around XML view binding, so the cleanest option is:
Create a dedicated fragment **without** `BaseFragment`, like this:
// # Block 54-2: import android.os.Bundle
class SecurePinNavigationFragment : Fragment() {
    private val viewModel: SecurePinComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                SecurePinScreen(
                    navController = findNavController(),
                    viewModel = viewModel,
                    onShowBottomBar = { show ->
                        (requireActivity() as? YourMainActivityType)?.showBottomBar(show)
                    },
                    onShowHeader = { show ->
                        (requireActivity() as? YourMainActivityType)?.showHeader(show)
                    }
                )
            }
        }
    }
}
Your composable does:
SecurePinUiEffect.NavigateBack -> navController.popBackStack()
So the fragment only needs to provide the `NavController`, which the code above does.
Here is a simpler version with a standard `Fragment`:
// # Block 119-3: import android.os.Bundle
class SecurePinNavigationFragment : Fragment() {
    private val viewModel: SecurePinComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            SecurePinScreen(
                navController = findNavController(),
                viewModel = viewModel,
                onShowBottomBar = { (requireActivity() as YourMainActivityType).showBottomBar(it) },
                onShowHeader = { (requireActivity() as YourMainActivityType).showHeader(it) }
            )
        }
    }
}
