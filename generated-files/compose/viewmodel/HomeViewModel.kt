package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.viewmodel.model.MessageUIEnum
import androidx.compose.runtime.Composable
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
// // ## 5) Refactored `HomeViewModel`

// This version removes fragment navigation and exposes `UiState + UiEvent` with a `SharedFlow<HomeUiEffect>`.


import android.app.Application
import android.media.ToneGenerator
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.repository.room.entities.message.MessageType
import ifac.td.taxi.ui.screen.home.*
import ifac.td.taxi.viewmodel.model.MessageUIEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val endShiftUseCase: EndShiftUseCase,
    private val zoningUseCase: ZoningUseCase,
    private val messageUseCase: MessageUseCase,
    private val roofLightUseCase: RoofLightUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val sendToBravoUseCase: SendToBravoUseCase,
    private val pendingTripsUseCase: PendingTripsUseCase,
    private val bravoCentral: BravoCentralUseCase,
    private val portugalUseCase: PortugalUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val chronometerManagerUseCase: ChronometerManagerUseCase,
    private val countdownManagerUseCase: CountdownManagerUseCase,
    private val tripUseCase: TripUseCase,
    private val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<HomeUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private var pendingServicesButton: Boolean? = null
    private var lastRoofLightState: Boolean? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    dashboardVisible = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.isHomeButton == true,
                    fixedPriceVisible = !bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.fixedPricesStreetTripsURL.isNullOrBlank(),
                    manualTripAllowed = licensingUseCase.canDoManualTrips(false)
                )
            }
            getMessages()
        }
    }

    fun onResume() {
        checkForLocationPermission()
        checkPendingServiceOnForHirePermission()
        checkKeepScreenOn()
        refreshButtons()
    }

    fun refreshButtons() {
        val locationAllowed = W2CLocation.isLocationAllowedByCentral()
        _uiState.update {
            if (locationAllowed) it.copy(buttons = it.buttons.copy(location = it.buttons.location.copy(style = HomeButtonStyle.ENABLE)))
            else it.copy(buttons = it.buttons.noLocationCentralMode)
        }
    }

    fun getMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = messageUseCase.getMessageByDriverId()
            _uiState.update {
                it.copy(hasMessages = when {
                    messages.isNullOrEmpty() -> MessageUIEnum.NO_MESSAGES
                    messages.any { msg -> !msg.isRead && msg.messageType == MessageType.MESSAGE } -> MessageUIEnum.HAS_NEW_MESSAGES
                    else -> MessageUIEnum.HAS_MESSAGES_BUT_NOT_NEW
                })
            }
            applyMessageButtonState()
        }
    }

    private fun applyMessageButtonState() {
        val state = _uiState.value.hasMessages
        _uiState.update {
            it.copy(buttons = it.buttons.copy(
                messages = when (state) {
                    MessageUIEnum.NO_MESSAGES -> it.buttons.messages.copy(style = HomeButtonStyle.DISABLE, backgroundColor = HomeButtonColor.BLUE)
                    MessageUIEnum.HAS_MESSAGES_BUT_NOT_NEW -> it.buttons.messages.copy(style = HomeButtonStyle.ENABLE, backgroundColor = HomeButtonColor.BLUE)
                    MessageUIEnum.HAS_NEW_MESSAGES -> it.buttons.messages.copy(style = HomeButtonStyle.ENABLE, backgroundColor = HomeButtonColor.ORANGE)
                    null -> it.buttons.messages
                }
            ))
        }
    }

    fun checkForLocationPermission() {
        viewModelScope.launch(Dispatchers.IO) {
            val parameters = licensingUseCase.getLicensingParameters()
            _uiState.update { it.copy(showLocationButton = parameters?.isDisableLocation) }
            if (parameters?.isDisableLocation == false) {
                _uiState.update { it.copy(buttons = it.buttons.copy(location = it.buttons.location.copy(style = HomeButtonStyle.DISABLE))) }
            }
        }
    }

    fun checkPendingServiceOnForHirePermission() {
        viewModelScope.launch(Dispatchers.IO) {
            val parameters = licensingUseCase.getLicensingParameters()
            _uiState.update { it.copy(pendingServicesButton = parameters?.isVacantPendingServices) }
        }
    }

    fun checkKeepScreenOn() {
        viewModelScope.launch(Dispatchers.IO) {
            val selected = userPreferencesUseCase.getUserPreferences()?.turnOffScreenPosition ?: 0
            val keep = selected == 0 || selected == 1
            _uiEffect.emit(HomeUiEffect.KeepScreenOn(keep))
            _uiState.update { it.copy(keepScreenOn = keep) }
        }
    }

    fun onLocationButtonClicked() {
        val enabled = _uiState.value.locationEnabled
        if (enabled.first) {
            _uiState.update { it.copy(dialog = HomeDialogSpec.LocationConfirmDeactivate) }
            viewModelScope.launch { _uiEffect.emit(HomeUiEffect.OpenDialog(HomeDialogSpec.LocationConfirmDeactivate)) }
        } else {
            if (W2CLocation.isLocationAllowedByCentral()) {
                _uiState.update { it.copy(dialog = HomeDialogSpec.LocationConfirmActivate) }
                viewModelScope.launch { _uiEffect.emit(HomeUiEffect.OpenDialog(HomeDialogSpec.LocationConfirmActivate)) }
            }
        }
    }

    fun confirmDeactivateLocation() {
        viewModelScope.launch {
            if (_uiState.value.shortBreakStatus == ShortBreakStatus.IN_SHORT_BREAK_FORCED) {
                _uiEffect.emit(HomeUiEffect.ShowToast(R.string.connect_taximeter))
                return@launch
            }
            _uiState.update { it.copy(locationEnabled = false to true) }
            sendLocationDisabledEffects()
        }
    }

    fun confirmActivateLocation() {
        viewModelScope.launch {
            if (_uiState.value.locationEnabled.second) {
                W2CLocation.resetTtsUb()
                _uiState.update { it.copy(locationEnabled = true to true) }
            } else {
                _uiEffect.emit(HomeUiEffect.ShowToast(R.string.location_disabled))
            }
        }
    }

    private suspend fun sendLocationDisabledEffects() {
        sendToBravoUseCase.checkForHireAfterDelocation(true)
        _uiEffect.emit(HomeUiEffect.PlayBeep(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP))
    }

    fun navigateToZoning() {
        viewModelScope.launch {
            val defaultToMacrozone = userPreferencesUseCase.getUserPreferences()?.macroZoneQuery
            val zoneFilter = zoningUseCase.getZoneConfiguration().zoneFilter
            val filterNavigatesToZoning = false
            if (defaultToMacrozone == true && !filterNavigatesToZoning) {
                _uiEffect.emit(HomeUiEffect.Navigate(HomeRoute.Zoning))
            } else if (W2CLocation.getLastIdZone() > 0 || filterNavigatesToZoning) {
                val idMacroZone = if (W2CLocation.getLastIdMacrozone() != 0) W2CLocation.getLastIdMacrozone().toLong() else 1L
                _uiEffect.emit(HomeUiEffect.Navigate(HomeRoute.ZoningDeepLink(idMacroZone)))
            } else {
                _uiEffect.emit(HomeUiEffect.Navigate(HomeRoute.Zoning))
            }
        }
    }

    fun navigateToPendingTrips() {
        viewModelScope.launch {
            _uiEffect.emit(HomeUiEffect.Navigate(HomeRoute.PendingTrips))
        }
    }

    fun navigateToMessages() {
        viewModelScope.launch { _uiEffect.emit(HomeUiEffect.Navigate(HomeRoute.Message)) }
    }

    fun navigateToReceipts() {
        viewModelScope.launch { _uiEffect.emit(HomeUiEffect.Navigate(HomeRoute.ReceiptHistory)) }
    }

    fun navigateToCentral() {
        viewModelScope.launch { _uiEffect.emit(HomeUiEffect.Navigate(HomeRoute.ContactCentral)) }
    }

    fun navigateToDashboard() {
        viewModelScope.launch { _uiEffect.emit(HomeUiEffect.Navigate(HomeRoute.Dashboard)) }
    }

    fun navigateToFixedPrice() {
        viewModelScope.launch { _uiEffect.emit(HomeUiEffect.Navigate(HomeRoute.FixedPrice)) }
    }

    fun onRoofLightClicked() {
        val roof = _uiState.value.roofLightState?.first
        val can = _uiState.value.roofLightState?.second == true
        if (!can) return
        _uiState.update { it.copy(dialog = HomeDialogSpec.RoofLightConfirm) }
        viewModelScope.launch { _uiEffect.emit(HomeUiEffect.OpenDialog(HomeDialogSpec.RoofLightConfirm)) }
    }

    fun setRoofLightOn() { viewModelScope.launch { roofLightUseCase.setRoofLightOn() } }
    fun setRoofLightOff() { viewModelScope.launch { roofLightUseCase.setRoofLightOff() } }

    fun checkRoofLight(value: Boolean?) {
        viewModelScope.launch {
            val isDisableLuminous = licensingUseCase.getLicensingParameters()?.isDisableLuminous
            if (value == null || lastRoofLightState != value) {
                if (roofLightUseCase.isRoofLightAvailable()) {
                    _uiState.update {
                        it.copy(roofLightState = value to isDisableLuminous)
                    }
                } else {
                    _uiState.update { it.copy(roofLightState = null) }
                }
                lastRoofLightState = value
            }
        }
    }

    fun updateLocationEnabled(enabled: Boolean, canEnable: Boolean) {
        _uiState.update { it.copy(locationEnabled = enabled to canEnable) }
    }

    fun onShortBreakStatusChanged(status: ShortBreakStatus?) {
        _uiState.update { it.copy(shortBreakStatus = status) }
        checkPendingButtonEnabled()
    }

    fun updateLocatedOnStop(it: Boolean) {
        _uiState.update { it.copy(locatedOnStop = it) }
    }

    fun updateLocationType(type: String) {
        _uiState.update { it.copy(locationType = type) }
    }

    fun checkPendingButtonEnabled() {
        val zone = true // replace with actual zoneFlow state if you expose it in this VM
        if (!zone) {
            _uiState.update { it.copy(buttons = it.buttons.copy(pending = it.buttons.pending.copy(style = HomeButtonStyle.DISABLE))) }
            return
        }

        val isInShortBreak = _uiState.value.shortBreakStatus == ShortBreakStatus.IN_SHORT_BREAK ||
                _uiState.value.shortBreakStatus == ShortBreakStatus.IN_SHORT_BREAK_FORCED

        val enabled = !isInShortBreak &&
                W2CLocation.isLocationAllowedByCentral() &&
                (W2CLocation.getIsInSoonInZone() || pendingServicesButton == true)

        _uiState.update {
            it.copy(buttons = it.buttons.copy(
                pending = it.buttons.pending.copy(
                    style = if (enabled) HomeButtonStyle.ENABLE else HomeButtonStyle.DISABLE,
                    backgroundColor = if (_uiState.value.orangeBtnPending == true) HomeButtonColor.ORANGE else HomeButtonColor.BLUE
                )
            ))
        }
    }

    fun checkPendingServiceAvailability(pendingAllowed: Boolean?) {
        pendingServicesButton = pendingAllowed
        checkPendingButtonEnabled()
    }

    fun updatePendingOrange(orange: Boolean?) {
        _uiState.update { it.copy(orangeBtnPending = orange) }
        checkPendingButtonEnabled()
    }

    fun checkZoningButton(locationEnabled: Boolean) {
        val enabled = (bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.locationRequestWhenDispatchOff == true) || locationEnabled
        _uiState.update { it.copy(zoningEnabled = enabled) }
    }

    fun setDialog(dialog: HomeDialogSpec?) {
        _uiState.update { it.copy(dialog = dialog) }
    }

    fun clearDialog() {
        _uiState.update { it.copy(dialog = null) }
    }

    fun handlePendingTripsPress(hasTrips: Boolean) {
        viewModelScope.launch {
            if (hasTrips) {
                _uiEffect.emit(HomeUiEffect.Navigate(HomeRoute.PendingTrips))
            } else {
                _uiEffect.emit(HomeUiEffect.ShowToast(R.string.toast_no_hay_pendientes))
            }
        }
    }

    fun canGoToHiredManual(taximeterConnected: Boolean): Boolean =
        licensingUseCase.canDoManualTrips(taximeterConnected)

    fun openManualTripDialog() {
        _uiState.update { it.copy(dialog = HomeDialogSpec.ManualTripConfirm) }
    }

    fun confirmManualTrip() {
        viewModelScope.launch {
            shiftStatusUseCase.changeStateHiredManual()
        }
    }

    fun logoff() {
        viewModelScope.launch(Dispatchers.IO) {
            endShiftUseCase.endShift()
            bravoCentral.logoff()
            shiftStatusUseCase.setStatus(ifConstants.STATE_DISCONNECTED, false)
            _uiEffect.emit(HomeUiEffect.Navigate(HomeRoute.Welcome))
        }
    }
}


