package ifac.td.taxi.ui.screen.compose

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDirections
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import ifac.td.taxi.compose.viewmodel.ComposeSharedViewModel
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ----------------------------------------------------
// EXACT BUTTON STATE / REDUCER
// ----------------------------------------------------

data class ChangeUserPasswordButtonsState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val repeatPassword: String = "",
    val acceptVisible: Boolean = true,
    val cancelVisible: Boolean = true,
    val acceptEnabled: Boolean = true,
    val cancelEnabled: Boolean = true,
    val acceptBackgroundColor: Color = Color(0xFF1976D2),
    val acceptContentColor: Color = Color.White,
    val cancelBackgroundColor: Color = Color(0xFFE0E0E0),
    val cancelContentColor: Color = Color.Black,
    val pendingDialog: ChangeUserPasswordDialogKind? = null,
    val pendingAction: ChangeUserPasswordPendingAction = ChangeUserPasswordPendingAction.None
)

enum class ChangeUserPasswordDialogKind {
    FillInputs,
    NoMatchPassword,
    EqualPassword,
    ChangePasswordSuccess,
    ChangePasswordError,
    UserError
}

enum class ChangeUserPasswordPendingAction {
    None,
    RequestUserPresenter,
    NavigateBack
}

private fun reduceChangeUserPasswordButtonsState(
    state: ChangeUserPasswordButtonsState,
    currentPassword: String = state.currentPassword,
    newPassword: String = state.newPassword,
    repeatPassword: String = state.repeatPassword,
    acceptClicked: Boolean = false,
    cancelClicked: Boolean = false
): ChangeUserPasswordButtonsState {
    return when {
        cancelClicked -> state.copy(
            pendingAction = ChangeUserPasswordPendingAction.NavigateBack
        )

        acceptClicked -> {
            val dialog = when {
                currentPassword.isEmpty() || newPassword.isEmpty() || repeatPassword.isEmpty() ->
                    ChangeUserPasswordDialogKind.FillInputs

                newPassword != repeatPassword ->
                    ChangeUserPasswordDialogKind.NoMatchPassword

                currentPassword == newPassword ->
                    ChangeUserPasswordDialogKind.EqualPassword

                else -> null
            }

            state.copy(
                currentPassword = currentPassword,
                newPassword = newPassword,
                repeatPassword = repeatPassword,
                pendingDialog = dialog,
                pendingAction = if (dialog == null) {
                    ChangeUserPasswordPendingAction.RequestUserPresenter
                } else {
                    ChangeUserPasswordPendingAction.None
                }
            )
        }

        else -> state.copy(
            currentPassword = currentPassword,
            newPassword = newPassword,
            repeatPassword = repeatPassword
        )
    }
}

// ----------------------------------------------------
// UI STATE / EFFECTS
// ----------------------------------------------------

data class ChangeUserPasswordUiState(
    val buttonsState: ChangeUserPasswordButtonsState = ChangeUserPasswordButtonsState()
)

sealed class ChangeUserPasswordUiEffect {
    data class ShowCustomDialog(val model: CustomDialog.CustomDialogModel) : ChangeUserPasswordUiEffect()
    data object NavigateBack : ChangeUserPasswordUiEffect()
}

// ----------------------------------------------------
// REAL SHARED VM ADAPTER (replaces placeholder)
// ----------------------------------------------------

class MainActivityViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _navigationDirectionsFlow = MutableSharedFlow<NavDirections>()
    val navigationDirectionsFlow: SharedFlow<NavDirections> = _navigationDirectionsFlow.asSharedFlow()

    private val _showDialogFlow = MutableSharedFlow<CustomDialog.CustomDialogModel>()
    val showDialogFlow: SharedFlow<CustomDialog.CustomDialogModel> = _showDialogFlow.asSharedFlow()

    private val _showToastFlow = MutableSharedFlow<Int>()
    val showToastFlow: SharedFlow<Int> = _showToastFlow.asSharedFlow()

    fun navigateTo(directions: NavDirections) {
        viewModelScope.launch { _navigationDirectionsFlow.emit(directions) }
    }

    fun openDialog(model: CustomDialog.CustomDialogModel) {
        viewModelScope.launch { _showDialogFlow.emit(model) }
    }

    fun showToast(messageRes: Int) {
        viewModelScope.launch { _showToastFlow.emit(messageRes) }
    }
}

