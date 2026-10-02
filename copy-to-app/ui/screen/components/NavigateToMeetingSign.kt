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
// # Block 114-2: import android.app.Application
class ToolsComposeViewModelCompose(
    application: Application,
    private val userPreferencesUseCase: UserPreferencesUseCase
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ToolsUiState())
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ToolsUiEffect>()
    val effects: SharedFlow<ToolsUiEffect> = _effects.asSharedFlow()
    init {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let { prefs ->
                _uiState.update {
                    it.copy(
                        meetingSignColors = MeetingSignColors(
                            textColor = prefs.meetingSignTextColor,
                            backgroundColor = prefs.meetingSignBackgroundColor
                        ),
                        buttons = it.buttons.copy(
                            meetingSign = it.buttons.meetingSign.copy(
                                enabled = true,
                                visible = true
                            )
                        )
                    )
                }
            }
        }
    }
    fun onEvent(event: ToolsUiEvent) {
        when (event) {
            ToolsUiEvent.RequirementsClicked -> {
                viewModelScope.launch {
                    _effects.emit(ToolsUiEffect.NavigateToRequirements)
                }
            }
            ToolsUiEvent.MeetingSignClicked -> {
                val colors = uiState.value.meetingSignColors
                viewModelScope.launch {
                    _effects.emit(
                        ToolsUiEffect.NavigateToMeetingSign(
                            textColor = colors.textColor,
                            backgroundColor = colors.backgroundColor
                        )
                    )
                }
            }
            ToolsUiEvent.DialogDismissed -> {
                _uiState.update { it.copy(dialogState = it.dialogState.copy(visible = false)) }
            }
            ToolsUiEvent.DialogConfirmed -> {
                _uiState.update { it.copy(dialogState = it.dialogState.copy(visible = false)) }
            }
            ToolsUiEvent.ShowInfoDialog -> {
                _uiState.update {
                    it.copy(
                        dialogState = ToolsFragmentComposeHostCustomDialogState(
                            visible = true,
                            title = "Info",
                            message = "Example dialog content",
                            confirmText = "OK",
                            dismissText = "Cancel"
                        )
                    )
                }
                viewModelScope.launch {
                    _effects.emit(ToolsUiEffect.ShowDialog)
                }
            }
        }
    }
}
data class ToolsUiState(
    val meetingSignColors: MeetingSignColors = MeetingSignColors(),
    val buttons: ToolsButtonsState = ToolsButtonsState.default(),
    val dialogState: ToolsFragmentComposeHostCustomDialogState = ToolsFragmentComposeHostCustomDialogState(),
)
data class MeetingSignColors(
    val textColor: Int = 0,
    val backgroundColor: Int = 0
)
data class ToolsButtonsState(
    val requirements: ToolsButtonState = ToolsButtonState(),
    val meetingSign: ToolsButtonState = ToolsButtonState(),
) {
    companion object {
        fun default() = ToolsButtonsState(
            requirements = ToolsButtonState(
                visible = true,
                enabled = true,
                colors = ToolsButtonColors.primary()
            ),
            meetingSign = ToolsButtonState(
                visible = true,
                enabled = true,
                colors = ToolsButtonColors.primary()
            )
        )
    }
}
data class ToolsButtonState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val colors: ToolsButtonColors = ToolsButtonColors.primary()
)
data class ToolsButtonColors(
    val containerColor: Long,
    val contentColor: Long,
    val disabledContainerColor: Long,
    val disabledContentColor: Long,
    val borderColor: Long = containerColor
) {
    companion object {
        fun primary() = ToolsButtonColors(
            containerColor = 0xFF1E88E5,
            contentColor = 0xFFFFFFFF,
            disabledContainerColor = 0xFF9E9E9E,
            disabledContentColor = 0xFFE0E0E0
        )
    }
}
data class ToolsFragmentComposeHostCustomDialogState(
    val visible: Boolean = false,
    val title: String = "",
    val message: String = "",
    val confirmText: String = "OK",
    val dismissText: String = "Cancel",
    val showDismissButton: Boolean = true
)
sealed interface ToolsUiEvent {
    data object RequirementsClicked : ToolsUiEvent
    data object MeetingSignClicked : ToolsUiEvent
    data object DialogDismissed : ToolsUiEvent
    data object DialogConfirmed : ToolsUiEvent
    data object ShowInfoDialog : ToolsUiEvent
}
sealed interface ToolsUiEffect {
    data object NavigateToRequirements : ToolsUiEffect
    data class NavigateToMeetingSign(
        val textColor: Int,
        val backgroundColor: Int
    ) : ToolsUiEffect
    data object ShowDialog : ToolsUiEffect
}
