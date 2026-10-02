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
class MacroZoningNavigationFragment : Fragment() {
    private val viewModel: MacroZoningViewModel by viewModels()
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
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val navController = findNavController()
                MacroZoningScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    uiEffects = viewModel.uiEffects,
                    onNavigateToZoning = { macroZoneId ->
                        val action =
                            ifac.td.taxi.ui.screen.MacroZoningNavigationFragmentDirections
                                .actionMacroZoningNavigationFragmentToZoningFragment(macroZoneId)
                        navController.navigate(action)
                    },
                    onNavigateToPendingTrips = {
                        val action =
                            ifac.td.taxi.ui.screen.MacroZoningNavigationFragmentDirections
                                .actionMacroZoningNavigationFragmentToPendingTripsFragment()
                        navController.navigate(action)
                    },
                    onNavigateToPreReservationTrips = {
                        val action =
                            ifac.td.taxi.ui.screen.MacroZoningNavigationFragmentDirections
                                .actionMacroZoningNavigationFragmentToPreReservationTripsFragment()
                        navController.navigate(action)
                    },
                    onShowToast = { message ->
                        // Keep it simple here; you can connect this to a host/activity callback if needed
                        android.widget.Toast.makeText(
                            requireContext(),
                            message,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onShowDialog = { dialogState ->
                        // If you want a native dialog, wire it here.
                        // For now, you can let the composable handle its own dialog state,
                        // or delegate to activity/host if you already have that pattern.
                    }
                )
            }
        }
    }
}
Replace the old `MacroZoningFragment` destination with the new fragment class:
<fragment
    android:id="@+id/macroZoningFragment"
    android:name="ifac.td.taxi.ui.screen.MacroZoningNavigationFragment"
    android:label="MacroZoningNavigationFragment">
    <action
        android:id="@+id/action_macroZoningNavigationFragment_to_zoningFragment"
        app:destination="@id/zoningFragment" />
    <action
        android:id="@+id/action_macroZoningNavigationFragment_to_pendingTripsFragment"
        app:destination="@id/pendingTripsFragment" />
    <action
        android:id="@+id/action_macroZoningNavigationFragment_to_preReservationTripsFragment"
        app:destination="@id/preReservationTripsFragment" />
</fragment>
That is exactly what the fragment above does:
private val viewModel: MacroZoningViewModel by viewModels()
So the ViewModel is owned by the Fragment lifecycle, not by Compose.
Your existing `MacroZoningScreen(...)` already supports:
So the fragment is just a host.  
Your composable already uses `repeatOnLifecycle` for `uiEffects`, so that is fine.
override fun onResume() {
    super.onResume()
    viewModel.initViewModeldata()
}
then call them from the fragment or move that initialization into `viewModel` / `init {}`.
Example:
override fun onResume() {
    super.onResume()
    viewModel.initViewModeldata()
}
class MacroZoningNavigationFragment : Fragment() {
    private val viewModel: MacroZoningViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MacroZoningScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                uiEffects = viewModel.uiEffects,
                onNavigateToZoning = { /* nav */ },
                onNavigateToPendingTrips = { /* nav */ },
                onNavigateToPreReservationTrips = { /* nav */ },
                onShowToast = { /* toast */ },
                onShowDialog = { /* dialog */ }
            )
        }
    }
}
