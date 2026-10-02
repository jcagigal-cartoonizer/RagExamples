package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ChangeDriverPinScreen
import ifac.td.taxi.compose.viewModel.ChangeDriverPinComposeViewModel
import ifac.td.taxi.viewModel.ChangeDriverPinViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class ChangeDriverPinNavigationFragment : Fragment() {
    private val viewModel: ChangeDriverPinComposeViewModel by viewModels()
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
                ChangeDriverPinScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        findNavController().navigateUp()
                    },
                    onShowToast = { message ->
                        // If you already have an app-level toast system, hook it here.
                        // For now this is left empty intentionally.
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
Replace the destination:
<fragment
    android:id="@+id/changeDriverPinFragment"
    android:name="ifac.td.taxi.ui.screen.ChangeDriverPinNavigationFragment"
    android:label="fragment_change_driver_pin"
    tools:layout="@layout/fragment_change_driver_pin" />
So now the graph points to:
android:name="ifac.td.taxi.ui.screen.ChangeDriverPinNavigationFragment"
instead of:
android:name="ifac.td.taxi.ui.screen.ChangeDriverPinFragment"
Your current composable already handles:
So the Fragment above only needs to host it.
// # Block 73-2: import android.os.Bundle
class ChangeDriverPinNavigationFragment : Fragment() {
    private val viewModel: ChangeDriverPinComposeViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // no-op
    }
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            ChangeDriverPinScreen(
                viewModel = viewModel,
                onNavigateBack = { findNavController().navigateUp() },
                onShowToast = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
Your composable uses:
viewModel.uiEffect.collect { effect -> ... }
That is fine, but in a Compose screen it is usually safer to collect with lifecycle awareness, like:
LaunchedEffect(Unit) {
    viewModel.uiEffect.collectLatest { ... }
}
