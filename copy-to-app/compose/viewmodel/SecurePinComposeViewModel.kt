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
// # Block 208-4: import android.app.Application
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
