package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 4-1: import android.app.Application
// import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.redsys.paysys.Operative.DTO.RedCLSChangePassData
import es.redsys.paysys.Utils.RedCLSErrorCodes
import ifac.td.taxi.domain.usecase.RedSysUseCase
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
data class ChangePasswordRedSysUiState(
    val user: String = "",
    val password: String = "",
    val newPassword: String = "",
    val repeatNewPassword: String = "",
    val isLoading: Boolean = false,
    val dialog: ChangePasswordRedSysDialogState? = null
)
sealed interface ChangePasswordRedSysUiEvent {
    data class UserChanged(val value: String) : ChangePasswordRedSysUiEvent
    data class PasswordChanged(val value: String) : ChangePasswordRedSysUiEvent
    data class NewPasswordChanged(val value: String) : ChangePasswordRedSysUiEvent
    data class RepeatNewPasswordChanged(val value: String) : ChangePasswordRedSysUiEvent
    data object AcceptClicked : ChangePasswordRedSysUiEvent
    data object CancelClicked : ChangePasswordRedSysUiEvent
    data object DialogAccepted : ChangePasswordRedSysUiEvent
    data object DialogDismissed : ChangePasswordRedSysUiEvent
}
sealed interface ChangePasswordRedSysUiEffect {
    data object NavigateBack : ChangePasswordRedSysUiEffect
}
data class ChangePasswordRedSysDialogState(
    val title: String,
    val description: String,
)
class ChangePasswordRedSysComposeViewModel(
    private val redSysUseCase: RedSysUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val TAG = "ChangePasswordRedSysViewModel"
    private val _uiState = MutableStateFlow(ChangePasswordRedSysUiState())
    val uiState: StateFlow<ChangePasswordRedSysUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ChangePasswordRedSysUiEffect>()
    val uiEffect: SharedFlow<ChangePasswordRedSysUiEffect> = _uiEffect.asSharedFlow()
    fun onEvent(event: ChangePasswordRedSysUiEvent) {
        when (event) {
            is ChangePasswordRedSysUiEvent.UserChanged ->
                _uiState.update { it.copy(user = event.value) }
            is ChangePasswordRedSysUiEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.value) }
            is ChangePasswordRedSysUiEvent.NewPasswordChanged ->
                _uiState.update { it.copy(newPassword = event.value) }
            is ChangePasswordRedSysUiEvent.RepeatNewPasswordChanged ->
                _uiState.update { it.copy(repeatNewPassword = event.value) }
            ChangePasswordRedSysUiEvent.AcceptClicked -> changePassword()
            ChangePasswordRedSysUiEvent.CancelClicked -> {
                viewModelScope.launch { _uiEffect.emit(ChangePasswordRedSysUiEffect.NavigateBack) }
            }
            ChangePasswordRedSysUiEvent.DialogAccepted -> {
                _uiState.update { it.copy(dialog = null) }
                viewModelScope.launch { _uiEffect.emit(ChangePasswordRedSysUiEffect.NavigateBack) }
            }
            ChangePasswordRedSysUiEvent.DialogDismissed -> {
                _uiState.update { it.copy(dialog = null) }
            }
        }
    }
    fun changePassword() {
        val state = _uiState.value
        val clsChange = RedCLSChangePassData(
            getApplication(),
            state.user,
            state.password,
            state.newPassword
        )
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = redSysUseCase.changePassword(clsChange)
            result?.let {
                Logs.d(TAG, "RedSys result: code: ${result.code}, desc: ${result.desc}, firma: ${result.firma}, mensaje: ${result.mensaje}, session: ${result.session}")
                val success = it.code == RedCLSErrorCodes.STATUS_OK
                val title = getApplication<Application>().getString(if (success) {
                    ifac.td.taxi.R.string.red_sys_title
                } else {
                    ifac.td.taxi.R.string.red_sys_title
                })
                val description = if (success) {
                    getApplication<Application>().getString(ifac.td.taxi.R.string.dialog_change_password)
                } else {
                    it.desc ?: getApplication<Application>().getString(ifac.td.taxi.R.string.no_response_error)
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dialog = ChangePasswordRedSysDialogState(
                            title = title,
                            description = description
                        )
                    )
                }
            } ?: run {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dialog = ChangePasswordRedSysDialogState(
                            title = getApplication<Application>().getString(ifac.td.taxi.R.string.red_sys_title),
                            description = getApplication<Application>().getString(ifac.td.taxi.R.string.no_response_error)
                        )
                    )
                }
            }
        }
    }
}
