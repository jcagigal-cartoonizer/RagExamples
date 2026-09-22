package ifac.td.taxi.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.CountdownManagerUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.StartShiftUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.LoginDriverUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import ifac.td.taxi.repository.room.entities.SessionEntity
import ifac.td.taxi.repository.room.entities.countdown.CountdownId
import ifac.td.taxi.repository.room.entities.countdown.CountdownIdCallback
import ifac.td.taxi.viewmodel.model.CountdownModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginDriverViewModel(
    private val loginDriverUseCase: LoginDriverUseCase,
    private val startShiftUseCase: StartShiftUseCase,
    private val ticketUseCase: TicketUseCase,
    private val sessionUseCase: SessionUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val countdownManagerUseCase: CountdownManagerUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "LoginDriverViewModel"

    private val _loginWithoutCentralFlow = MutableSharedFlow<Boolean>()
    val loginWithoutCentralFlow = _loginWithoutCentralFlow.asSharedFlow()

    private val _lastSessionFlow = MutableSharedFlow<String>()
    val lastSessionFlow = _lastSessionFlow.asSharedFlow()

    private val _noLoginConnectionFlow = MutableSharedFlow<Boolean>()
    val noLoginConnectionFlow = _noLoginConnectionFlow.asSharedFlow()

    private var user: SessionEntity? = null

    init {
        viewModelScope.launch {
            sessionUseCase.getSession()?.let {
                user = it
                user?.numDriver?.let { num -> _lastSessionFlow.emit(num) }
            }
        }
    }

    fun loginDriver(driverID: String, password: String, isReinforcement: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!isInternetConnectionAvailable(context)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                    }

                    navigateBack()
                    Logs.e(TAG, "loginDriver: Network connection not available")
                    return@launch
                }

                loginDriverUseCase.loginDriver(driverID, password, isReinforcement)
                val model = CountdownModel(
                    CountdownId.LOGIN_DRIVER,
                    15,
                    CountdownIdCallback.LOGIN_DRIVER
                )
                val callback: () -> Unit = {
                    viewModelScope.launch {
                        _noLoginConnectionFlow.emit(true)
                    }
                }
                countdownManagerUseCase.createOrUpdateCountdown(model)
                countdownManagerUseCase.startCountdown(model)
                countdownManagerUseCase.setCountdownFinishCallback(CountdownId.LOGIN_DRIVER, callback)
                withContext(Dispatchers.IO) {
                    ticketUseCase.loadTickets()
                }
            } catch (e: Exception) {
                Logs.e(TAG, "Error durante el inicio de sesión del conductor: $e")
            }
        }
    }

    fun loginWithoutCentral() {
        viewModelScope.launch {
            try {
                Logs.d(TAG, "loginWithoutCentral()")
                W2CLocation.setLocationAllowedByCentral(false)
                Logs.d(TAG, "setLocationAllowedByCentral: false")
                sessionUseCase.updateWithCentralAllowed(false)
                withContext(Dispatchers.IO) {
                    ticketUseCase.loadTickets()
                }
                startShiftUseCase.startShift()
                shiftStatusUseCase.setStatus(ifConstants.STATE_FOR_HIRE_NO_CENTRAL, false)
                _loginWithoutCentralFlow.emit(true)
            } catch (e: Exception) {
                Logs.e(TAG, "Error durante el inicio de sesión sin central: $e")
            }
        }
    }

    fun clickChangePin() {
        viewModelScope.launch {
            navigateTo(R.id.action_loginDriverFragment_to_changeDriverPinFragment)
        }
    }
}