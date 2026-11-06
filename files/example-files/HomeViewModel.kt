package ifac.td.taxi.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDeepLinkRequest
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
import ifac.td.taxi.domain.usecase.ChronometerManagerUseCaseImpl.ChronometerType.TimeControlChronometer
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
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.entities.countdown.CountdownId.TIMEOUT_BLUETOOTH_DISCONNECT
import ifac.td.taxi.repository.room.entities.countdown.CountdownState
import ifac.td.taxi.repository.room.entities.message.MessageType
import ifac.td.taxi.ui.screen.HomeFragmentDirections
import ifac.td.taxi.viewmodel.model.FilterOptions
import ifac.td.taxi.viewmodel.model.MessageUIEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val TAG = "HomeViewModel"

    private val _hasMessagesFlow = MutableStateFlow<MessageUIEnum?>(null)
    val hasMessagesFlow = _hasMessagesFlow.asStateFlow()

    private val _roofLightFlow = MutableStateFlow<Pair<Boolean?, Boolean?>?>(null)
    val roofLightFlow = _roofLightFlow.asStateFlow()

    private val _showLocationButtonFlow = MutableStateFlow<Boolean?>(null)
    val showLocationButtonFlow = _showLocationButtonFlow.asStateFlow()

    private val _pendingServicesButtonFlow = MutableStateFlow<Boolean?>(null)
    val pendingServicesButtonFlow = _pendingServicesButtonFlow.asStateFlow()

    private val _pendingServicesButtonPressedCallback = MutableSharedFlow<Boolean>()
    val pendingServicesButtonPressedCallback = _pendingServicesButtonPressedCallback.asSharedFlow()

    private val _keepOnScreenFlow = MutableSharedFlow<Boolean>()
    val keepOnScreenFlow = _keepOnScreenFlow.asSharedFlow()

    private val _hasDashboardButtonOn = MutableStateFlow(false)
    val hasDashboardButtonOn = _hasDashboardButtonOn.asStateFlow()

    private val _timeControlFlow = MutableStateFlow<String>("")
    val timeControlFlow = _timeControlFlow.asStateFlow()

    private val _zoningButtonStateFlow = MutableStateFlow<Boolean>(true)
    val zoningButtonStateFlow = _zoningButtonStateFlow.asStateFlow()

    private val _showEstimateFixedPriceButtonFlow = MutableStateFlow<Boolean>(false)
    val showEstimateFixedPriceButtonFlow = _showEstimateFixedPriceButtonFlow.asStateFlow()

    private val _meetingSignColors = MutableStateFlow<Pair<Int, Int>>(Pair(0,0))
    val meetingSignColors = _meetingSignColors.asStateFlow()

    private var lastRoofLightState: Boolean? = null

    init {
        viewModelScope.launch {
            _hasDashboardButtonOn.emit(
                //true
                bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.isHomeButton == true
            )
        }

        viewModelScope.launch {
            val url = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.fixedPricesStreetTripsURL
            url?.isNotBlank()?.let { _showEstimateFixedPriceButtonFlow.emit(it) }
            //_showEstimateFixedPriceButtonFlow.emit(true)
        }
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                _meetingSignColors.emit(Pair(it.meetingSignTextColor, it.meetingSignBackgroundColor))
            }
        }
    }

    fun logoff() {
        viewModelScope.launch(Dispatchers.IO) {
            endShift()
            bravoCentral.logoff()
            shiftStatusUseCase.setStatus(ifConstants.STATE_DISCONNECTED, false)
        }
    }

    private suspend fun endShift() {
        viewModelScope.launch(Dispatchers.IO) {
            endShiftUseCase.endShift()

            if (chronometerManagerUseCase.getActiveChronometers().contains(TimeControlChronometer)) {
                Logs.d(TAG, "TimeControlChronometer still running, stopping")
                chronometerManagerUseCase.stopChronometer(TimeControlChronometer)
                Logs.d(TAG, "TimeControlChronometer stopped, emitting empty string to timeControlFlow")
                _timeControlFlow.emit("")
            }

            if (countdownManagerUseCase.getCountdowns()
                    .any { it.idCountdown == TIMEOUT_BLUETOOTH_DISCONNECT && it.state == CountdownState.STARTED }
            ) {

                Logs.d(TAG, "TIMEOUT_BLUETOOTH_DISCONNECT still running, stopping")
                stopAndDeleteBluetoothCountdown()

//            countdownManagerUseCase.getCountdownById(TIMEOUT_BLUETOOTH_DISCONNECT)
//                ?.let {
//                    countdownManagerUseCase.stopCountdown(it.toModel())
//                }
            }
        }
    }

    fun getMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = messageUseCase.getMessageByDriverId()
            if (messages.isNullOrEmpty()) {
                _hasMessagesFlow.emit(MessageUIEnum.NO_MESSAGES)
            } else {
                val hasToShowBadge = messages.find { !it.isRead && it.messageType == MessageType.MESSAGE }
                if (hasToShowBadge != null) {
                    _hasMessagesFlow.emit(MessageUIEnum.HAS_NEW_MESSAGES)
                } else {
                    _hasMessagesFlow.emit(MessageUIEnum.HAS_MESSAGES_BUT_NOT_NEW)
                }
            }
        }
    }

    fun navigateToMessage() {
        viewModelScope.launch {
            navigateTo(R.id.action_homeFragment_to_messageFragment)
        }
    }

    fun navigateToReceiptHistory() {
        viewModelScope.launch {
            if (licensingUseCase.getLicensingParameters()?.isFiscalService == false) {
                //Está -1L porque no se puede mandar null
                navigateTo(HomeFragmentDirections.actionHomeFragmentToReceiptHistoryFragment(-1L))
            } else {
                val lastTrip: Trip? = tripUseCase.getLastTrip()
                if (lastTrip != null) {
                    val isSubscriber = lastTrip.paymentMethod == PaymentMethod.SUBSCRIBER.paymentString
                    if (lastTrip.isPendingPortugalService && !isSubscriber) {
                        val atcud = portugalUseCase.getPortugalData()?.atcud
                        if (atcud.isNullOrEmpty() || atcud == "0") {
                            Toast.makeText(context, "ATCUD 0", Toast.LENGTH_SHORT).show()
                        } else {
                            navigateTo(HomeFragmentDirections.actionHomeFragmentToPortugalInvoiceFragment())
                        }
                    } else {
                        navigateTo(HomeFragmentDirections.actionHomeFragmentToReceiptHistoryFragment(-1L))
                    }
                } else {
                    navigateTo(HomeFragmentDirections.actionHomeFragmentToReceiptHistoryFragment(-1L))
                }
            }

        }
    }

    fun activateRoofLight(value: Boolean) {
        viewModelScope.launch {
            roofLightUseCase.activateRoofLight(value)
        }
    }

    fun setRoofLightOff() {
        viewModelScope.launch {
            roofLightUseCase.setRoofLightOff()
        }
    }

    fun setRoofLightOn() {
        viewModelScope.launch {
            roofLightUseCase.setRoofLightOn()
        }
    }

    fun checkRoofLight(value: Boolean?) {
        viewModelScope.launch {
            val isDisableLuminous = licensingUseCase.getLicensingParameters()?.isDisableLuminous

            Logs.d(TAG, "isDisableLuminous: $isDisableLuminous")
            if (value == null || lastRoofLightState != value) {
                if (roofLightUseCase.isRoofLightAvailable()) {
                    Logs.d(TAG, "Rooflight state changed: ${if (value == true) "on" else "off"}")
                    _roofLightFlow.emit(Pair(value, isDisableLuminous))
                } else {
                    _roofLightFlow.emit(null)
                }
                lastRoofLightState = value
            }
        }
    }

    fun navigateToContactCentral() {
        viewModelScope.launch {
            navigateTo(R.id.action_homeFragment_to_contactCentralFragment)
        }
    }

    fun changeStateHiredManual() {
        viewModelScope.launch(Dispatchers.IO) {
            shiftStatusUseCase.changeStateHiredManual()
        }
    }

    fun canGoToHiredManual(taximeterConnected: Boolean): Boolean {
        return licensingUseCase.canDoManualTrips(taximeterConnected)
    }

    fun navigateToHiredFragment(tripId: Long) {
        viewModelScope.launch {
            Logs.d("myNav", "from viewModel")
            navigateTo(HomeDirections.goToOnTripFragment())
        }
    }

    fun checkForHireAfterDelocation() {
        viewModelScope.launch {
            sendToBravoUseCase.checkForHireAfterDelocation(true)
        }
    }

    fun checkForLocationPermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            Logs.d(TAG, "isShowButtonDisableLocation: ${parameters?.isDisableLocation}")
            _showLocationButtonFlow.emit(parameters?.isDisableLocation)
        }
    }

    fun checkPendingServiceOnForHirePermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            Logs.d(TAG, "isVacantPendingServices: ${parameters?.isVacantPendingServices}")
            _pendingServicesButtonFlow.emit(parameters?.isVacantPendingServices)
        }
    }

    fun returnLocationPermission(): Boolean? {
        val parameters = licensingUseCase.getLicensingParameters()
        Logs.d(TAG, "isDisableLocation: ${parameters?.isDisableLocation}")
        return parameters?.isDisableLocation
    }

    fun checkStartAutomaticLocation(): Boolean {
        licensingUseCase.getLicensingParameters()?.automaticStandLocation?.let {
            return it > 0
        }
        return false
    }

    fun canOpenPendingTripsFragment(pendingTripsListFlow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                if (it.closePendingTripsIfEmpty) {
                    //Check if pendingTrips
                    if (BravoCentral.isPendingTripsAllowed(false)) {
                        pendingTripsUseCase.getPendingTripsZone(pendingTripsListFlow, _pendingServicesButtonPressedCallback)
                    } else {
                        _pendingServicesButtonPressedCallback.emit(false)
                    }
                } else {
                    _pendingServicesButtonPressedCallback.emit(true)
                }
            }   ?: run {
                _pendingServicesButtonPressedCallback.emit(true)
            }
        }

    }

    private suspend fun stopAndDeleteBluetoothCountdown() {
        countdownManagerUseCase.stopCountdownById(TIMEOUT_BLUETOOTH_DISCONNECT)
        countdownManagerUseCase.deleteCountdownById(TIMEOUT_BLUETOOTH_DISCONNECT)
    }

    fun navigateToZoning() {
        viewModelScope.launch {
            val defaultToMacrozone = userPreferencesUseCase.getUserPreferences()?.macroZoneQuery
            val zoneFilter = zoningUseCase.getZoneConfiguration().zoneFilter
            val filterNavigatesToZoning =  zoneFilter == FilterOptions.NEARNESS || zoneFilter == FilterOptions.ID_NO_HIERARCHY ||
                    zoneFilter == FilterOptions.HOT_ZONES || zoneFilter == FilterOptions.FAVOURITES

            if (defaultToMacrozone == true && !filterNavigatesToZoning) {
                navigateTo(R.id.action_homeFragment_to_macroZoning)
            } else if (W2CLocation.getLastIdZone() > 0 || filterNavigatesToZoning) {
                val idMacroZone = if (W2CLocation.getLastIdMacrozone() != 0) {
                    W2CLocation.getLastIdMacrozone()
                } else {
                    1
                }
                val navDeepLink =
                    NavDeepLinkRequest.Builder
                        .fromUri("android-app://ifac.td.taxi/zoningFragment/$idMacroZone".toUri())
                        .build()
                navigateTo(navDeepLink)
            } else {
                navigateTo(R.id.action_homeFragment_to_macroZoning)
            }
        }
    }

    fun checkKeepScreenOn() {
        viewModelScope.launch {
            val selected = userPreferencesUseCase.getUserPreferences()?.turnOffScreenPosition ?: 0
            _keepOnScreenFlow.emit(
                when (selected) {
                    0 -> true
                    1 -> true
                    else -> false
                }
            )
        }
    }

    fun navigateToEstimateFixedPriceScreen() {
        viewModelScope.launch {
            navigateTo(R.id.action_homeFragment_to_FixedPriceMapFragment)
        }
    }

    fun isCurrentBluetoothITop(): Boolean {
        return bluetoothLocalUseCase.isCurrentBluetoothItop()
    }

    fun checkZoningButton(locationEnabled: Boolean) {
        viewModelScope.launch {
            val isEnabled = (bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.locationRequestWhenDispatchOff == true) || locationEnabled
            _zoningButtonStateFlow.emit(isEnabled)
        }
    }

    /*
    fun askShortBreakStatus() {
        viewModelScope.launch {
            shortBreakUseCase.askShortBreakStatus()
        }
    }

    fun startShortBreak() {
        viewModelScope.launch {
            shortBreakUseCase.startShortBreak(false)
        }
    }

    fun endShortBreak() {
        viewModelScope.launch {
            shortBreakUseCase.endShortBreak(false)
        }
    }

     */
}