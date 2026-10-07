package ifac.td.taxi.compose.viewmodel
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
import ifac.td.taxi.viewmodel.MeetingSignViewModel
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
import ifac.td.taxi.ui.screen.MeetingSignScreen
// # Block 90-3: import android.app.Application
class MeetingSignComposeViewModel(
    application: Application,
    private val meetingSignUseCase: MeetingSignUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
) : AndroidViewModel(application) {
    lateinit var buttonsState : MeetingSignButtonsState
    lateinit var dialogState : MeetingSignDialogState
    private val _uiState = MutableStateFlow(MeetingSignUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<MeetingSignUiEffect>()
    val effects: SharedFlow<MeetingSignUiEffect> = _effects.asSharedFlow()
    init {
        dialogState = _uiState.value.dialog
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
