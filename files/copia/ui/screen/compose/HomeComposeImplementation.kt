package ifac.td.taxi.ui.screen.compose

import android.app.Application
import android.media.ToneGenerator
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import com.interfacom.sdk.taximeter.bravocomm.BravoCentral
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import ifac.td.taxi.HomeDirections
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.ChronometerManagerUseCase
import ifac.td.taxi.domain.usecase.CountdownManagerUseCase
import ifac.td.taxi.domain.usecase.EndShiftUseCase
import ifac.td.taxi.domain.usecase.MessageUseCase
import ifac.td.taxi.domain.usecase.PendingTripsUseCase
import ifac.td.taxi.domain.usecase.PortugalUseCase
import ifac.td.taxi.domain.usecase.RoofLightUseCase
import ifac.td.taxi.domain.usecase.SendToBravoUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.repository.room.entities.countdown.CountdownId.TIMEOUT_BLUETOOTH_DISCONNECT
import ifac.td.taxi.repository.room.entities.countdown.CountdownState
import ifac.td.taxi.repository.room.entities.message.MessageType
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.model.MessageUIEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ------------------------------------------------------------
// Shared VM adapter: this is the real bridge for your existing MainActivityViewModel
// ------------------------------------------------------------

/**
 * Adapter that mirrors the old MainActivityViewModel shared flows used by HomeFragment.
 * In your app, you can back this with the real MainActivityViewModel or keep this as a Compose bridge.
 */
class HomeMainActivityViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _bravoStateFlow = MutableStateFlow(BravoState(false))
    val bravoStateFlow: StateFlow<BravoState> = _bravoStateFlow.asStateFlow()

    private val _roofLightFlow = MutableStateFlow<Boolean?>(null)
    val roofLightFlow: StateFlow<Boolean?> = _roofLightFlow.asStateFlow()

    private val _locationEnabledFlow = MutableStateFlow(Pair(false, false))
    val locationEnabledFlow: StateFlow<Pair<Boolean, Boolean>> = _locationEnabledFlow.asStateFlow()

    private val _locatedOnStop = MutableStateFlow(false)
    val locatedOnStop: StateFlow<Boolean> = _locatedOnStop.asStateFlow()

    private val _locationType = MutableStateFlow("")
    val locationType: StateFlow<String> = _locationType.asStateFlow()

    private val _hasTaximeterConnectionFlow = MutableStateFlow(false)
    val hasTaximeterConnectionFlow: StateFlow<Boolean> = _hasTaximeterConnectionFlow.asStateFlow()

    private val _orangeBtnPendingFlow = MutableStateFlow<Boolean?>(null)
    val orangeBtnPendingFlow: StateFlow<Boolean?> = _orangeBtnPendingFlow.asStateFlow()

    private val _shortBreakStatus = MutableStateFlow<ShortBreakStatus?>(ShortBreakStatus.NO_SHORT_BREAK)
    val shortBreakStatus: StateFlow<ShortBreakStatus?> = _shortBreakStatus.asStateFlow()

    private val _zoneFlow = MutableStateFlow<String?>(null)
    val zoneFlow: StateFlow<String?> = _zoneFlow.asStateFlow()

    private val _pendingTripsListFlow = MutableStateFlow<ArrayList<PendingTrip>?>(null)
    val pendingTripsListFlow: StateFlow<ArrayList<PendingTrip>?> = _pendingTripsListFlow.asStateFlow()

    private val _updateMessageUI = MutableStateFlow(false)
    val updateMessageUI: StateFlow<Boolean> = _updateMessageUI.asStateFlow()

    private val _timeControlFlow = MutableStateFlow<String?>(null)
    val timeControlFlow: StateFlow<String?> = _timeControlFlow.asStateFlow()

    private val _keepScreenActiveFlow = MutableStateFlow(false)
    val keepScreenActiveFlow: StateFlow<Boolean> = _keepScreenActiveFlow.asStateFlow()

    private val _hasDashboardButtonOn = MutableStateFlow(false)
    val hasDashboardButtonOn: StateFlow<Boolean> = _hasDashboardButtonOn.asStateFlow()

    private val _showEstimateFixedPriceButtonFlow = MutableStateFlow(false)
    val showEstimateFixedPriceButtonFlow: StateFlow<Boolean> = _showEstimateFixedPriceButtonFlow.asStateFlow()

    private val _pendingServicesButtonFlow = MutableStateFlow<Boolean?>(null)
    val pendingServicesButtonFlow: StateFlow<Boolean?> = _pendingServicesButtonFlow.asStateFlow()

    fun resetDispatchFlow() {}
    fun resetCurrentAmountFlow() {}
    fun emitManualZoningNavigation(value: Boolean) {}
    fun updateLocationManuallyDeLocatedFlow(value: Boolean) {}

    fun updateLocationEnabledFlow(enabled: Boolean, canEnable: Boolean) {
        _locationEnabledFlow.value = Pair(enabled, canEnable)
    }

    fun updateBravoState(voiceValue: Boolean) {
        _bravoStateFlow.value = BravoState(voiceValue)
    }

    fun updateRoofLight(value: Boolean?) {
        _roofLightFlow.value = value
    }

    fun updateLocatedOnStop(value: Boolean) {
        _locatedOnStop.value = value
    }

    fun updateLocationType(value: String) {
        _locationType.value = value
    }

    fun updateHasTaximeterConnection(value: Boolean) {
        _hasTaximeterConnectionFlow.value = value
    }

    fun updateOrangeBtnPending(value: Boolean?) {
        _orangeBtnPendingFlow.value = value
    }

    fun updateShortBreakStatus(value: ShortBreakStatus?) {
        _shortBreakStatus.value = value
    }

    fun updateZone(value: String?) {
        _zoneFlow.value = value
    }

    fun updatePendingTripsList(value: ArrayList<PendingTrip>?) {
        _pendingTripsListFlow.value = value
    }

    fun updateMessageUiRequested(value: Boolean) {
        _updateMessageUI.value = value
    }

    fun updateTimeControl(value: String?) {
        _timeControlFlow.value = value
    }

    fun updateKeepScreenActive(value: Boolean) {
        _keepScreenActiveFlow.value = value
    }

    fun updateDashboardButton(value: Boolean) {
        _hasDashboardButtonOn.value = value
    }

    fun updateFixedPriceButton(value: Boolean) {
        _showEstimateFixedPriceButtonFlow.value = value
    }

    fun updatePendingServicesButton(value: Boolean?) {
        _pendingServicesButtonFlow.value = value
    }
}

