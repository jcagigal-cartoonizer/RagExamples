package ifac.td.taxi.ui.screen.components
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
// # Block 5-1: import android.app.Application
data class PortugalSettingsUiState(
    val atcud: String = "",
    val sequenceNumber: String = "",
    val document: String = "",
    val resetHashEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isPinDialogVisible: Boolean = false,
    val isChangePinDialogVisible: Boolean = false,
)
sealed interface PortugalSettingsUiEvent {
    data object ScreenShown : PortugalSettingsUiEvent
    data class AtcudChanged(val value: String) : PortugalSettingsUiEvent
    data class SequenceNumberChanged(val value: String) : PortugalSettingsUiEvent
    data class DocumentChanged(val value: String) : PortugalSettingsUiEvent
    data class ResetHashChanged(val checked: Boolean) : PortugalSettingsUiEvent
    data object ClickAccept : PortugalSettingsUiEvent
    data object ClickCancel : PortugalSettingsUiEvent
    data object ClickChangePin : PortugalSettingsUiEvent
    data class PinEntered(val pin: String) : PortugalSettingsUiEvent
    data class NewPinEntered(val pin: String) : PortugalSettingsUiEvent
    data object DismissDialog : PortugalSettingsUiEvent
}
sealed interface PortugalSettingsUiEffect {
    data object NavigateBack : PortugalSettingsUiEffect
    data object ShowPinIncorrectToast : PortugalSettingsUiEffect
    data object OpenCurrentPinDialog : PortugalSettingsUiEffect
    data object OpenNewPinDialog : PortugalSettingsUiEffect
}
class PortugalSettingsComposeViewModel(
    application: Application,
    private val portugalUseCase: PortugalUseCase
) : AndroidViewModel(application) {
    private val TAG = this.javaClass.simpleName
    private val _uiState = MutableStateFlow(PortugalSettingsUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<PortugalSettingsUiEffect>()
    val uiEffect: SharedFlow<PortugalSettingsUiEffect> = _uiEffect.asSharedFlow()
    fun onEvent(event: PortugalSettingsUiEvent) {
        when (event) {
            PortugalSettingsUiEvent.ScreenShown -> getPortugalData()
            is PortugalSettingsUiEvent.AtcudChanged ->
                _uiState.value = _uiState.value.copy(atcud = event.value)
            is PortugalSettingsUiEvent.SequenceNumberChanged ->
                _uiState.value = _uiState.value.copy(sequenceNumber = event.value)
            is PortugalSettingsUiEvent.DocumentChanged ->
                _uiState.value = _uiState.value.copy(document = event.value)
            is PortugalSettingsUiEvent.ResetHashChanged ->
                _uiState.value = _uiState.value.copy(resetHashEnabled = event.checked)
            PortugalSettingsUiEvent.ClickCancel -> {
                viewModelScope.launch { _uiEffect.emit(PortugalSettingsUiEffect.NavigateBack) }
            }
            PortugalSettingsUiEvent.ClickAccept -> {
                val current = _uiState.value
                if (!current.resetHashEnabled) {
                    updatePortugalData(
                        atcud = current.atcud,
                        sequenceNumber = current.sequenceNumber.toIntOrNull() ?: 0,
                        document = current.document.toIntOrNull() ?: 0
                    )
                } else {
                    checkResetHashData()
                }
            }
            PortugalSettingsUiEvent.ClickChangePin -> {
                viewModelScope.launch { _uiEffect.emit(PortugalSettingsUiEffect.OpenCurrentPinDialog) }
            }
            is PortugalSettingsUiEvent.PinEntered -> checkPinPortugal(event.pin)
            is PortugalSettingsUiEvent.NewPinEntered -> updatePortugalPin(event.pin)
            PortugalSettingsUiEvent.DismissDialog -> {
                _uiState.value = _uiState.value.copy(
                    isPinDialogVisible = false,
                    isChangePinDialogVisible = false
                )
            }
        }
    }
    fun getPortugalData() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = portugalUseCase.getPortugalData()
            _uiState.value = _uiState.value.copy(
                atcud = data?.atcud.orEmpty(),
                sequenceNumber = data?.sequenceNumber?.toString().orEmpty(),
                document = data?.numTicketPrint?.toString().orEmpty()
            )
        }
    }
    fun updatePortugalData(atcud: String, sequenceNumber: Int, document: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val portugalData = portugalUseCase.getPortugalData()
            val portugalModel = PortugalModel(
                atcud = atcud,
                sequenceNumber = sequenceNumber,
                numTicketPrint = document,
                portugalPassword = portugalData?.portugalPassword.orEmpty()
            )
            portugalUseCase.updatePortugalData(portugalModel)
            _uiEffect.emit(PortugalSettingsUiEffect.NavigateBack)
        }
    }
    fun checkResetHashData() {
        val numTicket = _uiState.value.document.toIntOrNull() ?: 1
        if (numTicket > 1) {
            viewModelScope.launch {
                _uiEffect.emit(PortugalSettingsUiEffect.ShowPinIncorrectToast) // reuse effect? no
            }
        } else {
            updatePortugalData(
                atcud = _uiState.value.atcud,
                sequenceNumber = _uiState.value.sequenceNumber.toIntOrNull() ?: 0,
                document = _uiState.value.document.toIntOrNull() ?: 0
            )
        }
    }
    fun updatePortugalPin(portugalPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val portugalData = portugalUseCase.getPortugalData()
            portugalData?.let {
                it.portugalPassword = it.encryptPortugalPassword(portugalPassword)
                portugalUseCase.updatePortugalData(it)
            }
        }
    }
    fun checkPinPortugal(editTextString: String) {
        viewModelScope.launch {
            try {
                val pin = editTextString.toIntOrNull()
                val portugalCode = BuildConfig.portugal_p.toInt()
                val portugalPassword = portugalUseCase.getPortugalData()
                val valid = if (portugalPassword != null && portugalPassword.portugalPassword.isNotBlank()) {
                    portugalPassword.encryptPortugalPassword(pin.toString()) == portugalPassword.portugalPassword
                } else {
                    pin == portugalCode
                }
                if (valid) {
                    _uiEffect.emit(PortugalSettingsUiEffect.OpenNewPinDialog)
                } else {
                    _uiEffect.emit(PortugalSettingsUiEffect.ShowPinIncorrectToast)
                }
            } catch (e: Exception) {
                Logs.d(TAG, "checkPinPortugal: $e")
            }
        }
    }
}
