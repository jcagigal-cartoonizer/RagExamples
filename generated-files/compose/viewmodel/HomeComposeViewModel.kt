package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.LoginUserCustomDialog
import ifac.td.taxi.ui.screen.state.LoginUserButtons
import ifac.td.taxi.ui.screen.state.LoginUserButtonsState
import ifac.td.taxi.ui.screen.LoginUserScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import ifac.td.taxi.ui.screen.state.LoginUserUiEvent
import ifac.td.taxi.ui.screen.state.LoginUserUiState
import ifac.td.taxi.ui.screen.state.MessageUiState
import ifac.td.taxi.domain.usecase.PendingTripsUseCaseImpl
import ifac.td.taxi.ui.screen.state.DashboardDialogState
import ifac.td.taxi.ui.screen.state.DashboardButtonsState
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import ifac.td.taxi.ui.screen.state.ComposeButtonState
import ifac.td.taxi.ui.screen.state.LoginUserUiEffect
// // # 2) Compose-friendly ViewModel

// This replaces the fragment-driven event collection with `StateFlow + SharedFlow`.

// ## `LoginUserComposeViewModel.kt`


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.ConfigurationScreenPasswordUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.framework.sdk.model.UserInfo
import ifac.td.taxi.framework.sdk.usecase.ConfigurationUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginUserComposeViewModel(
    application: Application,
    private val externalBridge: ExternalBridgeInterface,
    private val configuration: ConfigurationUseCase,
    private val sessionUseCase: SessionUseCase,
    private val configurationScreenPasswordUseCase: ConfigurationScreenPasswordUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val migrationV2UseCase: MigrationV2UseCase,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUserUiState())
    val uiState: StateFlow<LoginUserUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<LoginUserUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<LoginUserUiEffect> = _effects.asSharedFlow()

    fun onScreenStarted(autoDownloadFromMigration: Boolean) {
        checkSavedData(autoDownloadFromMigration)
        checkHasSettingsPassword()
    }

    fun onUserChanged(value: String) {
        _uiState.update { it.copy(user = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun onCancelClicked() {
        viewModelScope.launch {
            _effects.emit(LoginUserUiEffect.NavigateBack)
        }
    }

    fun onAcceptClicked(autoDownloadFromMigration: Boolean) {
        val state = _uiState.value

        if (state.user.isBlank()) {
            viewModelScope.launch {
                _effects.emit(LoginUserUiEffect.ShowUserError(R.string.incorrect_login))
            }
            return
        }

        viewModelScope.launch {
            if (!isInternetConnectionAvailable(getApplication())) {
                _effects.emit(LoginUserUiEffect.ShowToast(R.string.network_error))
                _effects.emit(LoginUserUiEffect.NavigateBack)
                return@launch
            }

            _uiState.update { it.copy(isLoginLoading = true) }
            externalBridge.setUsername(state.user.trim())
            externalBridge.setPassword(state.password)

            configuration.downloadConfiguration(
                userInfo = UserInfo(user = state.user.trim(), password = state.password),
                fromMigration = autoDownloadFromMigration
            )
        }
    }

    fun onChangePasswordClicked() {
        viewModelScope.launch {
            _effects.emit(LoginUserUiEffect.NavigateToChangePassword)
        }
    }

    fun onChangeUserClicked() {
        val hasPassword = _uiState.value.configurationPasswordDialogVisible
        if (hasPassword) {
            viewModelScope.launch {
                _effects.emit(
                    LoginUserUiEffect.OpenConfigurationPasswordDialog(
                        title = R.string.pin_actual,
                        hint = R.string.pin_actual_hint,
                        maxLength = 4
                    )
                )
            }
        }
    }

    fun checkSavedData(autoDownloadFromMigration: Boolean = false) {
        viewModelScope.launch {
            val session = sessionUseCase.getSession()
            val user = session?.username
            val pass = session?.password

            if (!user.isNullOrEmpty() && !pass.isNullOrEmpty()) {
                _uiState.update {
                    it.copy(
                        user = user,
                        password = pass,
                        showChangePassword = true
                    )
                }

                if (autoDownloadFromMigration) {
                    onAcceptClicked(autoDownloadFromMigration = true)
                    migrateConfigsUser()
                    migrateShiftsAndTrips()
                    migratePartials()
                    migratePortugal()
                }
            } else {
                _uiState.update { it.copy(showChangePassword = false) }
            }
        }
    }

    fun checkHasSettingsPassword() {
        viewModelScope.launch {
            val hasPassword = configurationScreenPasswordUseCase.hasSettingsPassword()
            _uiState.update {
                it.copy(
                    configurationPasswordDialogVisible = hasPassword,
                    editTextsEnabled = !hasPassword,
                    showChangeUserButton = hasPassword
                )
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
                        editTextsEnabled = true,
                        showChangeUserButton = false,
                        configurationPasswordDialogVisible = false
                    )
                }
                _effects.emit(LoginUserUiEffect.HideConfigurationPasswordDialog)
            } else {
                _effects.emit(LoginUserUiEffect.ShowToast(R.string.pin_incorrecto))
            }
        }
    }

    fun downloadBravoConfiguration() {
        viewModelScope.launch {
            configuration.downloadBravoconfiguration()
        }
    }

    fun migrateConfigsUser() {
        viewModelScope.launch {
            migrationV2UseCase.migrateUserConfigPrefs()
        }
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

    fun onLoginSuccess() {
        _uiState.update { it.copy(isLoginLoading = false) }
        viewModelScope.launch { _effects.emit(LoginUserUiEffect.StartBravoService) }
    }

    fun onLoginFailure() {
        _uiState.update { it.copy(isLoginLoading = false) }
        viewModelScope.launch { _effects.emit(LoginUserUiEffect.ShowUserError(R.string.incorrect_login)) }
    }
}


// # 3) Compose screen preserving Navigation

// ## `LoginUserRoute.kt`

// This is the composable replacement for the fragment.


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun LoginUserRoute(
    navController: NavController,
    viewModel: LoginUserComposeViewModel,
    autoDownloadFromMigration: Boolean = false,
    onNavigateBack: () -> Unit,
    onStartBravoService: () -> Unit,
    onSaveCredentials: (user: String, pass: String) -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var dialogVisible by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf(R.string.pin_actual) }
    var dialogHint by remember { mutableStateOf(R.string.pin_actual_hint) }
    var dialogValue by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.onScreenStarted(autoDownloadFromMigration)
    }

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LoginUserUiEffect.NavigateBack -> onNavigateBack()
                LoginUserUiEffect.NavigateToChangePassword -> {
                    navController.navigate(R.id.action_loginUserFragment_to_changeUserPasswordFragment)
                }
                LoginUserUiEffect.StartBravoService -> onStartBravoService()
                is LoginUserUiEffect.ShowToast -> onShowToast(effect.messageRes)
                is LoginUserUiEffect.ShowUserError -> {
                    onShowToast(effect.messageRes)
                }
                is LoginUserUiEffect.OpenConfigurationPasswordDialog -> {
                    dialogVisible = true
                    dialogTitle = effect.title
                    dialogHint = effect.hint
                }
                LoginUserUiEffect.HideConfigurationPasswordDialog -> {
                    dialogVisible = false
                    dialogValue = ""
                }
                LoginUserUiEffect.DownloadBravoConfiguration -> viewModel.downloadBravoConfiguration()
                LoginUserUiEffect.SaveCredentials -> onSaveCredentials(uiState.user, uiState.password)
            }
        }
    }

    LoginUserScreen(
        uiState = uiState,
        buttonsState = LoginUserButtonsState.fromUiState(uiState),
        onUserChange = viewModel::onUserChanged,
        onPasswordChange = viewModel::onPasswordChanged,
        onCancel = {
            focusManager.clearFocus()
            viewModel.onCancelClicked()
        },
        onAccept = {
            focusManager.clearFocus()
            viewModel.onAcceptClicked(autoDownloadFromMigration)
        },
        onChangePassword = {
            focusManager.clearFocus()
            viewModel.onChangePasswordClicked()
        },
        onChangeUser = {
            focusManager.clearFocus()
        },
        dialogVisible = dialogVisible,
        dialogTitle = dialogTitle,
        dialogHint = dialogHint,
        dialogValue = dialogValue,
        onDialogValueChange = { dialogValue = it },
        onDialogCancel = { dialogVisible = false },
        onDialogAccept = {
            viewModel.checkSettingsPassword(dialogValue)
        }
    )
}


