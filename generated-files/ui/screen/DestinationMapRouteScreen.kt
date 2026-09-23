package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 212-3: import android.content.Intent
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.ui.model.RoutePointModel
import ifac.td.taxi.viewmodel.DestinationMapViewModel
import kotlinx.coroutines.flow.collectLatest
@Composable
fun DestinationMapRouteScreen(
    viewModel: DestinationMapComposeViewModel,
    onLaunchIntent: (Intent?) -> Unit,
    onShowHeader: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        onShowHeader(true)
    }
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is DestinationMapUiEffect.OpenNavigatorIntent -> onLaunchIntent(effect.intent)
                is DestinationMapUiEffect.ShowToast -> Unit
                DestinationMapUiEffect.HideKeyboard -> Unit
            }
        }
    }
    DestinationMapRouteContent(
        uiState = uiState,
        onRoutePointClick = viewModel::onRoutePointClicked,
        onAcceptClick = viewModel::onAcceptClicked,
        onCancelClick = viewModel::onCancelClicked,
        onDismissDialog = viewModel::dismissDialog,
        onConfirmDialog = { coordinates, address ->
            viewModel.confirmOpenNavigator(coordinates, address)
        },
        modifier = modifier
    )
}
@Composable
fun DestinationMapRouteContent(
    uiState: DestinationMapUiState,
    onRoutePointClick: (RoutePointModel) -> Unit,
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDialog: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.routePoints) { point ->
                    RoutePointItem(
                        routePoint = point,
                        onClick = { onRoutePointClick(point) }
                    )
                }
            }
            DestinationMapButtons(
                state = uiState.buttonsState,
                onAcceptClick = onAcceptClick,
                onCancelClick = onCancelClick
            )
        }
        when (val dialog = uiState.dialogState) {
            DestinationMapDialogState.Hidden -> Unit
            is DestinationMapDialogState.ConfirmOpenNavigator -> {
                DestinationMapCustomDialog(
                    title = "Open navigator",
                    message = dialog.address?.let {
                        "Do you want to open navigator for:\n$it"
                    } ?: "Do you want to open navigator?",
                    positiveText = "Open",
                    negativeText = "Cancel",
                    onPositiveClick = {
                        onConfirmDialog(dialog.coordinates, dialog.address)
                    },
                    onNegativeClick = onDismissDialog,
                    onDismiss = onDismissDialog
                )
            }
        }
    }
}
@Composable
fun RoutePointItem(
    routePoint: RoutePointModel,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        Column(Modifier.padding(16.dp)) {
            Text(text = routePoint.address)
            Text(text = routePoint.coordinatesTag)
        }
    }
}