// ----------------------------------------------------
// CHANGE USER PASSWORD VM
// ----------------------------------------------------

class ChangeUserPasswordViewModel(
    application: Application,
    private val sessionUseCase: SessionUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChangeUserPasswordUiState())
    val uiState: StateFlow<ChangeUserPasswordUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ChangeUserPasswordUiEffect>()
    val uiEffect: SharedFlow<ChangeUserPasswordUiEffect> = _uiEffect.asSharedFlow()

    fun onCurrentPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            buttonsState = _uiState.value.buttonsState.copy(currentPassword = value)
        )
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            buttonsState = _uiState.value.buttonsState.copy(newPassword = value)
        )
    }

    fun onRepeatPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            buttonsState = _uiState.value.buttonsState.copy(repeatPassword = value)
        )
    }

    fun onAcceptClicked() {
        val current = _uiState.value.buttonsState
        val next = reduceChangeUserPasswordButtonsState(
            state = current,
            acceptClicked = true
        )
        _uiState.value = _uiState.value.copy(buttonsState = next)

        when (next.pendingDialog) {
            ChangeUserPasswordDialogKind.FillInputs -> emitDialog(
                title = "dialog_fill_inputs",
                showToastOnAccept = true
            )

            ChangeUserPasswordDialogKind.NoMatchPassword -> emitDialog(
                title = "dialog_no_match_password",
                showToastOnAccept = true
            )

            ChangeUserPasswordDialogKind.EqualPassword -> emitDialog(
                title = "dialog_equal_password",
                showToastOnAccept = true
            )

            null -> requestUserPresenterAndChangePassword()

            else -> Unit
        }
    }

    fun onCancelClicked() {
        _uiState.value = _uiState.value.copy(
            buttonsState = reduceChangeUserPasswordButtonsState(
                state = _uiState.value.buttonsState,
                cancelClicked = true
            )
        )
        viewModelScope.launch { _uiEffect.emit(ChangeUserPasswordUiEffect.NavigateBack) }
    }

    private fun requestUserPresenterAndChangePassword() {
        viewModelScope.launch {
            if (sessionUseCase.isUserLoggedIn()) {
                val presenter = UserModule.provideUserPresenter(
                    object : ChangePasswordPinView {
                        override fun updateSuccess() {
                            viewModelScope.launch {
                                _uiEffect.emit(
                                    ChangeUserPasswordUiEffect.ShowCustomDialog(
                                        CustomDialog.CustomDialogModel(
                                            title = getApplication<Application>().getString(
                                                ifac.td.taxi.R.string.dialog_change_password
                                            ),
                                            buttons = arrayListOf(ButtonType.ACCEPT)
                                        )
                                    )
                                )
                            }
                        }

                        override fun updateFailure() {
                            viewModelScope.launch {
                                _uiEffect.emit(
                                    ChangeUserPasswordUiEffect.ShowCustomDialog(
                                        CustomDialog.CustomDialogModel(
                                            title = getApplication<Application>().getString(
                                                ifac.td.taxi.R.string.dialog_error_change_password
                                            ),
                                            buttons = arrayListOf(ButtonType.ACCEPT)
                                        )
                                    )
                                )
                            }
                        }
                    },
                    getApplication()
                )

                if (presenter != null) {
                    changeUserPassword(presenter, _uiState.value.buttonsState.newPassword.trim())
                } else {
                    emitUserErrorDialog()
                }
            } else {
                emitUserErrorDialog()
            }
        }
    }

    fun changeUserPassword(userPresenter: UserPresenter, newPassword: String) {
        userPresenter.changePassword(newPassword, getApplication())
    }

    private fun emitUserErrorDialog() {
        viewModelScope.launch {
            _uiEffect.emit(
                ChangeUserPasswordUiEffect.ShowCustomDialog(
                    CustomDialog.CustomDialogModel(
                        title = getApplication<Application>().getString(
                            ifac.td.taxi.R.string.dialog_user_error
                        ),
                        buttons = arrayListOf(ButtonType.ACCEPT)
                    )
                )
            )
        }
    }

    private fun emitDialog(title: String, showToastOnAccept: Boolean) {
        viewModelScope.launch {
            _uiEffect.emit(
                ChangeUserPasswordUiEffect.ShowCustomDialog(
                    CustomDialog.CustomDialogModel(
                        title = title,
                        buttons = arrayListOf(ButtonType.ACCEPT)
                    )
                )
            )
        }
    }
}