data class BravoState(val voiceValue: Boolean = false)

// ------------------------------------------------------------
// Home UI models
// ------------------------------------------------------------

data class HomeUiState(
    val buttons: HomeButtonsState = HomeButtonsState(),
    val dialog: HomeDialogState? = null
)

data class HomeButtonsState(
    val zoning: HomeButtonUi = HomeButtonUi(R.string.btn_zoning, true, true, HomeButtonColor.BLUE),
    val pending: HomeButtonUi = HomeButtonUi(R.string.btn_pending, false, true, HomeButtonColor.BLUE),
    val location: HomeButtonUi = HomeButtonUi(R.string.btn_location_on, true, true, HomeButtonColor.RED),
    val locateStand: HomeButtonUi = HomeButtonUi(R.string.btn_locate_stop, false, true, HomeButtonColor.DISABLED),
    val receipts: HomeButtonUi = HomeButtonUi(R.string.btn_receipts, true, true, HomeButtonColor.BLUE),
    val messages: HomeButtonUi = HomeButtonUi(R.string.btn_messages, false, true, HomeButtonColor.DISABLED),
    val central: HomeButtonUi = HomeButtonUi(R.string.btn_central, true, true, HomeButtonColor.RED),
    val dashboard: HomeButtonUi = HomeButtonUi(R.string.btn_dashboard, true, false, HomeButtonColor.BLUE),
    val fixedPrice: HomeButtonUi = HomeButtonUi(R.string.btn_fixed_price, true, false, HomeButtonColor.BLUE)
)

data class HomeButtonUi(
    @StringRes val textRes: Int,
    val enabled: Boolean,
    val visible: Boolean,
    val color: HomeButtonColor
)

enum class HomeButtonColor { BLUE, RED, GREEN, ORANGE, DISABLED, LOADING }

data class HomeDialogState(
    val visible: Boolean = false,
    val type: HomeDialogType? = null
)

enum class HomeDialogType {
    ActivateLocation,
    DeactivateLocation,
    CloseShift,
    HiredManual,
    ExitStand
}

sealed interface HomeAction {
    data object ZoningClicked : HomeAction
    data object PendingClicked : HomeAction
    data object LocationClicked : HomeAction
    data object ReceiptsClicked : HomeAction
    data object MessagesClicked : HomeAction
    data object CentralClicked : HomeAction
    data object DashboardClicked : HomeAction
    data object FixedPriceClicked : HomeAction
    data object LocateStandClicked : HomeAction
}

sealed interface HomeUiEffect {
    data class ShowToast(@StringRes val messageRes: Int) : HomeUiEffect
    data class Beep(val tone: Int) : HomeUiEffect
    data class KeepScreenOn(val keepOn: Boolean) : HomeUiEffect

    sealed interface Destination {
        data object Message : Destination
        data object ContactCentral : Destination
        data object Dashboard : Destination
        data object FixedPrice : Destination
        data object PendingTrips : Destination
        data object ReceiptHistory : Destination
        data object MacroZoning : Destination
        data object Welcome : Destination
        data class ZoningDeepLink(val idMacroZone: Int) : Destination
    }

    data class Navigate(val destination: Destination) : HomeUiEffect
}

// ------------------------------------------------------------
// Home ViewModel
// ------------------------------------------------------------

