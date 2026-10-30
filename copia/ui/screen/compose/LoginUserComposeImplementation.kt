package ifac.td.taxi.ui.screen.compose

import android.app.Application
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.interfacom.sdk.taximeter.bravocomm.StatusConnection
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.ConfigurationScreenPasswordUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.framework.sdk.model.UserInfo
import ifac.td.taxi.framework.sdk.usecase.ConfigurationUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// -----------------------------------------------------------------------------
// STATE
// -----------------------------------------------------------------------------

@Immutable
data class LoginUserUiState(
    val user: String = "",
    val password: String = "",
    val userError: String? = null,
    val showChangePasswordText: Boolean = false,
    val isUserEnabled: Boolean = true,
    val isPasswordEnabled: Boolean = true,
    val userTextColor: Long = 0xFFFFFFFF,
    val passwordTextColor: Long = 0xFFFFFFFF,
    val progressVisible: Boolean = false,
    val progress: Int = 0,
    val buttons: LoginUserButtonsState = LoginUserButtonsState(),
    val configurationPasswordDialog: LoginUserDialogState? = null,
)

@Immutable
data class LoginUserButtonsState(
    val cancel: LoginUserButtonState = LoginUserButtonState(
        visible = true,
        enabled = true,
        style = CustomButton.StyleButton.ENABLE,
        background = CustomButton.BackgroundButtonColor.GREEN
    ),
    val accept: LoginUserButtonState = LoginUserButtonState(
        visible = true,
        enabled = true,
        style = CustomButton.StyleButton.ENABLE,
        background = CustomButton.BackgroundButtonColor.GREEN
    ),
    val changeUser: LoginUserButtonState = LoginUserButtonState(
        visible = false,
        enabled = false,
        style = CustomButton.StyleButton.DISABLE,
        background = CustomButton.BackgroundButtonColor.GRAY
    )
)

@Immutable
data class LoginUserButtonState(
    val visible: Boolean,
    val enabled: Boolean,
    val style: CustomButton.StyleButton,
    val background: CustomButton.BackgroundButtonColor
)

@Immutable
data class LoginUserDialogState(
    val visible: Boolean = false,
    val title: String = "",
    val description: String = "",
    val pin: String = "",
    val maxLength: Int = 4
)

// -----------------------------------------------------------------------------
// EVENTS / EFFECTS
// -----------------------------------------------------------------------------

sealed interface LoginUserUiEvent {
    data class UserChanged(val value: String) : LoginUserUiEvent
    data class PasswordChanged(val value: String) : LoginUserUiEvent
    data object CancelClicked : LoginUserUiEvent
    data object AcceptClicked : LoginUserUiEvent
    data object ChangePasswordClicked : LoginUserUiEvent
    data object ChangeUserClicked : LoginUserUiEvent
    data class ConfigurationPinChanged(val value: String) : LoginUserUiEvent
    data object ConfigurationPinCancelClicked : LoginUserUiEvent
    data object ConfigurationPinAcceptClicked : LoginUserUiEvent
    data object ScreenStarted : LoginUserUiEvent
    data object RetryLoginButtonReset : LoginUserUiEvent
}

sealed interface LoginUserUiEffect {
    data object NavigateBack : LoginUserUiEffect
    data class NavigateTo(val directions: NavDirections) : LoginUserUiEffect
    data object ShowNetworkError : LoginUserUiEffect
    data object StartBravoService : LoginUserUiEffect
    data object DownloadBravoConfiguration : LoginUserUiEffect
    data object ShowIncorrectPasswordToast : LoginUserUiEffect
    data class OpenConfigurationPasswordDialog(
        val title: String,
        val description: String,
        val maxLength: Int = 4
    ) : LoginUserUiEffect
}

// -----------------------------------------------------------------------------
// REDUCER INPUTS
// -----------------------------------------------------------------------------

sealed interface LoginUserReducerInput {
    data class ProgressBarChanged(val visible: Boolean, val progress: Int) : LoginUserReducerInput
    data class CorrectLoginChanged(val correct: Boolean) : LoginUserReducerInput
    data object DownloadedInitialBravoConfiguration : LoginUserReducerInput
    data class LoginDataChanged(val user: String?, val password: String?) : LoginUserReducerInput
    data class ConfigurationPasswordDialogChanged(val hasPassword: Boolean?) : LoginUserReducerInput
    data class CorrectPasswordChanged(val correctPassword: Boolean) : LoginUserReducerInput
}

