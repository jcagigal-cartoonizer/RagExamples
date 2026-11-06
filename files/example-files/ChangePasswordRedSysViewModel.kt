package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.util.Logs
import es.redsys.paysys.Operative.DTO.RedCLSChangePassData
import es.redsys.paysys.Utils.RedCLSErrorCodes
import ifac.td.taxi.domain.usecase.RedSysUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ChangePasswordRedSysViewModel(
    private val redSysUseCase: RedSysUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "ChangePasswordRedSysViewModel"

    private val _changePasswordRedSysFlow = MutableSharedFlow<Pair<Boolean, String?>>()
    val changePasswordRedSysFlow = _changePasswordRedSysFlow.asSharedFlow()

    fun changePassword(user: String, password: String, newPassword: String) {
        val clsChange = RedCLSChangePassData(
            context,
            user,
            password,
            newPassword
        );

        viewModelScope.launch {
            val result = redSysUseCase.changePassword(clsChange)
            result?.let {
                Logs.d(TAG, "RedSys result: code: ${result.code}, desc: ${result.desc}, firma: ${result.firma}, mensaje: ${result.mensaje}, session: ${result.session}")
                if (it.code == RedCLSErrorCodes.STATUS_OK) {
                    _changePasswordRedSysFlow.emit(Pair(true, it.desc))
                } else {
                    _changePasswordRedSysFlow.emit(Pair(false, it.desc))
                }
            }
        }
    }
}