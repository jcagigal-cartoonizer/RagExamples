package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.ZoningUiEvent
import ifac.td.taxi.ui.screen.components.ZoningUiEffect
import ifac.td.taxi.ui.screen.components.ZoningDialogState?
import ifac.td.taxi.ui.screen.components.ZoningUiState
import ifac.td.taxi.ui.screen.components.ZoningButtonsState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 326-6: import android.app.Application
class ZoningComposeViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ZoningUiState())
    val uiState: StateFlow<ZoningUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ZoningUiEffect>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<ZoningUiEffect> = _effects.asSharedFlow()
    fun onEvent(event: ZoningUiEvent) {
        when (event) {
            is ZoningUiEvent.SetZones -> reduceZones(event.zones)
            is ZoningUiEvent.SelectZone -> selectZone(event.zone)
            is ZoningUiEvent.SetLoading -> _uiState.update { it.copy(isLoading = event.loading) }
            is ZoningUiEvent.SetProgress -> _uiState.update { it.copy(progress = event.progress) }
            is ZoningUiEvent.SetHeaderVisibility -> _uiState.update { it.copy(headerVisibility = event.visibility) }
            is ZoningUiEvent.SetPlaceholderText -> _uiState.update { it.copy(placeholderText = event.text) }
            is ZoningUiEvent.SetButtonsState -> _uiState.update { it.copy(buttonsState = event.state) }
            is ZoningUiEvent.SetDialog -> _uiState.update { it.copy(dialog = event.dialog) }
            is ZoningUiEvent.ClearDialog -> _uiState.update { it.copy(dialog = null) }
            is ZoningUiEvent.SetOrder -> _uiState.update { it.copy(listOrder = event.order) }
            is ZoningUiEvent.SetFilter -> _uiState.update { it.copy(listFilter = event.filter) }
            is ZoningUiEvent.EmitEffect -> viewModelScope.launch { _effects.emit(event.effect) }
            is ZoningUiEvent.SetMacroZoneName -> _uiState.update { it.copy(macroZoneName = event.name) }
            is ZoningUiEvent.SetScrollMode -> _uiState.update { it.copy(scrollMode = event.mode) }
        }
    }
    fun reduceZones(zones: List<ZoneModel>) {
        _uiState.update { it.copy(zones = zones, isLoading = false) }
    }
    fun selectZone(zone: ZoneModel?) {
        _uiState.update { it.copy(selectedZone = zone) }
    }
}
sealed interface ZoningUiEvent {
    data class SetZones(val zones: List<ZoneModel>) : ZoningUiEvent
    data class SelectZone(val zone: ZoneModel?) : ZoningUiEvent
    data class SetLoading(val loading: Boolean) : ZoningUiEvent
    data class SetProgress(val progress: Int) : ZoningUiEvent
    data class SetHeaderVisibility(val visibility: ZoneHeaderVisibility) : ZoningUiEvent
    data class SetPlaceholderText(val text: String) : ZoningUiEvent
    data class SetButtonsState(val state: ZoningButtonsState) : ZoningUiEvent
    data class SetDialog(val dialog: ZoningDialogState?) : ZoningUiEvent
    data object ClearDialog : ZoningUiEvent
    data class SetOrder(val order: OrderOptions) : ZoningUiEvent
    data class SetFilter(val filter: FilterOptions) : ZoningUiEvent
    data class EmitEffect(val effect: ZoningUiEffect) : ZoningUiEvent
    data class SetMacroZoneName(val name: String) : ZoningUiEvent
    data class SetScrollMode(val mode: ScrollModeUi) : ZoningUiEvent
}