object LoginUserButtonsReducer {
    fun reduce(current: LoginUserUiState, input: LoginUserReducerInput): LoginUserUiState {
        return when (input) {
            is LoginUserReducerInput.ProgressBarChanged -> current.copy(
                progressVisible = input.visible,
                progress = input.progress
            )

            is LoginUserReducerInput.CorrectLoginChanged -> {
                if (input.correct) {
                    current.copy(
                        buttons = current.buttons.copy(
                            accept = current.buttons.accept.copy(
                                enabled = false,
                                style = CustomButton.StyleButton.LOADING,
                                background = CustomButton.BackgroundButtonColor.GREEN
                            )
                        ),
                        userError = null
                    )
                } else {
                    current.copy(
                        userError = current.userError ?: "incorrect_login",
                        buttons = current.buttons.copy(
                            accept = current.buttons.accept.copy(
                                enabled = true,
                                style = CustomButton.StyleButton.ENABLE,
                                background = CustomButton.BackgroundButtonColor.GREEN
                            )
                        )
                    )
                }
            }

            LoginUserReducerInput.DownloadedInitialBravoConfiguration -> current.copy(
                progressVisible = false,
                progress = 0
            )

            is LoginUserReducerInput.LoginDataChanged -> {
                val hasData = !input.user.isNullOrEmpty() && !input.password.isNullOrEmpty()
                current.copy(
                    user = input.user.orEmpty(),
                    password = input.password.orEmpty(),
                    showChangePasswordText = hasData
                )
            }

            is LoginUserReducerInput.ConfigurationPasswordDialogChanged -> {
                if (input.hasPassword == true) {
                    current.copy(
                        isUserEnabled = false,
                        isPasswordEnabled = false,
                        userTextColor = 0xFFB0B0B0,
                        passwordTextColor = 0xFFB0B0B0,
                        buttons = current.buttons.copy(
                            changeUser = current.buttons.changeUser.copy(
                                visible = true,
                                enabled = true,
                                style = CustomButton.StyleButton.ENABLE,
                                background = CustomButton.BackgroundButtonColor.GREEN
                            )
                        ),
                        configurationPasswordDialog = LoginUserDialogState(
                            visible = false,
                            title = "",
                            description = "",
                            pin = "",
                            maxLength = 4
                        )
                    )
                } else {
                    current.copy(
                        isUserEnabled = true,
                        isPasswordEnabled = true,
                        userTextColor = 0xFFFFFFFF,
                        passwordTextColor = 0xFFFFFFFF,
                        buttons = current.buttons.copy(
                            changeUser = current.buttons.changeUser.copy(
                                visible = false,
                                enabled = false,
                                style = CustomButton.StyleButton.DISABLE,
                                background = CustomButton.BackgroundButtonColor.GRAY
                            )
                        )
                    )
                }
            }

            is LoginUserReducerInput.CorrectPasswordChanged -> {
                if (input.correctPassword) {
                    current.copy(
                        isUserEnabled = true,
                        isPasswordEnabled = true,
                        userTextColor = 0xFFFFFFFF,
                        passwordTextColor = 0xFFFFFFFF,
                        buttons = current.buttons.copy(
                            changeUser = current.buttons.changeUser.copy(
                                visible = true,
                                enabled = false,
                                style = CustomButton.StyleButton.DISABLE,
                                background = CustomButton.BackgroundButtonColor.GRAY
                            )
                        ),
                        configurationPasswordDialog = current.configurationPasswordDialog?.copy(
                            visible = false,
                            pin = ""
                        )
                    )
                } else current
            }
        }
    }
}

// -----------------------------------------------------------------------------
// LOGIN USER VIEWMODEL
// -----------------------------------------------------------------------------