// # 4) The Compose screen

// ## `LoginUserScreen.kt`


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginUserScreen(
    uiState: LoginUserUiState,
    buttonsState: LoginUserButtonsState,
    onUserChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    onChangePassword: () -> Unit,
    onChangeUser: () -> Unit,
    dialogVisible: Boolean,
    dialogTitle: Int,
    dialogHint: Int,
    dialogValue: String,
    onDialogValueChange: (String) -> Unit,
    onDialogCancel: () -> Unit,
    onDialogAccept: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LoginTextField(
            value = uiState.user,
            onValueChange = onUserChange,
            enabled = uiState.editTextsEnabled,
            label = "User"
        )

        Spacer(modifier = Modifier.height(12.dp))

        LoginTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            enabled = uiState.editTextsEnabled,
            label = "Password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.showChangePassword) {
            Text(
                text = "Change password",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LoginUserButtons(
            buttonsState = buttonsState,
            onCancel = onCancel,
            onAccept = onAccept,
            onChangePassword = onChangePassword,
            onChangeUser = onChangeUser
        )
    }

    if (dialogVisible) {
        ComposeLoginUserCustomDialog(
            titleRes = dialogTitle,
            hintRes = dialogHint,
            value = dialogValue,
            onValueChange = onDialogValueChange,
            onCancel = onDialogCancel,
            onAccept = onDialogAccept,
            maxLength = 4,
            isPin = true
        )
    }
}


