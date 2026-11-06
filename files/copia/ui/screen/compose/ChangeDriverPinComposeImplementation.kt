package ifac.td.taxi.ui.screen.compose

import android.app.Application
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import ifac.td.taxi.R
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// -----------------------------------------------------------------------------
// UI STATE / EFFECT / ACTIONS
// -----------------------------------------------------------------------------

data class ChangeDriverPinUiState(
    val driverNumber: String = "",
    val currentPin: String = "",
    val newPin: String = "",
    val repeatPin: String = "",
    val buttons: ChangeDriverPinButtonsState = ChangeDriverPinButtonsState.initial(),
    val dialog: ChangeDriverPinDialogState? = null
)

data class ChangeDriverPinButtonsState(
    val accept: ChangeDriverPinButtonState,
    val cancel: ChangeDriverPinButtonState
) {
    companion object {
        fun initial() = ChangeDriverPinButtonsState(
            accept = ChangeDriverPinButtonState(
                visible = true,
                enabled = true,
                loading = false,
                textRes = R.string.accept,
                backgroundColorRes = R.color.colorPrimary,
                contentColorRes = android.R.color.white
            ),
            cancel = ChangeDriverPinButtonState(
                visible = true,
                enabled = true,
                loading = false,
                textRes = R.string.cancel,
                backgroundColorRes = android.R.color.transparent,
                contentColorRes = R.color.colorPrimary
            )
        )
    }
}

data class ChangeDriverPinButtonState(
    val visible: Boolean,
    val enabled: Boolean,
    val loading: Boolean,
    @StringRes val textRes: Int,
    @ColorRes val backgroundColorRes: Int,
    @ColorRes val contentColorRes: Int
)

sealed interface ChangeDriverPinDialogState {
    data class Message(
        @StringRes val titleRes: Int,
        val navigateBackOnAccept: Boolean = false,
        val showToastOnAccept: Boolean = false
    ) : ChangeDriverPinDialogState
}

sealed interface ChangeDriverPinUiEffect {
    data object NavigateBack : ChangeDriverPinUiEffect
    data class ShowDialog(val dialog: ChangeDriverPinDialogState.Message) : ChangeDriverPinUiEffect
    data class ShowToast(@StringRes val messageRes: Int) : ChangeDriverPinUiEffect
}

sealed interface ChangeDriverPinAction {
    data class DriverNumberChanged(val value: String) : ChangeDriverPinAction
    data class CurrentPinChanged(val value: String) : ChangeDriverPinAction
    data class NewPinChanged(val value: String) : ChangeDriverPinAction
    data class RepeatPinChanged(val value: String) : ChangeDriverPinAction
    data object AcceptClicked : ChangeDriverPinAction
    data object CancelClicked : ChangeDriverPinAction
    data object DialogAccepted : ChangeDriverPinAction
    data object DialogDismissed : ChangeDriverPinAction
    data object ChangeSuccess : ChangeDriverPinAction
    data object ChangeFailure : ChangeDriverPinAction
    data object NoUser : ChangeDriverPinAction
    data object ChangeStarted : ChangeDriverPinAction
}

// -----------------------------------------------------------------------------
// REDUCER
// -----------------------------------------------------------------------------

