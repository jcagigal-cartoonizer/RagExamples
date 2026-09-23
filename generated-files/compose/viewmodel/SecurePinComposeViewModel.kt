package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.SecurePinUiState
import ifac.td.taxi.ui.screen.components.SecurePinButtonsState
import ifac.td.taxi.ui.screen.components.SecurePinUiEffect
import ifac.td.taxi.ui.screen.components.SecurePinUiEvent
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 210-4: import android.app.Application
class SecurePinComposeViewModel(
    application: Application,
    private val userPreferencesUseCase: UserPreferencesUseCase,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SecurePinUiState())
    val uiState: StateFlow<SecurePinUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<SecurePinUiEffect>()
    val effects: SharedFlow<SecurePinUiEffect> = _effects.asSharedFlow()
    fun onEvent(event: SecurePinUiEvent) {
        when (event) {
            SecurePinUiEvent.CheckSecurePin -> checkSecurePin()
            is SecurePinUiEvent.PinChanged -> updatePin(event.value)
            is SecurePinUiEvent.PinRepeatChanged -> updatePinRepeat(event.value)
            SecurePinUiEvent.AcceptClicked -> onAccept()
            SecurePinUiEvent.CancelClicked -> onCancel()
            SecurePinUiEvent.DialogDismissed -> {
                _uiState.update { it.copy(isDialogVisible = false) }
            }
            SecurePinUiEvent.DialogAccepted -> {
                _uiState.update { it.copy(isDialogVisible = false) }
                viewModelScope.launch {
                    _effects.emit(SecurePinUiEffect.NavigateBack)
                }
            }
        }
    }
    fun checkSecurePin() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSecurePin = userPreferencesUseCase.getSecurePin()
            val hasSecurePin = !currentSecurePin.isNullOrEmpty()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        hasSecurePin = hasSecurePin,
                        buttonsState = SecurePinButtonsState.from(hasSecurePin)
                    )
                }
            }
        }
    }
    fun updatePin(value: String) {
        _uiState.update {
            it.copy(
                pin = value,
                buttonsState = SecurePinButtonsState.from(it.hasSecurePin)
            )
        }
    }
    fun updatePinRepeat(value: String) {
        _uiState.update {
            it.copy(
                pinRepeat = value,
                buttonsState = SecurePinButtonsState.from(it.hasSecurePin)
            )
        }
    }
    fun onAccept() {
        val state = _uiState.value
        val pin = state.pin
        val pinRepeat = state.pinRepeat
        if (pin.isEmpty() && pinRepeat.isEmpty()) {
            savePin("")
            return
        }
        if (isValidPassword(pin) && isValidRepeatPassword(pinRepeat, pin)) {
            savePin(pin)
        }
    }
    fun onCancel() {
        viewModelScope.launch {
            _effects.emit(SecurePinUiEffect.NavigateBack)
        }
    }
    fun savePin(pin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesUseCase.insertSecurePin(pin)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isDialogVisible = true) }
            }
            _effects.emit(SecurePinUiEffect.ShowSuccessDialog)
        }
    }
    fun isValidPassword(pin: String): Boolean {
        return pin.length >= 4
    }
    fun isValidRepeatPassword(pinRepeat: String, pin: String): Boolean {
        return pinRepeat.isNotEmpty() && pinRepeat == pin
    }
}
