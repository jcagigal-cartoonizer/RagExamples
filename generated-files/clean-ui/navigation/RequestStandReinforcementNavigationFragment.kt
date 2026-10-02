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
class RequestStandReinforcementNavigationFragment : Fragment() {
    private val viewModel: RequestStandReinforcementViewModel by viewModels()
    private val safeArgs: RequestStandReinforcementNavigationFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If your ViewModel needs the args to initialize state, do it here or in onViewCreated.
        viewModel.onScreenStarted(
            idMacrozone = safeArgs.idMacrozone,
            idZone = safeArgs.idZone,
            isInFavourites = safeArgs.isInFavourites
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.onScreenStopped()
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
                RequestStandReinforcementScreen(
                    uiState = viewModel.uiState,
                    onAction = ::handleAction
                )
            }
        }
    }
    fun handleAction(action: RequestStandReinforcementAction) {
        when (action) {
            RequestStandReinforcementAction.CancelClicked -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            RequestStandReinforcementAction.AddFavouriteClicked -> {
                viewModel.addZoneToFavourites(safeArgs.idMacrozone, safeArgs.idZone)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            RequestStandReinforcementAction.RemoveFavouriteClicked -> {
                viewModel.removeZoneFromFavourites(safeArgs.idMacrozone, safeArgs.idZone)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            is RequestStandReinforcementAction.ReinforcementClicked -> {
                viewModel.sendReinforcementRequest(
                    safeArgs.idMacrozone,
                    safeArgs.idZone,
                    action.number
                )
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            RequestStandReinforcementAction.DialogConfirmed -> {
                viewModel.onDialogConfirmed()
            }
            RequestStandReinforcementAction.DialogDismissed -> {
                viewModel.onDialogDismissed()
            }
        }
    }
}
`by viewModels()` creates the ViewModel with the default `Fragment` factory.
So this works if your `RequestStandReinforcementViewModel`:
If `uiState` is not a plain property, use Compose collection inside `setContent`, for example:
setContent {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    RequestStandReinforcementScreen(
        uiState = uiState,
        onAction = ::handleAction
    )
}
