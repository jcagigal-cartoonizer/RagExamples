package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.sdk.usecase.ShowLegalTextUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
data class LegalTextUiState(
    val legalText: String? = null,
    val buttonsState: LegalTextButtonsState = LegalTextButtonsState(),
    val isDialogVisible: Boolean = false,
    val dialog: LegalTextDialogState = LegalTextDialogState()
)
data class LegalTextDialogState(
    val title: String = "",
    val message: String = "",
    val confirmText: String = "",
    val dismissText: String = ""
)
sealed interface LegalTextUiEffect {
    data object NavigateToWelcome : LegalTextUiEffect
    data object ShowAcceptDialog : LegalTextUiEffect
    data object HideAcceptDialog : LegalTextUiEffect
}
class LegalTextComposeViewModelCompose(
    application: Application,
    private val showLegalTextUseCase: ShowLegalTextUseCase,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LegalTextUiState())
    val uiState: StateFlow<LegalTextUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<LegalTextUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<LegalTextUiEffect> = _effects.asSharedFlow()
    fun loadLegalText() {
        viewModelScope.launch {
            val legalText = showLegalTextUseCase.getLegalText()
            _uiState.update {
                it.copy(
                    legalText = legalText,
                    buttonsState = it.buttonsState.copy(
                        acceptEnabled = !legalText.isNullOrBlank()
                    )
                )
            }
        }
    }
    fun onAcceptClick() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDialogVisible = true,
                    dialog = LegalTextDialogState(
                        title = getApplication<Application>().getString(R.string.legal_text_dialog_title),
                        message = getApplication<Application>().getString(R.string.legal_text_dialog_message),
                        confirmText = getApplication<Application>().getString(R.string.accept),
                        dismissText = getApplication<Application>().getString(R.string.cancel)
                    )
                )
            }
            _effects.tryEmit(LegalTextUiEffect.ShowAcceptDialog)
        }
    }
    fun dismissDialog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDialogVisible = false) }
            _effects.tryEmit(LegalTextUiEffect.HideAcceptDialog)
        }
    }
    fun confirmDialog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDialogVisible = false) }
            _effects.emit(LegalTextUiEffect.HideAcceptDialog)
            _effects.emit(LegalTextUiEffect.NavigateToWelcome)
        }
    }
    fun setDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isDialogVisible = visible) }
    }
}
