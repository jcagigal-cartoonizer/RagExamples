package ifac.td.taxi.ui.screen.compose

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import es.redsys.paysys.Operative.DTO.RedCLSChangePassData
import es.redsys.paysys.Utils.RedCLSErrorCodes
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.RedSysUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ChangePasswordRedSysButtonsState(
    val cancelVisible: Boolean = true,
    val acceptVisible: Boolean = true,
    val cancelEnabled: Boolean = true,
    val acceptEnabled: Boolean = true,
    val acceptLoading: Boolean = false,
    val cancelLoading: Boolean = false,
    val cancelText: String = "Cancel",
    val acceptText: String = "Accept",
    val cancelBackgroundColor: Long = 0x00000000,
    val acceptBackgroundColor: Long = 0xFF1976D2,
    val cancelContentColor: Long = 0xFF000000,
    val acceptContentColor: Long = 0xFFFFFFFF
)

sealed interface ChangePasswordRedSysButtonsAction {
    data object OnAcceptClicked : ChangePasswordRedSysButtonsAction
    data object OnCancelClicked : ChangePasswordRedSysButtonsAction
    data class OnChangePasswordResult(val success: Boolean, val text: String?) :
        ChangePasswordRedSysButtonsAction
}

fun ChangePasswordRedSysButtonsState.reduce(
    action: ChangePasswordRedSysButtonsAction
): ChangePasswordRedSysButtonsState {
    return when (action) {
        ChangePasswordRedSysButtonsAction.OnAcceptClicked -> copy(
            acceptLoading = true,
            acceptEnabled = false,
            cancelEnabled = false
        )

        ChangePasswordRedSysButtonsAction.OnCancelClicked -> this

        is ChangePasswordRedSysButtonsAction.OnChangePasswordResult -> copy(
            acceptLoading = false,
            acceptEnabled = true,
            cancelEnabled = true
        )
    }
}

@Immutable
data class ChangePasswordRedSysDialogState(
    val title: String,
    val description: String,
    val confirmText: String = "Accept"
)

@Immutable
data class ChangePasswordRedSysUiState(
    val user: String = "",
    val password: String = "",
    val newPassword: String = "",
    val repeatNewPassword: String = "",
    val buttons: ChangePasswordRedSysButtonsState = ChangePasswordRedSysButtonsState(),
    val dialog: ChangePasswordRedSysDialogState? = null
)

sealed interface ChangePasswordRedSysUiEffect {
    data object NavigateBack : ChangePasswordRedSysUiEffect
    data class ShowDialog(val dialog: ChangePasswordRedSysDialogState) : ChangePasswordRedSysUiEffect
}

open class BaseViewModel(val context: Application) : ViewModel()

class ChangePasswordRedSysViewModel(
    private val redSysUseCase: RedSysUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "ChangePasswordRedSysViewModel"

    private val _changePasswordRedSysFlow = MutableSharedFlow<Pair<Boolean, String?>>()
    val changePasswordRedSysFlow: SharedFlow<Pair<Boolean, String?>> = _changePasswordRedSysFlow.asSharedFlow()

    fun changePassword(user: String, password: String, newPassword: String) {
        val clsChange = RedCLSChangePassData(
            context,
            user,
            password,
            newPassword
        )

        viewModelScope.launch {
            val result = redSysUseCase.changePassword(clsChange)
            result?.let {
                Logs.d(
                    TAG,
                    "RedSys result: code: ${result.code}, desc: ${result.desc}, firma: ${result.firma}, mensaje: ${result.mensaje}, session: ${result.session}"
                )
                if (it.code == RedCLSErrorCodes.STATUS_OK) {
                    _changePasswordRedSysFlow.emit(Pair(true, it.desc))
                } else {
                    _changePasswordRedSysFlow.emit(Pair(false, it.desc))
                }
            }
        }
    }
}

open class BaseComposeViewModel(application: Application) : ViewModel() {
    val app: Application = application
}

class ComposeSharedViewModel(
    application: Application
) : BaseComposeViewModel(application) {

    private val _navigationDirectionsFlow = MutableSharedFlow<NavDirections>()
    val navigationDirectionsFlow: SharedFlow<NavDirections> = _navigationDirectionsFlow.asSharedFlow()

    private val _showDialogFlow = MutableSharedFlow<String>()
    val showDialogFlow: SharedFlow<String> = _showDialogFlow.asSharedFlow()

    private val _loadingStateFlow = MutableSharedFlow<Boolean>()
    val loadingStateFlow: SharedFlow<Boolean> = _loadingStateFlow.asSharedFlow()

    suspend fun navigate(directions: NavDirections) {
        _navigationDirectionsFlow.emit(directions)
    }

    suspend fun showDialog(message: String) {
        _showDialogFlow.emit(message)
    }

    suspend fun setLoading(loading: Boolean) {
        _loadingStateFlow.emit(loading)
    }
}