class HomeViewModelCompose(
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
    application: Application,
    private val shared: HomeMainActivityViewModel
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<HomeUiEffect>()
    val effects: SharedFlow<HomeUiEffect> = _effects.asSharedFlow()

    fun onScreenStarted() {
        getMessages()
        checkForLocationPermission()
        checkPendingServiceOnForHirePermission()
        checkKeepScreenOn()
        initStaticButtons()
        collectSharedFlows()
    }

    private fun initStaticButtons() {
        viewModelScope.launch {
            val dashboardVisible = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.isHomeButton == true
            val fixedPriceVisible = !bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.fixedPricesStreetTripsURL.isNullOrBlank()
            _uiState.update {
                it.copy(
                    buttons = it.buttons.copy(
                        dashboard = it.buttons.dashboard.copy(visible = dashboardVisible),
                        fixedPrice = it.buttons.fixedPrice.copy(visible = fixedPriceVisible),
                        receipts = it.buttons.receipts.copy(visible = !fixedPriceVisible)
                    )
                )
            }
        }
    }

    private fun collectSharedFlows() {
        viewModelScope.launch {
            shared.bravoStateFlow.collect { state ->
                _uiState.update {
                    it.copy(buttons = it.buttons.copy(
                        central = it.buttons.central.copy(
                            color = if (state.voiceValue) HomeButtonColor.BLUE else HomeButtonColor.RED
                        )
                    ))
                }
            }
        }

        viewModelScope.launch {
            shared.roofLightFlow.collect { value ->
                val disableLuminous = licensingUseCase.getLicensingParameters()?.isDisableLuminous == true
                _uiState.update {
                    it.copy(buttons = it.buttons.copy(
                        roofLight = when {
                            !disableLuminous -> it.buttons.roofLight.copy(enabled = false, color = HomeButtonColor.DISABLED)
                            value == true -> it.buttons.roofLight.copy(enabled = true, color = HomeButtonColor.GREEN)
                            value == false -> it.buttons.roofLight.copy(enabled = true, color = HomeButtonColor.RED)
                            else -> it.buttons.roofLight.copy(enabled = false, color = HomeButtonColor.DISABLED)
                        }
                    ))
                }
            }
        }

        viewModelScope.launch {
            shared.locationEnabledFlow.collect { value ->
                if (returnLocationPermission() == true) {
                    _uiState.update {
                        it.copy(buttons = it.buttons.copy(
                            location = if (value.first) {
                                it.buttons.location.copy(
                                    textRes = R.string.btn_location_off,
                                    enabled = true,
                                    color = HomeButtonColor.GREEN
                                )
                            } else {
                                it.buttons.location.copy(
                                    textRes = R.string.btn_location_on,
                                    enabled = true,
                                    color = HomeButtonColor.RED
                                )
                            }
                        ))
                    }
                    checkZoningButton(value.first)
                }
            }
        }

        viewModelScope.launch {
            shared.locatedOnStop.collect { value ->
                _uiState.update {
                    it.copy(buttons = it.buttons.copy(
                        locateStand = if (value) {
                            it.buttons.locateStand.copy(
                                enabled = true,
                                color = if (shared.locationType.value == "P") HomeButtonColor.GREEN else HomeButtonColor.RED
                            )
                        } else {
                            it.buttons.locateStand.copy(enabled = false)
                        }
                    ))
                }
            }
        }

        viewModelScope.launch {
            shared.locationType.collect { value ->
                onLocationType(value)
            }
        }

        viewModelScope.launch {
            shared.hasTaximeterConnectionFlow.collect {
                updateTopBarIconRight()
            }
        }

        viewModelScope.launch {
            shared.orangeBtnPendingFlow.collect { orange ->
                _uiState.update {
                    it.copy(buttons = it.buttons.copy(
                        pending = it.buttons.pending.copy(
                            color = if (orange == true) HomeButtonColor.ORANGE else HomeButtonColor.BLUE
                        )
                    ))
                }
            }
        }

        viewModelScope.launch {
            shared.shortBreakStatus.collect {
                checkPendingButtonEnabled()
            }
        }

        viewModelScope.launch {
            shared.updateMessageUI.collect {
                getMessages()
            }
        }

        viewModelScope.launch {
            shared.timeControlFlow.collect {
                _effects.emit(HomeUiEffect.KeepScreenOn(true))
            }
        }

        viewModelScope.launch {
            shared.hasDashboardButtonOn.collect { visible ->
                _uiState.update {
                    it.copy(buttons = it.buttons.copy(
                        dashboard = it.buttons.dashboard.copy(visible = visible)
                    ))
                }
            }
        }

        viewModelScope.launch {
            shared.showEstimateFixedPriceButtonFlow.collect { visible ->
                _uiState.update {
                    it.copy(buttons = it.buttons.copy(
                        fixedPrice = it.buttons.fixedPrice.copy(visible = visible),
                        receipts = it.buttons.receipts.copy(visible = !visible)
                    ))
                }
            }
        }

        viewModelScope.launch {
            shared.zoneFlow.collect { zone ->
                if (zone.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(buttons = it.buttons.copy(
                            pending = it.buttons.pending.copy(enabled = false)
                        ))
                    }
                } else {
                    checkPendingButtonEnabled()
                }
            }
        }

        viewModelScope.launch {
            shared.keepScreenActiveFlow.collect { keep ->
                _effects.emit(HomeUiEffect.KeepScreenOn(keep))
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.ZoningClicked -> {
                shared.emitManualZoningNavigation(true)
                navigateToZoning()
            }
            HomeAction.PendingClicked -> canOpenPendingTripsFragment(shared.pendingTripsListFlow as MutableStateFlow<ArrayList<PendingTrip>?>)
            HomeAction.LocationClicked -> openLocationDialog()
            HomeAction.ReceiptsClicked -> navigateToReceiptHistory()
            HomeAction.MessagesClicked -> navigateToMessage()
            HomeAction.CentralClicked -> navigateToContactCentral()
            HomeAction.DashboardClicked -> viewModelScope.launch {
                _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.Dashboard))
            }
            HomeAction.FixedPriceClicked -> navigateToEstimateFixedPriceScreen()
            HomeAction.LocateStandClicked -> triggerLocateOnStand()
        }
    }

    fun getMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = messageUseCase.getMessageByDriverId()
            val result = if (messages.isNullOrEmpty()) {
                MessageUIEnum.NO_MESSAGES
            } else {
                val hasBadge = messages.any { !it.isRead && it.messageType == MessageType.MESSAGE }
                if (hasBadge) MessageUIEnum.HAS_NEW_MESSAGES else MessageUIEnum.HAS_MESSAGES_BUT_NOT_NEW
            }
            _uiState.update {
                it.copy(buttons = it.buttons.copy(
                    messages = when (result) {
                        MessageUIEnum.NO_MESSAGES -> it.buttons.messages.copy(enabled = false, color = HomeButtonColor.DISABLED)
                        MessageUIEnum.HAS_MESSAGES_BUT_NOT_NEW -> it.buttons.messages.copy(enabled = true, color = HomeButtonColor.BLUE)
                        MessageUIEnum.HAS_NEW_MESSAGES -> it.buttons.messages.copy(enabled = true, color = HomeButtonColor.ORANGE)
                        null -> it.buttons.messages
                    }
                ))
            }
        }
    }

    fun checkForLocationPermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            if (parameters?.isDisableLocation == false) {
                _uiState.update {
                    it.copy(buttons = it.buttons.copy(
                        location = it.buttons.location.copy(enabled = false)
                    ))
                }
            }
        }
    }

    fun checkPendingServiceOnForHirePermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            shared.updatePendingServicesButton(parameters?.isVacantPendingServices)
            checkPendingButtonEnabled()
        }
    }

    fun checkKeepScreenOn() {
        viewModelScope.launch {
            val selected = userPreferencesUseCase.getUserPreferences()?.turnOffScreenPosition ?: 0
            _effects.emit(HomeUiEffect.KeepScreenOn(selected == 0 || selected == 1))
        }
    }

    fun navigateToMessage() {
        viewModelScope.launch {
            _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.Message))
        }
    }

    fun navigateToContactCentral() {
        viewModelScope.launch {
            _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.ContactCentral))
        }
    }

    fun navigateToEstimateFixedPriceScreen() {
        viewModelScope.launch {
            _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.FixedPrice))
        }
    }

    fun navigateToReceiptHistory() {
        viewModelScope.launch {
            if (licensingUseCase.getLicensingParameters()?.isFiscalService == false) {
                _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.ReceiptHistory))
            } else {
                val lastTrip: Trip? = tripUseCase.getLastTrip()
                if (lastTrip != null) {
                    val isSubscriber = lastTrip.paymentMethod == PaymentMethod.SUBSCRIBER.paymentString
                    if (lastTrip.isPendingPortugalService && !isSubscriber) {
                        val atcud = portugalUseCase.getPortugalData()?.atcud
                        if (atcud.isNullOrEmpty() || atcud == "0") {
                            _effects.emit(HomeUiEffect.ShowToast(R.string.toast_no_hay_pendientes))
                        } else {
                            _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.ReceiptHistory))
                        }
                    } else {
                        _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.ReceiptHistory))
                    }
                } else {
                    _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.ReceiptHistory))
                }
            }
        }
    }

    fun navigateToZoning() {
        viewModelScope.launch {
            val defaultToMacrozone = userPreferencesUseCase.getUserPreferences()?.macroZoneQuery
            val zoneFilter = zoningUseCase.getZoneConfiguration().zoneFilter
            val filterNavigatesToZoning = false

            if (defaultToMacrozone == true && !filterNavigatesToZoning) {
                _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.MacroZoning))
            } else if (W2CLocation.getLastIdZone() > 0 || filterNavigatesToZoning) {
                val idMacroZone = if (W2CLocation.getLastIdMacrozone() != 0) W2CLocation.getLastIdMacrozone() else 1
                _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.ZoningDeepLink(idMacroZone)))
            } else {
                _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.MacroZoning))
            }
        }
    }

    fun checkZoningButton(locationEnabled: Boolean) {
        viewModelScope.launch {
            val isEnabled = (bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.locationRequestWhenDispatchOff == true) || locationEnabled
            _uiState.update {
                it.copy(buttons = it.buttons.copy(
                    zoning = it.buttons.zoning.copy(enabled = isEnabled)
                ))
            }
        }
    }

    fun canOpenPendingTripsFragment(pendingTripsListFlow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                if (it.closePendingTripsIfEmpty) {
                    if (BravoCentral.isPendingTripsAllowed(false)) {
                        pendingTripsUseCase.getPendingTripsZone(pendingTripsListFlow, MutableSharedFlow())
                        _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.PendingTrips))
                    } else {
                        _effects.emit(HomeUiEffect.ShowToast(R.string.toast_no_hay_pendientes))
                    }
                } else {
                    _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.PendingTrips))
                }
            } ?: run {
                _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.PendingTrips))
            }
        }
    }

    private fun checkPendingButtonEnabled() {
        val zone = shared.zoneFlow.value
        if (zone.isNullOrEmpty()) {
            _uiState.update {
                it.copy(buttons = it.buttons.copy(
                    pending = it.buttons.pending.copy(enabled = false)
                ))
            }
            return
        }

        val shortBreak = shared.shortBreakStatus.value
        val isInShortBreak = shortBreak == ShortBreakStatus.IN_SHORT_BREAK ||
                shortBreak == ShortBreakStatus.IN_SHORT_BREAK_FORCED

        if (isInShortBreak) {
            _uiState.update {
                it.copy(buttons = it.buttons.copy(
                    pending = it.buttons.pending.copy(enabled = false)
                ))
            }
            return
        }

        if (!W2CLocation.isLocationAllowedByCentral()) {
            _uiState.update {
                it.copy(buttons = it.buttons.copy(
                    pending = it.buttons.pending.copy(enabled = false)
                ))
            }
            return
        }

        if (W2CLocation.getIsInSoonInZone()) {
            _uiState.update {
                it.copy(buttons = it.buttons.copy(
                    pending = it.buttons.pending.copy(enabled = true)
                ))
            }
            return
        }

        if (shared.pendingServicesButtonFlow.value == true) {
            _uiState.update {
                it.copy(buttons = it.buttons.copy(
                    pending = it.buttons.pending.copy(enabled = true)
                ))
            }
            return
        }

        _uiState.update {
            it.copy(buttons = it.buttons.copy(
                pending = it.buttons.pending.copy(enabled = false)
            ))
        }
    }

    private fun onLocationType(type: String) {
        when (type) {
            "Z" -> {
                if (shared.locatedOnStop.value) {
                    _uiState.update {
                        it.copy(buttons = it.buttons.copy(
                            locateStand = it.buttons.locateStand.copy(
                                enabled = true,
                                color = HomeButtonColor.RED,
                                textRes = R.string.btn_locate_stop
                            )
                        ))
                    }
                } else {
                    if (W2CLocation.locationStopAllowed()) {
                        _uiState.update {
                            it.copy(buttons = it.buttons.copy(
                                locateStand = it.buttons.locateStand.copy(
                                    enabled = true,
                                    color = HomeButtonColor.RED,
                                    textRes = R.string.btn_locate_stop
                                )
                            ))
                        }
                    } else {
                        _uiState.update {
                            it.copy(buttons = it.buttons.copy(
                                locateStand = it.buttons.locateStand.copy(enabled = false)
                            ))
                        }
                    }
                }
            }

            "P" -> {
                if (!checkStartAutomaticLocation()) {
                    _uiState.update {
                        it.copy(buttons = it.buttons.copy(
                            locateStand = it.buttons.locateStand.copy(
                                enabled = true,
                                color = HomeButtonColor.GREEN,
                                textRes = R.string.btn_locate_stop_exit
                            )
                        ))
                    }
                } else {
                    _uiState.update {
                        it.copy(buttons = it.buttons.copy(
                            locateStand = it.buttons.locateStand.copy(enabled = false)
                        ))
                    }
                }
            }
        }
    }

    private fun openLocationDialog() {
        val enabled = shared.locationEnabledFlow.value
        if (enabled.first) {
            if (shared.shortBreakStatus.value == ShortBreakStatus.IN_SHORT_BREAK_FORCED) {
                viewModelScope.launch { _effects.emit(HomeUiEffect.ShowToast(R.string.connect_taximeter)) }
                return
            }
            _uiState.update { it.copy(dialog = HomeDialogState(true, HomeDialogType.DeactivateLocation)) }
        } else {
            if (W2CLocation.isLocationAllowedByCentral()) {
                _uiState.update { it.copy(dialog = HomeDialogState(true, HomeDialogType.ActivateLocation)) }
            }
        }
    }

    fun onDialogDismissed() {
        _uiState.update { it.copy(dialog = null) }
    }

    fun onDialogConfirmed() {
        when (_uiState.value.dialog?.type) {
            HomeDialogType.ActivateLocation -> {
                shared.updateLocationEnabledFlow(enabled = true, canEnable = true)
                shared.updateLocationManuallyDeLocatedFlow(false)
            }

            HomeDialogType.DeactivateLocation -> {
                shared.updateLocationEnabledFlow(enabled = false, canEnable = true)
                shared.updateLocationManuallyDeLocatedFlow(true)
                viewModelScope.launch { _effects.emit(HomeUiEffect.Beep(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP)) }
                viewModelScope.launch { sendToBravoUseCase.checkForHireAfterDelocation(true) }
            }

            HomeDialogType.CloseShift -> {
                logoff()
                viewModelScope.launch { _effects.emit(HomeUiEffect.Navigate(HomeUiEffect.Destination.Welcome)) }
            }

            HomeDialogType.HiredManual -> {
                changeStateHiredManual()
            }

            HomeDialogType.ExitStand -> {
                if (W2CLocation.getLocationTypeSent() == 'P') {
                    W2CLocation.sendLocateZone()
                } else {
                    W2CLocation.sendLocateStop()
                    W2CLocation.setTryingToRelocateInStand(false)
                }
            }

            null -> Unit
        }
        _uiState.update { it.copy(dialog = null) }
    }

    fun triggerLocateOnStand() {
        if (W2CLocation.getLocationTypeSent() == 'P') {
            _uiState.update { it.copy(dialog = HomeDialogState(true, HomeDialogType.ExitStand)) }
        } else {
            W2CLocation.sendLocateStop()
            W2CLocation.setTryingToRelocateInStand(false)
        }
    }

    fun changeStateHiredManual() {
        viewModelScope.launch(Dispatchers.IO) {
            shiftStatusUseCase.changeStateHiredManual()
        }
    }

    fun logoff() {
        viewModelScope.launch(Dispatchers.IO) {
            endShiftUseCase.endShift()
            bravoCentral.logoff()
            shiftStatusUseCase.setStatus(ifConstants.STATE_DISCONNECTED, false)
        }
    }

    fun isCurrentBluetoothITop(): Boolean = bluetoothLocalUseCase.isCurrentBluetoothItop()

    fun returnLocationPermission(): Boolean? = licensingUseCase.getLicensingParameters()?.isDisableLocation

    fun checkStartAutomaticLocation(): Boolean =
        licensingUseCase.getLicensingParameters()?.automaticStandLocation?.let { it > 0 } ?: false

    private fun updateTopBarIconRight() {
        if (canGoToHiredManual(shared.hasTaximeterConnectionFlow.value)) {
            _uiState.update { it.copy(dialog = HomeDialogState(true, HomeDialogType.HiredManual)) }
        }
    }

    fun canGoToHiredManual(taximeterConnected: Boolean): Boolean {
        return licensingUseCase.canDoManualTrips(taximeterConnected)
    }
}

