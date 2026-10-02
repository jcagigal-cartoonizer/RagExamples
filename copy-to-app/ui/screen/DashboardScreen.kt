@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenZoneDetails: (ZoneModel) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    TextButton(onClick = { onNavigateBack() }) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            DashboardHeader(
                buttonsState = uiState.buttonsState,
                onOnStopClick = { onEvent(DashboardUiEvent.OnHeaderClicked(DashboardHeaderAction.ON_STOP)) },
                onOnZoneClick = { onEvent(DashboardUiEvent.OnHeaderClicked(DashboardHeaderAction.ON_ZONE)) },
                onHiredClick = { onEvent(DashboardUiEvent.OnHeaderClicked(DashboardHeaderAction.HIRED)) },
                onTripsClick = { onEvent(DashboardUiEvent.OnHeaderClicked(DashboardHeaderAction.TRIPS)) },
            )
            Spacer(Modifier.height(12.dp))
            PendingTripsSection(
                size = uiState.pendingTrips.size,
                trips = uiState.pendingTrips,
            )
            Spacer(Modifier.height(12.dp))
            ZonesSection(
                title = "Nearby Zones",
                zones = uiState.nearbyZones,
                onZoneClick = { onEvent(DashboardUiEvent.ZoneClicked(it)) },
                onZoneLongClick = { onEvent(DashboardUiEvent.ZoneLongClicked(it)) }
            )
            Spacer(Modifier.height(12.dp))
            ZonesSection(
                title = "Far Zones",
                zones = uiState.farZones,
                onZoneClick = { onEvent(DashboardUiEvent.ZoneClicked(it)) },
                onZoneLongClick = { onEvent(DashboardUiEvent.ZoneLongClicked(it)) }
            )
            Spacer(Modifier.height(12.dp))
            ZonesSection(
                title = "Actual Zone",
                zones = uiState.actualZones,
                onZoneClick = { onEvent(DashboardUiEvent.ZoneClicked(it)) },
                onZoneLongClick = { onEvent(DashboardUiEvent.ZoneLongClicked(it)) }
            )
        }
        if (uiState.dialogState.visible) {
            DashboardCustomDialogCustomDialog(
                state = uiState.dialogState,
                onDismiss = { onEvent(DashboardUiEvent.DismissDialog) },
                onPrimaryAction = { onEvent(DashboardUiEvent.ConfirmDialog) },
                onSecondaryAction = { onEvent(DashboardUiEvent.CancelDialog) }
            )
        }
    }
}
@Composable
fun DashboardHeader(
    buttonsState: DashboardButtonsState,
    onOnStopClick: () -> Unit,
    onOnZoneClick: () -> Unit,
    onHiredClick: () -> Unit,
    onTripsClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DashboardHeaderRow(
            label = buttonsState.onStop.text,
            visible = buttonsState.onStop.visible,
            style = buttonsState.onStop.style,
            onClick = onOnStopClick
        )
        DashboardHeaderRow(
            label = buttonsState.onZone.text,
            visible = buttonsState.onZone.visible,
            style = buttonsState.onZone.style,
            onClick = onOnZoneClick
        )
        DashboardHeaderRow(
            label = buttonsState.hired.text,
            visible = buttonsState.hired.visible,
            style = buttonsState.hired.style,
            onClick = onHiredClick
        )
        DashboardHeaderRow(
            label = buttonsState.trips.text,
            visible = buttonsState.trips.visible,
            style = buttonsState.trips.style,
            onClick = onTripsClick
        )
    }
}
@Composable
fun DashboardHeaderRow(
    label: String,
    visible: Boolean,
    style: DashboardButtonStyle,
    onClick: () -> Unit,
) {
    if (!visible) return
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.backgroundColor,
            contentColor = style.contentColor
        ),
        shape = style.shape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = style.elevation)
    ) {
        Text(label)
    }
}
@Composable
fun PendingTripsSection(size: Int, trips: List<Any>) {
    Column {
        Text("Pending trips: $size")
        // Replace with LazyColumn + your row layout
    }
}
@Composable
fun ZonesSection(
    title: String,
    zones: List<ZoneModel>,
    onZoneClick: (ZoneModel) -> Unit,
    onZoneLongClick: (ZoneModel) -> Unit,
) {
    Column {
        Text(title)
        LazyColumn(
            modifier = Modifier.heightIn(max = 180.dp)
        ) {
            items(zones) { zone ->
                TextButton(
                    onClick = { onZoneClick(zone) }
                ) {
                    Text(zone.zone.nombreZone ?: "Zone")
                }
            }
        }
    }
}
