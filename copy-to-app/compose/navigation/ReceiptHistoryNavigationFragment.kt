package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ReceiptHistoryScreen
import ifac.td.taxi.compose.viewModel.ReceiptHistoryComposeViewModel
import ifac.td.taxi.viewModel.ReceiptHistoryViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 6-1: import android.os.Bundle
class ReceiptHistoryNavigationFragment : Fragment() {
    private val viewModel: ReceiptHistoryViewModel by viewModels()
    private val args: ReceiptHistoryNavigationFragmentArgs by navArgs()
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
                YourAppTheme {
                    ReceiptHistoryNavigationRoute(
                        navController = findNavController(),
                        viewModel = viewModel,
                        ticketId = args.ticketId
                    )
                }
            }
        }
    }
}
@Composable
fun ReceiptHistoryNavigationRoute(
    navController: NavController,
    viewModel: ReceiptHistoryViewModel,
    ticketId: Long
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState by viewModel.buttonsState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    // Trigger screen startup logic
    androidx.compose.runtime.LaunchedEffect(ticketId) {
        viewModel.getTicket(ticketId)
        viewModel.checkVoucherConfiguration()
        viewModel.checkFiscalLicensing()
        viewModel.loadLicensingInvoice()
    }
    // Collect one-shot effects
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ReceiptHistoryUiEffect.NavigateToTripHistory -> {
                    navController.navigate(R.id.action_receiptHistoryFragment_to_tripHistoryFragment)
                }
                ReceiptHistoryUiEffect.NavigateToHome -> {
                    navController.navigate(HomeDirections.goToHomeFragment())
                }
                ReceiptHistoryUiEffect.NavigateToOnTrip -> {
                    navController.navigate(HomeDirections.goToOnTripFragment())
                }
                is ReceiptHistoryUiEffect.NavigateToOnlineInvoice -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections
                            .actionReceiptHistoryFragmentToOnlineInvoiceFragment(effect.tripId)
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToOfflineInvoice -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections
                            .actionReceiptHistoryFragmentToOfflineInvoiceFragment(effect.tripId)
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToRefundMonei -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections
                            .actionReceiptHistoryFragmentToRefundMoneiFragment(effect.tripId)
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToReceiptRedSys -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections
                            .actionReceiptHistoryFragmentToReceiptRedSysFragment(effect.operations)
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToCropImage -> {
                    navController.navigate(
                        HomeDirections.goToCropImageViewFragment(effect.dispatchNumber)
                    )
                }
                is ReceiptHistoryUiEffect.ShareTicketImage -> {
                    // If you want to share from Compose later, expose a callback here.
                }
                is ReceiptHistoryUiEffect.ToastRes -> {
                    // If you want to show a toast, forward via callback or use context here.
                }
                is ReceiptHistoryUiEffect.ShowDialog -> {
                    // If dialog state is already in VM, Compose will render it.
                }
            }
        }
    }
    ReceiptHistoryScreen(
        uiState = uiState,
        buttonsState = buttonsState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent
    )
}