// ----------------------------------------------------
// COMPOSE SCREEN
// ----------------------------------------------------

@Composable
fun ChangeUserPasswordRoute(
    viewModel: ChangeUserPasswordViewModel,
    sharedViewModel: ComposeSharedViewModel,
    mainActivityViewModel: MainActivityViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (NavDirections) -> Unit,
    onShowToast: (Int) -> Unit,
    resolveString: (String) -> String
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogModel by remember { mutableStateOf<CustomDialog.CustomDialogModel?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ChangeUserPasswordUiEffect.NavigateBack -> onNavigateBack()
                is ChangeUserPasswordUiEffect.ShowCustomDialog -> dialogModel = effect.model
            }
        }
    }

    LaunchedEffect(Unit) {
        mainActivityViewModel.navigationDirectionsFlow.collect { directions ->
            onNavigate(directions)
        }
    }

    LaunchedEffect(Unit) {
        mainActivityViewModel.showDialogFlow.collect { model ->
            dialogModel = model
        }
    }

    LaunchedEffect(Unit) {
        mainActivityViewModel.showToastFlow.collect { resId ->
            onShowToast(resId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = uiState.buttonsState.currentPassword,
            onValueChange = viewModel::onCurrentPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Actual password") }
        )

        OutlinedTextField(
            value = uiState.buttonsState.newPassword,
            onValueChange = viewModel::onNewPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New password") }
        )

        OutlinedTextField(
            value = uiState.buttonsState.repeatPassword,
            onValueChange = viewModel::onRepeatPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Repeat password") }
        )

        ChangeUserPasswordButtons(
            state = uiState.buttonsState,
            onAccept = viewModel::onAcceptClicked,
            onCancel = viewModel::onCancelClicked
        )
    }

    dialogModel?.let { model ->
        AlertDialog(
            onDismissRequest = { dialogModel = null },
            title = { Text(model.title) },
            confirmButton = {
                TextButton(onClick = {
                    dialogModel = null
                    if (model.buttons.contains(ButtonType.ACCEPT)) {
                        if (model.title == resolveString(ifac.td.taxi.R.string.dialog_change_password.toString())) {
                            onNavigateBack()
                        }
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ChangeUserPasswordButtons(
    state: ChangeUserPasswordButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit
) {
    Row {
        if (state.acceptVisible) {
            Button(
                onClick = onAccept,
                enabled = state.acceptEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = state.acceptBackgroundColor,
                    contentColor = state.acceptContentColor
                )
            ) {
                Text("Accept")
            }
        }

        if (state.acceptVisible && state.cancelVisible) {
            Spacer(modifier = Modifier.width(12.dp))
        }

        if (state.cancelVisible) {
            Button(
                onClick = onCancel,
                enabled = state.cancelEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = state.cancelBackgroundColor,
                    contentColor = state.cancelContentColor
                )
            ) {
                Text("Cancel")
            }
        }
    }
}
