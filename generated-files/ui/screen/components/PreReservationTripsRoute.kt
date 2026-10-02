package ifac.td.taxi.ui.screen.components
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
// # Block 262-4: import android.widget.Toast
@Composable
fun PreReservationTripsRoute(
    viewModel: PreReservationTripsComposeViewModel,
    onNavigateBack: () -> Unit = {},
    showHeader: Boolean = true,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.uiEffects.collectLatest { effect ->
            when (effect) {
                is PreReservationTripsUiEffect.ShowToast -> {
                    Toast.makeText(context, context.getString(effect.messageRes), Toast.LENGTH_SHORT).show()
                }
                is PreReservationTripsUiEffect.ShowDialog -> {
                    // dialog is held in state below
                }
                PreReservationTripsUiEffect.DismissDialog -> Unit
                PreReservationTripsUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }
    PreReservationTripsScreen(
        uiState = uiState.copy(showHeader = showHeader),
        onTripClick = { viewModel.onEvent(PreReservationTripsUiEvent.TripClicked(it)) },
        onDismissDialog = { viewModel.onEvent(PreReservationTripsUiEvent.DialogCancelClicked) },
        onAcceptDialog = { trip, remove -> viewModel.onDialogConfirmed(trip, remove) }
    )
}
@Composable
fun PreReservationTripsScreen(
    uiState: PreReservationTripsUiState,
    onTripClick: (Prereservation) -> Unit,
    onDismissDialog: () -> Unit,
    onAcceptDialog: (Prereservation, Boolean) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (uiState.showHeader) {
                Text(
                    text = stringResource(id = R.string.prereservation),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
            PreReservationTripsButtons(
                state = uiState.buttonsState,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.trips) { trip ->
                    PreReservationTripRow(
                        trip = trip,
                        onClick = { onTripClick(trip) }
                    )
                }
            }
        }
        uiState.dialog?.let { dialog ->
            PreReservationTripsButtonStylePreReservationTripsCustomDialog(
                title = dialog.title,
                description = dialog.description,
                acceptLabel = dialog.acceptLabel,
                cancelLabel = dialog.cancelLabel,
                onAccept = {
                    onAcceptDialog(
                        // you may want to keep the selected trip in state;
                        // this is a template and should be wired with a selectedTrip field
                        trip = uiState.trips.first(),
                        remove = dialog.isDestructive
                    )
                },
                onCancel = onDismissDialog,
                destructive = dialog.isDestructive
            )
        }
    }
}
@Composable
fun PreReservationTripRow(
    trip: Prereservation,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = trip.pickupZone ?: "")
            Text(text = trip.pickupTime.getDateTimeFromISO8601())
        }
    }
}