fun reduceChangeDriverPinUiState(
    current: ChangeDriverPinUiState,
    action: ChangeDriverPinAction
): ChangeDriverPinUiState {
    return when (action) {
        is ChangeDriverPinAction.DriverNumberChanged ->
            current.copy(driverNumber = action.value)

        is ChangeDriverPinAction.CurrentPinChanged ->
            current.copy(currentPin = action.value)

        is ChangeDriverPinAction.NewPinChanged ->
            current.copy(newPin = action.value)

        is ChangeDriverPinAction.RepeatPinChanged ->
            current.copy(repeatPin = action.value)

        ChangeDriverPinAction.AcceptClicked -> {
            when {
                current.driverNumber.isEmpty() ||
                    current.currentPin.isEmpty() ||
                    current.newPin.isEmpty() ||
                    current.repeatPin.isEmpty() -> current.copy(
                    dialog = ChangeDriverPinDialogState.Message(
                        titleRes = R.string.dialog_fill_inputs,
                        showToastOnAccept = true
                    )
                )

                current.newPin != current.repeatPin -> current.copy(
                    dialog = ChangeDriverPinDialogState.Message(
                        titleRes = R.string.dialog_no_match_pin,
                        showToastOnAccept = true
                    )
                )

                current.currentPin == current.newPin -> current.copy(
                    dialog = ChangeDriverPinDialogState.Message(
                        titleRes = R.string.dialog_equal_pin,
                        showToastOnAccept = true
                    )
                )

                else -> current.copy(
                    buttons = current.buttons.copy(
                        accept = current.buttons.accept.copy(enabled = false, loading = true),
                        cancel = current.buttons.cancel.copy(enabled = false)
                    )
                )
            }
        }

        ChangeDriverPinAction.ChangeStarted ->
            current.copy(
                buttons = current.buttons.copy(
                    accept = current.buttons.accept.copy(enabled = false, loading = true),
                    cancel = current.buttons.cancel.copy(enabled = false)
                )
            )

        ChangeDriverPinAction.ChangeSuccess ->
            current.copy(
                buttons = current.buttons.copy(
                    accept = current.buttons.accept.copy(enabled = true, loading = false),
                    cancel = current.buttons.cancel.copy(enabled = true)
                ),
                dialog = ChangeDriverPinDialogState.Message(
                    titleRes = R.string.dialog_change_pin,
                    navigateBackOnAccept = true
                )
            )

        ChangeDriverPinAction.ChangeFailure ->
            current.copy(
                buttons = current.buttons.copy(
                    accept = current.buttons.accept.copy(enabled = true, loading = false),
                    cancel = current.buttons.cancel.copy(enabled = true)
                ),
                dialog = ChangeDriverPinDialogState.Message(
                    titleRes = R.string.dialog_error_change_pin
                )
            )

        ChangeDriverPinAction.NoUser ->
            current.copy(
                buttons = current.buttons.copy(
                    accept = current.buttons.accept.copy(enabled = true, loading = false),
                    cancel = current.buttons.cancel.copy(enabled = true)
                ),
                dialog = ChangeDriverPinDialogState.Message(
                    titleRes = R.string.dialog_user_error,
                    navigateBackOnAccept = true
                )
            )

        ChangeDriverPinAction.DialogAccepted,
        ChangeDriverPinAction.DialogDismissed ->
            current.copy(dialog = null)

        ChangeDriverPinAction.CancelClicked -> current
    }
}

// -----------------------------------------------------------------------------
// REAL SCREEN VM (matches your existing BaseViewModel + SDK API)
// -----------------------------------------------------------------------------

class ChangeDriverPinViewModel(context: Application) : BaseViewModel(context) {

