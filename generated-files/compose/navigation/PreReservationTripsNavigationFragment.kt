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
