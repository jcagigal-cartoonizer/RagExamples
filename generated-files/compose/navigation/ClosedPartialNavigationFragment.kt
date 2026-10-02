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
// # Block 4-1: import android.os.Bundle
class ClosedPartialNavigationFragment : Fragment() {
    private val viewModel: ClosedPartialViewModel by viewModels()
    private val args: ClosedPartialNavigationFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If your ViewModel needs the argument, pass it here if you expose an init method.
        // Example:
        // viewModel.setJustClosed(args.justClosed)
        //
        // Or trigger initial loading if your ViewModel supports it:
        // viewModel.onEvent(ClosedPartialUiEvent.Init(args.justClosed))
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
                val uiState by viewModel.uiState.collectAsState()
                ClosedPartialScreen(
                    uiState = uiState,
                    onEvent = { event ->
                        when (event) {
                            ClosedPartialUiEvent.CancelClicked -> {
                                viewModel.onEvent(event)
                                findNavController().navigateUp()
                            }
                            ClosedPartialUiEvent.PrintClicked -> {
                                viewModel.onEvent(event)
                            }
                            ClosedPartialUiEvent.TotalizersClicked -> {
                                viewModel.onEvent(event)
                                findNavController().navigate(
                                    R.id.action_closedPartialFragment_to_totalizersFragment
                                )
                            }
                            ClosedPartialUiEvent.DialogConfirmed -> {
                                viewModel.onEvent(event)
                            }
                            ClosedPartialUiEvent.DialogDismissed -> {
                                viewModel.onEvent(event)
                            }
                        }
                    }
                )
            }
        }
    }
}
Your old fragment was doing:
With Compose, the recommended approach is:
If `justClosed` affects logic, you can pass it to the ViewModel in one of these ways:
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.setJustClosed(args.justClosed)
}
viewModel.onEvent(ClosedPartialUiEvent.Init(args.justClosed))
ClosedPartialUiEvent.DialogConfirmed -> {
    viewModel.onEvent(event)
    findNavController().navigateUp()
}
