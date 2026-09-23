package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.WelcomeUiState
import ifac.td.taxi.ui.screen.components.WelcomeUiAction
import ifac.td.taxi.ui.screen.components.WelcomeButtonsState
import ifac.td.taxi.ui.screen.components.WelcomeUiEffect
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 218-3: import android.app.Application
class WelcomeComposeViewModel(
    private val sessionUseCase: SessionUseCase,
    private val shiftUseCase: ShiftUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val ticketUseCase: TicketUseCase,
    private val loginDriverUseCase: LoginDriverUseCase,
    private val startShiftUseCase: StartShiftUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val permissionsUseCase: PermissionsUseCase,
    private val partialUseCase: PartialUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val migrationV2UseCase: MigrationV2UseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    context: Application,
) : BaseViewModel(context) {
    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<WelcomeUiEffect>(extraBufferCapacity = 64)
    val effects: SharedFlow<WelcomeUiEffect> = _effects.asSharedFlow()
    fun onScreenStarted() {
        checkClosuresPermission()
        checkUserLogin()
        haveShifts()
        checkAllPermissions()
        makeMigrationV2()
        refreshButtons()
    }
    fun onUiAction(action: WelcomeUiAction) {
        when (action) {
            WelcomeUiAction.StartPressed -> handleStartPressed()
            WelcomeUiAction.SettingsPressed -> clickSettings()
            WelcomeUiAction.ShiftsPressed -> clickShifts()
            WelcomeUiAction.StatisticsPressed -> clickStatistics()
            WelcomeUiAction.ConfigurationPressed -> clickConfiguration()
            WelcomeUiAction.PermissionsPressed -> clickPermissions()
            WelcomeUiAction.PartialsPressed -> clickParciales()
            WelcomeUiAction.ExitPressed -> emitEffect(WelcomeUiEffect.ShowExitDialog)
            WelcomeUiAction.ConfirmExit -> emitEffect(WelcomeUiEffect.ExitApp)
            WelcomeUiAction.ConfirmBatteryOptimization -> clickMenuLogin()
            is WelcomeUiAction.SubmitSecurePin -> verifyCurrentSecurePin(action.pin, action.destination)
        }
    }
    fun handleStartPressed() {
        if (!hasITopMeterPaired()) {
            checkAdditionalPermissionsAndLogin()
        } else {
            val state = _uiState.value.buttons.start
            if (state.enabled && !state.loading) {
                val last = (state.lastItopStatus)
                if (last != null) tryLoginDriver(last)
            }
        }
    }
    fun checkAdditionalPermissionsAndLogin() {
        viewModelScope.launch {
            if (!isBatteryOptimizationDisabled()) {
                emitEffect(WelcomeUiEffect.ShowBatteryOptimizationDialog)
            } else {
                clickMenuLogin()
            }
        }
    }
    fun refreshButtons() {
        val current = _uiState.value
        _uiState.value = current.copy(
            buttons = current.buttons.copy(
                start = current.buttons.start.copy(
                    visible = true,
                    text = "Inicio"
                ),
                settings = current.buttons.settings.copy(visible = true, text = "Ajustes"),
                shifts = current.buttons.shifts.copy(visible = true, text = "Turnos"),
                statistics = current.buttons.statistics.copy(visible = true, text = "Estadísticas"),
                configuration = current.buttons.configuration.copy(visible = true, text = "Configuración"),
                permissions = current.buttons.permissions.copy(visible = true, text = "Permisos"),
                partials = current.buttons.partials.copy(visible = true, text = "Parciales"),
                exit = current.buttons.exit.copy(visible = true, text = "Salir")
            )
        )
    }
    fun clickMenuLogin() {
        viewModelScope.launch {
            if (!sessionUseCase.isUserLoggedIn()) {
                navigateTo(R.id.action_welcomeFragment_to_loginUserFragment)
                return@launch
            }
            val conectionMode = bravoCentralConfigurationUseCase.getConnectionMode()
            var cntMethods = 0
            if ((conectionMode and 0x01) != 0) cntMethods++
            if ((conectionMode and 0x02) != 0) cntMethods++
            if ((conectionMode and 0x04) != 0) cntMethods++
            val startTurnMode = licensingUseCase.getStartTurnMode() ?: 1
            if (startTurnMode == 0 && cntMethods < 2) {
                if ((conectionMode and 0x01) != 0) {
                    loginDriverUseCase.loginDriver("0000", "", false)
                }
                if ((conectionMode and 0x02) != 0) {
                    W2CLocation.setLocationAllowedByCentral(false)
                    sessionUseCase.updateWithCentralAllowed(false)
                    withContext(Dispatchers.IO) { ticketUseCase.loadTickets() }
                    startShiftUseCase.startShift()
                    shiftStatusUseCase.setStatus(ifConstants.STATE_FOR_HIRE_NO_CENTRAL, false)
                    emitEffect(WelcomeUiEffect.Navigate(R.id.action_welcomeFragment_to_homeFragment))
                }
                if ((conectionMode and 0x04) != 0) {
                    loginDriverUseCase.loginDriver("0000", "", true)
                }
            } else {
                val action =
                    WelcomeFragmentDirections.actionWelcomeFragmentToLoginDriverFragment()
                        .setConnectionMode(conectionMode)
                        .setStartTurnMode(startTurnMode)
                emitEffect(WelcomeUiEffect.Navigate(action))
            }
        }
    }
    fun clickShifts() = requestSecurePinIfNeeded(WelcomeUiEffect.SecurePinDestination.SHIFTS)
    fun clickStatistics() = requestSecurePinIfNeeded(WelcomeUiEffect.SecurePinDestination.STATISTICS)
    fun requestSecurePinIfNeeded(destination: WelcomeUiEffect.SecurePinDestination) {
        viewModelScope.launch {
            val currentSecurePin = userPreferencesUseCase.getSecurePin()
            if (!currentSecurePin.isNullOrEmpty()) {
                emitEffect(WelcomeUiEffect.ShowSecurePinDialog(destination))
            } else {
                navigateToDestination(destination)
            }
        }
    }
    fun verifyCurrentSecurePin(currentPin: String, destination: WelcomeUiEffect.SecurePinDestination) {
        viewModelScope.launch {
            val actualPin = userPreferencesUseCase.getSecurePin()
            if (currentPin == actualPin) navigateToDestination(destination)
            else emitEffect(WelcomeUiEffect.Toast(R.string.pin_incorrecto))
        }
    }
    private suspend fun navigateToDestination(destination: WelcomeUiEffect.SecurePinDestination) {
        when (destination) {
            WelcomeUiEffect.SecurePinDestination.SHIFTS ->
                emitEffect(WelcomeUiEffect.Navigate(R.id.action_welcomeFragment_to_shiftsFragment))
            WelcomeUiEffect.SecurePinDestination.STATISTICS ->
                emitEffect(WelcomeUiEffect.Navigate(R.id.action_welcomeFragment_to_statisticsFragment))
        }
    }
    fun clickSettings() = emitNav(R.id.action_welcomeFragment_to_settingsFragment)
    fun clickConfiguration() = emitNav(R.id.action_welcomeFragment_to_loginUserFragment)
    fun clickPermissions() = emitNav(R.id.action_welcomeFragment_to_permissionsFragment)
    fun clickParciales() {
        viewModelScope.launch {
            val activePartial = partialUseCase.getActivePartial()
            if (activePartial != null && activePartial.hasTrips()) {
                emitEffect(WelcomeUiEffect.Navigate(R.id.action_welcomeFragment_to_partials))
            } else {
                emitEffect(WelcomeUiEffect.Navigate(R.id.action_welcomeFragment_to_closed_partial))
            }
        }
    }
    fun checkClosuresPermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            _uiState.update { it.copy(hasClosuresButton = parameters?.isClosuresButton == true) }
            recomputeButtons()
        }
    }
    fun checkUserLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            val loggedIn = sessionUseCase.isUserLoggedIn()
            _uiState.update { it.copy(isLoggedIn = loggedIn) }
            recomputeButtons()
        }
    }
    fun haveShifts() {
        viewModelScope.launch {
            val shifts = shiftUseCase.getLastShift()
            _uiState.update { it.copy(hasShifts = shifts != null) }
            recomputeButtons()
        }
    }
    fun checkAllPermissions() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasAllPermissions = permissionsUseCase.hasAllPermissions()
            _uiState.update { it.copy(hasAllPermissions = hasAllPermissions) }
            recomputeButtons()
        }
    }
    fun updateLastItopState(state: ITopMeterStatus?) {
        _uiState.update { it.copy(lastItopState = state) }
        recomputeButtons()
    }
    fun setDownloadingConfiguration(isDownloading: Boolean) {
        _uiState.update { it.copy(isDownloadingBravoConfig = isDownloading) }
        recomputeButtons()
    }
    fun setLoginSuccess(correct: Boolean) {
        _uiState.update { it.copy(correctLogin = correct) }
        recomputeButtons()
    }
    fun recomputeButtons() {
        _uiState.update { state ->
            state.copy(
                buttons = WelcomeButtonsState.from(
                    isLoggedIn = state.isLoggedIn,
                    hasShifts = state.hasShifts,
                    hasClosuresButton = state.hasClosuresButton,
                    hasAllPermissions = state.hasAllPermissions,
                    isDownloadingBravoConfig = state.isDownloadingBravoConfig,
                    isITopPaired = hasITopMeterPaired(),
                    lastItopState = state.lastItopState,
                    correctLogin = state.correctLogin,
                )
            )
        }
    }
    fun emitNav(resId: Int) = emitEffect(WelcomeUiEffect.Navigate(resId))
    fun emitEffect(effect: WelcomeUiEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }
    fun isBatteryOptimizationDisabled(): Boolean {
        val powerManager = getApplication<Application>().getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(getApplication<Application>().packageName) == true
    }
    fun makeMigrationV2() {
        viewModelScope.launch {
            val hasOldTrips = migrationV2UseCase.checkHasOldTrip()
            val hasOldShift = migrationV2UseCase.checkHasOldShift()
            if (hasOldShift || hasOldTrips) {
                val checkExistSession = sessionUseCase.getSession()
                if (checkExistSession == null) {
                    if (migrationV2UseCase.migrateSession()) {
                        emitEffect(WelcomeUiEffect.Navigate(
                            WelcomeFragmentDirections.actionWelcomeFragmentToLoginUserFragment()
                                .setAutoDownloadConfigurationFromMigration(true)
                        ))
                    }
                }
            }
        }
    }
    fun hasITopMeterPaired(): Boolean = bluetoothLocalUseCase.isCurrentBluetoothItop()
    fun tryLoginDriver(lastITopCom: ITopMeterStatus?) {
        viewModelScope.launch {
            lastITopCom?.driverCardData?.let {
                if (it.length > 18) {
                    val cardData = it.substring(12, 17)
                    loginDriverUseCase.loginDriver(cardData, cardData, false)
                }
            }
        }
    }
}
