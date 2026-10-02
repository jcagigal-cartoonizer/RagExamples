@Composable
fun PointsOfInterestScreen(
    state: PointsOfInterestUiState,
    dialogState: PointsOfInterestDialogState?,
    onEvent: (PointsOfInterestUiEvent) -> Unit,
    onDialogAction: (ButtonTypeUi) -> Unit,
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("Points of Interest") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.searchText,
                onValueChange = { onEvent(PointsOfInterestUiEvent.SearchTextChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search POI") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { onEvent(PointsOfInterestUiEvent.SearchClicked) }
                )
            )
            Spacer(Modifier.height(12.dp))
            PointsOfInterestButtons(
                state = state.buttons,
                onCancel = { onEvent(PointsOfInterestUiEvent.CancelClicked) },
                onSearch = { onEvent(PointsOfInterestUiEvent.SearchClicked) }
            )
            Spacer(Modifier.height(16.dp))
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.pois) { poi ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onEvent(PointsOfInterestUiEvent.PoiClicked(poi)) }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(text = poi.poi)
                                Text(text = poi.zone?.nombreZone ?: "Out of zone")
                            }
                        }
                    }
                }
            }
        }
        dialogState?.let {
            PointsOfInterestCustomDialogCustomDialog(
                state = it,
                onDismissRequest = { onEvent(PointsOfInterestUiEvent.DialogDismissed) },
                onButtonClick = onDialogAction
            )
        }
    }
}
