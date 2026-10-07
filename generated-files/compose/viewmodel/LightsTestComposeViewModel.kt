package ifac.td.taxi.compose.viewmodel
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
import ifac.td.taxi.viewmodel.LightsTestViewModel
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
import ifac.td.taxi.ui.screen.LightsTestScreen
// # Block 130-4: import android.app.Application
class LightsTestComposeViewModel(
    application: Application,
    private val lightSkyGlassUseCase: LightSkyGlassUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
) : AndroidViewModel(application) {
    lateinit var buttonsState : LightsTestButtonsState
    lateinit var dialogState : LightsTestDialogState
    private val _uiState = MutableStateFlow(LightsTestUiState())
    val uiState: StateFlow<LightsTestUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<LightsTestUiEffect>()
    val uiEffect: SharedFlow<LightsTestUiEffect> = _uiEffect.asSharedFlow()
    fun onEvent(event: LightsTestUiEvent) {
        when (event) {
            LightsTestUiEvent.ScreenStarted -> loadBeeperVolume()
            LightsTestUiEvent.UvLightClicked -> handleUvClicked()
            LightsTestUiEvent.CourtesyLightClicked -> handleCourtesyClicked()
            is LightsTestUiEvent.BeeperVolumeChanged -> setBeeperVolume(event.volume)
            LightsTestUiEvent.DialogDismissed -> _uiState.update { it.copy(dialog = null) }
            LightsTestUiEvent.DialogConfirmed -> {
                _uiState.update { it.copy(dialog = null) }
                turnUVLight()
            }
            LightsTestUiEvent.BackClicked -> {
viewModelScope.launch { emitEffect(LightsTestUiEffect.NavigateBack)
}
}
        }
    }
    fun handleUvClicked() {
        val state = _uiState.value
        if (!state.buttonsState.uvButton.enabled) return
        // Optional dialog support example:
        _uiState.update { it.copy(dialog = LightsTestDialogState.ConfirmUvLight) }
    }
    fun handleCourtesyClicked() {
        val state = _uiState.value
        if (!state.buttonsState.courtesyButton.enabled) return
        turnCourtesyLight()
    }
    fun turnUVLight() {
        viewModelScope.launch {
            lightSkyGlassUseCase.turnUVLight()
            _uiState.update {
                it.copy(
                    isUvLightOn = true,
                    isUvLightAvailable = false
                )
            }
            delay(8000L)
            _uiState.update {
                it.copy(
                    isUvLightOn = false,
                    isUvLightAvailable = true
                )
            }
        }
    }
    fun turnCourtesyLight() {
        viewModelScope.launch {
            lightSkyGlassUseCase.turnCourtesyLight()
            _uiState.update { state ->
                state.copy(
                    isCourtesyLightOn = !state.isCourtesyLightOn
                )
            }
        }
    }
    fun setBeeperVolume(volume: Int) {
        viewModelScope.launch {
            lightSkyGlassUseCase.setBeeperVolume(volume)
            userPreferencesUseCase.insertBeeperVolumeValue(volume)
            _uiState.update { it.copy(beeperVolume = volume) }
        }
    }
    fun loadBeeperVolume() {
        viewModelScope.launch {
            val preferences = userPreferencesUseCase.getUserPreferences()
            if (preferences != null) {
                _uiState.update { it.copy(beeperVolume = preferences.beeperVolume) }
            } else {
                userPreferencesUseCase.insertUserPreferences(UserPreferences())
                _uiState.update { it.copy(beeperVolume = 50) }
            }
        }
    }
    fun emitEffect(effect: LightsTestUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}
