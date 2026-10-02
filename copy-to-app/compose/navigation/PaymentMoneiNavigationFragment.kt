package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.PaymentMoneiScreen
import ifac.td.taxi.compose.viewModel.PaymentMoneiComposeViewModel
import ifac.td.taxi.viewModel.PaymentMoneiViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 5-1: import android.os.Bundle
class PaymentMoneiNavigationFragment : Fragment() {
    // Compose ViewModel for this screen
    private val viewModel: PaymentMoneiComposeViewModel by viewModel()
    // Shared activity VM
    private val sharedViewModel: MainActivityViewModel by activityViewModel()
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
                TaxiTheme {
                    PaymentMoneiScreen(
                        viewModel = viewModel,
                        sharedViewModel = sharedViewModel,
                        onNavigateBack = {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        },
                        onNavigateHome = {
                            // If you have NavController available from activity/host, navigate here.
                            // Otherwise just go back for now.
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        },
                        onPrintTicket = { ticketBody ->
                            // Delegate printing to activity/shared VM logic if needed
                            sharedViewModel.printTicket(ticketBody)
                        },
                        showHeader = { visible ->
                            // If your activity exposes a header API, call it here.
                            // Example:
                            // (activity as? YourMainActivity)?.showHeader(visible)
                        }
                    )
                }
            }
        }
    }
}
That means the Fragment should ideally use the standard Fragment delegate:
private val viewModel: PaymentMoneiComposeViewModel by viewModels()
However, your `PaymentMoneiComposeViewModel` likely comes from **Koin**, and for Koin-backed fragments the correct delegate is usually:
private val viewModel: PaymentMoneiComposeViewModel by viewModel()
Here is the AndroidX version:
// # Block 76-2: import android.os.Bundle
class PaymentMoneiNavigationFragment : Fragment() {
    private val viewModel: PaymentMoneiComposeViewModel by viewModels()
    private val sharedViewModel: MainActivityViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )
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
                TaxiTheme {
                    PaymentMoneiScreen(
                        viewModel = viewModel,
                        sharedViewModel = sharedViewModel,
                        onNavigateBack = {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        },
                        onNavigateHome = {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        },
                        onPrintTicket = { ticketBody ->
                            sharedViewModel.printTicket(ticketBody)
                        },
                        showHeader = { visible ->
                            // hook to your activity UI if needed
                        }
                    )
                }
            }
        }
    }
}
