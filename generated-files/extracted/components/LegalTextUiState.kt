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
// # Block 111-2: import android.app.Application
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
            // dialog-based handling
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
