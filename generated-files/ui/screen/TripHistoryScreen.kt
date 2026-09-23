package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.TripHistoryScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 332-4: import androidx.compose.foundation.clickable
@Composable
fun TripHistoryScreen(
    uiState: TripHistoryUiState,
    buttonsState: TripHistoryButtonsState,
    onBackClick: () -> Unit,
    onAllClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onTripClick: (Trip) -> Unit,
    onDialogDismiss: () -> Unit,
    onDialogAccept: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TripHistoryButtons(
            state = buttonsState,
            onBackClick = onBackClick,
            onAllClick = onAllClick,
            onDeleteClick = onDeleteClick,
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f, fill = true).fillMaxWidth()
        ) {
            itemsIndexed(uiState.trips, key = { _, item -> item.id }) { index, trip ->
                TripRow(
                    trip = trip,
                    selected = trip.id in uiState.selectedTrips,
                    onClick = { onTripClick(trip) },
                    onToggleSelection = { /* optional selection UI hook */ }
                )
                if (index == uiState.trips.lastIndex && !uiState.isLastPage && !uiState.isLoading) {
                    onLoadMore()
                }
            }
            if (uiState.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
    uiState.dialog?.let { dialogState ->
        TripHistoryCustomDialog(
            state = dialogState,
            onDismiss = onDialogDismiss,
            onAccept = onDialogAccept,
        )
    }
}
@Composable
fun TripRow(
    trip: Trip,
    selected: Boolean,
    onClick: () -> Unit,
    onToggleSelection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Trip #${trip.id}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = if (selected) "Selected" else "Tap to open",
            style = MaterialTheme.typography.bodySmall
        )
        Divider(Modifier.padding(top = 12.dp))
    }
}