class LoginUserComposeViewModel(
    private val externalBridge: ExternalBridgeInterface,
    private val configuration: ConfigurationUseCase,
    private val sessionUseCase: SessionUseCase,
    private val configurationScreenPasswordUseCase: ConfigurationScreenPasswordUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val migrationV2UseCase: MigrationV2UseCase,
    application: Application,
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _uiState = MutableStateFlow(LoginUserUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<LoginUserUiEffect>()
    val effects = _effects.asSharedFlow()

    fun onEvent(event: LoginUserUiEvent) {
        when (event) {
            is LoginUserUiEvent.UserChanged ->
                _uiState.update { it.copy(user = event.value, userError = null) }

            is LoginUserUiEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.value) }

            LoginUserUiEvent.CancelClicked ->
                viewModelScope.launch { _effects.emit(LoginUserUiEffect.NavigateBack) }

            LoginUserUiEvent.AcceptClicked -> clickAccept()

            LoginUserUiEvent.ChangePasswordClicked ->
                viewModelScope.launch {
                    _effects.emit(
                        LoginUserUiEffect.NavigateTo(
                            LoginUserFragmentDirections.actionLoginUserFragmentToChangeUserPasswordFragment()
                        )
                    )
                }

            LoginUserUiEvent.ChangeUserClicked -> {
                val dialog = _uiState.value.configurationPasswordDialog
                if (dialog != null) {
                    viewModelScope.launch {
                        _effects.emit(
                            LoginUserUiEffect.OpenConfigurationPasswordDialog(
                                title = context.getString(R.string.pin_actual),
                                description = context.getString(R.string.pin_actual_hint),
                                maxLength = 4
                            )
                        )
                    }
                }
            }

            is LoginUserUiEvent.ConfigurationPinChanged ->
                _uiState.update {
                    it.copy(configurationPasswordDialog = it.configurationPasswordDialog?.copy(pin = event.value))
                }

            LoginUserUiEvent.ConfigurationPinCancelClicked ->
                _uiState.update {
                    it.copy(configurationPasswordDialog = it.configurationPasswordDialog?.copy(visible = false, pin = ""))
                }

            LoginUserUiEvent.ConfigurationPinAcceptClicked -> {
                val pin = _uiState.value.configurationPasswordDialog?.pin.orEmpty()
                if (pin.isNotBlank()) checkSettingsPassword(pin)
            }

            LoginUserUiEvent.ScreenStarted -> {
                checkSavedData()
                checkHasSettingsPassword()
            }

            LoginUserUiEvent.RetryLoginButtonReset ->
                _uiState.update {
                    it.copy(
                        buttons = it.buttons.copy(
                            accept = it.buttons.accept.copy(
                                enabled = true,
                                style = CustomButton.StyleButton.ENABLE,
                                background = CustomButton.BackgroundButtonColor.GREEN
                            )
                        )
                    )
                }
        }
    }

    fun checkSavedData() {
        viewModelScope.launch {
            val session = sessionUseCase.getSession()
            val user = session?.username
            val pass = session?.password
            _uiState.update {
                it.copy(
                    user = user.orEmpty(),
                    password = pass.orEmpty(),
                    showChangePasswordText = !user.isNullOrEmpty() && !pass.isNullOrEmpty()
                )
            }
        }
    }

    fun checkHasSettingsPassword() {
        viewModelScope.launch {
            val hasPassword = configurationScreenPasswordUseCase.hasSettingsPassword()
            _uiState.update {
                if (hasPassword) {
                    it.copy(
                        isUserEnabled = false,
                        isPasswordEnabled = false,
                        userTextColor = 0xFFB0B0B0,
                        passwordTextColor = 0xFFB0B0B0,
                        buttons = it.buttons.copy(
                            changeUser = it.buttons.changeUser.copy(
                                visible = true,
                                enabled = true,
                                style = CustomButton.StyleButton.ENABLE,
                                background = CustomButton.BackgroundButtonColor.GREEN
                            )
                        ),
                        configurationPasswordDialog = LoginUserDialogState(
                            visible = false,
                            title = context.getString(R.string.pin_actual),
                            description = context.getString(R.string.pin_actual_hint),
                            pin = "",
                            maxLength = 4
                        )
                    )
                } else {
                    it.copy(
                        isUserEnabled = true,
                        isPasswordEnabled = true,
                        userTextColor = 0xFFFFFFFF,
                        passwordTextColor = 0xFFFFFFFF,
                        buttons = it.buttons.copy(
                            changeUser = it.buttons.changeUser.copy(
                                visible = false,
                                enabled = false,
                                style = CustomButton.StyleButton.DISABLE,
                                background = CustomButton.BackgroundButtonColor.GRAY
                            )
                        )
                    )
                }
            }
        }
    }

    fun checkSettingsPassword(pin: String) {
        viewModelScope.launch {
            val encrypted = configurationScreenPasswordUseCase.encryptSettingsPassword(pin)
            val saved = configurationScreenPasswordUseCase.getSettingsPassword()
            val correct = encrypted == saved

            if (correct) {
                _uiState.update {
                    it.copy(
                        isUserEnabled = true,
                        isPasswordEnabled = true,
                        userTextColor = 0xFFFFFFFF,
                        passwordTextColor = 0xFFFFFFFF,
                        buttons = it.buttons.copy(
                            changeUser = it.buttons.changeUser.copy(
                                visible = true,
                                enabled = false,
                                style = CustomButton.StyleButton.DISABLE,
                                background = CustomButton.BackgroundButtonColor.GRAY
                            )
                        ),
                        configurationPasswordDialog = it.configurationPasswordDialog?.copy(
                            visible = false,
                            pin = ""
                        )
                    )
                }
            } else {
                _uiState.update {
                    it.copy(configurationPasswordDialog = it.configurationPasswordDialog?.copy(visible = false, pin = ""))
                }
                _effects.emit(LoginUserUiEffect.ShowIncorrectPasswordToast)
            }
        }
    }

    private fun clickAccept() {
        val state = _uiState.value
        if (state.user.isNotBlank() && state.password.isNotBlank()) {
            _uiState.update {
                it.copy(
                    buttons = it.buttons.copy(
                        accept = it.buttons.accept.copy(
                            enabled = false,
                            style = CustomButton.StyleButton.LOADING,
                            background = CustomButton.BackgroundButtonColor.GREEN
                        )
                    )
                )
            }
            loginUser(
                user = state.user.trim(),
                password = state.password,
                fromMigration = false
            )
        }
    }

    fun loginUser(user: String, password: String, fromMigration: Boolean = false) {
        viewModelScope.launch {
            if (!isInternetConnectionAvailable(context)) {
                _effects.emit(LoginUserUiEffect.ShowNetworkError)
                _effects.emit(LoginUserUiEffect.NavigateBack)
                return@launch
            }

            externalBridge.setUsername(user)
            externalBridge.setPassword(password)

            configuration.downloadConfiguration(
                userInfo = UserInfo(user = user, password = password),
                fromMigration = fromMigration
            )
        }
    }

    fun clickChangePassword() {
        viewModelScope.launch {
            _effects.emit(
                LoginUserUiEffect.NavigateTo(
                    LoginUserFragmentDirections.actionLoginUserFragmentToChangeUserPasswordFragment()
                )
            )
        }
    }

    fun downloadBravoConfiguration() {
        viewModelScope.launch {
            _effects.emit(LoginUserUiEffect.DownloadBravoConfiguration)
        }
    }

    fun migrateConfigsUser() {
        viewModelScope.launch { migrationV2UseCase.migrateUserConfigPrefs() }
    }

    fun migrateShiftsAndTrips() {
        viewModelScope.launch {
            val allShifts = migrationV2UseCase.getAllShifts()
            if (allShifts.isNotEmpty()) migrationV2UseCase.migrateShifts(allShifts)
            migrateTrips()
        }
    }

    private suspend fun migrateTrips() {
        val allTrips = migrationV2UseCase.getAllTrips()
        allTrips.forEach { pair ->
            pair.second?.let { trip ->
                if (migrationV2UseCase.existsShiftId(trip.fkShiftId)) {
                    migrationV2UseCase.migrateTrip(trip)
                }
            }
        }
    }

    fun migratePartials() {
        viewModelScope.launch { migrationV2UseCase.migratePartials() }
    }

    fun migratePortugal() {
        viewModelScope.launch {
            if (licensingUseCase.getLicensingParameters()?.isFiscalService == true) {
                migrationV2UseCase.migratePortugal()
            }
        }
    }
}

