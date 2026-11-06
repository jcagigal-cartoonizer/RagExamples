package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import ifac.td.taxi.domain.usecase.SessionUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ChangeUserPasswordViewModel(
    context: Application,
    private val sessionUseCase: SessionUseCase,
) : BaseViewModel(context) {

    private val _userCredentialsFlow = MutableSharedFlow<UserPresenter?>()
    val userCredentialsFlow = _userCredentialsFlow.asSharedFlow()

    fun changeUserPassword(userPresenter: UserPresenter, newPassword: String) {
        userPresenter.changePassword(newPassword, context)
    }

    fun getUserPresenter(changePasswordPinView: ChangePasswordPinView) {
        viewModelScope.launch {
            if (sessionUseCase.isUserLoggedIn()) {
                _userCredentialsFlow.emit(UserModule.provideUserPresenter(changePasswordPinView, context))
            }
        }
    }
}