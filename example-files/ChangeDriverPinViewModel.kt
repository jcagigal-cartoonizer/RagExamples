package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ChangeDriverPinViewModel(context: Application) : BaseViewModel(context) {

    private val _userCredentialsFlow = MutableSharedFlow<UserPresenter?>()
    val userCredentialsFlow = _userCredentialsFlow.asSharedFlow()

    fun changeDriverPin(userPresenter: UserPresenter, driverNumber: String, oldPin : String, newPin: String){
        userPresenter.changeDriverPin(driverNumber, oldPin, newPin, context)
    }

    fun getUserPresenter(view: ChangePasswordPinView) {
        viewModelScope.launch {
            _userCredentialsFlow.emit(UserModule.provideUserPresenter(view, context))
        }
    }
}