    private val _uiState = MutableStateFlow(ChangeDriverPinUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ChangeDriverPinUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private val _userCredentialsFlow = MutableSharedFlow<UserPresenter?>(extraBufferCapacity = 1)
    val userCredentialsFlow = _userCredentialsFlow.asSharedFlow()

    fun onAction(action: ChangeDriverPinAction) {
        _uiState.update { reduceChangeDriverPinUiState(it, action) }

        when (action) {
            ChangeDriverPinAction.AcceptClicked -> {
                val state = _uiState.value
                if (
                    state.driverNumber.isEmpty() ||
                    state.currentPin.isEmpty() ||
                    state.newPin.isEmpty() ||
                    state.repeatPin.isEmpty()
                ) {
                    emitDialog(
                        titleRes = R.string.dialog_fill_inputs,
                        showToastOnAccept = true
                    )
                } else if (state.newPin != state.repeatPin) {
                    emitDialog(
                        titleRes = R.string.dialog_no_match_pin,
                        showToastOnAccept = true
                    )
                } else if (state.currentPin == state.newPin) {
                    emitDialog(
                        titleRes = R.string.dialog_equal_pin,
                        showToastOnAccept = true
                    )
                } else {
                    requestUserPresenter()
                }
            }

            ChangeDriverPinAction.CancelClicked -> {
                viewModelScope.launch { _uiEffect.emit(ChangeDriverPinUiEffect.NavigateBack) }
            }

            ChangeDriverPinAction.DialogAccepted -> {
                val dialog = _uiState.value.dialog
                _uiState.update { it.copy(dialog = null) }

                if (dialog is ChangeDriverPinDialogState.Message) {
                    if (dialog.showToastOnAccept) {
                        viewModelScope.launch {
                            _uiEffect.emit(ChangeDriverPinUiEffect.ShowToast(R.string.toast_aceptado))
                        }
                    }
                    if (dialog.navigateBackOnAccept) {
                        viewModelScope.launch {
                            _uiEffect.emit(ChangeDriverPinUiEffect.NavigateBack)
                        }
                    }
                }
            }

            ChangeDriverPinAction.DialogDismissed -> {
                _uiState.update { it.copy(dialog = null) }
            }

            ChangeDriverPinAction.ChangeSuccess -> {
                emitDialog(
                    titleRes = R.string.dialog_change_pin,
                    navigateBackOnAccept = true
                )
            }

            ChangeDriverPinAction.ChangeFailure -> {
                emitDialog(
                    titleRes = R.string.dialog_error_change_pin
                )
            }

            ChangeDriverPinAction.NoUser -> {
                emitDialog(
                    titleRes = R.string.dialog_user_error,
                    navigateBackOnAccept = true
                )
            }

            else -> Unit
        }
    }

    fun getUserPresenter(view: ChangePasswordPinView) {
        viewModelScope.launch {
            _userCredentialsFlow.emit(UserModule.provideUserPresenter(view, context))
        }
    }

    fun changeDriverPin(
        userPresenter: UserPresenter,
        driverNumber: String,
        oldPin: String,
        newPin: String
    ) {
        userPresenter.changeDriverPin(driverNumber, oldPin, newPin, context)
    }

    private fun emitDialog(
        @StringRes titleRes: Int,
        navigateBackOnAccept: Boolean = false,
        showToastOnAccept: Boolean = false
    ) {
        val dialog = ChangeDriverPinDialogState.Message(
            titleRes = titleRes,
            navigateBackOnAccept = navigateBackOnAccept,
            showToastOnAccept = showToastOnAccept
        )
        _uiState.update { it.copy(dialog = dialog) }
        viewModelScope.launch { _uiEffect.emit(ChangeDriverPinUiEffect.ShowDialog(dialog)) }
    }
}

// -----------------------------------------------------------------------------
// SHARED / ACTIVITY VM ADAPTER
// Replace the placeholder with your real MainActivityViewModel adapter.
// This is the bridge that compiles against your existing shared VM pattern.
// -----------------------------------------------------------------------------

class ChangeDriverPinMainActivityViewModel(
    context: Application
) : BaseViewModel(context) {

    private val _navDirections = MutableSharedFlow<NavDirections>(extraBufferCapacity = 1)
    val navDirections = _navDirections.asSharedFlow()

    private val _uiEffect = MutableSharedFlow<ChangeDriverPinUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()

    fun navigateBack() {
        // If your MainActivityViewModel already exposes a back event, map it here.
        // Otherwise the screen can popBackStack() directly from the NavController.
        viewModelScope.launch {
            _uiEffect.emit(ChangeDriverPinUiEffect.NavigateBack)
        }
    }

    fun navigate(directions: NavDirections) {
        viewModelScope.launch {
            _navDirections.emit(directions)
        }
    }

    fun showToast(@StringRes messageRes: Int) {
        viewModelScope.launch {
            _uiEffect.emit(ChangeDriverPinUiEffect.ShowToast(messageRes))
        }
    }

    fun showDialog(dialog: ChangeDriverPinDialogState.Message) {
        viewModelScope.launch {
            _uiEffect.emit(ChangeDriverPinUiEffect.ShowDialog(dialog))
        }
    }
}

// -----------------------------------------------------------------------------
// COMPOSE SCREEN
// -----------------------------------------------------------------------------

@Composable
fun ChangeDriverPinScreen(
    navController: NavController,
    viewModel: ChangeDriverPinViewModel,
    sharedViewModel: ChangeDriverPinMainActivityViewModel,
    onShowHeader: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
    onNavigateBack: () -> Unit = { navController.popBackStack() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onShowHeader(false)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                ChangeDriverPinUiEffect.NavigateBack -> onNavigateBack()
                is ChangeDriverPinUiEffect.ShowToast -> onShowToast(effect.messageRes)
                is ChangeDriverPinUiEffect.ShowDialog -> Unit
            }
        }
    }

    LaunchedEffect(sharedViewModel) {
        sharedViewModel.navDirections.collectLatest { directions ->
            navController.navigate(directions)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.userCredentialsFlow.collectLatest { userPresenter ->
            if (userPresenter != null) {
                viewModel.changeDriverPin(
                    userPresenter = userPresenter,
                    driverNumber = uiState.driverNumber.trim(),
                    oldPin = uiState.currentPin.trim(),
                    newPin = uiState.newPin.trim()
                )
            } else {
                viewModel.onAction(ChangeDriverPinAction.NoUser)
            }
        }
    }

    ChangeDriverPinContent(
        state = uiState,
        onEvent = viewModel::onAction
    )
}

