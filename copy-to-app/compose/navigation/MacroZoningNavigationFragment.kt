package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.MacroZoningScreen
import ifac.td.taxi.compose.viewModel.MacroZoningComposeViewModel
import ifac.td.taxi.viewModel.MacroZoningViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
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