// # 5) Compose buttons matching the XML behavior

// ## `LoginUserButtons.kt`


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row

@Composable
fun LoginUserButtons(
    buttonsState: LoginUserButtonsState,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    onChangePassword: () -> Unit,
    onChangeUser: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (buttonsState.cancel.visible) {
            StyledComposeButton(
                state = buttonsState.cancel,
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
        }
        if (buttonsState.accept.visible) {
            StyledComposeButton(
                state = buttonsState.accept,
                onClick = onAccept,
                modifier = Modifier.weight(1f)
            )
        }
        if (buttonsState.changeUser.visible) {
            StyledComposeButton(
                state = buttonsState.changeUser,
                onClick = onChangeUser,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


// ## Button styling helpers

// This is the “more closely matching custom button component” part.


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight

@Composable
fun StyledComposeButton(
    state: ComposeButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.visible) return

    val colors = when (state.style) {
        ComposeButtonStyle.GreenEnabled -> ComposeButtonColors.enabledGreen()
        ComposeButtonStyle.GreyDisabled -> ComposeButtonColors.disabledGrey()
        ComposeButtonStyle.Loading -> ComposeButtonColors.loadingGreen()
        ComposeButtonStyle.Ghost -> ComposeButtonColors.ghost()
        ComposeButtonStyle.DisabledGrey -> ComposeButtonColors.disabledGrey()
    }

    val enabled = state.enabled && !state.loading

    Box(
        modifier = modifier
            .height(48.dp)
            .background(colors.background, RoundedCornerShape(12.dp))
            .alpha(if (enabled) 1f else 0.6f)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (state.loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = colors.content,
                modifier = Modifier.height(20.dp)
            )
        } else {
            Text(
                text = state.text,
                color = colors.content,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

data class ComposeButtonColors(
    val background: Color,
    val content: Color
) {
    companion object {
        @Composable
        fun enabledGreen() = ComposeButtonColors(
            background = Color(0xFF2E7D32),
            content = Color.White
        )

        @Composable
        fun disabledGrey() = ComposeButtonColors(
            background = Color(0xFFB0B0B0),
            content = Color.White
        )

        @Composable
        fun loadingGreen() = ComposeButtonColors(
            background = Color(0xFF2E7D32),
            content = Color.White
        )

        @Composable
        fun ghost() = ComposeButtonColors(
            background = Color.Transparent,
            content = Color.White
        )
    }
}


// // # 6) Compose CustomDialog equivalent
