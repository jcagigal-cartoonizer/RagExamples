package ifac.td.taxi.compose.viewmodel
import  android.app.Application
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// # Block 202-4: import android.app.Application
class LoginUserComposeViewModel(
    private val externalBridge: ExternalBridgeInterface,
    private val configuration: ConfigurationUseCase,
    private val sessionUseCase: SessionUseCase,
    private val configurationScreenPasswordUseCase: ConfigurationScreenPasswordUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val migrationV2UseCase: MigrationV2UseCase,
    private val context: Application,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUserUiState())
    val uiState: StateFlow<LoginUserUiState> = _uiState.asStateFlow()
    private val _buttonsState = MutableStateFlow(LoginUserButtonsState.initial())
    val buttonsState: StateFlow<LoginUserButtonsState> = _buttonsState.asStateFlow()
    private val _effects = MutableSharedFlow<LoginUserUiEffect>()
    val effects = _effects.asSharedFlow()
    init {
        checkSavedData()
        checkHasSettingsPassword()
    }
    fun onEvent(event: LoginUserEvent) {
        when (event) {
            LoginUserEvent.CancelClicked -> emitEffect(LoginUserUiEffect.HideKeyboard) { 
                emitEffect(LoginUserUiEffect.NavigateBack) 
            }
            LoginUserEvent.AcceptClicked -> clickAccept()
            LoginUserEvent.ChangePasswordClicked -> emitEffect(LoginUserUiEffect.HideKeyboard) {
                emitEffect(LoginUserUiEffect.NavigateToChangePassword)
            }
            LoginUserEvent.ChangeUserClicked -> {
                if (uiState.value.hasSettingsPassword) {
                    emitEffect(LoginUserUiEffect.OpenSettingsPasswordDialog)
                }
            }
            is LoginUserEvent.UserChanged -> {
                _uiState.update { it.copy(user = event.value, userError = null) }
                refreshButtons()
            }
            is LoginUserEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.value) }
                refreshButtons()
            }
            is LoginUserEvent.SettingsPasswordSubmitted -> {
                checkSettingsPassword(event.pin)
            }
            LoginUserEvent.AutoDownloadRequested -> {
                // handled in the screen with accepted login flow
            }
        }
    }
    fun clickAccept() {
        val state = uiState.value
        if (!state.userValid || !state.passwordValid) return
        _buttonsState.update { it.copy(accept = it.accept.toLoading()) }
        viewModelScope.launch {
            if (!isInternetConnectionAvailable(context)) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                }
                emitEffect(LoginUserUiEffect.NavigateBack)
                return@launch
            }
            externalBridge.setUsername(state.user.trim())
            externalBridge.setPassword(state.password)
            configuration.downloadConfiguration(
                userInfo = UserInfo(
                    user = state.user.trim(),
                    password = state.password
                ),
                fromMigration = state.fromMigration
            )
        }
    }
    fun downloadBravoConfiguration() {
        viewModelScope.launch {
            configuration.downloadBravoconfiguration()
        }
    }
    fun checkSavedData() {
        viewModelScope.launch {
            val session = sessionUseCase.getSession()
            val user = session?.username.orEmpty()
            val password = session?.password.orEmpty()
            _uiState.update {
                it.copy(
                    user = user,
                    password = password,
                    changePasswordVisible = user.isNotEmpty() && password.isNotEmpty()
                )
            }
            refreshButtons()
        }
    }
    fun checkHasSettingsPassword() {
        viewModelScope.launch {
            val hasPassword = configurationScreenPasswordUseCase.hasSettingsPassword()
            _uiState.update { it.copy(hasSettingsPassword = hasPassword) }
            refreshButtons()
        }
    }
    fun checkSettingsPassword(pin: String) {
        viewModelScope.launch {
            val userPassword = configurationScreenPasswordUseCase.encryptSettingsPassword(pin)
            val savedPassword = configurationScreenPasswordUseCase.getSettingsPassword()
            val correct = userPassword == savedPassword
            if (correct) {
                _uiState.update { it.copy(userEnabled = true, passwordEnabled = true) }
                _buttonsState.update { it.copy(changeUser = it.changeUser.disabled()) }
                emitEffect(LoginUserUiEffect.CloseSettingsPasswordDialog)
            } else {
                emitEffect(LoginUserUiEffect.ShowToast(R.string.pin_incorrecto))
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
    fun refreshButtons() {
        val state = uiState.value
        _buttonsState.update {
            it.copyFromUiState(state)
        }
    }
    fun emitEffect(effect: LoginUserUiEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }
    private inline fun emitEffect(first: LoginUserUiEffect, next: () -> Unit) {
        viewModelScope.launch {
            _effects.emit(first)
            next()
        }
    }
}
