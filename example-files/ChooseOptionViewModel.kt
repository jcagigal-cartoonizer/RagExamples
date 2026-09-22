package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.domain.usecase.CountdownManagerUseCase
import ifac.td.taxi.domain.usecase.EndShiftUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.LoginDriverUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.entities.countdown.CountdownId
import ifac.td.taxi.repository.room.entities.countdown.CountdownIdCallback
import ifac.td.taxi.repository.room.entities.countdown.CountdownState
import ifac.td.taxi.viewmodel.model.CountdownModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChooseOptionViewModel(
    context: Application,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val loginDriverUseCase: LoginDriverUseCase,
    private val sessionUseCase: SessionUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val endShiftUseCase: EndShiftUseCase,
) : BaseViewModel(context) {

    private val TAG = "ChooseOptionViewModel"

    fun sendShiftSelectedAnswer(selectedId: String) {
        viewModelScope.launch {
            Logs.d(TAG, "sendShiftSelectedAnswer: selectedId = $selectedId")
            shiftStatusUseCase.sendShiftSelectedAnswer(selectedId, W2CLocation.getZoning()?.version ?: "00")
        }
    }

    fun loginDriver() {
        viewModelScope.launch {
            sessionUseCase.getSession()?.let {
                it.numDriver?.let { numDriver ->
                    loginDriverUseCase.loginDriver(numDriver, it.pwdDriver?:"", it.reinforcement)
                }
            }
        }

    }

    /**
     * Cancela el proceso de selección de turno cerrando la sesión del driver.
     * Necesario al ir atrás para que el servidor Bravo no siga reenviando los turnos
     * (openBravoShifts) y para poder volver a hacer login correctamente.
     */
    fun logoffDriver() {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "logoffDriver: cancelling shift selection")
            endShiftUseCase.endShift()
            bravoCentralUseCase.logoff()
            shiftStatusUseCase.setStatus(ifConstants.STATE_DISCONNECTED, false)
        }
    }
}