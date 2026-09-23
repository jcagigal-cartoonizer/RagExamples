package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.ZoningUiEvent
import ifac.td.taxi.compose.viewmodel.ZoningComposeViewModel
import ifac.td.taxi.ui.screen.components.ZoningCustomDialog
import ifac.td.taxi.ui.screen.components.ZoningScreen
import ifac.td.taxi.ui.screen.components.ZoningUiEffect
import ifac.td.taxi.ui.screen.components.ZoningCustomDialogModel
import ifac.td.taxi.ui.screen.components.ZoningButtonsRow
import ifac.td.taxi.ui.screen.components.ZoningButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 393-7: import androidx.compose.foundation.layout.*
@Composable
fun ZoningScreen(
    viewModel: ZoningComposeViewModel,
    onNavigateToServices: (Int, Int) -> Unit,
    onNavigateToCars: (Int, Int) -> Unit,
    onNavigateToPendingTrips: () -> Unit,
    onNavigateToHomeAndOnTrip: () -> Unit,
    onNavigateToPoi: () -> Unit,
    onOpenFilterSheet: (FilterOptions) -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ZoningUiEffect.ShowToast -> onShowToast(effect.messageRes)
                is ZoningUiEffect.NavigateToServices -> onNavigateToServices(effect.idMacroZone, effect.idZone)
                is ZoningUiEffect.NavigateToCars -> onNavigateToCars(effect.idMacroZone, effect.idZone)
                is ZoningUiEffect.NavigateToPendingTrips -> onNavigateToPendingTrips()
                is ZoningUiEffect.NavigateToHomeAndOnTrip -> onNavigateToHomeAndOnTrip()
                is ZoningUiEffect.NavigateToPointsOfInterest -> onNavigateToPoi()
                is ZoningUiEffect.OpenFilterSheet -> onOpenFilterSheet(effect.currentFilter)
                else -> Unit
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.progress in 1..999) {
                LinearProgressIndicator(
                    progress = { state.progress / 1000f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            ZoningButtonsRow(
                buttonsState = state.buttonsState,
                onLocateOnHired = {},
                onSoonInZone = {},
                onPending = {},
                onTrips = {},
                onCars = {},
            )
            if (state.headerVisibility.stand || state.headerVisibility.zone || state.headerVisibility.hired || state.headerVisibility.trips) {
                Text(
                    text = state.placeholderText.ifBlank { state.macroZoneName },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.zones) { zone ->
                    ZoneRow(zone = zone) {
                        viewModel.onEvent(ZoningUiEvent.SelectZone(zone))
                    }
                }
            }
        }
        state.dialog?.let { dialog ->
            ComposeZoningCustomDialog(
                model = ComposeZoningCustomDialogModel(
                    title = dialog.title,
                    description = dialog.description,
                    buttons = dialog.buttons.map {
                        DialogAction(it.type, labelForButton(it.type))
                    },
                    listOptions = dialog.listOptions
                ),
                onDismiss = { viewModel.onEvent(ZoningUiEvent.ClearDialog) },
                onAction = { type, selectedOption ->
                    viewModel.onEvent(ZoningUiEvent.ClearDialog)
                }
            )
        }
    }
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
fun labelForButton(type: ButtonTypeUi): String = when (type) {
    ButtonTypeUi.Cancel -> "Cancel"
    ButtonTypeUi.Accept -> "Accept"
    ButtonTypeUi.AddFavourites -> "Add favourites"
    ButtonTypeUi.RemoveFavourites -> "Remove favourites"
    ButtonTypeUi.GoingHome -> "Going home"
    ButtonTypeUi.OpenReinforcement -> "Reinforcement"
    ButtonTypeUi.WithZone -> "With zone"
    ButtonTypeUi.WithoutZone -> "Without zone"
    ButtonTypeUi.Poi -> "POI"
}
That fully replaces:
Here is the pattern you should use:
fun reduceButtons(
    showLocateOnHired: Boolean,
    showSoonInZone: Boolean,
    pendingEnabled: Boolean,
    tripsEnabled: Boolean,
    carsEnabled: Boolean,
    soonInZoneIsIn: Boolean,
    pendingOrange: Boolean
) {
    val state = ZoningButtonsState(
        locateOnHired = if (showLocateOnHired)
            ComposeActionButtonState.Enabled(
                textRes = R.string.btn_soon_to_clear,
                iconRes = R.drawable.ubactivar,
                color = ComposeButtonColor.Blue
            )
        else ComposeActionButtonState.Hidden(),
        soonInZone = if (showSoonInZone)
            ComposeActionButtonState.Enabled(
                textRes = R.string.btn_soon_in_zone,
                iconRes = R.drawable.ic_siz,
                color = if (soonInZoneIsIn) ComposeButtonColor.Red else ComposeButtonColor.Green
            )
        else ComposeActionButtonState.Hidden(),
        pending = if (pendingEnabled)
            ComposeActionButtonState.Enabled(
                textRes = R.string.pending,
                iconRes = R.drawable.ic_pending,
                color = if (pendingOrange) ComposeButtonColor.Orange else ComposeButtonColor.Blue
            )
        else ComposeActionButtonState.Disabled(
            textRes = R.string.pending,
            iconRes = R.drawable.ic_pending,
            color = if (pendingOrange) ComposeButtonColor.Orange else ComposeButtonColor.Blue
        ),
        trips = if (tripsEnabled)
            ComposeActionButtonState.Enabled(
                textRes = R.string.trips,
                iconRes = R.drawable.ic_trips,
                color = ComposeButtonColor.Blue
            )
        else ComposeActionButtonState.Disabled(
            textRes = R.string.trips,
            iconRes = R.drawable.ic_trips,
            color = ComposeButtonColor.Blue
        ),
        cars = if (carsEnabled)
            ComposeActionButtonState.Enabled(
                textRes = R.string.cars,
                iconRes = R.drawable.ic_cars,
                color = ComposeButtonColor.Blue
            )
        else ComposeActionButtonState.Disabled(
            textRes = R.string.cars,
            iconRes = R.drawable.ic_cars,
            color = ComposeButtonColor.Blue
        ),
    )
    onEvent(ZoningUiEvent.SetButtonsState(state))
}
Your current fragment/viewmodel logic is very large and relies on:
A production migration should be done in this order:
1. **Introduce `ZoningUiState`, `ZoningUiEvent`, `ZoningUiEffect`**
2. **Mirror existing flows into the state**
3. **Move button logic into `ZoningButtonsState` reduction**
4. **Replace dialog calls with `UiEffect`**
5. **Keep Fragment navigation as the bridge**
6. **Finally remove XML and RecyclerView**
1. a **full Compose version of the missing zone list and top bar**
2. a **complete migration of your current `ZoningViewModel` into `ZoningComposeViewModel` using your existing domain/use cases**
3. a **real bridge Fragment** that preserves all current `HomeDirections` / `ZoningFragmentDirections` navigation exactly.