// -----------------------------------------------------------------------------
// MAIN ACTIVITY SHARED VM ADAPTER
// -----------------------------------------------------------------------------

@Immutable
data class LoginUserMainActivityBridgeState(
    val progressVisible: Boolean = false,
    val progress: Int = 0,
    val waitingForXmpp: Boolean = false
)

sealed interface LoginUserMainActivityBridgeEffect {
    data object StartBravoService : LoginUserMainActivityBridgeEffect
    data class SaveCredentials(val user: String, val password: String) : LoginUserMainActivityBridgeEffect
    data object NavigateBack : LoginUserMainActivityBridgeEffect
    data object DownloadBravoConfiguration : LoginUserMainActivityBridgeEffect
    data object ShowIncorrectLoginUi : LoginUserMainActivityBridgeEffect
}

class LoginUserMainActivityViewModel(
    private val sharedViewModel: MainActivityViewModel
) : ViewModel() {

    private val _bridgeState = MutableStateFlow(LoginUserMainActivityBridgeState())
    val bridgeState = _bridgeState.asStateFlow()

    private val _effects = MutableSharedFlow<LoginUserMainActivityBridgeEffect>()
    val effects = _effects.asSharedFlow()

    private var xmppJob: Job? = null
    private var bound = false

    fun bind() {
        if (bound) return
        bound = true

        viewModelScope.launch {
            sharedViewModel.progressBarFlow.collectLatest { payload ->
                _bridgeState.update {
                    it.copy(
                        progressVisible = payload.first == View.VISIBLE,
                        progress = payload.second
                    )
                }
            }
        }

        viewModelScope.launch {
            sharedViewModel.correctLoginFlow.collectLatest { payload ->
                val correct = payload.first
                if (correct) {
                    _effects.emit(LoginUserMainActivityBridgeEffect.StartBravoService)

                    val user = payload.second ?: ""
                    val password = payload.third ?: ""
                    _effects.emit(LoginUserMainActivityBridgeEffect.SaveCredentials(user, password))

                    _bridgeState.update { it.copy(waitingForXmpp = true) }

                    xmppJob?.cancel()
                    xmppJob = viewModelScope.launch {
                        sharedViewModel.xmppStatusFlow.collectLatest { xmpp ->
                            if (xmpp == StatusConnection.CONNECTED) {
                                _effects.emit(LoginUserMainActivityBridgeEffect.DownloadBravoConfiguration)
                            }
                        }
                    }
                } else {
                    _effects.emit(LoginUserMainActivityBridgeEffect.ShowIncorrectLoginUi)
                }
            }
        }

        viewModelScope.launch {
            sharedViewModel.downloadedInitialBravoconfigurationFlow.collectLatest {
                _effects.emit(LoginUserMainActivityBridgeEffect.NavigateBack)
                sharedViewModel.updateProgressBarFlow(View.GONE, 0)
                _bridgeState.update {
                    it.copy(progressVisible = false, progress = 0, waitingForXmpp = false)
                }
                xmppJob?.cancel()
                xmppJob = null
            }
        }
    }

    override fun onCleared() {
        xmppJob?.cancel()
        super.onCleared()
    }
}

