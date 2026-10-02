package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.FixedPriceScreen
import ifac.td.taxi.compose.viewModel.FixedPriceComposeViewModel
import ifac.td.taxi.viewModel.FixedPriceViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 5-1: import android.os.Bundle
class FixedPriceNavigationFragment : Fragment() {
    private val viewModel: FixedPriceComposeViewModel by viewModels()
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
                FixedPriceScreen(
                    navController = findNavController(),
                    viewModel = viewModel,
                    onBack = { findNavController().popBackStack() },
                    onShowToast = { message ->
                        // If your Activity has a toast helper, you can replace this.
                        // Example:
                        // Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    },
                    onHideKeyboard = {
                        // Optional: if needed, you can hide keyboard here.
                    }
                )
            }
        }
    }
}
