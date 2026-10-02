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
// # Block 392-7: import androidx.compose.foundation.layout.*
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
            ComposeZoningScreenZoningCustomDialog(
                model = ComposeZoningScreenCustomDialogModel(
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
Example:
class ZoningComposeFragment : Fragment() {
    private val viewModel: ZoningComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            ZoningScreen(
                viewModel = viewModel,
                onNavigateToServices = { idMacro, idZone ->
                    findNavController().navigate(
                        ZoningFragmentDirections.actionZoningFragmentToZoningServicesFragment(idMacro, idZone)
                    )
                },
                onNavigateToCars = { idMacro, idZone ->
                    findNavController().navigate(
                        ZoningFragmentDirections.actionZoningFragmentToZoningCarsFragment(idMacro, idZone)
                    )
                },
                onNavigateToPendingTrips = {
                    findNavController().navigate(R.id.action_zoningFragment_to_pendingTripsFragment)
                },
                onNavigateToHomeAndOnTrip = {
                    findNavController().navigate(ZoningFragmentDirections.actionZoningFragmentToHomeFragment())
                    findNavController().navigate(HomeDirections.goToOnTripFragment())
                },
                onNavigateToPoi = {
                    findNavController().navigate(ZoningFragmentDirections.actionZoningFragmentToPointsOfInterestFragment())
                },
                onOpenFilterSheet = { /* open compose/bottom sheet */ },
                onShowToast = { res -> Toast.makeText(requireContext(), getString(res), Toast.LENGTH_SHORT).show() }
            )
        }
    }
}
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
