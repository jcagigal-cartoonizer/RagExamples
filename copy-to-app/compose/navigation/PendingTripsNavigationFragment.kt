package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.PendingTripsScreen
import ifac.td.taxi.compose.viewModel.PendingTripsComposeViewModel
import ifac.td.taxi.viewModel.PendingTripsViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class PendingTripsNavigationFragment : Fragment() {
    private val viewModel: PendingTripsComposeViewModel by viewModels()
    private val sharedViewModel: MainActivityViewModel by viewModels(ownerProducer = { requireActivity() })
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
                PendingTripsScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        // Same behavior as old fragment: go back and reset manual zoning nav if needed
                        sharedViewModel.emitManualZoningNavigation(false)
                        parentFragmentManager.popBackStack()
                    },
                    onShowHeader = { show ->
                        // If your activity exposes this, call it here.
                        // Example:
                        // (activity as? YourMainActivity)?.showHeader(show)
                    }
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.onScreenStarted()
    }
    override fun onPause() {
        viewModel.onScreenStopped()
        super.onPause()
    }
}
Your old `PendingTripsFragment` did a few extra things:
// # Block 61-2: import android.os.Bundle
class PendingTripsNavigationFragment : Fragment() {
    private val viewModel: PendingTripsComposeViewModel by viewModels()
    private val sharedViewModel: MainActivityViewModel by viewModels(ownerProducer = { requireActivity() })
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
                PendingTripsScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        sharedViewModel.emitManualZoningNavigation(false)
                        parentFragmentManager.popBackStack()
                    },
                    onShowHeader = { show ->
                        // hook to activity if needed
                    }
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.checkAcceptDialogPermission()
        viewModel.onScreenStarted()
    }
    override fun onPause() {
        viewModel.onScreenStopped()
        sharedViewModel.finishPendingTripsTimer()
        super.onPause()
    }
}
