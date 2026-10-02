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
            ZoningCarsCustomDialogCustomDialog(
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
