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
// # Block 102-3: import android.app.Application
class ChooseOptionComposeViewModel(
    application: Application,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val loginDriverUseCase: LoginDriverUseCase,
    private val sessionUseCase: SessionUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val endShiftUseCase: EndShiftUseCase,
) : AndroidViewModel(application) {
    private val TAG = "ChooseOptionViewModel"
    private val _uiState = MutableStateFlow(ChooseOptionUiState())
    val uiState: StateFlow<ChooseOptionUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ChooseOptionUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<ChooseOptionUiEffect> = _uiEffect.asSharedFlow()
    fun initOptions(ids: List<String>, labels: List<String>) {
        _uiState.value = _uiState.value.copy(
            options = ids.zip(labels).map { (id, label) ->
                ChooseOptionOptionItem(id = id, label = label)
            }
        )
    }
    fun onEvent(event: ChooseOptionUiEvent) {
        when (event) {
            ChooseOptionUiEvent.BackPressed -> goBackCancellingShift()
            is ChooseOptionUiEvent.OptionClicked -> {
                _uiState.value = _uiState.value.copy(
                    isDialogVisible = true,
                    dialogState = ChooseOptionDialogState(
                        title = "Warning",
                        description = "Are you sure you want to select option: ${event.label}?",
                        buttons = ChooseOptionButtonsState.confirmCancelAccept(),
                        selectedId = event.id,
                        selectedLabel = event.label
                    )
                )
                _uiEffect.tryEmit(ChooseOptionUiEffect.ShowDialog(_uiState.value.dialogState!!))
            }
            ChooseOptionUiEvent.DialogCancelClicked -> {
                _uiState.value = _uiState.value.copy(isDialogVisible = false, dialogState = null)
                _uiEffect.tryEmit(ChooseOptionUiEffect.HideDialog)
            }
            ChooseOptionUiEvent.DialogAcceptClicked -> {
                val dialog = _uiState.value.dialogState ?: return
                viewModelScope.launch {
                    sendShiftSelectedAnswer(dialog.selectedId)
                    loginDriver()
                    _uiState.value = _uiState.value.copy(isDialogVisible = false, dialogState = null)
                    _uiEffect.emit(ChooseOptionUiEffect.CloseScreenAndGoBack)
                }
            }
        }
    }
    fun sendShiftSelectedAnswer(selectedId: String) {
        viewModelScope.launch {
            Logs.d(TAG, "sendShiftSelectedAnswer: selectedId = $selectedId")
            shiftStatusUseCase.sendShiftSelectedAnswer(
                selectedId,
                W2CLocation.getZoning()?.version ?: "00"
            )
        }
    }
    fun loginDriver() {
        viewModelScope.launch {
            sessionUseCase.getSession()?.let {
                it.numDriver?.let { numDriver ->
                    loginDriverUseCase.loginDriver(
                        numDriver,
                        it.pwdDriver ?: "",
                        it.reinforcement
                    )
                }
            }
        }
    }
    fun logoffDriver() {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "logoffDriver: cancelling shift selection")
            endShiftUseCase.endShift()
            bravoCentralUseCase.logoff()
            shiftStatusUseCase.setStatus(ifConstants.STATE_DISCONNECTED, false)
            _uiEffect.emit(ChooseOptionUiEffect.NavigateBack)
        }
    }
    fun goBackCancellingShift() {
        logoffDriver()
    }
}
