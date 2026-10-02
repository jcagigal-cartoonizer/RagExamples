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
