package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.MeetingSignUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class MeetingSignComposeViewModel(
    application: Application,
    private val meetingSignUseCase: MeetingSignUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MeetingSignUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<MeetingSignUiEffect>()
    val effects: SharedFlow<MeetingSignUiEffect> = _effects.asSharedFlow()
    init {
        viewModelScope.launch {
            val currentText = meetingSignUseCase.getMessageSignName().orEmpty()
            _uiState.update { it.copy(message = currentText) }
        }
    }
    fun onMessageTextChanged(text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(message = text) }
            meetingSignUseCase.setMessageSignText(text)
        }
    }
    fun onOptionsClicked() {
        _uiState.update { state ->
            val open = !state.isMenuOpen
            state.copy(
                isMenuOpen = open,
                buttonsState = if (open) openedButtonsState(state) else closedButtonsState(state)
            )
        }
    }
    fun onEditClicked() {
        viewModelScope.launch {
            val currentName = uiState.value.message.takeIf { it.isNotBlank() }
            _effects.emit(
                MeetingSignUiEffect.OpenEditDialog(
                    isCancellable = true,
                    currentName = currentName
                )
            )
        }
    }
    fun onDispatchClicked() {
        viewModelScope.launch {
            if (uiState.value.fromDispatch && !uiState.value.isInSettings) {
                _effects.emit(MeetingSignUiEffect.NavigateToInfoDispatch)
            } else {
                _effects.emit(MeetingSignUiEffect.NavigateBack)
            }
        }
    }
    fun onDialogConfirm(text: String?) {
        if (!text.isNullOrBlank()) {
            onMessageTextChanged(text)
        } else {
            viewModelScope.launch {
                _effects.emit(MeetingSignUiEffect.ShowToast(R.string.editText_error_no_text))
            }
        }
    }
    fun onDialogDismiss() {
        _uiState.update { it.copy(dialogState = null) }
    }
    fun setEnvironment(fromDispatch: Boolean, isInSettings: Boolean, textColor: Int, backgroundColor: Int) {
        _uiState.update {
            it.copy(
                fromDispatch = fromDispatch,
                isInSettings = isInSettings,
                textColor = textColor,
                backgroundColor = backgroundColor,
                buttonsState = closedButtonsState(it)
            )
        }
    }
    fun onShiftStatusChanged(lastStatus: Int?, currentStatus: Int?) {
        if (checkShiftStatusChangeToHired(lastStatus, currentStatus)) {
            viewModelScope.launch {
                if (uiState.value.fromDispatch && !uiState.value.isInSettings) {
                    _effects.emit(MeetingSignUiEffect.NavigateToInfoDispatch)
                }
            }
        }
        _uiState.update { it.copy(lastShiftStatus = currentStatus) }
    }
    fun checkShiftStatusChangeToHired(lastStatus: Int?, currentStatus: Int?): Boolean {
        return lastStatus != null && currentStatus != null &&
            shiftStatusUseCase.isVacant(lastStatus) &&
            shiftStatusUseCase.isHired(currentStatus)
    }
    fun closedButtonsState(state: MeetingSignUiState): MeetingSignButtonsState {
        val dispatchIcon = if (state.fromDispatch && !state.isInSettings) R.drawable.more else R.drawable.back
        return state.buttonsState.copy(
            options = state.buttonsState.options.copy(
                visible = true,
                alpha = 1f,
                translationY = 0f,
                rotation = 0f,
            ),
            edit = state.buttonsState.edit.copy(
                visible = false,
                alpha = 0f,
                translationY = 100f,
                rotation = 0f,
            ),
            dispatch = state.buttonsState.dispatch.copy(
                iconRes = dispatchIcon,
                visible = false,
                alpha = 0f,
                translationY = 100f,
                rotation = 0f,
            )
        )
    }
    fun openedButtonsState(state: MeetingSignUiState): MeetingSignButtonsState {
        return state.buttonsState.copy(
            options = state.buttonsState.options.copy(rotation = 180f),
            edit = state.buttonsState.edit.copy(visible = true, alpha = 1f, translationY = 0f),
            dispatch = state.buttonsState.dispatch.copy(visible = true, alpha = 1f, translationY = 0f),
        )
    }
}
