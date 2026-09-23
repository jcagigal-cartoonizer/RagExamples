package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SecurePinViewModel(
    context: Application,
    private val userPreferencesUseCase: UserPreferencesUseCase,
) : BaseViewModel(context) {

    private val TAG = "SecurePinViewModel"

    // null = aún no comprobado, true = ya existe un pin (modo actualizar), false = no existe (modo crear)
    private val _hasSecurePinFlow = MutableStateFlow<Boolean?>(null)
    val hasSecurePinFlow = _hasSecurePinFlow.asStateFlow()

    fun checkSecurePin() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSecurePin = userPreferencesUseCase.getSecurePin()
            val hasSecurePin = !currentSecurePin.isNullOrEmpty()
            Logs.d(TAG, "checkSecurePin: hasSecurePin=$hasSecurePin")
            _hasSecurePinFlow.emit(hasSecurePin)
        }
    }

    fun insertSecurePin(pin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "insertSecurePin: isEmpty=${pin.isEmpty()}")
            userPreferencesUseCase.insertSecurePin(pin)
        }
    }
}