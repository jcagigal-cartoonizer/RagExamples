package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.UserPreferencesUiState
import ifac.td.taxi.ui.screen.components.UserPreferencesUiEvent
import ifac.td.taxi.ui.screen.components.UserPreferencesUiEffect
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 339-2: import android.app.Application
class UserPreferencesComposeViewModel(
    private val externalBridgeInterface: ExternalBridgeInterface,
    private val shiftUseCase: ShiftUseCase,
    private val sessionUseCase: SessionUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val sendLogsUseCase: SendLogsUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val portugalUseCase: PortugalUseCase,
    private val ingenicoUseCase: IngenicoUseCase,
    private val app: Application,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserPreferencesUiState())
    val uiState: StateFlow<UserPreferencesUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<UserPreferencesUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<UserPreferencesUiEffect> = _uiEffect.asSharedFlow()
    init {
        load()
    }
    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = userPreferencesUseCase.getUserPreferences() ?: UserPreferences()
            val isLoggedIn = sessionUseCase.isUserLoggedIn()
            val hasShifts = !(shiftUseCase.getAll().isNullOrEmpty())
            val isSkyGlassAvailable = Taximeter.getInstance().isSkyGlass && externalBridgeInterface.isSkyGlassWithTimeControlAvailable
            val lightOffVisible = computeLightOffVisibility()
            val fiscal = licensingUseCase.getLicensingParameters()?.isFiscalService ?: false
            val ingenico = ingenicoUseCase.hasIngenicoInstalled()
            val driverTurnMode = licensingUseCase.getStartTurnMode()
            _uiState.update {
                it.copy(
                    isLoggedIn = isLoggedIn,
                    hasShifts = hasShifts,
                    taximeterSkyGlass = isSkyGlassAvailable,
                    lightOffVisible = lightOffVisible,
                    fiscalLicensing = fiscal,
                    ingenicoInstalled = ingenico,
                    driverTurnMode = driverTurnMode,
                    model = prefs
                ).deriveVisibility()
            }
        }
    }
    fun onAction(event: UserPreferencesUiEvent) {
        when (event) {
            UserPreferencesUiEvent.ClickCancel -> emitEffect(UserPreferencesUiEffect.NavigateBack)
            UserPreferencesUiEvent.ClickAccept -> saveAndClose()
            UserPreferencesUiEvent.ClickSecurePin -> navigateToSecurePin()
            UserPreferencesUiEvent.ClickFiscalPortugal -> requestPortugalPin()
            UserPreferencesUiEvent.ClickSendLogs -> emitEffect(
                UserPreferencesUiEffect.OpenDialog(
                    UserPreferencesDialogModel(
                        title = app.getString(R.string.dialog_add_comment),
                        buttons = listOf(UserPreferencesDialogButton.Cancel, UserPreferencesDialogButton.Accept),
                        isPin = false,
                        showTextField = true
                    )
                )
            )
            UserPreferencesUiEvent.ClickDeleteShifts -> emitEffect(
                UserPreferencesUiEffect.OpenDatePickerDialog
            )
            is UserPreferencesUiEvent.SetBeepNoBt -> updatePrefs { copy(beepNoBt = event.value) }
            is UserPreferencesUiEvent.SetVibrateNoBt -> updatePrefs { copy(vibrateNoBT = event.value) }
            is UserPreferencesUiEvent.SetUseExternalApp -> updatePrefs { copy(useExternalApp = event.value) }
            is UserPreferencesUiEvent.SetUseCustomPaymentTimerWithExternalApp -> updatePrefs { copy(useCustomPaymentTimerWithExternalApp = event.value) }
            is UserPreferencesUiEvent.SetPrintRedSysAlways -> updatePrefs { copy(printRedSysCommerceTicketAlways = event.value) }
            is UserPreferencesUiEvent.SetUseTimeControl -> updatePrefs { copy(useTimeControl = event.value) }
            is UserPreferencesUiEvent.SetShowConfAccept -> updatePrefs { copy(showConfAcceptDispatch = event.value) }
            is UserPreferencesUiEvent.SetShowConfReject -> updatePrefs { copy(showConfRejectDispatch = event.value) }
            is UserPreferencesUiEvent.SetUseFloatingWindow -> updatePrefs { copy(useFloatingWindow = event.value) }
            is UserPreferencesUiEvent.SetLightOffOnDispatched -> updatePrefs { copy(lightOffOnDispatched = event.value) }
            is UserPreferencesUiEvent.SetClosePendingTripsIfEmpty -> updatePrefs { copy(closePendingTripsIfEmpty = event.value) }
            UserPreferencesUiEvent.RequestPhoneCall -> emitEffect(UserPreferencesUiEffect.RequestPhonePermission)
            UserPreferencesUiEvent.RequestBluetooth -> emitEffect(UserPreferencesUiEffect.RequestBluetoothPermission)
            UserPreferencesUiEvent.RequestOverlay -> emitEffect(UserPreferencesUiEffect.RequestOverlayPermission)
            UserPreferencesUiEvent.ToggleSound -> toggleSection { copy(soundExpanded = !soundExpanded) }
            UserPreferencesUiEvent.ToggleInvoices -> toggleSection { copy(invoicesExpanded = !invoicesExpanded) }
            UserPreferencesUiEvent.TogglePinPad -> toggleSection { copy(pinPadExpanded = !pinPadExpanded) }
            UserPreferencesUiEvent.ToggleShifts -> toggleSection { copy(shiftsExpanded = !shiftsExpanded) }
            UserPreferencesUiEvent.ToggleSystem -> toggleSection { copy(systemExpanded = !systemExpanded) }
            UserPreferencesUiEvent.ToggleLocation -> toggleSection { copy(locationExpanded = !locationExpanded) }
        }
    }
    fun onDialogAction(action: UserPreferencesUiEffect.DialogAction) {
        when (action) {
            is UserPreferencesUiEffect.DialogAction.Accept -> {
                val dialog = uiState.value.activeDialog ?: return
                if (dialog.title == app.getString(R.string.dialog_add_comment)) {
                    sendLogsUseCase.prepareLogSending(action.text)
                }
                clearDialog()
            }
            UserPreferencesUiEffect.DialogAction.Cancel -> clearDialog()
        }
    }
    fun onRingtoneOrNotificationPicked(data: Intent?, lastSelectedType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                } ?: return@launch
            val title = RingtoneManager.getRingtone(app, uri)?.getTitle(app).orEmpty()
            _uiState.update {
                val model = it.model
                if (lastSelectedType == RingtoneManager.TYPE_RINGTONE) {
                    model.copy(ringtoneUri = uri, ringtoneTitle = title)
                } else {
                    model.copy(notificationUri = uri, notificationTitle = title)
                }
                it.copy(model = model).deriveVisibility()
            }
        }
    }
    fun saveAndClose() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = uiState.value.model
            userPreferencesUseCase.insertUserPreferences(current)
            emitEffect(UserPreferencesUiEffect.KeepScreenOn)
            emitEffect(UserPreferencesUiEffect.NavigateBack)
        }
    }
    fun navigateToSecurePin() {
        viewModelScope.launch(Dispatchers.IO) {
            val actualPin = userPreferencesUseCase.getSecurePin()
            if (actualPin.isNullOrEmpty()) {
                emitEffect(UserPreferencesUiEffect.NavigateToSecurePin)
            } else {
                emitEffect(
                    UserPreferencesUiEffect.OpenDialog(
                        UserPreferencesDialogModel(
                            title = app.getString(R.string.user_preferences_btn_secure_pin),
                            hint = app.getString(R.string.pin_actual_hint),
                            buttons = listOf(UserPreferencesDialogButton.Cancel, UserPreferencesDialogButton.Accept),
                            isPin = true,
                            showTextField = true
                        )
                    )
                )
            }
        }
    }
    fun requestPortugalPin() {
        emitEffect(
            UserPreferencesUiEffect.OpenDialog(
                UserPreferencesDialogModel(
                    title = app.getString(R.string.btn_portugal_password),
                    hint = "PIN Portugal",
                    buttons = listOf(UserPreferencesDialogButton.Cancel, UserPreferencesDialogButton.Accept),
                    isPin = true,
                    showTextField = true
                )
            )
        )
    }
    fun onPortugalPinEntered(pin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val portugalCode = BuildConfig.portugal_p
            val portugalPassword = portugalUseCase.getPortugalData()
            if (pin == portugalCode || (portugalPassword != null && portugalPassword.portugalPassword == portugalPassword.encryptPortugalPassword(pin))) {
                emitEffect(UserPreferencesUiEffect.NavigateToPortugalSettings)
            }
        }
    }
    fun onCurrentSecurePinEntered(currentPin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val actualPin = userPreferencesUseCase.getSecurePin()
            if (currentPin == actualPin) emitEffect(UserPreferencesUiEffect.NavigateToSecurePin)
            else emitEffect(UserPreferencesUiEffect.ShowToast(R.string.pin_incorrecto))
        }
    }
    fun updatePrefs(block: UserPreferences.() -> UserPreferences) {
        _uiState.update { state ->
            val updated = state.model.block()
            state.copy(model = updated).deriveVisibility()
        }
    }
    fun toggleSection(block: UserPreferencesUiState.() -> UserPreferencesUiState) {
        _uiState.update(block)
    }
    fun clearDialog() {
        _uiState.update { it.copy(activeDialog = null) }
        emitEffect(UserPreferencesUiEffect.CloseDialog)
    }
    fun computeLightOffVisibility(): Boolean {
        val txm = Taximeter.getInstance()
        return if (txm.isConnected && txm.isRoofLightAvailable) {
            if (txm.internalMeterType == TaximeterConstants.METER_SOFTWARE ||
                txm.internalMeterType == TaximeterConstants.METER_DUAL_SOFTWARE &&
                Taximeter.getExternalInterface().isUseTaxitronicPrinter
            ) {
                true
            } else {
                licensingUseCase.getLicensingParameters()?.isDisableLuminous ?: false
            }
        } else false
    }
    fun emitEffect(effect: UserPreferencesUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}
