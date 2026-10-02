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
// # Block 122-2: import android.app.Application
class HomeComposeViewModel(
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
    app: Application,
) : AndroidViewModel(app) {
    private val _runtime = MutableStateFlow(HomeRuntimeState())
    val runtime = _runtime.asStateFlow()
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<HomeUiEffect>(extraBufferCapacity = 64)
    val effects = _effects.asSharedFlow()
    init {
        viewModelScope.launch {
            _uiState.update { it.copy(buttons = HomeButtonsState.initial()) }
            _runtime.update {
                it.copy(
                    showDashboardButton = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.isHomeButton == true,
                    showEstimateFixedPrice = !bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.fixedPricesStreetTripsURL.isNullOrBlank(),
                )
            }
        }
    }
    fun onScreenStarted() {
        getMessages()
        checkForLocationPermission()
        checkPendingServiceOnForHirePermission()
        checkKeepScreenOn()
    }
    fun reduceButtons() {
        val r = _runtime.value
        val b = HomeButtonsState(
            location = if (!r.locationPermissionAllowed) {
                HomeComposeButtonState.Disabled(textRes = if (r.locationEnabled) R.string.btn_location_off else R.string.btn_location_on)
            } else if (r.locationEnabled) {
                HomeComposeButtonState.Enabled(textRes = R.string.btn_location_off, background = ButtonColor.Green)
            } else {
                HomeComposeButtonState.Enabled(textRes = R.string.btn_location_on, background = ButtonColor.Red)
            },
            zoning = HomeComposeButtonState(
                enabled = r.locationEnabled || canRequestDispatchOff(),
                visible = true,
                textRes = null,
                background = ButtonColor.Blue
            ),
            pending = pendingButtonState(r),
            locateStand = locateStandState(r),
            central = HomeComposeButtonState.Enabled(background = if (r.hasTaximeterConnection) ButtonColor.Blue else ButtonColor.Red),
            receipts = HomeComposeButtonState.Enabled(),
            messages = when (r.hasMessages) {
                MessageUIEnum.HAS_NEW_MESSAGES -> HomeComposeButtonState.Enabled(background = ButtonColor.Orange)
                MessageUIEnum.HAS_MESSAGES_BUT_NOT_NEW -> HomeComposeButtonState.Enabled(background = ButtonColor.Blue)
                else -> HomeComposeButtonState.Disabled()
            },
            roofLight = roofLightState(r),
            dashboard = if (r.showDashboardButton) HomeComposeButtonState.Enabled() else HomeComposeButtonState.Hidden(),
            fixedPrice = if (r.showEstimateFixedPrice) HomeComposeButtonState.Enabled() else HomeComposeButtonState.Hidden(),
        )
        _uiState.update { it.copy(buttons = b) }
    }
    fun pendingButtonState(r: HomeRuntimeState): HomeComposeButtonState {
        val isInShortBreak = r.shortBreakStatus == ShortBreakStatus.IN_SHORT_BREAK ||
                r.shortBreakStatus == ShortBreakStatus.IN_SHORT_BREAK_FORCED
        val enabled = r.zone.isNullOrEmpty().not() &&
                !isInShortBreak &&
                W2CLocation.isLocationAllowedByCentral() &&
                (W2CLocation.getIsInSoonInZone() || r.pendingServicesButton == true)
        return if (enabled) HomeComposeButtonState.Enabled(background = if (r.orangePending) ButtonColor.Orange else ButtonColor.Blue) else HomeComposeButtonState.Disabled()
    }
    fun locateStandState(r: HomeRuntimeState): HomeComposeButtonState {
        if (!W2CLocation.isLocationAllowedByCentral()) return HomeComposeButtonState.Disabled(textRes = R.string.btn_locate_stop)
        return when (r.locationType) {
            "Z" -> if (r.locatedOnStop || W2CLocation.locationStopAllowed())
                HomeComposeButtonState.Enabled(textRes = R.string.btn_locate_stop, background = ButtonColor.Red)
            else HomeComposeButtonState.Disabled(textRes = R.string.btn_locate_stop)
            "P" -> if (!checkStartAutomaticLocation()) HomeComposeButtonState.Enabled(textRes = R.string.btn_locate_stop_exit, background = ButtonColor.Green)
            else HomeComposeButtonState.Disabled(textRes = R.string.btn_locate_stop_exit)
            else -> HomeComposeButtonState.Disabled(textRes = R.string.btn_locate_stop)
        }
    }
    fun roofLightState(r: HomeRuntimeState): HomeComposeButtonState {
        if (r.roofLightDisableLuminous != true) return HomeComposeButtonState.Disabled()
        return when (r.roofLightState) {
            true -> HomeComposeButtonState.Enabled(background = ButtonColor.Green)
            false -> HomeComposeButtonState.Enabled(background = ButtonColor.Red)
            else -> HomeComposeButtonState.Disabled()
        }
    }
    fun canRequestDispatchOff(): Boolean =
        bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.locationRequestWhenDispatchOff == true
    fun onMessagesClicked() = viewModelScope.launch { _effects.emit(HomeUiEffect.OpenMessages) }
    fun onReceiptsClicked() = viewModelScope.launch { _effects.emit(HomeUiEffect.OpenReceiptHistory) }
    fun onCentralClicked() = viewModelScope.launch { _effects.emit(HomeUiEffect.OpenContactCentral) }
    fun onDashboardClicked() = viewModelScope.launch { _effects.emit(HomeUiEffect.OpenDashboard) }
    fun onFixedPriceClicked() = viewModelScope.launch { _effects.emit(HomeUiEffect.OpenFixedPrice) }
    fun onPendingClicked() = viewModelScope.launch {
        _effects.emit(HomeUiEffect.OpenPendingTrips)
    }
    fun onZoningClicked() = viewModelScope.launch { navigateToZoning() }
    fun onLocationClicked() = viewModelScope.launch {
        val enabled = _runtime.value.locationEnabled
        if (enabled) {
            _uiState.update { it.copy(dialog = HomeDialogState(HomeDialogType.DEACTIVATE_LOCATION, "Confirm deactivate location")) }
        } else {
            if (W2CLocation.isLocationAllowedByCentral()) {
                _uiState.update { it.copy(dialog = HomeDialogState(HomeDialogType.ACTIVATE_LOCATION, "Confirm activate location")) }
            }
        }
    }
    fun onRoofLightClicked() = viewModelScope.launch {
        val r = _runtime.value
        if (r.roofLightState == true) setRoofLightOff() else setRoofLightOn()
    }
    fun onLocateStandClicked() = viewModelScope.launch {
        if (W2CLocation.getLocationTypeSent() == 'P') {
            _uiState.update { it.copy(dialog = HomeDialogState(HomeDialogType.LOCATE_STOP_EXIT, "Exit stand?")) }
        } else {
            W2CLocation.sendLocateStop()
            W2CLocation.setTryingToRelocateInStand(false)
        }
    }
    fun onDialogDismissed() {
        _uiState.update { it.copy(dialog = null) }
    }
    fun onDialogAccepted(type: HomeDialogType) = viewModelScope.launch {
        when (type) {
            HomeDialogType.ACTIVATE_LOCATION -> {
                _effects.emit(HomeUiEffect.Beep(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP))
                _effects.emit(HomeUiEffect.ShowToast(R.string.location_disabled))
            }
            HomeDialogType.DEACTIVATE_LOCATION -> {
                _effects.emit(HomeUiEffect.Beep(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP))
            }
            HomeDialogType.EXIT_SHIFT -> logoff()
            HomeDialogType.MANUAL_TRIP -> _effects.emit(HomeUiEffect.NavigateToRes(R.id.action_homeFragment_to_onTripFragment))
            HomeDialogType.LOCATE_STOP_EXIT -> {
                W2CLocation.sendLocateZone()
            }
        }
        onDialogDismissed()
    }
    fun getMessages() = viewModelScope.launch(Dispatchers.IO) {
        val messages = messageUseCase.getMessageByDriverId()
        val ui = if (messages.isNullOrEmpty()) {
            MessageUIEnum.NO_MESSAGES
        } else if (messages.any { !it.isRead && it.messageType == MessageType.MESSAGE }) {
            MessageUIEnum.HAS_NEW_MESSAGES
        } else {
            MessageUIEnum.HAS_MESSAGES_BUT_NOT_NEW
        }
        _runtime.update { it.copy(hasMessages = ui) }
        reduceButtons()
    }
    fun checkForLocationPermission() = viewModelScope.launch {
        _runtime.update { it.copy(locationPermissionAllowed = licensingUseCase.getLicensingParameters()?.isDisableLocation == true) }
        reduceButtons()
    }
    fun checkPendingServiceOnForHirePermission() = viewModelScope.launch {
        _runtime.update { it.copy(pendingServicesButton = licensingUseCase.getLicensingParameters()?.isVacantPendingServices) }
        reduceButtons()
    }
    fun checkKeepScreenOn() = viewModelScope.launch {
        val selected = userPreferencesUseCase.getUserPreferences()?.turnOffScreenPosition ?: 0
        _uiState.update { it.copy(isKeepScreenOn = selected == 0 || selected == 1) }
    }
    fun onSharedLocationEnabled(enabled: Boolean, canEnable: Boolean) {
        _runtime.update { it.copy(locationEnabled = enabled, canEnableLocation = canEnable) }
        reduceButtons()
    }
    fun onSharedShortBreakStatus(status: ShortBreakStatus?) {
        _runtime.update { it.copy(shortBreakStatus = status) }
        reduceButtons()
    }
    fun onSharedZone(zone: String?) {
        _runtime.update { it.copy(zone = zone) }
        reduceButtons()
    }
    fun onSharedLocatedOnStop(value: Boolean) {
        _runtime.update { it.copy(locatedOnStop = value) }
        reduceButtons()
    }
    fun onSharedLocationType(value: String) {
        _runtime.update { it.copy(locationType = value) }
        reduceButtons()
    }
    fun onSharedOrangePending(value: Boolean) {
        _runtime.update { it.copy(orangePending = value) }
        reduceButtons()
    }
    fun onSharedHasTaximeterConnection(value: Boolean) {
        _runtime.update { it.copy(hasTaximeterConnection = value) }
        reduceButtons()
    }
    fun onSharedDashboardVisible(value: Boolean) {
        _runtime.update { it.copy(showDashboardButton = value) }
        reduceButtons()
    }
    fun onSharedEstimateVisible(value: Boolean) {
        _runtime.update { it.copy(showEstimateFixedPrice = value) }
        reduceButtons()
    }
    fun onSharedRoofLight(value: Boolean?) {
        _runtime.update { it.copy(roofLightState = value) }
        reduceButtons()
    }
    fun onSharedRoofLightAvailability(isAvailable: Boolean, disableLuminous: Boolean?) {
        _runtime.update { it.copy(roofLightAvailable = isAvailable, roofLightDisableLuminous = disableLuminous) }
        reduceButtons()
    }
    fun navigateToZoning() = viewModelScope.launch {
        val navUri = "android-app://ifac.td.taxi/zoningFragment/${W2CLocation.getLastIdMacrozone().takeIf { it != 0 } ?: 1}"
        _effects.emit(HomeUiEffect.NavigateDeepLink(navUri))
    }
    fun logoff() = viewModelScope.launch(Dispatchers.IO) {
        endShiftUseCase.endShift()
        bravoCentral.logoff()
        shiftStatusUseCase.setStatus(com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_DISCONNECTED, false)
        _effects.emit(HomeUiEffect.LogoffAndGoToWelcome)
    }
    fun setRoofLightOn() = viewModelScope.launch { roofLightUseCase.setRoofLightOn() }
    fun setRoofLightOff() = viewModelScope.launch { roofLightUseCase.setRoofLightOff() }
    fun checkStartAutomaticLocation(): Boolean =
        licensingUseCase.getLicensingParameters()?.automaticStandLocation?.let { it > 0 } == true
}
