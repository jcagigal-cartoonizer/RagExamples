package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.InformationMessageUiEffect
import ifac.td.taxi.compose.viewmodel.InformationMessageComposeViewModel
import ifac.td.taxi.ui.screen.components.InformationMessageUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 127-2: import android.app.Application
data class InformationMessageUiState(
    val informationMessages: List<String> = emptyList(),
    val selectedInformationMessage: Pair<Int, String>? = null,
    val dialogVisible: Boolean = false,
    val dialogTitle: String = "",
    val isLoading: Boolean = false
)
sealed interface InformationMessageUiEffect {
    data object NavigateBack : InformationMessageUiEffect
    data object NavigateBackAfterSend : InformationMessageUiEffect
    data class ShowToast(val messageRes: Int) : InformationMessageUiEffect
}
class InformationMessageComposeViewModel(
    private val alfaMessageHandler: AlfaMessageHandler,
    private val informationMessageUseCase: InformationMessagesUseCase,
    private val context: Application
) : ViewModel() {
    private val TAG = "InformationMessageComposeVM"
    private val _uiState = MutableStateFlow(InformationMessageUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<InformationMessageUiEffect>()
    val effects: SharedFlow<InformationMessageUiEffect> = _effects.asSharedFlow()
    fun initVM() {
        if (_uiState.value.informationMessages.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val informationMessages = informationMessageUseCase.getInformationMessages().toList()
            Logs.d(TAG, "initVM: $informationMessages")
            _uiState.update {
                it.copy(informationMessages = informationMessages)
            }
        }
    }
    fun onMessageSelected(index: Int, message: String) {
        _uiState.update {
            it.copy(
                selectedInformationMessage = index to message,
                dialogVisible = true,
                dialogTitle = context.getString(R.string.request_information)
            )
        }
    }
    fun onDialogDismiss() {
        _uiState.update {
            it.copy(
                dialogVisible = false,
                selectedInformationMessage = null
            )
        }
    }
    fun onDialogButtonPressed(button: InformationMessageInformationMessageDialogButtonType, messageId: Int) {
        when (button) {
            InformationMessageInformationMessageDialogButtonType.CANCEL -> onDialogDismiss()
            InformationMessageInformationMessageDialogButtonType.ACCEPT -> {
                viewModelScope.launch(Dispatchers.IO) {
                    Logs.d(TAG, "sendInformationMessage: $messageId")
                    alfaMessageHandler.requestInformation(messageId, 0, 0)
                    _effects.emit(InformationMessageUiEffect.ShowToast(R.string.datos_enviados))
                    _effects.emit(InformationMessageUiEffect.NavigateBackAfterSend)
                    _uiState.update { it.copy(dialogVisible = false, selectedInformationMessage = null) }
                }
            }
        }
    }
    fun onCancelPressed() {
        viewModelScope.launch {
            _effects.emit(InformationMessageUiEffect.NavigateBack)
        }
    }
}
enum class InformationMessageInformationMessageDialogButtonType {
    CANCEL,
    ACCEPT
}
