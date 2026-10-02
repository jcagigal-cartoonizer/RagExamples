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
