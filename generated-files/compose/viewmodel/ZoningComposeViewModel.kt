package ifac.td.taxi.compose.viewmodel
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
// # Block 325-6: import android.app.Application
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
