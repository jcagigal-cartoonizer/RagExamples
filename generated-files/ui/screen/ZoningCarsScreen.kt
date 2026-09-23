package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.ZoningCarsUiEvent
import ifac.td.taxi.ui.screen.components.ZoningCarsScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 96-2: import androidx.compose.foundation.layout.*
@Composable
fun ZoningCarsScreen(
    uiState: ZoningCarsUiState,
    buttonsState: ZoningCarsButtonsState,
    dialogState: ZoningCarsDialogState?,
    onEvent: (ZoningCarsUiEvent) -> Unit,
    onDismissDialog: () -> Unit,
    onDialogButton: (String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = uiState.zoneName,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            if (uiState.showProgress) {
                LinearProgressIndicator(
                    progress = { uiState.refreshProgress / 1000f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
            if (uiState.isLoadingCars) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
            ZoneCarsList(
                rows = uiState.carRows,
                modifier = Modifier.weight(1f)
            )
            ZoningCarsButtonsRow(
                state = buttonsState,
                onBackClick = { onEvent(ZoningCarsUiEvent.BackClicked) },
                onCloseClick = { onEvent(ZoningCarsUiEvent.CloseClicked) },
                onPendingClick = { onEvent(ZoningCarsUiEvent.PendingClicked) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        dialogState?.let {
            ZoningCarsCustomDialog(
                state = it,
                onDismiss = onDismissDialog,
                onButtonClick = onDialogButton,
            )
        }
    }
}
@Composable
fun ZoneCarsList(
    rows: List<ZoneCarRowModel>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.padding(16.dp)) {
        items(rows) { row ->
            Text(
                text = "Row ${row.index}: hired=${row.carHired?.name ?: "-"}, zone=${row.carZone?.name ?: "-"}, stop=${row.carRank?.name ?: "-"}"
            )
            Divider()
        }
    }
}