@Composable
fun ChangeDriverPinContent(
    state: ChangeDriverPinUiState,
    onEvent: (ChangeDriverPinAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = state.driverNumber,
            onValueChange = { onEvent(ChangeDriverPinAction.DriverNumberChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.driver_number)) }
        )

        OutlinedTextField(
            value = state.currentPin,
            onValueChange = { onEvent(ChangeDriverPinAction.CurrentPinChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.current_pin)) },
            visualTransformation = PasswordVisualTransformation()
        )

        OutlinedTextField(
            value = state.newPin,
            onValueChange = { onEvent(ChangeDriverPinAction.NewPinChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.new_pin)) },
            visualTransformation = PasswordVisualTransformation()
        )

        OutlinedTextField(
            value = state.repeatPin,
            onValueChange = { onEvent(ChangeDriverPinAction.RepeatPinChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.repeat_pin)) },
            visualTransformation = PasswordVisualTransformation()
        )

        ChangeDriverPinButtons(
            state = state.buttons,
            onAcceptClick = { onEvent(ChangeDriverPinAction.AcceptClicked) },
            onCancelClick = { onEvent(ChangeDriverPinAction.CancelClicked) }
        )
    }

    val dialog = state.dialog
    if (dialog is ChangeDriverPinDialogState.Message) {
        AlertDialog(
            onDismissRequest = { onEvent(ChangeDriverPinAction.DialogDismissed) },
            title = { Text(stringResource(dialog.titleRes)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ChangeDriverPinAction.DialogAccepted) }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// CUSTOM BUTTON API MATCHING YOUR EXISTING COMPONENT
// -----------------------------------------------------------------------------

@Composable
fun ChangeDriverPinButtons(
    state: ChangeDriverPinButtonsState,
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.cancel.visible) {
            CustomButton(
                text = stringResource(state.cancel.textRes),
                onClick = onCancelClick,
                enabled = state.cancel.enabled,
                buttonType = ButtonType.CANCEL,
                modifier = Modifier.weight(1f)
            )
        }

        if (state.accept.visible) {
            CustomButton(
                text = if (state.accept.loading) "..." else stringResource(state.accept.textRes),
                onClick = onAcceptClick,
                enabled = state.accept.enabled && !state.accept.loading,
                buttonType = ButtonType.ACCEPT,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
