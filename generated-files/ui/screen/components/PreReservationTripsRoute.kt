package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PreReservationTripsUiEvent
import ifac.td.taxi.ui.screen.components.PreReservationTripsScreen
import ifac.td.taxi.compose.viewmodel.PreReservationTripsComposeViewModel
import ifac.td.taxi.ui.screen.components.PreReservationTripsUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 263-4: import android.widget.Toast
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
            PreReservationTripsCustomDialog(
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
