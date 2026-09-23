package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.SplashScreenComposeViewModel
import ifac.td.taxi.ui.screen.components.SplashScreenButtonsState = SplashScreenButtonsState
import ifac.td.taxi.ui.screen.components.SplashScreenCustomDialogState = SplashScreenCustomDialogState
import ifac.td.taxi.ui.screen.components.SplashScreenUiState
import ifac.td.taxi.ui.screen.components.SplashScreenUiEvent
import ifac.td.taxi.ui.screen.components.SplashScreenUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 153-2: import android.app.Application
data class SplashScreenUiState(
    val isReconnecting: Boolean = false,
    val showSplashVideo: Boolean = true,
    val isSkipped: Boolean = false,
    val showDialog: Boolean = false,
    val dialogState: SplashScreenCustomDialogState = SplashScreenCustomDialogState(),
    val buttonsState: SplashScreenButtonsState = SplashScreenButtonsState()
)
sealed class SplashScreenUiEvent {
    data object ScreenStarted : SplashScreenUiEvent()
    data object VideoPrepared : SplashScreenUiEvent()
    data object VideoCompleted : SplashScreenUiEvent()
    data object VideoError : SplashScreenUiEvent()
    data object TimeoutReached : SplashScreenUiEvent()
    data object SkipRequested : SplashScreenUiEvent()
    data object DialogConfirm : SplashScreenUiEvent()
    data object DialogDismiss : SplashScreenUiEvent()
    data class ReconnectionChecked(val serviceWasActive: Boolean) : SplashScreenUiEvent()
}
sealed class SplashScreenUiEffect {
    data object NavigateToLegalText : SplashScreenUiEffect()
    data object NavigateToWelcome : SplashScreenUiEffect()
    data object HideSplash : SplashScreenUiEffect()
    data object ShowSkipDialog : SplashScreenUiEffect()
}
class SplashScreenComposeViewModel(
    application: Application,
    private val sessionUseCase: SessionUseCase
) : BaseViewModel(application) {
    private val TAG = "SplashScreenViewModel"
    private val SPLASH_TIMEOUT = 4800L
    private val _uiState = MutableStateFlow(SplashScreenUiState())
    val uiState: StateFlow<SplashScreenUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<SplashScreenUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<SplashScreenUiEffect> = _uiEffect.asSharedFlow()
    fun onEvent(event: SplashScreenUiEvent) {
        when (event) {
            SplashScreenUiEvent.ScreenStarted -> {
                checkIsReconnecting()
                startSplashTimeout()
            }
            SplashScreenUiEvent.VideoPrepared -> {
                Logs.d(TAG, "Video prepared")
            }
            SplashScreenUiEvent.VideoCompleted,
            SplashScreenUiEvent.VideoError,
            SplashScreenUiEvent.TimeoutReached -> {
                if (!_uiState.value.isSkipped) {
                    endSplash()
                }
            }
            SplashScreenUiEvent.SkipRequested -> {
                endSplashWithoutCheck()
            }
            SplashScreenUiEvent.DialogConfirm -> {
                _uiState.update { it.copy(showDialog = false) }
                viewModelScope.launch {
                    _uiEffect.emit(SplashScreenUiEffect.NavigateToLegalText)
                }
            }
            SplashScreenUiEvent.DialogDismiss -> {
                _uiState.update { it.copy(showDialog = false) }
                viewModelScope.launch {
                    _uiEffect.emit(SplashScreenUiEffect.NavigateToWelcome)
                }
            }
            is SplashScreenUiEvent.ReconnectionChecked -> {
                _uiState.update { it.copy(isReconnecting = event.serviceWasActive) }
                if (event.serviceWasActive) {
                    viewModelScope.launch {
                        delay(50)
                        if (!_uiState.value.isSkipped) {
                            endSplashWithoutCheck()
                        }
                    }
                }
            }
        }
    }
    fun checkIsReconnecting() {
        viewModelScope.launch(Dispatchers.IO) {
            val serviceWasActive = sessionUseCase.getServiceWasActive()
            Logs.d(TAG, "checkIsReconnecting serviceWasActive: $serviceWasActive")
            _uiEffect.emit(SplashScreenUiEffect.HideSplash)
            onEvent(SplashScreenUiEvent.ReconnectionChecked(serviceWasActive))
        }
    }
    fun startSplashTimeout() {
        viewModelScope.launch {
            delay(SPLASH_TIMEOUT)
            onEvent(SplashScreenUiEvent.TimeoutReached)
        }
    }
    fun endSplash() {
        _uiState.update {
            it.copy(
                showSplashVideo = false,
                showDialog = false
            )
        }
        viewModelScope.launch {
            _uiEffect.emit(SplashScreenUiEffect.HideSplash)
            _uiEffect.emit(SplashScreenUiEffect.NavigateToWelcome)
        }
    }
    fun endSplashWithoutCheck() {
        _uiState.update {
            it.copy(
                showSplashVideo = false,
                isSkipped = true,
                showDialog = false
            )
        }
        viewModelScope.launch {
            _uiEffect.emit(SplashScreenUiEffect.HideSplash)
            _uiEffect.emit(SplashScreenUiEffect.NavigateToLegalText)
        }
    }
}
