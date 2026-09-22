package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.domain.usecase.EndShiftUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.LoginDriverUseCase
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
data class ChooseOptionUiState(
    val options: List<ChooseOptionItem> = emptyList(),
    val buttonsState: ChooseOptionButtonsState = ChooseOptionButtonsState.default(),
    val dialogState: ChooseOptionDialogState = ChooseOptionDialogState.Hidden,
    val isLoading: Boolean = false,
)
data class ChooseOptionItem(
    val id: String,
    val label: String,
)
sealed interface ChooseOptionDialogState {
    data object Hidden : ChooseOptionDialogState
    data class ConfirmSelection(
        val selectedId: String,
        val selectedLabel: String,
    ) : ChooseOptionDialogState
}
sealed interface ChooseOptionUiEffect {
    data object NavigateBack : ChooseOptionUiEffect
    data object RequestCloseDialog : ChooseOptionUiEffect
    data object RequestOpenDialog : ChooseOptionUiEffect
    data object FinishSelectionAndLogin : ChooseOptionUiEffect
}
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
    fun setOptions(ids: List<String>, labels: List<String>) {
        val items = ids.zip(labels).map { (id, label) -> ChooseOptionItem(id, label) }
        _uiState.value = _uiState.value.copy(
            options = items,
            buttonsState = ChooseOptionButtonsState.fromCount(items.size)
        )
    }
    fun onOptionClick(item: ChooseOptionItem) {
        _uiState.value = _uiState.value.copy(
            dialogState = ChooseOptionDialogState.ConfirmSelection(
                selectedId = item.id,
                selectedLabel = item.label
            )
        )
        _uiEffect.tryEmit(ChooseOptionUiEffect.RequestOpenDialog)
    }
    fun onDialogDismiss() {
        _uiState.value = _uiState.value.copy(dialogState = ChooseOptionDialogState.Hidden)
        _uiEffect.tryEmit(ChooseOptionUiEffect.RequestCloseDialog)
    }
    fun onDialogAccept() {
        val dialog = _uiState.value.dialogState
        if (dialog is ChooseOptionDialogState.ConfirmSelection) {
            viewModelScope.launch {
                Logs.d(TAG, "sendShiftSelectedAnswer: selectedId = ${dialog.selectedId}")
                shiftStatusUseCase.sendShiftSelectedAnswer(
                    dialog.selectedId,
                    W2CLocation.getZoning()?.version ?: "00"
                )
                loginDriver()
                _uiEffect.emit(ChooseOptionUiEffect.NavigateBack)
                _uiState.value = _uiState.value.copy(dialogState = ChooseOptionDialogState.Hidden)
            }
        }
    }
    fun cancelSelectionAndLogoff() {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "cancelSelectionAndLogoff: cancelling shift selection")
            endShiftUseCase.endShift()
            bravoCentralUseCase.logoff()
            shiftStatusUseCase.setStatus(ifConstants.STATE_DISCONNECTED, false)
            _uiEffect.emit(ChooseOptionUiEffect.NavigateBack)
        }
    }
    fun loginDriver() {
        viewModelScope.launch {
            sessionUseCase.getSession()?.let { session ->
                session.numDriver?.let { numDriver ->
                    loginDriverUseCase.loginDriver(
                        numDriver,
                        session.pwdDriver ?: "",
                        session.reinforcement
                    )
                }
            }
        }
    }
}
