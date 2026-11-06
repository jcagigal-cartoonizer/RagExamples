package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SplashScreenViewModel(context: Application, private val sessionUseCase: SessionUseCase) : BaseViewModel(context) {

    private val TAG = "SplashScreenViewModel"

    private val _serviceActiveFlow = MutableSharedFlow<Boolean>()
    val serviceActiveFlow = _serviceActiveFlow.asSharedFlow()

    fun checkIsReconnecting(reconnectionFlowValue: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val serviceWasActive = sessionUseCase.getServiceWasActive()
            Logs.d(TAG, "checkIsReconnecting serviceWasActive: $serviceWasActive, reconnectionFlowValue: $reconnectionFlowValue")
            delay(50)
            _serviceActiveFlow.emit(serviceWasActive)
        }
    }

}