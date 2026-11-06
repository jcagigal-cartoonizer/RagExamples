package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.model.UserPreferences
import ifac.td.taxi.domain.usecase.LightSkyGlassUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LightsTestViewModel(
    private var lightSkyGlassUseCase: LightSkyGlassUseCase,
    private var userPreferencesUseCase: UserPreferencesUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val _uvLightFlow = MutableSharedFlow<Boolean>()
    val uvLightFlow = _uvLightFlow.asSharedFlow()

    private val _beeperLightFlow = MutableStateFlow<Int?>(null)
    val beeperLightFlow = _beeperLightFlow.asStateFlow()

    fun turnUVLight() {
        viewModelScope.launch {
            lightSkyGlassUseCase.turnUVLight()
            delay(8000L)
            _uvLightFlow.emit(false)
        }
    }

    fun turnCourtesyLight() {
        viewModelScope.launch {
            lightSkyGlassUseCase.turnCourtesyLight()
        }
    }

    fun setBeeperVolume(volume: Int?) {
        viewModelScope.launch {
            lightSkyGlassUseCase.setBeeperVolume(volume ?: 50)
            volume?.let { value ->
                userPreferencesUseCase.insertBeeperVolumeValue(value)
            }
        }
    }

    fun getBeeperVolume() {
        viewModelScope.launch {
            val preferences = userPreferencesUseCase.getUserPreferences()
            if (preferences != null) {
                _beeperLightFlow.emit(preferences.beeperVolume)
            } else {
                userPreferencesUseCase.insertUserPreferences(UserPreferences())
            }
        }
    }

}