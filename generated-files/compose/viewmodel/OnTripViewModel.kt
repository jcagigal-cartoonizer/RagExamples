package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.OnTripButtonsState
import ifac.td.taxi.ui.screen.components.OnTripUiEffect
import ifac.td.taxi.ui.screen.components.OnTripUiState
import ifac.td.taxi.ui.screen.components.OnTripButtonState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 121-3: import android.app.Application
class OnTripComposeViewModel(
    private val messageUseCase: MessageUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val dispatchNotificationUseCase: DispatchNotificationUseCase,
    private val navigatorUseCase: NavigatorUseCase,
    private val locationUseCase: LocationUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val roofLightUseCase: RoofLightUseCase,
    private val zoningUseCase: ZoningUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val TAG = "OnTripViewModel"
    private val _uiState = MutableStateFlow(OnTripUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<OnTripUiEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()
    private var lastRoofLightState: Boolean? = null
    init {
        viewModelScope.launch(Dispatchers.IO) {
            val shiftStatus = shiftStatusUseCase.getStatus()
            val isManual = shiftStatus?.isManual == true && shiftStatus.currentStatus != ifConstants.STATE_DISPATCHED
            if (isManual) _effects.tryEmit(OnTripUiEffect.ShowTopBarNext)
        }
        viewModelScope.launch {
            val url = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.fixedPricesStreetTripsURL
            _uiState.update {
                it.copy(
                    buttons = it.buttons.copy(
                        fixedPrice = if (!url.isNullOrBlank()) visibleEnabled(""),
                        receipts = if (!url.isNullOrBlank()) hidden()
                    )
                )
            }
        }
    }
    fun onScreenStarted(locationAllowedByCentral: Boolean) {
        if (locationAllowedByCentral) {
            checkNoClientButton()
        }
        getMessages()
    }
    fun getMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = messageUseCase.getMessageByDriverId()
            val messageState = when {
                messages.isNullOrEmpty() -> MessageUIEnum.NO_MESSAGES
                messages.any { !it.isUrgent && !it.isRead } -> MessageUIEnum.HAS_NEW_MESSAGES
                else -> MessageUIEnum.HAS_MESSAGES_BUT_NOT_NEW
            }
            _uiState.update {
                it.copy(buttons = it.buttons.copy(messages = buttonForMessages(messageState)))
            }
        }
    }
    fun checkNoClientButton() {
        viewModelScope.launch(Dispatchers.IO) {
            val parameters = licensingUseCase.getLicensingParameters()
            val enabled = parameters?.isNoClientHired == true
            _uiState.update {
                it.copy(buttons = it.buttons.copy(client = it.buttons.client.copy(visible = true, enabled = enabled)))
            }
        }
    }
    fun checkRoofLight(value: Boolean?) {
        viewModelScope.launch {
            val isDisableLuminous = licensingUseCase.getLicensingParameters()?.isDisableLuminous ?: false
            if (value == null || lastRoofLightState != value) {
                if (roofLightUseCase.isRoofLightAvailable()) {
                    _uiState.update {
                        it.copy(buttons = it.buttons.copy(roofLight = roofLightButton(value, isDisableLuminous)))
                    }
                } else {
                    _uiState.update {
                        it.copy(buttons = it.buttons.copy(roofLight = hidden()))
                    }
                }
                lastRoofLightState = value
            }
        }
    }
    fun onFixedPriceClicked() = emitEffect(OnTripUiEffect.NavigateToEstimateFixedPrice)
    fun onReceiptsClicked() = emitEffect(OnTripUiEffect.NavigateToReceiptHistory)
    fun onMessagesClicked() = emitEffect(OnTripUiEffect.NavigateToMessage)
    fun onCentralClicked() = emitEffect(OnTripUiEffect.NavigateToContactCentral)
    fun onDispatchInfoClicked() = emitEffect(OnTripUiEffect.NavigateToDispatchInfo)
    fun onNavigateClicked() {
        viewModelScope.launch {
            navigatorUseCase.openNavigatorApp()?.let { intent ->
                emitEffect(OnTripUiEffect.OpenExternalIntent(intent.action ?: ""))
            }
        }
    }
    fun onZoningClicked(locatedOnHired: Boolean) {
        viewModelScope.launch {
            licensingUseCase.getLicensingParameters()?.locationInHired?.let {
                if (it == LocationInHiredEnum.WITHOUT_ZONE_OPTION.value) {
                    if (!locatedOnHired) {
                        showDialogWithoutZone()
                    } else {
                        sendPositionStatic()
                    }
                } else {
                    openZoningScreen()
                }
            }
        }
    }
    fun onClientClicked(dispatchId: Long?) {
        if (hasITopTaximeterConnected()) {
            emitEffect(OnTripUiEffect.ShowToastItopRestricted)
            return
        }
        emitEffect(OnTripUiEffect.ShowConfirmNoClientDialog)
    }
    fun onNotificationClicked(hasAtDoor: Boolean, hasInCab: Boolean) {
        if (hasITopTaximeterConnected()) {
            emitEffect(OnTripUiEffect.ShowToastItopRestricted)
            return
        }
        emitEffect(OnTripUiEffect.ShowSelectNotificationDialog)
    }
    fun onRoofLightClicked(lightState: Boolean?) {
        viewModelScope.launch {
            if (lightState == true) setRoofLightOff() else setRoofLightOn()
        }
    }
    fun sendAtTheDoorNotification(dispatch: InfoDispatchModel?) {
        viewModelScope.launch { dispatch?.let { dispatchNotificationUseCase.atTheDoor(it.id) } }
    }
    fun sendInCabNotification(dispatch: InfoDispatchModel?) {
        viewModelScope.launch { dispatch?.let { dispatchNotificationUseCase.inCab(it.id) } }
    }
    fun sendNoClientNotification(infoDispatch: Long?) {
        viewModelScope.launch { infoDispatch?.let { dispatchNotificationUseCase.noClient(it) } }
    }
    fun navigateToReceiptHistory() = emitEffect(OnTripUiEffect.NavigateToReceiptHistory)
    fun navigateToMessage() = emitEffect(OnTripUiEffect.NavigateToMessage)
    fun navigateToContactCentral() = emitEffect(OnTripUiEffect.NavigateToContactCentral)
    fun canGoToHiredManual(taximeterConnected: Boolean): Boolean =
        licensingUseCase.canDoManualTrips(taximeterConnected)
    fun goToHiredManual(id: Long) {
        viewModelScope.launch { shiftStatusUseCase.goToHiredManualFromDispatch(tripId = id) }
    }
    fun setRoofLightOff() { viewModelScope.launch { roofLightUseCase.setRoofLightOff() } }
    fun setRoofLightOn() { viewModelScope.launch { roofLightUseCase.setRoofLightOn() } }
    fun isHired(value: Int?): Boolean = value?.let { shiftStatusUseCase.isHired(it) } == true
    fun changeStateToPaymentManual() {
        viewModelScope.launch {
            shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_PAYMENT)?.let {
                shiftStatusUseCase.setStatus(it, true)
            }
        }
    }
    fun hasITopTaximeterConnected(): Boolean = bluetoothLocalUseCase.isCurrentBluetoothItop()
    fun updateButtonsForTrip(
        locationAllowedByCentral: Boolean,
        hiredZoneExists: Boolean,
        hasTaximeterConnection: Boolean,
        currentStatus: Int?,
        isManual: Boolean,
        tripFromDispatch: Boolean?,
        dispatch: InfoDispatchModel?,
        roofLight: Boolean?,
        canDoManualTrips: Boolean
    ) {
        val base = if (locationAllowedByCentral) enabledTripButtons() else buttonsWithoutCentral()
        val updated = base.copy(
            zoning = if (hiredZoneExists) zoningButtonHired() else if (locationAllowedByCentral) zoningButtonDefault() else hidden(),
            navigate = if (isHired(currentStatus)) visibleEnabled("") else hidden(),
            dispatchInfo = if (tripFromDispatch == true) visibleEnabled("") else if (locationAllowedByCentral) disabledVisible("") else hidden(),
            notifications = notificationButton(dispatch, hasTaximeterConnection, isManual),
            client = clientButton(hasTaximeterConnection),
            roofLight = roofLightButton(roofLight, licensingUseCase.getLicensingParameters()?.isDisableLuminous == true, canDoManualTrips)
        )
        _uiState.update { it.copy(buttons = updated, topBarRightVisible = shouldShowTopRight(hasTaximeterConnection, currentStatus, isManual, canDoManualTrips)) }
    }
    fun shouldShowTopRight(
        taximeterConnection: Boolean,
        currentStatus: Int?,
        isManual: Boolean,
        canDoManualTrips: Boolean
    ): Boolean {
        return !taximeterConnection && isHired(currentStatus) ||
            isManual ||
            (currentStatus == ifConstants.STATE_DISPATCHED && canDoManualTrips)
    }
    fun buttonForMessages(state: MessageUIEnum) = when (state) {
        MessageUIEnum.NO_MESSAGES -> disabledVisible("")
        MessageUIEnum.HAS_MESSAGES_BUT_NOT_NEW -> visibleEnabled("", ButtonBackground.Blue)
        MessageUIEnum.HAS_NEW_MESSAGES -> visibleEnabled("", ButtonBackground.Orange)
    }
    fun notificationButton(dispatch: InfoDispatchModel?, hasItop: Boolean, isManual: Boolean): OnTripButtonState {
        if (hasItop && !isManual) return disabledVisible("")
        val hasAtDoor = dispatch?.isAtDoorNotificationEnabled() == true && dispatch.isAtDoorNotificationSent == false
        val hasInCab = dispatch?.riderInCab == true && dispatch.isRiderInCabNotificationSent == false
        return when {
            hasAtDoor && hasInCab -> visibleEnabled("")
            !hasAtDoor && hasInCab -> visibleEnabled("", type = ButtonTypeUi.RiderInCab)
            hasAtDoor && !hasInCab -> visibleEnabled("", type = ButtonTypeUi.AtDoor)
            else -> disabledVisible("")
        }
    }
    fun clientButton(hasItop: Boolean): OnTripButtonState =
        if (hasItop) disabledVisible("") else visibleEnabled("")
    fun roofLightButton(value: Boolean?, disableLuminous: Boolean, canDoManualTrips: Boolean = true): OnTripButtonState {
        if (!disableLuminous || !canDoManualTrips) return hidden()
        return when (value) {
            true -> visibleEnabled("", ButtonBackground.Green)
            false -> visibleEnabled("", ButtonBackground.Red)
            else -> disabledVisible("")
        }
    }
    fun zoningButtonHired() = visibleEnabled("", ButtonBackground.Red)
    fun zoningButtonDefault() = visibleEnabled("", ButtonBackground.Green)
    fun buttonsWithoutCentral() = OnTripButtonsState(
        fixedPrice = visibleEnabled(""),
        receipts = visibleEnabled(""),
        messages = visibleEnabled(""),
    )
    fun enabledTripButtons() = OnTripButtonsState(
        fixedPrice = visibleEnabled(""),
        notifications = disabledVisible(""),
        zoning = visibleEnabled(""),
        navigate = visibleEnabled(""),
        dispatchInfo = disabledVisible(""),
        receipts = visibleEnabled(""),
        messages = visibleEnabled(""),
        central = visibleEnabled(""),
        client = visibleEnabled(""),
        roofLight = disabledVisible("")
    )
    fun visibleEnabled(
        text: String,
        background: ButtonBackground = ButtonBackground.Default,
        type: ButtonTypeUi = ButtonTypeUi.Default
    ) = OnTripButtonState(true, true, text, null, background, ButtonStyle.Enabled, type)
    fun disabledVisible(text: String) =
        OnTripButtonState(true, false, text, null, ButtonBackground.Default, ButtonStyle.Disabled, ButtonTypeUi.Default)
    fun hidden() = OnTripButtonState(false, false, "", null, ButtonBackground.Default, ButtonStyle.Disabled, ButtonTypeUi.Default)
    fun emitEffect(effect: OnTripUiEffect) {
        _effects.tryEmit(effect)
    }
    private suspend fun showDialogWithoutZone() {
        _effects.emit(OnTripUiEffect.ShowSelectNotificationDialog)
    }
    fun sendPositionStatic() {
        W2CLocation.sendEvent('z', null)
    }
    private suspend fun openZoningScreen() {
        val defaultToMacrozone = userPreferencesUseCase.getUserPreferences()?.macroZoneQuery
        val zoneFilter = zoningUseCase.getZoneConfiguration().zoneFilter
        val filterNavigatesToZoning = zoneFilter == FilterOptions.NEARNESS ||
                zoneFilter == FilterOptions.ID_NO_HIERARCHY ||
                zoneFilter == FilterOptions.HOT_ZONES ||
                zoneFilter == FilterOptions.FAVOURITES
        if (defaultToMacrozone == true && !filterNavigatesToZoning) {
            _effects.emit(OnTripUiEffect.NavigateToMacroZoning)
        } else {
            val idMacroZone = if (W2CLocation.getLastIdMacrozone() != 0) W2CLocation.getLastIdMacrozone() else 1
            val deepLink = "android-app://ifac.td.taxi/zoningFragment/$idMacroZone"
            _effects.emit(OnTripUiEffect.NavigateToDeepLink(deepLink))
        }
    }
}
