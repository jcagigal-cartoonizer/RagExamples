package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.PortugalSettingsComposeViewModel
import ifac.td.taxi.ui.screen.components.PortugalSettingsUiState
import ifac.td.taxi.ui.screen.components.PortugalSettingsUiEffect
import ifac.td.taxi.ui.screen.components.PortugalSettingsUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
