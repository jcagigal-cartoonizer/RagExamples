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