class ChangePasswordRedSysMainActivityViewModel(
    private val application: Application,
    private val changePasswordRedSysViewModel: ChangePasswordRedSysViewModel,
    private val composeSharedViewModel: ComposeSharedViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordRedSysUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ChangePasswordRedSysUiEffect>()
    val uiEffect: SharedFlow<ChangePasswordRedSysUiEffect> = _uiEffect.asSharedFlow()

    init {
        observeChangePasswordResult()
    }

    private fun observeChangePasswordResult() {
        viewModelScope.launch {
            changePasswordRedSysViewModel.changePasswordRedSysFlow.collect { result ->
                val success = result.first
                val text = result.second

                _uiState.update { current ->
                    current.copy(
                        buttons = current.buttons.reduce(
                            ChangePasswordRedSysButtonsAction.OnChangePasswordResult(
                                success = success,
                                text = text
                            )
                        )
                    )
                }

                val dialog = ChangePasswordRedSysDialogState(
                    title = application.getString(R.string.red_sys_title),
                    description = if (success) {
                        application.getString(R.string.dialog_change_password)
                    } else {
                        text ?: application.getString(R.string.no_response_error)
                    },
                    confirmText = application.getString(android.R.string.ok)
                )

                _uiState.update { it.copy(dialog = dialog) }
                _uiEffect.emit(ChangePasswordRedSysUiEffect.ShowDialog(dialog))

                if (success) {
                    _uiEffect.emit(ChangePasswordRedSysUiEffect.NavigateBack)
                }
            }
        }
    }

    fun onUserChanged(value: String) {
        _uiState.update { it.copy(user = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value) }
    }

    fun onRepeatNewPasswordChanged(value: String) {
        _uiState.update { it.copy(repeatNewPassword = value) }
    }

    fun onAcceptClicked() {
        _uiState.update { current ->
            current.copy(
                buttons = current.buttons.reduce(ChangePasswordRedSysButtonsAction.OnAcceptClicked)
            )
        }

        val current = _uiState.value
        if (current.user.isNotBlank() &&
            current.password.isNotBlank() &&
            current.newPassword.isNotBlank() &&
            current.repeatNewPassword.isNotBlank() &&
            current.newPassword == current.repeatNewPassword
        ) {
            changePasswordRedSysViewModel.changePassword(
                user = current.user,
                password = current.password,
                newPassword = current.newPassword
            )
        } else {
            _uiState.update {
                it.copy(
                    buttons = it.buttons.copy(
                        acceptLoading = false,
                        acceptEnabled = true,
                        cancelEnabled = true
                    )
                )
            }
        }
    }

    fun onCancelClicked() {
        viewModelScope.launch {
            _uiEffect.emit(ChangePasswordRedSysUiEffect.NavigateBack)
        }
    }

    fun onDialogConfirmed() {
        _uiState.update { it.copy(dialog = null) }
        viewModelScope.launch {
            _uiEffect.emit(ChangePasswordRedSysUiEffect.NavigateBack)
        }
    }

    fun onDialogDismissed() {
        _uiState.update { it.copy(dialog = null) }
    }
}

@Composable
fun ChangePasswordRedSysScreen(
    navController: NavController,
    viewModel: ChangePasswordRedSysMainActivityViewModel,
    sharedViewModel: ComposeSharedViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                ChangePasswordRedSysUiEffect.NavigateBack -> onBack()
                is ChangePasswordRedSysUiEffect.ShowDialog -> Unit
            }
        }
    }

    LaunchedEffect(sharedViewModel) {
        sharedViewModel.navigationDirectionsFlow.collect { directions ->
            navController.navigate(directions)
        }
    }

    LaunchedEffect(sharedViewModel) {
        sharedViewModel.showDialogFlow.collect { message ->
            // hook for activity-level dialog host if needed
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = uiState.user,
            onValueChange = viewModel::onUserChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("User") }
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") }
        )

        OutlinedTextField(
            value = uiState.newPassword,
            onValueChange = viewModel::onNewPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New password") }
        )

        OutlinedTextField(
            value = uiState.repeatNewPassword,
            onValueChange = viewModel::onRepeatNewPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Repeat new password") }
        )

        ChangePasswordRedSysButtons(
            state = uiState.buttons,
            onCancel = viewModel::onCancelClicked,
            onAccept = viewModel::onAcceptClicked
        )
    }

    uiState.dialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = viewModel::onDialogDismissed,
            title = { Text(dialog.title) },
            text = { Text(dialog.description) },
            confirmButton = {
                TextButton(onClick = viewModel::onDialogConfirmed) {
                    Text(dialog.confirmText)
                }
            }
        )
    }
}

@Composable
fun ChangePasswordRedSysButtons(
    state: ChangePasswordRedSysButtonsState,
    onCancel: () -> Unit,
    onAccept: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.cancelVisible) {
            Button(
                onClick = onCancel,
                enabled = state.cancelEnabled && !state.cancelLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(state.cancelBackgroundColor),
                    contentColor = Color(state.cancelContentColor)
                )
            ) {
                Text(state.cancelText)
            }
        }

        if (state.acceptVisible) {
            Button(
                onClick = onAccept,
                enabled = state.acceptEnabled && !state.acceptLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(state.acceptBackgroundColor),
                    contentColor = Color(state.acceptContentColor)
                )
            ) {
                if (state.acceptLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(state.acceptText)
                }
            }
        }
    }
}
