package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.PortugalInvoiceScreen
import ifac.td.taxi.compose.viewModel.PortugalInvoiceComposeViewModel
import ifac.td.taxi.viewModel.PortugalInvoiceViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 3-1: import android.os.Bundle
class PortugalInvoiceNavigationFragment : Fragment() {
    private val viewModel: PortugalInvoiceComposeViewModel by viewModels()
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
                MaterialTheme {
                    PortugalInvoiceScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        },
                        onNavigateToReceiptHistory = { tripId ->
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToReceiptHistoryFragment()
                            // If you need tripId in ReceiptHistoryFragment, use the proper generated action/args.
                            parentFragmentManager.fragments // no-op placeholder
                        },
                        showHeader = false
                    )
                }
            }
        }
    }
}
// # Block 50-2: import android.os.Bundle
class PortugalInvoiceNavigationFragment : Fragment() {
    private val viewModel: PortugalInvoiceComposeViewModel by viewModels()
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
                MaterialTheme {
                    PortugalInvoiceScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            findNavController().navigateUp()
                        },
                        onNavigateToReceiptHistory = { tripId ->
                            // Use your real generated action if ReceiptHistoryFragment expects args.
                            findNavController().navigate(
                                R.id.action_portugalInvoiceFragment_to_receiptHistoryFragment
                            )
                        },
                        showHeader = false
                    )
                }
            }
        }
    }
}
