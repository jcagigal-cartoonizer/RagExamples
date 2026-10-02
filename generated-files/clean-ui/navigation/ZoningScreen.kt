package ifac.td.taxi.compose.navigation
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
// # Block 105-2: import android.content.res.Configuration
@Composable
fun ZoningScreen(
    navController: NavController,
    viewModel: ZoningComposeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        viewModel.uiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                when (effect) {
                    is ZoningUiEffect.NavigateToServices -> {
                        val action = ZoningFragmentDirections
                            .actionZoningFragmentToZoningServicesFragment(effect.idMacroZone, effect.idZone)
                        navController.navigate(action)
                    }
                    is ZoningUiEffect.NavigateToCars -> {
                        val action = ZoningFragmentDirections
                            .actionZoningFragmentToZoningCarsFragment(effect.idMacroZone, effect.idZone)
                        navController.navigate(action)
                    }
                    ZoningUiEffect.NavigateToPendingTrips -> {
                        navController.navigate(R.id.action_zoningFragment_to_pendingTripsFragment)
                    }
                    ZoningUiEffect.NavigateToHomeAndOnTrip -> {
                        navController.navigate(ZoningFragmentDirections.actionZoningFragmentToHomeFragment())
                        navController.navigate(HomeDirections.goToOnTripFragment())
                    }
                    ZoningUiEffect.NavigateToPointsOfInterest -> {
                        navController.navigate(ZoningFragmentDirections.actionZoningFragmentToPointsOfInterestFragment())
                    }
                    is ZoningUiEffect.ShowToast -> Unit
                    is ZoningUiEffect.OpenFilterSheet -> Unit
                    is ZoningUiEffect.OpenDialog -> Unit
                }
            }
    }
    LaunchedEffect(Unit) {
        viewModel.onEvent(ZoningUiEvent.ScreenResumed)
    }
    Scaffold(
        topBar = {
            ZoningTopBar(
                title = uiState.placeholderText.ifBlank { uiState.macroZoneName },
                onBack = { viewModel.onBackPressed(navController) },
                onFilter = { viewModel.onOpenFilterClicked() }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (uiState.progress in 1..999) {
                    LinearProgressIndicator(
                        progress = { uiState.progress / 1000f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ZoningButtonsRow(
                    buttonsState = uiState.buttonsState,
                    onLocateOnHired = { viewModel.onEvent(ZoningUiEvent.ClickLocateOnHired) },
                    onSoonInZone = { viewModel.onEvent(ZoningUiEvent.ClickSoonInZone) },
                    onPending = { viewModel.onEvent(ZoningUiEvent.ClickPending) },
                    onTrips = { viewModel.onEvent(ZoningUiEvent.ClickTrips) },
                    onCars = { viewModel.onEvent(ZoningUiEvent.ClickCars) },
                )
                if (uiState.headerVisibility.stand ||
                    uiState.headerVisibility.zone ||
                    uiState.headerVisibility.hired ||
                    uiState.headerVisibility.trips
                ) {
                    ZoningHeaderRow(
                        state = uiState,
                        onStand = { viewModel.onEvent(ZoningUiEvent.HeaderClicked(OrderOptions.IN_STAND)) },
                        onZone = { viewModel.onEvent(ZoningUiEvent.HeaderClicked(OrderOptions.IN_ZONE)) },
                        onHired = { viewModel.onEvent(ZoningUiEvent.HeaderClicked(OrderOptions.HIRED)) },
                        onTrips = { viewModel.onEvent(ZoningUiEvent.HeaderClicked(OrderOptions.TRIPS)) },
                        onName = { viewModel.onEvent(ZoningUiEvent.HeaderClicked(OrderOptions.NAME)) },
                    )
                }
                if (uiState.isLoadingZones) {
                    Box(Modifier.fillMaxWidth().padding(24.dp)) {
                        CircularProgressIndicator()
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.zones) { zone ->
                        ZoneRow(zone = zone) {
                            viewModel.onEvent(ZoningUiEvent.SelectZone(zone))
                        }
                    }
                }
            }
            uiState.dialog?.let { dialog ->
                ZoningDialog(
                    dialog = dialog,
                    onDismiss = { viewModel.onEvent(ZoningUiEvent.ClearDialog) },
                    onAction = { type, option ->
                        viewModel.onEvent(ZoningUiEvent.ButtonAction(type, option))
                    }
                )
            }
        }
    }
}
@Composable
fun ZoningTopBar(
    title: String,
    onBack: () -> Unit,
    onFilter: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            TextButton(onClick = onBack) {
                Text("Back")
            }
        },
        actions = {
            TextButton(onClick = onFilter) {
                Text("Filter")
            }
        }
    )
}
@Composable
fun ZoningHeaderRow(
    state: ZoningUiState,
    onStand: () -> Unit,
    onZone: () -> Unit,
    onHired: () -> Unit,
    onTrips: () -> Unit,
    onName: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.headerVisibility.stand) TextButton(onClick = onStand) { Text("Stand") }
        if (state.headerVisibility.zone) TextButton(onClick = onZone) { Text("Zone") }
        if (state.headerVisibility.hired) TextButton(onClick = onHired) { Text("Hired") }
        if (state.headerVisibility.trips) TextButton(onClick = onTrips) { Text("Trips") }
        TextButton(onClick = onName) { Text(stringResource(R.string.sort_by_name)) }
    }
}
@Composable
fun ZoningDialog(
    dialog: ZoningDialogModel,
    onDismiss: () -> Unit,
    onAction: (ButtonTypeUi, ZoningDialogListOption?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialog.title) },
        text = {
            Column {
                dialog.description?.let { Text(it) }
                dialog.listOptions?.forEach { (id, label) ->
                    TextButton(onClick = { onAction(ButtonTypeUi.Accept, ZoningDialogListOption(id, label)) }) {
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            dialog.buttons.firstOrNull()?.let { button ->
                TextButton(onClick = { onAction(button.type, null) }) {
                    Text(button.label)
                }
            }
        }
    )
}
@Composable
fun ZoningButtonsRow(
    buttonsState: ZoningButtonsState,
    onLocateOnHired: () -> Unit,
    onSoonInZone: () -> Unit,
    onPending: () -> Unit,
    onTrips: () -> Unit,
    onCars: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ZoningActionButton(buttonsState.locateOnHired, onLocateOnHired, Modifier.weight(1f))
        ZoningActionButton(buttonsState.soonInZone, onSoonInZone, Modifier.weight(1f))
        ZoningActionButton(buttonsState.pending, onPending, Modifier.weight(1f))
        ZoningActionButton(buttonsState.trips, onTrips, Modifier.weight(1f))
        ZoningActionButton(buttonsState.cars, onCars, Modifier.weight(1f))
    }
}
@Composable
fun ZoningActionButton(
    state: ComposeActionButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is ComposeActionButtonState.Hidden -> {
            Spacer(modifier = modifier.height(48.dp))
        }
        is ComposeActionButtonState.Enabled -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(48.dp)
            ) {
                Text(text = stringResource(state.textRes))
            }
        }
        is ComposeActionButtonState.Disabled -> {
            Button(
                onClick = onClick,
                enabled = false,
                modifier = modifier.height(48.dp)
            ) {
                Text(text = stringResource(state.textRes))
            }
        }
    }
}
@Composable
fun ZoneRow(zone: ZoneModel, onClick: () -> Unit) {
    Surface(
        tonalElevation = if (zone.isSelected) 3.dp else 1.dp,
        shape = MaterialTheme.shapes.medium,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = zone.zone.nombreZone)
        }
    }
}
