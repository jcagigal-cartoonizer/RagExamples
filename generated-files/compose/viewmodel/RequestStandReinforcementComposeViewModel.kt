package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementUiEffect
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementUiState
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementButtonsState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 155-2: import androidx.lifecycle.ViewModel
class RequestStandReinforcementComposeViewModel(
    private val zoningUseCase: ZoningUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(RequestStandReinforcementUiState())
    val uiState: StateFlow<RequestStandReinforcementUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<RequestStandReinforcementUiEffect>()
    val effects: SharedFlow<RequestStandReinforcementUiEffect> = _effects.asSharedFlow()
    fun init(
        idMacrozone: Int,
        idZone: Int,
        isInFavourites: Boolean
    ) {
        _uiState.update {
            it.copy(
                idMacrozone = idMacrozone,
                idZone = idZone,
                isInFavourites = isInFavourites,
                buttons = RequestStandReinforcementButtonsState.from(isInFavourites)
            )
        }
    }
    fun onAction(action: RequestStandReinforcementAction) {
        when (action) {
            RequestStandReinforcementAction.CancelClicked -> {
                emitEffect(RequestStandReinforcementUiEffect.NavigateBack)
            }
            RequestStandReinforcementAction.AddFavouriteClicked -> {
                runRequest {
                    zoningUseCase.addFavouriteZone(
                        _uiState.value.idMacrozone,
                        _uiState.value.idZone
                    )
                    emitEffect(RequestStandReinforcementUiEffect.NavigateBack)
                }
            }
            RequestStandReinforcementAction.RemoveFavouriteClicked -> {
                runRequest {
                    zoningUseCase.removeFavouriteZone(
                        _uiState.value.idMacrozone,
                        _uiState.value.idZone
                    )
                    emitEffect(RequestStandReinforcementUiEffect.NavigateBack)
                }
            }
            is RequestStandReinforcementAction.ReinforcementClicked -> {
                runRequest {
                    zoningUseCase.sendReinforcementRequest(
                        _uiState.value.idMacrozone,
                        _uiState.value.idZone,
                        action.numCustomers
                    )
                    emitEffect(RequestStandReinforcementUiEffect.ShowToast(R.string.sending))
                    emitEffect(RequestStandReinforcementUiEffect.NavigateBack)
                }
            }
            RequestStandReinforcementAction.DialogConfirmed -> {
                _uiState.update { it.copy(dialog = it.dialog.copy(isVisible = false)) }
                emitEffect(RequestStandReinforcementUiEffect.HideDialog)
            }
            RequestStandReinforcementAction.DialogDismissed -> {
                _uiState.update { it.copy(dialog = it.dialog.copy(isVisible = false)) }
                emitEffect(RequestStandReinforcementUiEffect.HideDialog)
            }
        }
    }
    fun setDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(dialog = it.dialog.copy(isVisible = visible)) }
    }
    fun runRequest(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
    fun emitEffect(effect: RequestStandReinforcementUiEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
