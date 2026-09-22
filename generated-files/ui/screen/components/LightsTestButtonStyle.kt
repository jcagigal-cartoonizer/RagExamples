package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 128-3: import android.app.Application
// import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.model.UserPreferences
import ifac.td.taxi.domain.usecase.LightSkyGlassUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class LightsTestComposeViewModel(
    application: Application,
    private val lightSkyGlassUseCase: LightSkyGlassUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
) : AndroidViewModel(application) {
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
            LightsTestUiEvent.BackClicked -> emitEffect(LightsTestUiEffect.NavigateBack)
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
// // # Block 234-4: import android.app.Activity
// import android.app.Activity
import androidx.activity.compose.BackHandler
