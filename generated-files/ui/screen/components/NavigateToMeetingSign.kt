package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ToolsButtonState = ToolsButtonState
import ifac.td.taxi.ui.screen.components.ToolsButtonsState
import ifac.td.taxi.compose.viewmodel.ToolsComposeViewModel
import ifac.td.taxi.ui.screen.components.ToolsCustomDialogState = ToolsCustomDialogState
import ifac.td.taxi.ui.screen.components.ToolsButtonColors
import ifac.td.taxi.ui.screen.components.ToolsCustomDialogState
import ifac.td.taxi.ui.screen.components.ToolsUiEvent
import ifac.td.taxi.ui.screen.components.ToolsButtonState
import ifac.td.taxi.ui.screen.components.ToolsUiState
import ifac.td.taxi.ui.screen.components.ToolsUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
                        dialogState = ToolsCustomDialogState(
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
    val dialogState: ToolsCustomDialogState = ToolsCustomDialogState(),
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
data class ToolsCustomDialogState(
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
