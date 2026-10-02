package ifac.td.taxi.compose.navigation
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
