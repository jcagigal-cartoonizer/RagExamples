package ifac.td.taxi.ui.screen.components
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
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.ChangeDriverPinScreen
// # Block 73-2: import android.app.Application
class ChangeDriverPinComposeViewModel(context: Application) : BaseViewModel(context) {
    companion object {
        fun fromViewModel(viewModel: ChangeDriverPinViewModel): ChangeDriverPinComposeViewModel {
            val composeViewModel = ChangeDriverPinComposeViewModel(
                context : viewModel.context,
            )
            return composeViewModel
        }

    private val _uiState = MutableStateFlow(ChangeDriverPinUiState())
    val uiState: StateFlow<ChangeDriverPinUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ChangeDriverPinUiEffect>()
    val uiEffect: SharedFlow<ChangeDriverPinUiEffect> = _uiEffect.asSharedFlow()
    private val _pendingDialogAction = MutableStateFlow<PendingDialogAction?>(null)
    fun onEvent(event: ChangeDriverPinUiEvent) {
        when (event) {
            is ChangeDriverPinUiEvent.DriverNumberChanged -> {
                _uiState.update { it.copy(driverNumber = event.value) }
            }
            is ChangeDriverPinUiEvent.CurrentPinChanged -> {
                _uiState.update { it.copy(currentPin = event.value) }
            }
            is ChangeDriverPinUiEvent.NewPinChanged -> {
                _uiState.update { it.copy(newPin = event.value) }
            }
            is ChangeDriverPinUiEvent.RepeatPinChanged -> {
                _uiState.update { it.copy(repeatPin = event.value) }
            }
            ChangeDriverPinUiEvent.AcceptClicked -> handleAcceptClicked()
            ChangeDriverPinUiEvent.CancelClicked -> emitEffect(ChangeDriverPinUiEffect.NavigateBack)
            is ChangeDriverPinUiEvent.DialogButtonClicked -> {
                handleDialogButton(event.buttonType)
            }
        }
    }
    fun onDialogButtonClicked(buttonType: ButtonType) {
        onEvent(ChangeDriverPinUiEvent.DialogButtonClicked(buttonType))
    }
    fun handleAcceptClicked() {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            viewModelScope.launch {
                emitEffect(
                    ChangeDriverPinUiEffect.ShowDialog(
                        ChangeDriverPinCustomDialogStateCustomDialogState(
                            title = validationError,
                            description = null,
                            buttons = listOf(ButtonType.ACCEPT),
                            cancelable = false
                        )
                    )
                )
            }
            return
        }
        viewModelScope.launch {
            val userPresenter = UserModule.provideUserPresenter(
                object : ChangePasswordPinView {
                    override fun updateSuccess() {
                        _pendingDialogAction.value = PendingDialogAction.SuccessNavigateBack
                        viewModelScope.launch {
                            emitEffect(
                                ChangeDriverPinUiEffect.ShowDialog(
                                    ChangeDriverPinCustomDialogStateCustomDialogState(
                                        title = getStringResource(R.string.dialog_change_pin),
                                        description = null,
                                        buttons = listOf(ButtonType.ACCEPT),
                                        cancelable = false
                                    )
                                )
                            )
                        }
                    }
                    override fun updateFailure() {
                        _pendingDialogAction.value = PendingDialogAction.NoOp
                        viewModelScope.launch {
                            emitEffect(
                                ChangeDriverPinUiEffect.ShowDialog(
                                    ChangeDriverPinCustomDialogStateCustomDialogState(
                                        title = getStringResource(R.string.dialog_error_change_pin),
                                        description = null,
                                        buttons = listOf(ButtonType.ACCEPT),
                                        cancelable = false
                                    )
                                )
                            )
                        }
                    }
                },
                context
            )
            if (userPresenter != null) {
                _uiState.update { it.copy(isLoading = true) }
                changeDriverPin(
                    userPresenter = userPresenter,
                    driverNumber = state.driverNumber.trim(),
                    oldPin = state.currentPin.trim(),
                    newPin = state.newPin.trim()
                )
                _uiState.update { it.copy(isLoading = false) }
            } else {
                emitEffect(
                    ChangeDriverPinUiEffect.ShowDialog(
                        ChangeDriverPinCustomDialogStateCustomDialogState(
                            title = getStringResource(R.string.dialog_user_error),
                            description = null,
                            buttons = listOf(ButtonType.ACCEPT),
                            cancelable = false
                        )
                    )
                )
            }
        }
    }
    fun handleDialogButton(buttonType: ButtonType) {
        when (_pendingDialogAction.value) {
            PendingDialogAction.SuccessNavigateBack -> {
                if (buttonType == ButtonType.ACCEPT) {
                    _pendingDialogAction.value = null
                    viewModelScope.launch {
                        emitEffect(ChangeDriverPinUiEffect.NavigateBack)
                    }
                }
            }
            PendingDialogAction.NoOp -> {
                _pendingDialogAction.value = null
            }
            null -> Unit
        }
    }
    fun changeDriverPin(userPresenter: UserPresenter, driverNumber: String, oldPin: String, newPin: String) {
        userPresenter.changeDriverPin(driverNumber, oldPin, newPin, context)
    }
    fun validate(state: ChangeDriverPinUiState): String? {
        return when {
            state.driverNumber.isBlank() ||
                state.currentPin.isBlank() ||
                state.newPin.isBlank() ||
                state.repeatPin.isBlank() ->
                getStringResource(R.string.dialog_fill_inputs)
            state.newPin != state.repeatPin ->
                getStringResource(R.string.dialog_no_match_pin)
            state.currentPin == state.newPin ->
                getStringResource(R.string.dialog_equal_pin)
            else -> null
        }
    }
    private suspend fun emitEffect(effect: ChangeDriverPinUiEffect) {
        _uiEffect.emit(effect)
    }
    fun getStringResource(resId: Int): String {
        return context.getString(resId)
    }
}
private enum class PendingDialogAction {
    SuccessNavigateBack,
    NoOp
}