// -----------------------------------------------------------------------------
// COMPOSE SCREEN
// -----------------------------------------------------------------------------

@Composable
fun LoginUserRoute(
    navController: NavController,
    viewModel: LoginUserComposeViewModel,
    mainActivityBridgeViewModel: LoginUserMainActivityViewModel,
    onStartBravoService: () -> Unit,
    onSaveCredentials: (String, String) -> Unit,
    onShowToast: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onDownloadBravoConfiguration: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bridgeState by mainActivityBridgeViewModel.bridgeState.collectAsStateWithLifecycle()

    var dialogState by remember { mutableStateOf<LoginUserDialogState?>(null) }

    LaunchedEffect(Unit) {
        mainActivityBridgeViewModel.bind()
        viewModel.onEvent(LoginUserUiEvent.ScreenStarted)
    }

    LaunchedEffect(mainActivityBridgeViewModel) {
        mainActivityBridgeViewModel.effects.collectLatest { effect ->
            when (effect) {
                LoginUserMainActivityBridgeEffect.StartBravoService -> onStartBravoService()
                is LoginUserMainActivityBridgeEffect.SaveCredentials ->
                    onSaveCredentials(effect.user, effect.password)

                LoginUserMainActivityBridgeEffect.NavigateBack -> onNavigateBack()
                LoginUserMainActivityBridgeEffect.DownloadBravoConfiguration -> onDownloadBravoConfiguration()
                LoginUserMainActivityBridgeEffect.ShowIncorrectLoginUi -> {
                    viewModel.onEvent(LoginUserUiEvent.RetryLoginButtonReset)
                    viewModel.onLoginIncorrect()
                }
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                LoginUserUiEffect.NavigateBack -> onNavigateBack()
                is LoginUserUiEffect.NavigateTo -> navController.navigate(effect.directions)
                LoginUserUiEffect.ShowNetworkError -> onShowToast(R.string.network_error)
                LoginUserUiEffect.StartBravoService -> onStartBravoService()
                LoginUserUiEffect.DownloadBravoConfiguration -> onDownloadBravoConfiguration()
                LoginUserUiEffect.ShowIncorrectPasswordToast -> onShowToast(R.string.pin_incorrecto)
                is LoginUserUiEffect.OpenConfigurationPasswordDialog -> {
                    dialogState = LoginUserDialogState(
                        visible = true,
                        title = effect.title,
                        description = effect.description,
                        pin = "",
                        maxLength = effect.maxLength
                    )
                }
            }
        }
    }

    LoginUserScreen(
        state = uiState.copy(
            progressVisible = bridgeState.progressVisible,
            progress = bridgeState.progress
        ),
        onEvent = viewModel::onEvent,
        dialogState = dialogState,
        onDialogPinChange = { pin ->
            dialogState = dialogState?.copy(pin = pin)
            viewModel.onEvent(LoginUserUiEvent.ConfigurationPinChanged(pin))
        },
        onDialogCancel = {
            dialogState = null
            viewModel.onEvent(LoginUserUiEvent.ConfigurationPinCancelClicked)
        },
        onDialogAccept = {
            viewModel.onEvent(LoginUserUiEvent.ConfigurationPinAcceptClicked)
            dialogState = null
        }
    )
}

