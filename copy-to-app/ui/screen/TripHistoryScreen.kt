package ifac.td.taxi.ui.screen
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
        TripHistoryCustomDialogCustomDialog(
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
