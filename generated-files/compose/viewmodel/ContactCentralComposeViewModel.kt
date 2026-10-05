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
import ifac.td.taxi.ui.screen.ContactCentralScreen
// # Block 236-3: import android.app.Application
class ContactCentralComposeViewModel(
    private val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val predefinedMessagesUseCase: PredefinedMessagesUseCase,
    private val voicePetitionUseCase: VoicePetitionUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    application: Application
) : AndroidViewModel(application) {
    private val TAG = "ContactCentralComposeVM"
    private val _uiState = MutableStateFlow(ContactCentralUiState())
    val uiState: StateFlow<ContactCentralUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ContactCentralUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<ContactCentralUiEffect> = _uiEffect.asSharedFlow()
    init {
        viewModelScope.launch(Dispatchers.IO) {
            val hasMessages = predefinedMessagesUseCase.getPredefinedMessages().isNotEmpty()
            val hasShortBreak = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.hasShortBreak ?: false
            _uiState.update {
                it.copy(
                    hasPredefinedMessages = hasMessages,
                    hasShortBreak = hasShortBreak,
                )
            }
        }
    }
    fun onEvent(event: ContactCentralUiEvent) {
        when (event) {
            ContactCentralUiEvent.ScreenStarted -> {
                checkITopTaximeter()
            }
            ContactCentralUiEvent.CancelClicked -> {
                emitEffect(ContactCentralUiEffect.NavigateBack)
            }
            ContactCentralUiEvent.ShortBreakClicked -> handleShortBreakClicked()
            ContactCentralUiEvent.VoiceCallClicked -> handleVoiceCallClicked()
            ContactCentralUiEvent.MessagesClicked -> {
                emitEffect(ContactCentralUiEffect.NavigateToPredefinedMessages)
            }
            ContactCentralUiEvent.InformationClicked -> {
                emitEffect(ContactCentralUiEffect.NavigateToInformationMessages)
            }
            ContactCentralUiEvent.DialogAccepted -> {
                sendVoiceRequest(true)
                emitEffect(ContactCentralUiEffect.HideVoiceRequestDialog)
                emitEffect(ContactCentralUiEffect.NavigateBack)
            }
            ContactCentralUiEvent.DialogCancelled -> {
                emitEffect(ContactCentralUiEffect.HideVoiceRequestDialog)
            }
        }
    }
    fun onSharedStateChanged(
        shortBreakStatus: ShortBreakStatus?,
        voiceValue: Boolean,
        zone: String?,
        currentShiftStatus: Int?,
    ) {
        _uiState.update {
            it.copy(
                shortBreakStatus = shortBreakStatus,
                voiceValue = voiceValue,
                zone = zone,
                currentShiftStatus = currentShiftStatus
            )
        }
    }
    fun onShortBreakStatusForced() {
        emitEffect(ContactCentralUiEffect.ShowToast(R.string.short_break_forced))
    }
    fun isHired(currentStatus: Int): Boolean = shiftStatusUseCase.isHired(currentStatus)
    fun buildButtonsState(): ContactCentralButtonsState {
        val state = _uiState.value
        return ContactCentralButtonsState.from(
            hasPredefinedMessages = state.hasPredefinedMessages,
            hasShortBreak = state.hasShortBreak,
            isITopTaximeter = state.isITopTaximeter,
            shortBreakStatus = state.shortBreakStatus,
            voiceValue = state.voiceValue,
            zone = state.zone,
            currentShiftStatus = state.currentShiftStatus,
            isHired = ::isHired
        )
    }
    fun handleShortBreakClicked() {
        val state = _uiState.value
        if (state.shortBreakStatus == ShortBreakStatus.IN_SHORT_BREAK_FORCED) {
            onShortBreakStatusForced()
            return
        }
        emitEffect(ContactCentralUiEffect.StartShortBreakLoading)
        emitEffect(ContactCentralUiEffect.RequestShortBreakAction)
    }
    fun handleVoiceCallClicked() {
        if (_uiState.value.voiceValue) {
            _uiState.update { it.copy(dialog = ContactCentralDialogState(isVisible = true, title = context.getString(R.string.btn_voice_request))) }
            emitEffect(ContactCentralUiEffect.ShowVoiceRequestDialog)
        } else {
            sendVoiceRequest(false)
            emitEffect(ContactCentralUiEffect.NavigateBack)
        }
    }
    fun sendVoiceRequest(value: Boolean?) {
        voicePetitionUseCase.voiceRequest(value ?: true)
    }
    fun checkITopTaximeter() {
        viewModelScope.launch {
            val isITop = bluetoothLocalUseCase.isCurrentBluetoothItop()
            Logs.d(TAG, "isITopTaximeter: $isITop")
            _uiState.update { it.copy(isITopTaximeter = isITop) }
        }
    }
    fun emitEffect(effect: ContactCentralUiEffect) {
        _uiEffect.tryEmit(effect)
    }
}
