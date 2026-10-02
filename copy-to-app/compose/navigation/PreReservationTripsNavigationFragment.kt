package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.PreReservationTripsScreen
import ifac.td.taxi.compose.viewModel.PreReservationTripsComposeViewModel
import ifac.td.taxi.viewModel.PreReservationTripsViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class PreReservationTripsNavigationFragment : BaseFragment<Any, PreReservationTripsViewModel>() {
    private val viewModel: PreReservationTripsViewModel by viewModels()
    override fun getViewModel(): PreReservationTripsViewModel = viewModel
    override fun getViewBinding() = null
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
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    PreReservationTripsScreen(
                        uiState = uiState,
                        onTripClick = { trip ->
                            viewModel.onTripClick(trip)
                        },
                        onDismissDialog = {
                            viewModel.onDismissDialog()
                        },
                        onAcceptDialog = { trip, remove ->
                            viewModel.onAcceptDialog(trip, remove)
                        }
                    )
                }
            }
        }
    }
    override fun setupComponents() {
        viewModel.onScreenStarted()
    }
    override fun setupObservers() {
        // If your ViewModel exposes side effects/events, observe them here.
        // Example:
        // viewLifecycleOwner.lifecycleScope.launch {
        //     repeatOnLifecycle(Lifecycle.State.STARTED) {
        //         viewModel.events.collect { event -> ... }
        //     }
        // }
    }
    override fun onPause() {
        viewModel.removeHandlerCallback()
        super.onPause()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.onScreenStopped()
    }
}
// # Block 79-2: import android.os.Bundle
class PreReservationTripsNavigationFragment : Fragment() {
    private val viewModel: PreReservationTripsViewModel by viewModels()
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
                    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                    PreReservationTripsScreen(
                        uiState = uiState,
                        onTripClick = { trip -> viewModel.onTripClick(trip) },
                        onDismissDialog = { viewModel.onDismissDialog() },
                        onAcceptDialog = { trip, remove ->
                            viewModel.onAcceptDialog(trip, remove)
                        }
                    )
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        viewModel.onScreenStarted()
    }
    override fun onStop() {
        viewModel.onScreenStopped()
        super.onStop()
    }
    override fun onPause() {
        viewModel.removeHandlerCallback()
        super.onPause()
    }
}
Replace:
<fragment
    android:id="@+id/preReservationTripsFragment"
    android:name="ifac.td.taxi.ui.screen.PreReservationTripsFragment"
    android:label="PreReservationTripsFragment" />
with:
<fragment
    android:id="@+id/preReservationTripsFragment"
    android:name="ifac.td.taxi.ui.screen.PreReservationTripsNavigationFragment"
    android:label="PreReservationTripsNavigationFragment" />
Your Compose screen currently expects dialog state inside `uiState.dialog`.  
So the fragment should not manually open `CustomDialog`; instead, the ViewModel should update `uiState.dialog`, and the composable will render the dialog.
That is the main architectural difference from the old XML-based fragment.
Right now your composable has:
onAccept = {
    onAcceptDialog(
        trip = uiState.trips.first(),
        remove = dialog.isDestructive
    )
}
That is only a placeholder. You should store the selected trip in state, for example:
selectedTrip: Prereservation?
Then use it here:
onAccept = {
    uiState.selectedTrip?.let { selected ->
        onAcceptDialog(selected, dialog.isDestructive)
    }
}
