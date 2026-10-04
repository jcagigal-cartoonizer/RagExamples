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
import ifac.td.taxi.ui.screen.SplashScreenScreen
// # Block 153-2: import android.app.Application
data class SplashScreenUiState(
    val isReconnecting: Boolean = false,
    val showSplashVideo: Boolean = true,
    val isSkipped: Boolean = false,
    val showDialog: Boolean = false,
    val dialogState: SplashScreenHostCustomDialogState = SplashScreenHostCustomDialogState(),
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
