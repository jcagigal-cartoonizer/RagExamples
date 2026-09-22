package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import android.app.Application
import android.widget.Toast
import androidx.annotation.StringRes
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
data class LoginUserUiState(
    val user: String = "",
    val password: String = "",
    val showChangePasswordText: Boolean = false,
    val userError: String? = null,
    val progress: ProgressState = ProgressState(),
    val buttons: LoginUserButtonsState = LoginUserButtonsState(),
    val editTextsEnabled: Boolean = true,
    val hasSettingsPassword: Boolean = false,
) {
    data class ProgressState(
        val visible: Boolean = false,
        val percent: Int = 0
    )
}
sealed interface LoginUserUiEvent {
    data object ScreenStarted : LoginUserUiEvent
    data class UserChanged(val value: String) : LoginUserUiEvent
    data class PasswordChanged(val value: String) : LoginUserUiEvent
    data object CancelClicked : LoginUserUiEvent
    data object AcceptClicked : LoginUserUiEvent
    data object ChangePasswordClicked : LoginUserUiEvent
    data class ConfigurationPasswordEntered(val pin: String) : LoginUserUiEvent
    data object DownloadBravoConfiguration : LoginUserUiEvent
    data object AutoDownloadMigration : LoginUserUiEvent
}
sealed interface LoginUserUiEffect {
    data object NavigateBack : LoginUserUiEffect
    data object NavigateToChangePassword : LoginUserUiEffect
    data object OpenSettingsPasswordDialog : LoginUserUiEffect
    data object HideSettingsPasswordDialog : LoginUserUiEffect
    data object ShowIncorrectPinToast : LoginUserUiEffect
    data object StartBravoService : LoginUserUiEffect
    data object DownloadBravoConfiguration : LoginUserUiEffect
}
class LoginUserComposeViewModelCompose(
    private val externalBrigde: ExternalBridgeInterface,
    private val configuration: ConfigurationUseCase,
    private val sessionUseCase: SessionUseCase,
    private val configurationScreenPasswordUseCase: ConfigurationScreenPasswordUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val migrationV2UseCase: MigrationV2UseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LoginUserUiState())
    val uiState: StateFlow<LoginUserUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<LoginUserUiEffect>()
    val effects: SharedFlow<LoginUserUiEffect> = _effects.asSharedFlow()
    fun onEvent(event: LoginUserUiEvent) {
        when (event) {
            LoginUserUiEvent.ScreenStarted -> {
                checkSavedData()
                checkHasSettingsPassword()
            }
            is LoginUserUiEvent.UserChanged -> {
                _uiState.update { it.copy(user = event.value, userError = null) }
            }
            is LoginUserUiEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.value) }
            }
            LoginUserUiEvent.CancelClicked -> {
                viewModelScope.launch { _effects.emit(LoginUserUiEffect.NavigateBack) }
            }
            LoginUserUiEvent.AcceptClicked -> {
                loginUser(
                    user = uiState.value.user.trim(),
                    password = uiState.value.password,
                    fromMigration = false
                )
            }
            LoginUserUiEvent.ChangePasswordClicked -> {
                viewModelScope.launch { _effects.emit(LoginUserUiEffect.NavigateToChangePassword) }
            }
            is LoginUserUiEvent.ConfigurationPasswordEntered -> {
                checkSettingsPassword(event.pin)
            }
            LoginUserUiEvent.DownloadBravoConfiguration -> {
                downloadBravoConfiguration()
            }
            LoginUserUiEvent.AutoDownloadMigration -> {
                migrateConfigsUser()
                migrateShiftsAndTrips()
                migratePartials()
                migratePortugal()
            }
        }
    }
    fun loginUser(user: String, password: String, fromMigration: Boolean = false) {
        viewModelScope.launch {
            if (!isInternetConnectionAvailable(getApplication())) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().getString(R.string.network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                _effects.emit(LoginUserUiEffect.NavigateBack)
                return@launch
            }
            _uiState.update {
                it.copy(
                    buttons = it.buttons.copy(accept = it.buttons.accept.loading()),
                    userError = null
                )
            }
            externalBrigde.setUsername(user)
            externalBrigde.setPassword(password)
            configuration.downloadConfiguration(
                userInfo = UserInfo(user = user, password = password),
                fromMigration = fromMigration
            )
            _effects.emit(LoginUserUiEffect.StartBravoService)
        }
    }
    fun downloadBravoConfiguration() {
        viewModelScope.launch {
            configuration.downloadBravoconfiguration()
            _effects.emit(LoginUserUiEffect.DownloadBravoConfiguration)
        }
    }
    fun checkSavedData() {
        viewModelScope.launch {
            val session = sessionUseCase.getSession()
            val user = session?.username.orEmpty()
            val pass = session?.password.orEmpty()
            _uiState.update {
                it.copy(
                    user = user,
                    password = pass,
                    showChangePasswordText = user.isNotEmpty() && pass.isNotEmpty()
                )
            }
        }
    }
    fun checkHasSettingsPassword() {
        viewModelScope.launch {
            val hasPassword = configurationScreenPasswordUseCase.hasSettingsPassword()
            _uiState.update {
                it.copy(
                    hasSettingsPassword = hasPassword,
                    editTextsEnabled = !hasPassword,
                    buttons = it.buttons.copy(
                        changeUser = it.buttons.changeUser.copy(
                            visible = hasPassword
                        )
                    )
                )
            }
            if (hasPassword) {
                _effects.emit(LoginUserUiEffect.OpenSettingsPasswordDialog)
            }
        }
    }
    fun checkSettingsPassword(pin: String) {
        viewModelScope.launch {
            val userPassword = configurationScreenPasswordUseCase.encryptSettingsPassword(pin)
            val savedPassword = configurationScreenPasswordUseCase.getSettingsPassword()
            val correct = userPassword == savedPassword
            if (correct) {
                _uiState.update {
                    it.copy(editTextsEnabled = true)
                }
                _effects.emit(LoginUserUiEffect.HideSettingsPasswordDialog)
            } else {
                _effects.emit(LoginUserUiEffect.ShowIncorrectPinToast)
            }
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
