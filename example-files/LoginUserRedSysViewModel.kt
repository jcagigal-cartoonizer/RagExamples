package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.util.Logs
import es.redsys.paysys.Utils.RedCLSErrorCodes
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.RedSysLoginResponse
import ifac.td.taxi.domain.usecase.RedSysUseCase
import ifac.td.taxi.framework.TemporalData
import ifac.td.taxi.ui.custom.button.CustomButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginUserRedSysViewModel(
    private val redSysUseCase: RedSysUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "LoginUserRedSysViewModel"

    private val _loginRedSysFlow = MutableSharedFlow<Boolean>()
    val loginRedSysFlow = _loginRedSysFlow.asSharedFlow()

    private val _userRedSysFlow = MutableSharedFlow<String?>()
    val userRedSysFlow = _userRedSysFlow.asSharedFlow()

    private val _okStatusFlow = MutableSharedFlow<CustomButton.StyleButton>()
    val okStatusFlow = _okStatusFlow.asSharedFlow()

    private var userRedSys: String = ""
    private var passwordRedSys: String = ""

    fun loginRedSys(user: String, password: String) {
        userRedSys = user
        passwordRedSys = password

        viewModelScope.launch(Dispatchers.IO) {
            redSysUseCase.configureRedSys()

            val loginRedSys = redSysUseCase.loginRedSys(
                username = userRedSys,
                password = passwordRedSys
            )

            when (loginRedSys) {
                is RedSysLoginResponse.Success -> {
                    if (loginRedSys.merchantList.size > 0) {
                        redSysUseCase.saveUsernameRedSys(userRedSys)
                        if (passwordRedSys.isNotEmpty()) {
                            TemporalData.passwordRedSys = passwordRedSys
                        }
                        _loginRedSysFlow.emit(true)
                    } else {
                        _loginRedSysFlow.emit(false)
                    }
                }
                is RedSysLoginResponse.Error -> {
                    Logs.d(TAG, "Login RedSys failed or returned empty array: ${loginRedSys.errorMessage}")
                    if (loginRedSys.errorCode == RedCLSErrorCodes.STATUS_KO_FORMATO_RESP_LOGIN_PWD_CAD) {
                        _okStatusFlow.emit(CustomButton.StyleButton.ENABLE)
                        navigateTo(R.id.action_loginUserRedSysFragment_to_changePasswordRedSysFragment)
                    } else {
                        _loginRedSysFlow.emit(false)
                    }
                }
            }
        }
    }

    fun getUserRedSys() {
        viewModelScope.launch {
            val user = redSysUseCase.getUsernameRedSys()
            _userRedSysFlow.emit(user)
        }
    }
}