// ------------------------------------------------------------
// Compose screen
// ------------------------------------------------------------

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModelCompose,
    sharedViewModel: HomeMainActivityViewModel,
    onShowHeader: (Boolean) -> Unit,
    onKeepScreenActive: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
    onBeep: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onShowHeader(true)
        viewModel.onScreenStarted()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.ShowToast -> onShowToast(effect.messageRes)
                is HomeUiEffect.Beep -> onBeep(effect.tone)
                is HomeUiEffect.KeepScreenOn -> onKeepScreenActive(effect.keepOn)
                is HomeUiEffect.Navigate -> handleNavigation(navController, effect.destination)
            }
        }
    }

    // Shared VM bridge collectors
    LaunchedEffect(sharedViewModel) { sharedViewModel.bravoStateFlow.collect { viewModel.onSharedBravoState(it.voiceValue) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.roofLightFlow.collect { viewModel.onSharedRoofLight(it) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.locationEnabledFlow.collect { viewModel.onSharedLocationEnabled(it) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.locatedOnStop.collect { viewModel.onSharedLocatedOnStop(it) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.locationType.collect { viewModel.onSharedLocationType(it) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.hasTaximeterConnectionFlow.collect { viewModel.onSharedTaximeterConnection(it) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.orangeBtnPendingFlow.collect { viewModel.onSharedOrangePending(it) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.shortBreakStatus.collect { viewModel.onSharedShortBreakStatus(it) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.zoneFlow.collect { viewModel.onSharedZone(it) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.updateMessageUI.collect { if (it) viewModel.getMessages() } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.timeControlFlow.collect { onKeepScreenActive(true) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.hasDashboardButtonOn.collect { viewModel.onSharedDashboardVisible(it) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.showEstimateFixedPriceButtonFlow.collect { viewModel.onSharedFixedPriceVisible(it) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.keepScreenActiveFlow.collect { onKeepScreenActive(it) } }
    LaunchedEffect(sharedViewModel) { sharedViewModel.pendingServicesButtonFlow.collect { viewModel.onSharedPendingServicesButton(it) } }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                HomeButton(state.buttons.zoning, onClick = { viewModel.onAction(HomeAction.ZoningClicked) })
                HomeButton(state.buttons.pending, onClick = { viewModel.onAction(HomeAction.PendingClicked) })
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                HomeButton(state.buttons.location, onClick = { viewModel.onAction(HomeAction.LocationClicked) })
                HomeButton(state.buttons.locateStand, onClick = { viewModel.onAction(HomeAction.LocateStandClicked) })
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                HomeButton(state.buttons.receipts, onClick = { viewModel.onAction(HomeAction.ReceiptsClicked) })
                HomeButton(state.buttons.messages, onClick = { viewModel.onAction(HomeAction.MessagesClicked) })
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                HomeButton(state.buttons.central, onClick = { viewModel.onAction(HomeAction.CentralClicked) })
                if (state.buttons.dashboard.visible) {
                    HomeButton(state.buttons.dashboard, onClick = { viewModel.onAction(HomeAction.DashboardClicked) })
                }
            }

            Spacer(Modifier.height(8.dp))

            if (state.buttons.fixedPrice.visible) {
                HomeButton(state.buttons.fixedPrice, onClick = { viewModel.onAction(HomeAction.FixedPriceClicked) })
            }
        }
    }

    state.dialog?.let { dialog ->
        HomeDialogHost(
            dialog = dialog,
            onDismiss = viewModel::onDialogDismissed,
            onConfirm = viewModel::onDialogConfirmed
        )
    }
}

private fun handleNavigation(navController: NavController, destination: HomeUiEffect.Destination) {
    when (destination) {
        HomeUiEffect.Destination.Message -> {
            val directions: NavDirections = HomeFragmentDirections.actionHomeFragmentToMessageFragment()
            navController.navigate(directions)
        }

        HomeUiEffect.Destination.ContactCentral -> {
            val directions: NavDirections = HomeFragmentDirections.actionHomeFragmentToContactCentralFragment()
            navController.navigate(directions)
        }

        HomeUiEffect.Destination.Dashboard -> {
            val directions: NavDirections = HomeFragmentDirections.actionHomeFragmentToDashboardFragment()
            navController.navigate(directions)
        }

        HomeUiEffect.Destination.FixedPrice -> {
            val directions: NavDirections = HomeFragmentDirections.actionHomeFragmentToFixedPriceMapFragment()
            navController.navigate(directions)
        }

        HomeUiEffect.Destination.PendingTrips -> {
            val directions: NavDirections = HomeFragmentDirections.actionHomeFragmentToPendingTripsFragment()
            navController.navigate(directions)
        }

        HomeUiEffect.Destination.ReceiptHistory -> {
            val directions: NavDirections = HomeFragmentDirections.actionHomeFragmentToReceiptHistoryFragment(-1L)
            navController.navigate(directions)
        }

        HomeUiEffect.Destination.MacroZoning -> {
            val directions: NavDirections = HomeFragmentDirections.actionHomeFragmentToMacroZoning()
            navController.navigate(directions)
        }

        HomeUiEffect.Destination.Welcome -> {
            val request = NavDeepLinkRequest.Builder
                .fromUri("android-app://ifac.td.taxi/welcome".toUri())
                .build()
            navController.navigate(request)
        }

        is HomeUiEffect.Destination.ZoningDeepLink -> {
            val request = NavDeepLinkRequest.Builder
                .fromUri("android-app://ifac.td.taxi/zoningFragment/${destination.idMacroZone}".toUri())
                .build()
            navController.navigate(request)
        }
    }
}

@Composable
private fun HomeButton(
    state: HomeButtonUi,
    onClick: () -> Unit
) {
    if (!state.visible) return

    val containerColor = when (state.color) {
        HomeButtonColor.BLUE -> MaterialTheme.colorScheme.primary
        HomeButtonColor.RED -> MaterialTheme.colorScheme.error
        HomeButtonColor.GREEN -> MaterialTheme.colorScheme.tertiary
        HomeButtonColor.ORANGE -> MaterialTheme.colorScheme.secondary
        HomeButtonColor.DISABLED -> MaterialTheme.colorScheme.surfaceVariant
        HomeButtonColor.LOADING -> MaterialTheme.colorScheme.surfaceVariant
    }

    Button(
        onClick = onClick,
        enabled = state.enabled,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(state.textRes))
    }
}

@Composable
private fun HomeDialogHost(
    dialog: HomeDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!dialog.visible || dialog.type == null) return

    val titleRes = when (dialog.type) {
        HomeDialogType.ActivateLocation -> R.string.confirm_activate_location
        HomeDialogType.DeactivateLocation -> R.string.confirm_deactivate_location
        HomeDialogType.CloseShift -> R.string.confirm_close_shift
        HomeDialogType.HiredManual -> R.string.dialog_hired_manual_title
        HomeDialogType.ExitStand -> R.string.dialog_exit_onStand_title
    }

    val textRes = when (dialog.type) {
        HomeDialogType.HiredManual -> R.string.confirm_change_to_hired
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(titleRes)) },
        text = textRes?.let { { Text(text = stringResource(it)) } },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(android.R.string.cancel))
            }
        }
    )
}

// ------------------------------------------------------------
// Shared bridge -> ViewModel helpers
// ------------------------------------------------------------

private fun HomeViewModelCompose.onSharedBravoState(voiceValue: Boolean) {
    _uiState.update {
        it.copy(buttons = it.buttons.copy(
            central = it.buttons.central.copy(
                color = if (voiceValue) HomeButtonColor.BLUE else HomeButtonColor.RED
            )
        ))
    }
}

private fun HomeViewModelCompose.onSharedRoofLight(value: Boolean?) {
    val disableLuminous = licensingUseCase.getLicensingParameters()?.isDisableLuminous == true
    _uiState.update {
        it.copy(buttons = it.buttons.copy(
            roofLight = when {
                !disableLuminous -> HomeButtonUi(R.string.btn_light, false, true, HomeButtonColor.DISABLED)
                value == true -> HomeButtonUi(R.string.btn_light, true, true, HomeButtonColor.GREEN)
                value == false -> HomeButtonUi(R.string.btn_light, true, true, HomeButtonColor.RED)
                else -> HomeButtonUi(R.string.btn_light, false, true, HomeButtonColor.DISABLED)
            }
        ))
    }
}

private fun HomeViewModelCompose.onSharedLocationEnabled(value: Pair<Boolean, Boolean>) {
    if (returnLocationPermission() == true) {
        _uiState.update {
            it.copy(buttons = it.buttons.copy(
                location = if (value.first) {
                    it.buttons.location.copy(textRes = R.string.btn_location_off, enabled = true, color = HomeButtonColor.GREEN)
                } else {
                    it.buttons.location.copy(textRes = R.string.btn_location_on, enabled = true, color = HomeButtonColor.RED)
                }
            ))
        }
        checkZoningButton(value.first)
    }
}

private fun HomeViewModelCompose.onSharedLocatedOnStop(value: Boolean) {
    _uiState.update {
        it.copy(buttons = it.buttons.copy(
            locateStand = if (value) {
                it.buttons.locateStand.copy(
                    enabled = true,
                    color = if (shared.locationType.value == "P") HomeButtonColor.GREEN else HomeButtonColor.RED
                )
            } else {
                it.buttons.locateStand.copy(enabled = false)
            }
        ))
    }
}

private fun HomeViewModelCompose.onSharedLocationType(value: String) {
    when (value) {
        "Z" -> {
            if (shared.locatedOnStop.value) {
                _uiState.update {
                    it.copy(buttons = it.buttons.copy(
                        locateStand = it.buttons.locateStand.copy(enabled = true, color = HomeButtonColor.RED, textRes = R.string.btn_locate_stop)
                    ))
                }
            } else {
                if (W2CLocation.locationStopAllowed()) {
                    _uiState.update {
                        it.copy(buttons = it.buttons.copy(
                            locateStand = it.buttons.locateStand.copy(enabled = true, color = HomeButtonColor.RED, textRes = R.string.btn_locate_stop)
                        ))
                    }
                } else {
                    _uiState.update {
                        it.copy(buttons = it.buttons.copy(
                            locateStand = it.buttons.locateStand.copy(enabled = false)
                        ))
                    }
                }
            }
        }

        "P" -> {
            if (!checkStartAutomaticLocation()) {
                _uiState.update {
                    it.copy(buttons = it.buttons.copy(
                        locateStand = it.buttons.locateStand.copy(enabled = true, color = HomeButtonColor.GREEN, textRes = R.string.btn_locate_stop_exit)
                    ))
                }
            } else {
                _uiState.update {
                    it.copy(buttons = it.buttons.copy(
                        locateStand = it.buttons.locateStand.copy(enabled = false)
                    ))
                }
            }
        }
    }
}

private fun HomeViewModelCompose.onSharedTaximeterConnection(value: Boolean) {
    updateTopBarIconRight()
}

private fun HomeViewModelCompose.onSharedOrangePending(value: Boolean?) {
    _uiState.update {
        it.copy(buttons = it.buttons.copy(
            pending = it.buttons.pending.copy(color = if (value == true) HomeButtonColor.ORANGE else HomeButtonColor.BLUE)
        ))
    }
}

private fun HomeViewModelCompose.onSharedShortBreakStatus(value: ShortBreakStatus?) {
    checkPendingButtonEnabled()
}

private fun HomeViewModelCompose.onSharedZone(value: String?) {
    if (value.isNullOrBlank()) {
        _uiState.update {
            it.copy(buttons = it.buttons.copy(
                pending = it.buttons.pending.copy(enabled = false)
            ))
        }
    } else {
        checkPendingButtonEnabled()
    }
}

private fun HomeViewModelCompose.onSharedDashboardVisible(value: Boolean) {
    _uiState.update {
        it.copy(buttons = it.buttons.copy(
            dashboard = it.buttons.dashboard.copy(visible = value)
        ))
    }
}

private fun HomeViewModelCompose.onSharedFixedPriceVisible(value: Boolean) {
    _uiState.update {
        it.copy(buttons = it.buttons.copy(
            fixedPrice = it.buttons.fixedPrice.copy(visible = value),
            receipts = it.buttons.receipts.copy(visible = !value)
        ))
    }
}

private fun HomeViewModelCompose.onSharedPendingServicesButton(value: Boolean?) {
    shared.updatePendingServicesButton(value)
    checkPendingButtonEnabled()
}