@Composable
fun LoginUserScreen(
    state: LoginUserUiState,
    onEvent: (LoginUserUiEvent) -> Unit,
    dialogState: LoginUserDialogState?,
    onDialogPinChange: (String) -> Unit,
    onDialogCancel: () -> Unit,
    onDialogAccept: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.user,
            onValueChange = { onEvent(LoginUserUiEvent.UserChanged(it)) },
            enabled = state.isUserEnabled,
            label = { Text("User") },
            isError = state.userError != null,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color(state.userTextColor))
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(LoginUserUiEvent.PasswordChanged(it)) },
            enabled = state.isPasswordEnabled,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color(state.passwordTextColor))
        )

        state.userError?.let {
            Spacer(Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(20.dp))

        LoginUserButtons(
            buttons = state.buttons,
            onCancel = { onEvent(LoginUserUiEvent.CancelClicked) },
            onAccept = { onEvent(LoginUserUiEvent.AcceptClicked) },
            onChangeUser = { onEvent(LoginUserUiEvent.ChangeUserClicked) },
            onChangePassword = { onEvent(LoginUserUiEvent.ChangePasswordClicked) },
            showChangePasswordText = state.showChangePasswordText
        )

        if (state.progressVisible) {
            Spacer(Modifier.height(20.dp))
            CircularProgressIndicator()
            Spacer(Modifier.height(8.dp))
            Text(text = "Progress: ${state.progress}%")
        }
    }

    if (dialogState?.visible == true) {
        AlertDialog(
            onDismissRequest = onDialogCancel,
            title = { Text(dialogState.title) },
            text = {
                Column {
                    Text(dialogState.description)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dialogState.pin,
                        onValueChange = {
                            if (it.length <= dialogState.maxLength) onDialogPinChange(it)
                        },
                        label = { Text("PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDialogAccept) { Text("Accept") }
            },
            dismissButton = {
                TextButton(onClick = onDialogCancel) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun LoginUserButtons(
    buttons: LoginUserButtonsState,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    onChangeUser: () -> Unit,
    onChangePassword: () -> Unit,
    showChangePasswordText: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (buttons.cancel.visible) {
            Button(
                onClick = onCancel,
                enabled = buttons.cancel.enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2ECC71),
                    contentColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Cancel") }
        }

        if (buttons.accept.visible) {
            Button(
                onClick = onAccept,
                enabled = buttons.accept.enabled && buttons.accept.style != CustomButton.StyleButton.LOADING,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (buttons.accept.background) {
                        CustomButton.BackgroundButtonColor.GREEN -> Color(0xFF2ECC71)
                        CustomButton.BackgroundButtonColor.GRAY -> Color(0xFF9E9E9E)
                    },
                    contentColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            ) {
                if (buttons.accept.style == CustomButton.StyleButton.LOADING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Accept")
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    if (buttons.changeUser.visible) {
        Button(
            onClick = onChangeUser,
            enabled = buttons.changeUser.enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = when (buttons.changeUser.background) {
                    CustomButton.BackgroundButtonColor.GREEN -> Color(0xFF2ECC71)
                    CustomButton.BackgroundButtonColor.GRAY -> Color(0xFF9E9E9E)
                },
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Change user") }
    }

    if (showChangePasswordText) {
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onChangePassword) { Text("Change password") }
    }
}

// -----------------------------------------------------------------------------
// FRAGMENT DIRECTIONS PLACEHOLDER NOTE
// -----------------------------------------------------------------------------
//
// Replace LoginUserFragmentDirections.actionLoginUserFragmentToChangeUserPasswordFragment()
// with your generated Safe Args class if the fragment name differs.
// The code above is already using NavDirections, not R.id navigation.
// -----------------------------------------------------------------------------


