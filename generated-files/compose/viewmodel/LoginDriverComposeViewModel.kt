package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.domain.usecase.CountdownManagerUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.StartShiftUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.LoginDriverUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import ifac.td.taxi.repository.room.entities.countdown.CountdownId
import ifac.td.taxi.repository.room.entities.countdown.CountdownIdCallback
import ifac.td.taxi.viewmodel.model.CountdownModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class LoginDriverComposeViewModel(
    private val loginDriverUseCase: LoginDriverUseCase,
    private val startShiftUseCase: StartShiftUseCase,
    private val ticketUseCase: TicketUseCase,
    private val sessionUseCase: SessionUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val countdownManagerUseCase: CountdownManagerUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val context get() = getApplication<Application>()
    private val _uiState = MutableStateFlow(LoginDriverUiState())
    val uiState: StateFlow<LoginDriverUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<LoginDriverUiEffect>()
    val uiEffect: SharedFlow<LoginDriverUiEffect> = _uiEffect.asSharedFlow()
    init {
        viewModelScope.launch {
            sessionUseCase.getSession()?.let {
                val num = it.numDriver.orEmpty()
                _uiState.value = _uiState.value.copy(lastSessionDriverId = num, driverId = num)
                _uiEffect.emit(LoginDriverUiEffect.SetLastSessionDriver(num))
            }
        }
    }
    fun onArgsReceived(connectionMode: Int, startTurnMode: Int) {
        val funConCentral = (connectionMode and 0x01) != 0
        val funSinCentral = (connectionMode and 0x02) != 0
        val funRefuerzo = (connectionMode and 0x04) != 0
        var btnCnt = 1
        if (funConCentral) btnCnt++
        if (funSinCentral) btnCnt++
        if (funRefuerzo) btnCnt++
        val flowRatio = when {
            btnCnt > 2 -> if (isLandscape()) "1:1" else "3:2"
            else -> if (isLandscape()) "1:1" else if (funConCentral) "5:2" else "3:2"
        }
        _uiState.value = _uiState.value.copy(
            startTurnMode = startTurnMode,
            connectionMode = connectionMode,
            canShowDriverContainer = startTurnMode != 0,
            driverId = if (startTurnMode == 0) "0000" else _uiState.value.driverId,
            password = if (startTurnMode == 0) "" else _uiState.value.password,
            buttons = LoginDriverButtonsState(
                conCentral = ButtonStyleState(
                    visible = funConCentral,
                    enabled = true,
                    isLoading = false,
                    background = LoginButtonBackground.GREEN,
                    textColor = LoginButtonColors.white()
                ),
                sinCentral = ButtonStyleState(
                    visible = funSinCentral,
                    enabled = funSinCentral,
                    isLoading = false,
                    background = LoginButtonBackground.BLUE,
                    textColor = LoginButtonColors.white()
                ),
                refuerzo = ButtonStyleState(
                    visible = funRefuerzo,
                    enabled = funRefuerzo,
                    isLoading = false,
                    background = LoginButtonBackground.ORANGE,
                    textColor = LoginButtonColors.white()
                ),
                showFlowContainer = true,
                flowContainerRatio = flowRatio
            )
        )
        if (!funConCentral && funSinCentral && !funRefuerzo) {
            loginWithoutCentral()
        }
    }
    fun onDriverChanged(value: String) {
        _uiState.value = _uiState.value.copy(driverId = value)
    }
    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }
    fun clickChangePin() {
        viewModelScope.launch {
            _uiEffect.emit(LoginDriverUiEffect.NavigateToChangePin)
        }
    }
    fun onCancelClicked() {
        viewModelScope.launch {
            _uiEffect.emit(LoginDriverUiEffect.NavigateBack)
        }
    }
    fun loginDriver(driverID: String, password: String, isReinforcement: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!isInternetConnectionAvailable(context)) {
                    _uiEffect.emit(LoginDriverUiEffect.ShowNetworkErrorToast)
                    _uiEffect.emit(LoginDriverUiEffect.NavigateBack)
                    return@launch
                }
                loginDriverUseCase.loginDriver(driverID, password, isReinforcement)
                val model = CountdownModel(
                    CountdownId.LOGIN_DRIVER,
                    15,
                    CountdownIdCallback.LOGIN_DRIVER
                )
                countdownManagerUseCase.createOrUpdateCountdown(model)
                countdownManagerUseCase.startCountdown(model)
                countdownManagerUseCase.setCountdownFinishCallback(CountdownId.LOGIN_DRIVER) {
                    viewModelScope.launch {
                        _uiEffect.emit(LoginDriverUiEffect.ShowErrorLoginToast)
                    }
                }
                withContext(Dispatchers.IO) {
                    ticketUseCase.loadTickets()
                }
            } catch (e: Exception) {
                Logs.e("LoginDriverComposeViewModel", "loginDriver error: $e")
            }
        }
    }
    fun loginWithoutCentral() {
        viewModelScope.launch {
            try {
                W2CLocation.setLocationAllowedByCentral(false)
                sessionUseCase.updateWithCentralAllowed(false)
                withContext(Dispatchers.IO) {
                    ticketUseCase.loadTickets()
                }
                startShiftUseCase.startShift()
                shiftStatusUseCase.setStatus(ifConstants.STATE_FOR_HIRE_NO_CENTRAL, false)
                _uiEffect.emit(LoginDriverUiEffect.LoginWithoutCentralFinished)
            } catch (e: Exception) {
                Logs.e("LoginDriverComposeViewModel", "loginWithoutCentral error: $e")
            }
        }
    }
    fun onLoginResult(correct: Boolean, showIncorrectCredentials: Boolean, startTurnMode: Int) {
        viewModelScope.launch {
            if (startTurnMode == 0) {
                _uiEffect.emit(LoginDriverUiEffect.ShowIncorrectLoginNoCredentialsToast)
            } else {
                if (!correct) {
                    _uiEffect.emit(LoginDriverUiEffect.ShowIncorrectCredentials(showIncorrectCredentials))
                }
            }
        }
    }
    fun onNoLoginConnection(enableButton: Boolean, shouldToast: Boolean) {
        _uiState.value = _uiState.value.copy(
            buttons = _uiState.value.buttons.copy(
                conCentral = _uiState.value.buttons.conCentral.copy(enabled = true, isLoading = false)
            )
        )
        viewModelScope.launch {
            if (shouldToast) _uiEffect.emit(LoginDriverUiEffect.ShowErrorLoginToast)
        }
    }
    fun isLandscape(): Boolean {
        return context.resources.configuration.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE
    }
}
