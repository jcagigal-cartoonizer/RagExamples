package ifac.td.taxi.compose.navigation
import  android.app.Application
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
// # Block 368-3: import androidx.lifecycle.ViewModel
class ZoningComposeViewModel(
    // inject your existing use cases here
) : ViewModel() {
    private val _uiState = MutableStateFlow(ZoningUiState())
    val uiState: StateFlow<ZoningUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ZoningUiEffect>()
    val uiEffect: SharedFlow<ZoningUiEffect> = _uiEffect.asSharedFlow()
    fun onEvent(event: ZoningUiEvent) {
        when (event) {
            ZoningUiEvent.ScreenResumed -> {
                // init + start collecting from existing flows
                mirrorExistingState()
            }
            ZoningUiEvent.ClickTrips -> {
                val zone = _uiState.value.selectedZone
                if (zone != null) {
                    viewModelScope.launch {
                        _uiEffect.emit(ZoningUiEffect.NavigateToServices(zone.zone.idMacrozone, zone.zone.idZone))
                    }
                } else {
                    emitToast(R.string.select_zone_first)
                }
            }
            ZoningUiEvent.ClickCars -> {
                val zone = _uiState.value.selectedZone
                if (zone != null) {
                    viewModelScope.launch {
                        _uiEffect.emit(ZoningUiEffect.NavigateToCars(zone.zone.idMacrozone, zone.zone.idZone))
                    }
                } else {
                    emitToast(R.string.select_zone_first)
                }
            }
            ZoningUiEvent.ClickPending -> {
                viewModelScope.launch { _uiEffect.emit(ZoningUiEffect.NavigateToPendingTrips) }
            }
            ZoningUiEvent.ClickLocateOnHired -> {
                // call your existing locate/delocate logic
            }
            ZoningUiEvent.ClickSoonInZone -> {
                // call your existing SIZ logic
            }
            is ZoningUiEvent.SelectZone -> {
                _uiState.update { it.copy(selectedZone = event.zone) }
            }
            ZoningUiEvent.ClearDialog -> {
                _uiState.update { it.copy(dialog = null) }
            }
            is ZoningUiEvent.HeaderClicked -> {
                // apply sorting logic with your existing use case / repository logic
            }
            is ZoningUiEvent.FilterChanged -> {
                _uiState.update { it.copy(currentFilter = event.filter) }
            }
            is ZoningUiEvent.ButtonAction -> {
                // dialog response mapping
            }
        }
    }
    fun mirrorExistingState() {
        viewModelScope.launch {
            // Replace these with your existing flows:
            // sharedViewModel.zoneFlow, macroZoneFlow, refreshProgressFlow, etc.
            // Example of reduction to buttons:
            reduceButtons(
                showLocateOnHired = true,
                showSoonInZone = true,
                pendingEnabled = true,
                tripsEnabled = true,
                carsEnabled = true,
                soonInZoneIsIn = false,
                pendingOrange = false
            )
            _uiState.update { it.copy(
                progress = 0,
                macroZoneName = "Macrozone",
                placeholderText = "Macrozone",
            ) }
        }
    }
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
        _uiState.update { it.copy(buttonsState = state) }
    }
    fun onBackPressed(navController: androidx.navigation.NavController) {
        // preserve your current back behavior here
    }
    fun onOpenFilterClicked() {
        viewModelScope.launch {
            _uiEffect.emit(ZoningUiEffect.OpenFilterSheet(_uiState.value.currentFilter))
        }
    }
    fun emitToast(messageRes: Int) {
        viewModelScope.launch {
            _uiEffect.emit(ZoningUiEffect.ShowToast(messageRes))
        }
    }
}
