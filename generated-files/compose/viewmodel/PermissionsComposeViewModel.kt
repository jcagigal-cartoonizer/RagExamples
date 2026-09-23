package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.PermissionsUiEffect
import ifac.td.taxi.ui.screen.components.PermissionsUiState
import ifac.td.taxi.ui.screen.components.PermissionsUiEvent
import ifac.td.taxi.ui.screen.components.PermissionsButtonsState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 281-2: import android.app.Application
class PermissionsComposeViewModel(
    application: Application,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val redSysUseCase: RedSysUseCase,
) : AndroidViewModel(application) {
    private val context: Context get() = getApplication<Application>()
    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<PermissionsUiEffect>()
    val effects: SharedFlow<PermissionsUiEffect> = _effects.asSharedFlow()
    fun onEvent(event: PermissionsUiEvent) {
        when (event) {
            PermissionsUiEvent.OnScreenStarted -> {
                checkRedSysInstalled()
                checkPhoneNumberConfigured()
                refreshAllPermissions()
            }
            is PermissionsUiEvent.HighlightPermission -> {
                _uiState.update {
                    it.copy(
                        buttons = it.buttons.highlight(event.permissionString)
                    )
                }
            }
            PermissionsUiEvent.ClickBluetooth -> handlePermissionClick(PERMISSION_BLUETOOTH)
            PermissionsUiEvent.ClickCamera -> handlePermissionClick(PERMISSION_CAMERA)
            PermissionsUiEvent.ClickPhoneCalls -> handlePermissionClick(PERMISSION_PHONE_CALL)
            PermissionsUiEvent.ClickNotifications -> handleNotificationClick()
            PermissionsUiEvent.ClickLocation -> handlePermissionClick(PERMISSION_LOCATION)
            PermissionsUiEvent.ClickBackgroundLocation -> handleBackgroundLocationClick()
            PermissionsUiEvent.ClickBattery -> handleBatteryClick()
            PermissionsUiEvent.ClickMicrophone -> handlePermissionClick(PERMISSION_MICROPHONE)
            PermissionsUiEvent.ClickOverlay -> handleOverlayClick()
            PermissionsUiEvent.ClickSystemSettings -> handleSystemSettingsClick()
            PermissionsUiEvent.ClickRedSysPassword -> handleRedSysPasswordClick()
            PermissionsUiEvent.DialogDismiss -> {
                _uiState.update { it.copy(dialogState = PermissionsDialogState.hidden()) }
            }
            is PermissionsUiEvent.DialogUsernameChanged -> {
                _uiState.update { st -> st.copy(dialogState = st.dialogState.copy(username = event.value)) }
            }
            is PermissionsUiEvent.DialogPasswordChanged -> {
                _uiState.update { st -> st.copy(dialogState = st.dialogState.copy(password = event.value)) }
            }
            PermissionsUiEvent.DialogAccept -> acceptRedSysDialog()
            PermissionsUiEvent.DialogChangePassword -> {
                viewModelScope.launch {
                    _effects.emit(PermissionsUiEffect.Navigate(R.id.action_permissionsFragment_to_changePasswordRedSysFragment3))
                }
            }
        }
    }
    fun refreshAllPermissions() {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = PermissionsButtonsState.fromSystem(context, showPhoneCalls = havePhoneNumberConfigured(), showRedSys = haveRedSysConfigured())
            _uiState.update { it.copy(buttons = updated) }
        }
    }
    fun checkRedSysInstalled() {
        viewModelScope.launch(Dispatchers.IO) {
            val configured = haveRedSysConfigured()
            _uiState.update { it.copy(showRedSysSection = configured) }
            if (configured) {
                getUsernameRedSys()
            }
        }
    }
    fun checkPhoneNumberConfigured() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(showPhoneCallsSection = havePhoneNumberConfigured()) }
        }
    }
    private suspend fun haveRedSysConfigured(): Boolean {
        val userPreferences = userPreferencesUseCase.getUserPreferences()
        return userPreferences?.pinPadSerialNumber?.isNotBlank() == true
    }
    private suspend fun havePhoneNumberConfigured(): Boolean {
        val userPreferences = userPreferencesUseCase.getUserPreferences()
        return userPreferences?.phoneCall?.isNotBlank() == true
    }
    fun isBatteryOptimizationDisabled(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
    }
    fun handlePermissionClick(permission: String) {
        viewModelScope.launch {
            _effects.emit(PermissionsUiEffect.RequestPermission(permission))
        }
    }
    fun handleNotificationClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handlePermissionClick(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModelScope.launch {
                _effects.emit(PermissionsUiEffect.ShowToast(R.string.toast_notif_activadas_por_defecto))
            }
            _uiState.update { it.copy(buttons = it.buttons.disable(NotificationButton)) }
        }
    }
    fun handleBackgroundLocationClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            handlePermissionClick(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            viewModelScope.launch {
                _effects.emit(PermissionsUiEffect.ShowToast(R.string.toast_android_antiguo_ubicacion_seg_plano))
            }
        }
    }
    fun handleBatteryClick() {
        viewModelScope.launch {
            _effects.emit(PermissionsUiEffect.RequestIgnoreBatteryOptimization)
        }
    }
    fun handleOverlayClick() {
        viewModelScope.launch {
            _effects.emit(PermissionsUiEffect.LaunchOverlayPermission)
        }
    }
    fun handleSystemSettingsClick() {
        viewModelScope.launch {
            _effects.emit(PermissionsUiEffect.RequestManageWriteSettings)
        }
    }
    fun handleRedSysPasswordClick() {
        viewModelScope.launch(Dispatchers.IO) {
            if (TemporalData.passwordRedSys.isBlank()) {
                val username = redSysUseCase.getUsernameRedSys().orEmpty()
                _uiState.update {
                    it.copy(
                        dialogState = PermissionsDialogState.visible(
                            username = it.redSysUsername ?: username,
                            password = ""
                        )
                    )
                }
            } else {
                _effects.emit(PermissionsUiEffect.ShowToast(R.string.toast_contrasena_ya_guardada))
            }
        }
    }
    fun acceptRedSysDialog() {
        viewModelScope.launch(Dispatchers.IO) {
            val dialog = uiState.value.dialogState
            val user = dialog.username.orEmpty()
            val pwd = dialog.password.orEmpty()
            if (user.isNotBlank()) {
                saveUsernameRedSys(user)
            }
            if (pwd.isNotBlank()) {
                TemporalData.passwordRedSys = pwd
            }
            if (user.isNotBlank() && pwd.isNotBlank()) {
                checkLoginRedsys(user) { result ->
                    when (result) {
                        is RedSysLoginResponse.Error -> {
                            viewModelScope.launch {
                                if (result.errorCode == RedCLSErrorCodes.STATUS_KO_FORMATO_RESP_LOGIN_PWD_CAD) {
                                    _effects.emit(PermissionsUiEffect.ShowToast(R.string.red_sys_pwd_expired))
                                    _effects.emit(PermissionsUiEffect.Navigate(R.id.action_permissionsFragment_to_changePasswordRedSysFragment3))
                                } else {
                                    _effects.emit(PermissionsUiEffect.ShowToast(R.string.incorrect_login))
                                }
                                _uiState.update { st -> st.copy(dialogState = st.dialogState.copy(show = false)) }
                            }
                        }
                        is RedSysLoginResponse.Success -> {
                            viewModelScope.launch {
                                _effects.emit(PermissionsUiEffect.ShowToast(R.string.toast_guardada_contrasena))
                                _uiState.update { st -> st.copy(dialogState = st.dialogState.copy(show = false)) }
                            }
                        }
                    }
                }
            }
        }
    }
    fun onPermissionResult(permissionString: String, granted: Boolean) {
        _uiState.update {
            it.copy(buttons = it.buttons.updatePermission(permissionString, granted))
        }
    }
    private suspend fun checkLoginRedsys(username: String, callback: (RedSysLoginResponse) -> Unit) {
        redSysUseCase.configureRedSys()
        redSysUseCase.getUsernameRedSys()?.let {
            val response = redSysUseCase.loginRedSys(username, TemporalData.passwordRedSys)
            callback(response)
        }
    }
    fun saveUsernameRedSys(user: String) {
        viewModelScope.launch(Dispatchers.IO) {
            redSysUseCase.saveUsernameRedSys(user)
        }
    }
    fun getUsernameRedSys() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(redSysUsername = redSysUseCase.getUsernameRedSys())
            }
        }
    }
}
