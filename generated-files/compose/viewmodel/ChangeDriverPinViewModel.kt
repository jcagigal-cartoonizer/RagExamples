package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import ifac.td.taxi.ui.screen.ChangeDriverPinButtonsState
import ifac.td.taxi.ui.screen.ChangeDriverPinUiEffect
import ifac.td.taxi.ui.screen.ChangeDriverPinUiEvent
import ifac.td.taxi.ui.screen.ChangeDriverPinUiState
import ifac.td.taxi.ui.screen.CustomDialogState
import ifac.td.taxi.ui.screen.ChangeDriverPinDialogButtonType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class ChangeDriverPinComposeViewModel(
    context: Application
) : AndroidViewModel(context) {
    private val app = context
    private val _uiState = MutableStateFlow(ChangeDriverPinUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ChangeDriverPinUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()
    private var cachedUserPresenter: UserPresenter? = null
    fun onEvent(event: ChangeDriverPinUiEvent) {
        when (event) {
            is ChangeDriverPinUiEvent.CurrentDriverNumberChanged -> {
                _uiState.update { it.copy(currentDriverNumber = event.value) }
            }
            is ChangeDriverPinUiEvent.CurrentPinChanged -> {
                _uiState.update { it.copy(currentPin = event.value) }
            }
            is ChangeDriverPinUiEvent.NewPinChanged -> {
                _uiState.update { it.copy(newPin = event.value) }
            }
            is ChangeDriverPinUiEvent.RepeatNewPinChanged -> {
                _uiState.update { it.copy(repeatNewPin = event.value) }
            }
            ChangeDriverPinUiEvent.AcceptClicked -> handleAccept()
            ChangeDriverPinUiEvent.CancelClicked -> emitEffect(ChangeDriverPinUiEffect.NavigateBack)
            ChangeDriverPinUiEvent.DialogConfirmed -> {
                emitEffect(ChangeDriverPinUiEffect.NavigateBack)
            }
            ChangeDriverPinUiEvent.DialogDismissed -> Unit
        }
    }
    fun handleAccept() {
        val state = _uiState.value
        when {
            state.currentDriverNumber.isBlank() ||
                state.currentPin.isBlank() ||
                state.newPin.isBlank() ||
                state.repeatNewPin.isBlank() -> {
                emitDialog(app.getString(R.string.dialog_fill_inputs))
            }
            state.newPin != state.repeatNewPin -> {
                emitDialog(app.getString(R.string.dialog_no_match_pin))
            }
            state.currentPin == state.newPin -> {
                emitDialog(app.getString(R.string.dialog_equal_pin))
            }
            else -> {
                emitEffect(ChangeDriverPinUiEffect.RequestUserPresenter)
            }
        }
    }
    fun onUserPresenterReady() {
        val presenter = cachedUserPresenter
        val state = _uiState.value
        if (presenter == null) {
            emitDialog(app.getString(R.string.dialog_user_error), navigateBackOnConfirm = true)
            return
        }
        emitEffect(
            ChangeDriverPinUiEffect.ChangePin(
                driverNumber = state.currentDriverNumber.trim(),
                oldPin = state.currentPin.trim(),
                newPin = state.newPin.trim()
            )
        )
    }
    fun getUserPresenter(view: ChangePasswordPinView) {
        viewModelScope.launch {
            cachedUserPresenter = UserModule.provideUserPresenter(view, app)
            onUserPresenterReady()
        }
    }
    fun changeDriverPin(
        userPresenter: UserPresenter,
        driverNumber: String,
        oldPin: String,
        newPin: String
    ) {
        userPresenter.changeDriverPin(driverNumber, oldPin, newPin, app)
    }
    fun emitDialog(
        title: String,
        navigateBackOnConfirm: Boolean = false
    ) {
        emitEffect(
            ChangeDriverPinUiEffect.ShowDialog(
                CustomDialogState(
                    title = title,
                    buttons = listOf(ChangeDriverPinDialogButtonType.Accept)
                )
            )
        )
        if (navigateBackOnConfirm) {
        }
    }
    fun emitEffect(effect: ChangeDriverPinUiEffect) {
        _uiEffect.tryEmit(effect)
    }
    fun onPinChangeSuccess() {
        emitEffect(
            ChangeDriverPinUiEffect.ShowDialog(
                CustomDialogState(
                    title = app.getString(R.string.dialog_change_pin),
                    buttons = listOf(ChangeDriverPinDialogButtonType.Accept)
                )
            )
        )
    }
    fun onPinChangeFailure() {
        emitEffect(
            ChangeDriverPinUiEffect.ShowDialog(
                CustomDialogState(
                    title = app.getString(R.string.dialog_error_change_pin),
                    buttons = listOf(ChangeDriverPinDialogButtonType.Accept)
                )
            )
        )
    }
    fun updateButtonsState(state: ChangeDriverPinButtonsState) {
        _uiState.update { it.copy(buttonsState = state) }
    }
}
