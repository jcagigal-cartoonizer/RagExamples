package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.Intent
import android.media.ToneGenerator
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import com.interfacom.sdk.taximeter.bravocomm.models.StatusGPS
import com.interfacom.sdk.taximeter.bravocomm.models.responses.SanctionDeletionResponse
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
import com.interfacom.sdk.taximeter.log.RemoteLog
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import com.interfacom.sdk.taximeter.taximeter.events.TotalizersRetrievedEvent
import com.interfacom.sdk.taximeter.taximeter.models.prime.TaximeterPrimeConfiguration
import ifac.td.taxi.NavGraphDirections
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.ReconnectionState
import ifac.td.taxi.domain.model.ShiftStatus
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.model.UserPreferences
import ifac.td.taxi.domain.model.itop.ITopMeterStatus
import ifac.td.taxi.domain.model.skyglass.SkyGlassEvent
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.ChronometerManagerUseCase
import ifac.td.taxi.domain.usecase.ChronometerManagerUseCaseImpl.ChronometerType.TimeControlChronometer
import ifac.td.taxi.domain.usecase.CountdownManagerUseCase
import ifac.td.taxi.domain.usecase.DispatchNotificationUseCase
import ifac.td.taxi.domain.usecase.IngenicoUseCase
import ifac.td.taxi.domain.usecase.LightSkyGlassUseCase
import ifac.td.taxi.domain.usecase.MessageUseCase
import ifac.td.taxi.domain.usecase.PortugalUseCase
import ifac.td.taxi.domain.usecase.PrinterUseCase
import ifac.td.taxi.domain.usecase.SanctionUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.TTSUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.framework.sdk.PrimeManager
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.framework.sdk.usecase.DeviceUpdateUseCase
import ifac.td.taxi.framework.sdk.usecase.GetButtonMinutesUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.PhoneCallUseCaseImpl.PhoneCallError
import ifac.td.taxi.framework.sdk.usecase.ShowLegalTextUseCase
import ifac.td.taxi.framework.sdk.usecase.SoundManagerUseCase
import ifac.td.taxi.framework.sdk.usecase.TaximeterConnectUseCase
import ifac.td.taxi.framework.sdk.usecase.XMPPUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.framework.util.tickets.Tickets.tools
import ifac.td.taxi.repository.connections.service.model.BravoState
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.repository.room.dao.BravoConfigurationDao
import ifac.td.taxi.repository.room.entities.countdown.CountdownId
import ifac.td.taxi.repository.room.entities.countdown.CountdownIdCallback
import ifac.td.taxi.repository.room.entities.countdown.CountdownTimeAmount
import ifac.td.taxi.repository.room.entities.message.MessageEntity
import ifac.td.taxi.ui.MainActivity
import ifac.td.taxi.ui.custom.button.ButtonTimerState
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.dialog.filterDialog.CustomFilterDialog
import ifac.td.taxi.ui.extension.EventFlow
import ifac.td.taxi.ui.model.DispatchNotificationModel
import ifac.td.taxi.ui.model.HomeFragmentModel
import ifac.td.taxi.ui.model.IngenicoResponseModel
import ifac.td.taxi.ui.model.ScrollModeEnum
import ifac.td.taxi.viewmodel.model.CountdownModel
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.interfacom.sdk.taximeter.log.Log as SdkLog

class MainActivityViewModel(
    private val externalBridgeInterface: ExternalBridgeInterface,
    private val licensing: LicensingUseCase,
    private val xmppUseCase: XMPPUseCase,
    private val sessionUseCase: SessionUseCase,
    private val taximeterConnectUseCase: TaximeterConnectUseCase,
    private val lightSkyGlassUseCase: LightSkyGlassUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val portugalUseCase: PortugalUseCase,
    private val showLegalTextUseCase: ShowLegalTextUseCase,
    private val printerUseCase: PrinterUseCase,
    private val countdownManagerUseCase: CountdownManagerUseCase,
    private val dispatchNotificationUseCase: DispatchNotificationUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val sanctionUseCase: SanctionUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val tripUseCase: TripUseCase,
    private val ingenicoUseCase: IngenicoUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val soundManagerUseCase: SoundManagerUseCase,
    private val ttsUseCase: TTSUseCase,
    private val chronometerManagerUseCase: ChronometerManagerUseCase,
    private val deviceUpdateUseCase: DeviceUpdateUseCase,
    private val getButtonMinutesUseCase: GetButtonMinutesUseCase,
    private val messageUseCase: MessageUseCase,
    private val bravoConfigurationDao: BravoConfigurationDao,
//    private val parameters: Parameters,
    private val context: Application,
) : ViewModel() {


    private val TAG = "MainActivityViewModel"

    var permissionBluetoothCallBack: () -> Unit = {}

    private val _hasTaximeterConnectionFlow = MutableStateFlow(false)
    val hasTaximeterConnectionFlow = _hasTaximeterConnectionFlow.asStateFlow()

    private val _navigationFlow = MutableSharedFlow<Int>()
    val navigationFlow = _navigationFlow.asSharedFlow()

    private val _navigationDirectionsFlow = MutableSharedFlow<NavDirections>()
    val navigationDirectionsFlow = _navigationDirectionsFlow.asSharedFlow()

    private val _navigationDeepLinkFlow = MutableSharedFlow<NavDeepLinkRequest>()
    val navigationDeepLinkFlow = _navigationDeepLinkFlow.asSharedFlow()

    private val _navigationBackFlow = MutableSharedFlow<Boolean>()
    val navigationBackFlow = _navigationBackFlow.asSharedFlow()

    private val _pendingNavigationFlow = MutableStateFlow<Boolean?>(null)
    val pendingNavigationFlow = _pendingNavigationFlow.asStateFlow()

    private val _xmppStatusFlow = MutableStateFlow<Int>(0)
    val xmppStatusFlow = _xmppStatusFlow.asStateFlow()

    private val _actionOnBackFlow = MutableStateFlow<(() -> Unit)?>(null)
    val actionOnBackFlow = _actionOnBackFlow.asStateFlow()

    private val _showLegalTextFlow = MutableStateFlow<Boolean?>(null)
    val showLegalTextFlow = _showLegalTextFlow.asStateFlow()

    private val _gpsStatusFlow = MutableStateFlow<Int>(0)
    val gpsStatusFlow = _gpsStatusFlow.asStateFlow()

    private val _isExternalGpsFlow = MutableStateFlow(true)
    var isExternalGpsFlow = _isExternalGpsFlow.asStateFlow()

    private val _progressBarFlow = MutableSharedFlow<Pair<Int, Int>>()
    val progressBarFlow = _progressBarFlow.asSharedFlow()

    private val _correctLoginFlow = MutableSharedFlow<Pair<Boolean, Boolean>>()
    val correctLoginFlow = _correctLoginFlow.asSharedFlow()

    //private val _homeFragmentShareFlow = MutableStateFlow<HomeFragmentModel?>(null)
    //val homeFragmentSharedFlow = _homeFragmentShareFlow.asStateFlow()

    private val _roofLightFlow = MutableStateFlow<Boolean?>(null)
    val roofLightFlow = _roofLightFlow.asStateFlow()

    private val _userLoggedFlow = MutableStateFlow<Boolean?>(null)
    val userLoggedFlow = _userLoggedFlow.asStateFlow()

    private val _clientButtonValueFlow = MutableStateFlow(false)
    val clientButtonValueFlow = _clientButtonValueFlow.asStateFlow()

    // ZONING //

    private val _zoneFlow = MutableStateFlow<String?>(null)
    val zoneFlow = _zoneFlow.asStateFlow()

    //First boolean Location Enabled
    //Second boolean Can Enabled it if disabled
    private val _locationEnabledFlow = MutableStateFlow<Pair<Boolean, Boolean>>(Pair(true, true))
    val locationEnabledFlow = _locationEnabledFlow.asStateFlow()

    private val _locationManuallyDeLocatedFlow = MutableStateFlow<Boolean>(false)
    val locationManuallyDeLocatedFlow = _locationManuallyDeLocatedFlow.asStateFlow()

    private val _hiredZone = MutableStateFlow<Zone?>(null)
    val hiredZone = _hiredZone.asStateFlow()

    private val _locationType = MutableStateFlow<String>("")
    val locationType = _locationType.asStateFlow()

    private val _timeControlFlow = MutableStateFlow<String?>("")
    val timeControlFlow = _timeControlFlow.asStateFlow()

    private val _lastMessageFlow = MutableStateFlow<MessageEntity?>(null)
    val lastMessageFlow = _lastMessageFlow.asStateFlow()

    //////

    private val _currentAmountFlow = MutableStateFlow<Int?>(null)
    val currentAmountFlow = _currentAmountFlow.asStateFlow()

    private val _dispatchFlow = MutableStateFlow<InfoDispatchModel?>(null)
    val dispatchFlow = _dispatchFlow.asStateFlow()

    private val _multiDispatchFlow = MutableStateFlow<ArrayList<InfoDispatchModel>?>(ArrayList())
    val multiDispatchFlow = _multiDispatchFlow.asStateFlow()

    var tripFlow: MutableStateFlow<Trip?> = MutableStateFlow(null)

    //lateinit var shiftStatusFlow: StateFlow<ShiftStatus?>
    val shiftStatusFlow = MutableStateFlow<ShiftStatus?>(null)

    private val _showDialogFlow =
        MutableSharedFlow<Pair<CustomDialog.CustomDialogModel, (CustomDialog.CustomDialogResponse) -> Unit>>()
    val showDialogFlow = _showDialogFlow.asSharedFlow()

    val _loadingStateFlow = MutableSharedFlow<Boolean>()
    val loadingStateFlow: SharedFlow<Boolean> get() = _loadingStateFlow

    private val _printTicketFlow = MutableSharedFlow<Trip>()
    val printTicketFlow = _printTicketFlow.asSharedFlow()

    private val _cropImageFlow = MutableSharedFlow<Uri?>()
    val cropImageFlow = _cropImageFlow.asSharedFlow()

    private val _sumUpFlow = MutableSharedFlow<Int>()
    val sumUpFlow = _sumUpFlow.asSharedFlow()

    private val _sumUpResponseFlow = MutableSharedFlow<Pair<Int, Intent?>>()
    val sumUpResponseFlow = _sumUpResponseFlow.asSharedFlow()

    private val _dispatchMinimumPriceFlow = MutableStateFlow<Int?>(null)
    val dispatchMinimumPriceFlow = _dispatchMinimumPriceFlow.asStateFlow()

    private val _downloadedInitialBravoconfigurationFlow = MutableSharedFlow<Boolean>()
    val downloadedInitialBravoconfigurationFlow =
        _downloadedInitialBravoconfigurationFlow.asSharedFlow()

    private val _taximeterAvailableFlow = MutableSharedFlow<Boolean>()
    val taximeterAvailableFlow = _taximeterAvailableFlow.asSharedFlow()

    private val _locatedOnStop = MutableStateFlow<Boolean>(false)
    val locatedOnStop = _locatedOnStop.asStateFlow()

    private val _reconnectionFlow = MutableStateFlow(false)
    val reconnectionFlow = _reconnectionFlow.asStateFlow()

    private val _eventPendingTrips = MutableSharedFlow<Boolean>()
    val eventPendingTrips = _eventPendingTrips.asSharedFlow()

    val _pendingTripsListFlow = MutableStateFlow<ArrayList<PendingTrip>?>(null)
    val pendingTripsListFlow = _pendingTripsListFlow.asStateFlow()

    private val _inAppPaymentResponseFlow = MutableSharedFlow<Pair<Boolean, String>?>()
    val inAppPaymentResponseFlow = _inAppPaymentResponseFlow.asSharedFlow()

    private val _downloadingBravoConfigurationFlow = MutableStateFlow(false)
    val downloadingBravoConfigurationFlow = _downloadingBravoConfigurationFlow.asStateFlow()

    private val _orangeBtnPendingFlow = MutableStateFlow<Boolean?>(null)
    val orangeBtnPendingFlow = _orangeBtnPendingFlow.asStateFlow()

    private val _isReinforcementFlow = MutableSharedFlow<Boolean?>()
    val isReinforcementFlow = _isReinforcementFlow.asSharedFlow()

    val zoningChangedFlow = EventFlow<Pair<Int, Int>?>()

    private val _shortBreakStatus = MutableStateFlow<ShortBreakStatus?>(null)
    val shortBreakStatus = _shortBreakStatus.asStateFlow()

    private val _customerCallAvailable = MutableStateFlow<Boolean>(false)
    val customerCallAvailable = _customerCallAvailable.asStateFlow()

    private val _customerCallButtonStateAvailable = MutableStateFlow<Boolean>(true)
    val customerCallButtonStateAvailable = _customerCallButtonStateAvailable.asStateFlow()

    private val _keepScreenOnFlow = MutableSharedFlow<Int>()
    val keepScreenOnFlow = _keepScreenOnFlow.asSharedFlow()

    private val _lastITopStateReceived = MutableStateFlow<ITopMeterStatus?>(null)
    val lastITopStateReceived = _lastITopStateReceived.asStateFlow()

    private val _lastITopMeterBreak = MutableStateFlow<Boolean?>(null)
    val lastITopMeterBreak = _lastITopMeterBreak.asStateFlow()

    private val _updateMessageUI = MutableSharedFlow<Boolean>()
    val updateMessageUI = _updateMessageUI.asSharedFlow()

    private val _numericInputFlow = MutableSharedFlow<Triple<Boolean, MutableList<String>, ((CustomFilterDialog.CustomFilterDialogResponse) -> Unit)?>?>()
    val numericInputFlow = _numericInputFlow.asSharedFlow()

    private val _courtesyLightFlow = MutableStateFlow<Boolean?>(null)
    val courtesyLightFlow = _courtesyLightFlow.asStateFlow()

    private val _iTopMeterBreakStatus = MutableStateFlow<Boolean?>(null)
    val iTopMeterBreakStatus = _iTopMeterBreakStatus.asStateFlow()

    private val _lastShortBreakForced = MutableStateFlow<Boolean>(false)
    val lastShortBreakForced = _lastShortBreakForced.asStateFlow()

    private val _closeDialogFlow = MutableSharedFlow<CustomDialog.CustomDialogTAG>()
    val closeDialogFlow = _closeDialogFlow.asSharedFlow()

    private val _ingenicoResponseFlow = MutableSharedFlow<IngenicoResponseModel?>()
    val ingenicoResponseFlow = _ingenicoResponseFlow.asSharedFlow()

    private val _phoneCallErrorFlow = MutableSharedFlow<PhoneCallError?>()
    val phoneCallErrorFlow = _phoneCallErrorFlow.asSharedFlow()

    private val _canMakeCallsFlow = MutableStateFlow<Boolean>(true)
    val canMakeCallsFlow = _canMakeCallsFlow.asStateFlow()

    private val _buttonTimerState = MutableStateFlow<ButtonTimerState?>(null)
    val buttonTimerState = _buttonTimerState.asStateFlow()

    private val _disableDispatchButtonFlow = MutableSharedFlow<Boolean>()
    val disableDispatchButtonFlow = _disableDispatchButtonFlow.asSharedFlow()

    val manualZoningNavigationEventFlow = EventFlow<Boolean>()

    private val _zoningScrollPositionFlow = MutableStateFlow<ScrollModeEnum>(ScrollModeEnum.FOLLOW_SELECTED)
    val zoningScrollPositionFlow = _zoningScrollPositionFlow.asStateFlow()

    private val _taximeterTotalizersFlow = MutableStateFlow<TotalizersRetrievedEvent?>(null)
    val taximeterTotalizersFlow = _taximeterTotalizersFlow.asStateFlow()

    private val _redsysPaymentResultFlow = MutableSharedFlow<MainActivity.RedsysPaymentResult>()
    val redsysPaymentResultFlow = _redsysPaymentResultFlow.asSharedFlow()

    private val _roofLightDelocation = MutableStateFlow(false)
    val roofLightDelocation = _roofLightDelocation.asStateFlow()

    var backPressed = false
    private var streamId: String? = null

    private val _panicButtonAllow = MutableStateFlow<Int>(0x36)

    private var countDownPendingTrips: CountdownModel? = null

    private val homeFragmentModel = HomeFragmentModel()

    val dispatchNotificationModel = DispatchNotificationModel()

    val primeConfigurationFlow = MutableStateFlow<TaximeterPrimeConfiguration?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _isExternalGpsFlow.value = sessionUseCase.getUseExternalGPS() ?: true

            // Settear Flow en LightSkyGlassUseCase
            lightSkyGlassUseCase.setCourtesyLightFlow(_courtesyLightFlow)
            /*
            shiftStatusFlow
                .onEach { shiftStatus ->
                    shiftStatus?.currentStatus?.let {
                        android.util.Log.d("FLOW", "On Each -> From VM: collected -> $it")
                    }
                }.catch {
                    android.util.Log.d("FLOW", "Catched $it")
                }.launchIn(this)
            */

            viewModelScope.launch {
                shiftStatusFlow.collect { shiftStatus ->
                    shiftStatus?.currentStatus?.let {
                        //shiftStatusUpdated(shiftStatus)
                        Logs.d("FLOW", "From VM: collected -> $it")
                    }
                }
            }
        }
    }

    fun initExternalInterface() {
        Taximeter.setExternalInterface(externalBridgeInterface)
        externalBridgeInterface.setViewModel(this)
    }

    fun initUserExternalInterface() {
        viewModelScope.launch(Dispatchers.IO) {
            if (sessionUseCase.isUserLoggedIn()) {
                val session = sessionUseCase.getSession()
                session?.username?.let {
                    externalBridgeInterface.setUsername(it)
                    FirebaseCrashlytics.getInstance().setUserId(it)
                }
                session?.password?.let { externalBridgeInterface.setPassword(it) }
                SdkLog.initialize(true, true, session?.username ?: "")
            } else {
                SdkLog.initialize(true, true, "")
            }
        }
    }

    fun checkUserLogged() {
        viewModelScope.launch {
            _userLoggedFlow.emit(sessionUseCase.isUserLoggedIn())
        }
    }

    fun navigateTo(id: Int) {
        viewModelScope.launch {
            Logs.d("NAV", "navigateTo: $id")
            _navigationFlow.emit(id)
        }
    }

    fun navigateTo(directions: NavDirections) {
        viewModelScope.launch {
            Logs.d("NAV", "navigateTo: ${directions.actionId}")
            _navigationDirectionsFlow.emit(directions)
        }
    }

    fun navigateTo(deepLink: NavDeepLinkRequest) {
        viewModelScope.launch {
            Logs.d("NAV", "navigateTo: ")
            _navigationDeepLinkFlow.emit(deepLink)
        }
    }

    fun savePendingNavigation() {
        viewModelScope.launch {
            _pendingNavigationFlow.emit(true)
        }
    }

    fun clearPendingNavigation() {
        viewModelScope.launch {
            _pendingNavigationFlow.emit(null)
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navigationBackFlow.emit(true)
        }
    }

    fun updateBackCallback(invoked: (() -> Unit)?) {
        viewModelScope.launch {
            _actionOnBackFlow.emit(invoked)
        }
    }

    fun connectXMPP() {
        viewModelScope.launch {
            val accreditation = licensing.getBravoCentralAccreditation()
            if (accreditation != null) {
                xmppUseCase.connectXMPP(accreditation)
            }
        }
    }

    fun getXMPPStatus() {
        viewModelScope.launch {
            _xmppStatusFlow.emit(xmppUseCase.getStatusXMPP())
        }
    }

    fun getGPSStatus() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                viewModelScope.launch {
                    if (isExternalGpsFlow.value) {
                        if (W2CLocation.isLastPositionOk()) {
                            _gpsStatusFlow.emit(StatusGPS.CONNECTED)
                        } else {
                            _gpsStatusFlow.emit(StatusGPS.UNAVAILABLE)
                        }
                    } else {
                        _gpsStatusFlow.emit(W2CLocation.getStatusGPS())
                    }

                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    fun showLegalTextWindow(shouldSkip: Boolean = false) {
        viewModelScope.launch {
            if (!shouldSkip) {
                _showLegalTextFlow.emit(showLegalTextUseCase.showLegalTextAtStart())
            }
        }
    }

    fun roofLightEvent() {
        viewModelScope.launch {
            val isRoofLightOn = Taximeter.getInstance().isRooflightOn
            homeFragmentModel.roofLight = isRoofLightOn
            _roofLightFlow.emit(isRoofLightOn)
            //_homeFragmentShareFlow.emit(homeFragmentModel)
        }
    }

    fun getTaximeterStatus(): Int {
        return homeFragmentModel.taximeterStatus ?: 0
    }

    fun taximeterEvent(state: Int) {
        viewModelScope.launch {
            homeFragmentModel.taximeterStatus = state
            //_homeFragmentShareFlow.emit(homeFragmentModel)
        }
    }

    fun printMessage(header: String?, message: String, paperFeed: Boolean) {
        viewModelScope.launch {
            printerUseCase.printMessage(header, message, paperFeed)
        }
    }

    fun resumeCountdowns() {
        viewModelScope.launch {
            countdownManagerUseCase.resumeRunningCountdowns()
        }
    }

    fun insertUserPreferences() {
        viewModelScope.launch {
            var userPreferences = userPreferencesUseCase.getUserPreferences()
            if (userPreferences == null) {
                userPreferences = UserPreferences()
                userPreferencesUseCase.insertUserPreferences(userPreferences)
            }
        }
    }

    var manualFiltering = CustomFilterDialog.CustomFilterDialogResponse()
    fun checkNumericInput(
        data: MutableList<String>,
        response: ((CustomFilterDialog.CustomFilterDialogResponse) -> Unit)?
    ) {
        viewModelScope.launch {
            val numericInputFilter = userPreferencesUseCase.getUserPreferences()?.numericInputFilter ?: false
            _numericInputFlow.emit(Triple(numericInputFilter, data, response))
        }
    }

    fun updateGpsExternal(isExternal: Boolean) {
        _isExternalGpsFlow.value = isExternal
    }

    fun updateClientButtonFlow(shouldShowButton: Boolean) {
        viewModelScope.launch {
            _clientButtonValueFlow.emit(shouldShowButton)
        }
    }

    fun updateProgressBarFlow(visibility: Int, percentage: Int) {
        viewModelScope.launch {
            Logs.d(
                TAG,
                "updateProgressBarFlow: visibility: $visibility percentage: $percentage"
            )
            _progressBarFlow.emit(Pair(visibility, percentage))
        }
    }

    fun updateLocationEnabledFlow(enabled: Boolean, canEnable: Boolean) {
        viewModelScope.launch {
            _locationEnabledFlow.emit(Pair(enabled, canEnable))
        }
    }

    fun updateLocationManuallyDeLocatedFlow(enabled: Boolean) {
        viewModelScope.launch {
            Logs.d(TAG, "updateLocationManuallyDeLocatedFlow: $enabled")
            _locationManuallyDeLocatedFlow.emit(enabled)
        }
    }

    fun updateCorrectLoginFlow(correct: Pair<Boolean, Boolean>) {
        viewModelScope.launch {
            Logs.d(TAG, "updateCorrectLoginFlow: $correct")
            _correctLoginFlow.emit(correct)
        }
    }

    fun updateTripFlow(trip: Trip?) {
        viewModelScope.launch {
            tripFlow.emit(trip)
        }
    }

    fun resetDispatchFlow() {
        viewModelScope.launch {
            _dispatchFlow.emit(null)
        }
    }

    suspend fun showCurrentTripAmount(trip: Trip) {
        trip.taximeterAmount.let { taximeterAmount ->
            shiftStatusFlow.value?.let { shiftStatus ->
                shiftStatus.currentStatus?.let { currentStatus ->
                    if (shiftStatusUseCase.isHired(currentStatus)) {
                        if (dispatchFlow.value == null) {
                            sendCurrentAmount(taximeterAmount + trip.extras)
                        } else {
                            dispatchFlow.value?.let { dispatch ->
                                dispatch.amount?.let { amount ->
                                    if (dispatch.isConcertedPrice == true) {
                                        sendCurrentAmount(amount)
                                    } else if (dispatch.isConcertedMaximumPrice == true) {
                                        if (taximeterAmount <= amount) {
                                            sendCurrentAmount(taximeterAmount + trip.extras)
                                        } else {
                                            sendCurrentAmount(amount)
                                        }
                                    } else {
                                        sendCurrentAmount(taximeterAmount + trip.extras)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun requestBluetoothPermission() {
        permissionBluetoothCallBack.invoke()
    }

    //FIXME:: ST3-973 -> Comprobar en el USECASE de Despacho
    fun updateDispatchFlow(dispatch: InfoDispatchModel?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (dispatch == null) {
                resetDispatchFlow()
            }

            dispatch?.id?.let { dispatchId ->
                _dispatchFlow.emit(dispatch)
            }
        }
    }

    fun updateMultiDispatchFlow(list: ArrayList<InfoDispatchModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            _multiDispatchFlow.emit(list)
        }
    }

    fun updateTimeControlFlow(value: String) {
        viewModelScope.launch {
            _timeControlFlow.emit(value)
        }
    }


    fun updatePrintTicketFlow(ticket: Trip) {
        viewModelScope.launch {
            _printTicketFlow.emit(ticket)
        }
    }

    fun printTicket(trip: Trip) {
        viewModelScope.launch {
            portugalUseCase.increaseTicketPrinted(trip)
            printerUseCase.printTicket(
                trip,
                paperFeed = true
            )
        }
    }

    fun printTicket(ticket: String, paperFeed: Boolean = true) {
        viewModelScope.launch {
            printerUseCase.printTicket(
                buf = ticket,
                paperFeed = paperFeed
            )
        }
    }

    val updatePrintFlowCallback: (Trip) -> Unit = {
        updatePrintTicketFlow(it)
    }

    fun changeStatus(status: Int) {
        viewModelScope.launch {
            val state =
                shiftStatusUseCase.checkForSubStateModifications(status)
            state?.let { statusWithSubStatus ->
                shiftStatusUseCase.setStatus(statusWithSubStatus, true)
            }
        }
    }


    fun navigateToPaymentFragment(tripId: Long, isManual: Boolean, callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "navigateToPaymentFragment with tripId")
            withContext(Dispatchers.Main) {
                callback.invoke()
            }
            /*
            if (isManual) {
                shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_PAYMENT)
                    ?.let {
                        shiftStatusUseCase.setStatus(it, true)
                    }
            }

             */
            navigateTo(NavGraphDirections.goToPaymentFragment())
        }
    }

    fun updateCropImageFlow(uriContent: Uri) {
        viewModelScope.launch {
            Logs.d(TAG, "updateCropImageFlow: $uriContent")
            _cropImageFlow.emit(uriContent)
        }
    }

    fun openDialog(
        model: CustomDialog.CustomDialogModel,
        callback: ((CustomDialog.CustomDialogResponse) -> Unit),
    ) {
        viewModelScope.launch {
            _showDialogFlow.emit(Pair(model, callback))
        }
    }

    fun saveHiredZone(zone: Zone?) {
        viewModelScope.launch {
            _hiredZone.emit(zone)
        }
    }

    private suspend fun sendCurrentAmount(amount: Int?) {
        amount?.let {
            if (it > 0) {
                _currentAmountFlow.emit(it)
            }
        }
    }

    fun resetCurrentAmountFlow() {
        viewModelScope.launch {
            _currentAmountFlow.emit(0)
        }
    }

    val launchSumUpPayment: (amount: Int) -> Unit = {
        viewModelScope.launch {
            _sumUpFlow.emit(it)
        }
    }

    fun sumUpResponse(resultCode: Int, data: Intent?) {
        viewModelScope.launch {
            _sumUpResponseFlow.emit(Pair(resultCode, data))
        }
    }

    fun stopVibrations() {
        viewModelScope.launch {
            soundManagerUseCase.stopAll()
        }
    }

    fun cancelTTS() {
        viewModelScope.launch {
            ttsUseCase.cancelCurrentSpeech()
        }
    }

    fun saveDispatchMinimumPrice(dispatchMinimumPrice: Int) {
        viewModelScope.launch {
            _dispatchMinimumPriceFlow.value = dispatchMinimumPrice
        }
    }

    fun taximeterTripFinished(tripNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            tripUseCase.taximeterTripFinished(tripNumber)
        }
    }

    fun shiftTimeControl(data: String?) {
        viewModelScope.launch {
            data?.let {
                val time = if (it.contains(",")) {
                    it.substring(0, it.indexOf(",")).toInt()
                } else {
                    it.toInt()
                }
                userPreferencesUseCase.getUserPreferences()?.minutesBeforeWarningId?.let { minutesBeforeWarning ->
                    if (time in 1..minutesBeforeWarning && shiftStatusFlow.value?.currentStatus != ifConstants.STATE_DISCONNECTED) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.end_shift_time, time.toString()),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            soundManagerUseCase.playBeep(ToneGenerator.TONE_CDMA_PIP)
                        }
                    }
                }
            }
        }
    }

    fun downloadedBravoConfiguration() {
        viewModelScope.launch {
            _downloadedInitialBravoconfigurationFlow.emit(true)
        }
    }

    fun askForUserPermissionToUpdateDevice() {
        viewModelScope.launch {
            if (deviceUpdateUseCase.askForUserPermissionToUpdateDevice()) {
                _showDialogFlow.emit(
                    Pair(
                        CustomDialog.CustomDialogModel(
                            title = context.getString(R.string.dialog_update_device_title),
                            buttons = arrayListOf(
                                ButtonType.ACCEPT,
                                ButtonType.CANCEL
                            )
                        ),
                    ) { response ->
                        if (response.buttonPressed == ButtonType.ACCEPT) {
                            deviceUpdateUseCase.setSkyglassUpdateAuthorizedByUser(true)

                        } else {
                            deviceUpdateUseCase.setSkyglassUpdateAuthorizedByUser(false)
                        }
                    }
                )
            }
        }
    }

    fun checkTaximeterAvailability() {
        viewModelScope.launch {
            _taximeterAvailableFlow.emit(bluetoothLocalUseCase.getLocalBluetooth() != null)
        }
    }

    fun beep(beepType: Int) {
        viewModelScope.launch {
            soundManagerUseCase.playBeep(beepType)
        }
    }

    private val _bravoStateFlow = MutableStateFlow(BravoState())
    val bravoStateFlow = _bravoStateFlow.asStateFlow()

    suspend fun updateBravoState(bravoState: BravoState) {
        _bravoStateFlow.emit(bravoState)
    }

    suspend fun checkNoClient() {
        val bravoState = bravoStateFlow.value
        Logs.d(TAG, "checkBravoState Checking Bravo state: $bravoState")

        if ((shiftStatusFlow.value?.currentStatus?.let { shiftStatusUseCase.isHired(it) } == true) && !bravoState.clientButton) {
            Logs.i(TAG, "checkBravoState Condition met, preparing to show dialog")

            val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
                when (it.buttonPressed) {
                    ButtonType.ACCEPT -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            _bravoStateFlow.update { bravoState.copy(clientButton = true) }
                            dispatchFlow.value?.id?.let { dispatch ->
                                dispatchNotificationUseCase.noClient(dispatch)
                            }
                            Logs.d(TAG, "checkBravoState Dialog accepted, Bravo state updated")
                        }
                    }

                    else -> {
                        Logs.d(TAG, "checkBravoState Dialog dismissed or canceled")
                    }
                }
            }

            _showDialogFlow.emit(
                Pair(
                    CustomDialog.CustomDialogModel(
                        title = context.getString(R.string.dialog_cliente_en_taxi_title),
                        buttons = arrayListOf(
                            ButtonType.CANCEL,
                            ButtonType.ACCEPT,
                        ),
                        description = context.getString(R.string.notification_no_client_notification),
                        dialogTAG = CustomDialog.CustomDialogTAG.NO_CLIENT_IN_TAXI
                    ), callback
                )
            )
            Logs.d(TAG, "checkBravoState Dialog emitted")
        } else {
            Logs.d(TAG, "checkBravoState Condition not met, no action taken")
        }
    }

    fun updateLocatedOnStop(it: Boolean) {
        viewModelScope.launch {
            _locatedOnStop.emit(it)
        }
    }

    fun updateLocationType(type: String) {
        viewModelScope.launch {
            _locationType.emit(type)
        }
    }

    fun updateHasTaximeterConnectionFlow(isConnected: Boolean) {
        viewModelScope.launch {
            _hasTaximeterConnectionFlow.emit(isConnected)
        }
    }

    fun saveIsReconnecting(it: ReconnectionState) {
        viewModelScope.launch {
            if (it == ReconnectionState.NO_RECONNECTION) {
                Logs.d(TAG, "Reconnection state changed to NO_RECONNECTION")
                _reconnectionFlow.emit(false)
            } else {
                Logs.d(
                    TAG,
                    "Reconnection state changed to ${it.name}"
                ) // Log the specific reconnection state
                _reconnectionFlow.emit(true)
            }
        }
    }

    fun showTimeDestinationButtons(): Boolean {
        return getButtonMinutesUseCase.showTimeDestinationButtons()
    }

    fun startTimeControlTimer() {
        viewModelScope.launch {
            try {
                Logs.d(TAG, "startTimeControlTimer: Checking user preferences for time control")
                if (userPreferencesUseCase.getUserPreferences()?.useTimeControl == true) {
                    Logs.d(TAG, "startTimeControlTimer: User has time control enabled, starting chronometer")
                    chronometerManagerUseCase.startChronometer(TimeControlChronometer, 60) {
                        Logs.d(TAG, "startTimeControlTimer: Chronometer callback triggered, sending event")
                        Taximeter.getInstance()
                            .sendPerSkyG(SkyGlassEvent.TIME_LEFT, SkyGlassEvent.STANDARD_VALUE)
                    }
                } else {
                    Logs.d(TAG, "startTimeControlTimer: User does not use time control.")
                }
            } catch (e: Exception) {
                Logs.e(TAG, "startTimeControlTimer error: $e")
            }
        }
    }


    fun checkTimeControlTimer() {
        viewModelScope.launch {
            Logs.d(TAG, "checkTimeControlTimer: Checking active chronometers")
            if (!chronometerManagerUseCase.getActiveChronometers().contains(TimeControlChronometer)) {
                Logs.d(TAG, "checkTimeControlTimer: TimeControlChronometer is not active, starting timer")
                startTimeControlTimer()
            } else {
                Logs.d(TAG, "checkTimeControlTimer: TimeControlChronometer is already active")
            }
        }
    }


    private var hasPostedDialog: Boolean = false

    fun txTimeControl(data: String?) {
        viewModelScope.launch {
            Logs.d(TAG, "txTimeControl: Starting function with data = $data")

            val userPreferences = try {
                Logs.d(TAG, "txTimeControl: Getting UserPreferences...")
                userPreferencesUseCase.getUserPreferences().also {
                    Logs.d(TAG, "txTimeControl: User preferences received, useTimeControl: ${it?.useTimeControl}")
                }
            } catch (e: Exception) {
                Logs.e(TAG, "txTimeControl error fetching preferences: $e")
                return@launch
            }

            if (userPreferences == null) {
                Logs.d(TAG, "txTimeControl: userPreferences is null, return")
                return@launch
            }

            if (!userPreferences.useTimeControl || data.isNullOrEmpty()) return@launch

            val frame = data.substringBefore(",")
            Logs.d(TAG, "txTimeControl: Frame = $frame")

            if (frame.isEmpty() || frame.isBlank() || frame.toIntOrNull() == null) return@launch

            val timeLeftInMinutes = tools.myAtoi(frame)
            Logs.d(TAG, "txTimeControl: timeLeftInMinutes = $timeLeftInMinutes")

            val isTimeWarning = timeLeftInMinutes <= userPreferences.minutesBeforeWarningId
            val isLastMinutes = timeLeftInMinutes in 1..userPreferences.minutesBeforeWarningId

            if (!chronometerManagerUseCase.getActiveChronometers()
                    .contains(TimeControlChronometer) && timeLeftInMinutes >= 0 && shiftStatusFlow.value?.currentStatus != ifConstants.STATE_DISCONNECTED
            ) {
                Logs.d(TAG, "txTimeControl: No active chronometers found, starting timer...")
                startTimeControlTimer()
            }

            if (isLastMinutes && shiftStatusFlow.value?.currentStatus != ifConstants.STATE_DISCONNECTED) {
                Logs.d(TAG, "txTimeControl: Playing warning sound...")
                soundManagerUseCase.playBeep(ToneGenerator.TONE_CDMA_PIP)
            }

            if (isTimeWarning && !hasPostedDialog) {
                Logs.d(TAG, "txTimeControl: Showing warning dialog...")

                val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
                    Logs.d(TAG, "txTimeControl: Dialog response = ${it.buttonPressed}")
                    if (it.buttonPressed == ButtonType.ACCEPT) {
                        Logs.d(TAG, "txTimeControl: User accepted the warning.")
                    }
                }

                hasPostedDialog = true
                _showDialogFlow.emit(
                    Pair(
                        CustomDialog.CustomDialogModel(
                            title = context.getString(R.string.warning),
                            description = context.getString(
                                R.string.time_control_desc,
                                userPreferences.minutesBeforeWarningId.toString()
                            ),
                            buttons = arrayListOf(ButtonType.ACCEPT),
                        ), callback
                    )
                )
            }

            val formattedTime = formatMinutesToHourMinute(timeLeftInMinutes)
            Logs.d(TAG, "txTimeControl: Emitting formatted time = $formattedTime")
            _timeControlFlow.emit(formattedTime)
        }
    }


    private fun formatMinutesToHourMinute(minutes: Int): String {
        if (minutes > 0) return String.format("%01d:%02d", minutes / 60, minutes % 60)
        return ""
    }

    fun eventPendingTripsCollected(it: Boolean) {
        viewModelScope.launch {
            _eventPendingTrips.emit(it)
            _orangeBtnPendingFlow.emit(true)
            startPendingTripsTimer()
        }
    }

    fun updateInAppPaymentResponseFlow(response: Pair<Boolean, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            _inAppPaymentResponseFlow.emit(response)
        }
    }

    fun inPayment(): Boolean {
        val currentStatus = shiftStatusFlow.value?.currentStatus ?: return false
        return currentStatus in setOf(
            ifConstants.STATE_PAYMENT,
            ifConstants.STATE_PAYMENT_VACANT,
            ifConstants.STATE_PAYMENT_DISPATCHED,
            ifConstants.STATE_PAYMENT_NO_CENTRAL,
            ifConstants.STATE_PAYMENT_VACANT_DISPATCHED,
            ifConstants.STATE_PAYMENT_VACANT_NO_CENTRAL
        )
    }

    fun updateDownloadingBravoConfigurationFlow(it: Boolean) {
        viewModelScope.launch {
            Logs.d(TAG, "updateDownloadingBravoConfigurationFlow: $it")
            _downloadingBravoConfigurationFlow.emit(it)
        }
    }

    fun startPendingTripsTimer() {
        viewModelScope.launch {
            val timeToAccept = CountdownTimeAmount.TIMEOUT_ORANGE_PENDING_TRIPS.seconds

            countDownPendingTrips = CountdownModel(
                idCountdown = CountdownId.TIMEOUT_ORANGE_PENDING_TRIPS,
                idCallback = CountdownIdCallback.TIMEOUT_ORANGE_PENDING_TRIPS,
                timeInSeconds = timeToAccept
            )

            countDownPendingTrips?.let { countdown ->
                countdownManagerUseCase.createOrUpdateCountdown(countdown)
                countdownManagerUseCase.startCountdown(countdown)
                countdownManagerUseCase.setCountdownFinishCallback(
                    idCountdown = countdown.idCountdown
                ) {
                    viewModelScope.launch(Dispatchers.IO) {
                        _orangeBtnPendingFlow.emit(false)
                        countdownManagerUseCase.deleteCountdownById(
                            CountdownId.TIMEOUT_ORANGE_PENDING_TRIPS
                        )
                    }
                }
            }
        }
    }

    fun finishPendingTripsTimer() {
        viewModelScope.launch {
            Logs.d(TAG, "finishPendingTripsTimer")
            _orangeBtnPendingFlow.emit(false)
            countdownManagerUseCase.stopCountdownById(
                CountdownId.TIMEOUT_ORANGE_PENDING_TRIPS
            )
            countdownManagerUseCase.deleteCountdownById(
                CountdownId.TIMEOUT_ORANGE_PENDING_TRIPS
            )
        }
    }

    suspend fun panicButtonRefresh(value: Int? = null) {
        if (value == null) {
            _panicButtonAllow.value =
                bravoCentralUseCase.getBravoConfiguration()?.allowsPison ?: 0x36
        } else {
            _panicButtonAllow.value = value
        }
        Logs.d(TAG, "panicButtonRefreshFlow: ${_panicButtonAllow.value}")
    }

    fun getPanicButtonAllowed(): Int {
        return _panicButtonAllow.value
    }

    fun checkStartAutomaticLocation(): Boolean {
        licensingUseCase.getLicensingParameters()?.automaticStandLocation?.let {
            return it > 0
        }
        return false
    }

    fun updateSanctionData(response: SanctionDeletionResponse?) {
        viewModelScope.launch {
            if (response != null) {
                sanctionUseCase.setSanctionData(response)
            }
        }
    }

    fun checkIsReinforcement(state: Int) {
        viewModelScope.launch {
            if (state == ifConstants.STATE_FOR_HIRE) {
                _isReinforcementFlow.emit(sessionUseCase.getSession()?.reinforcement)
            }
        }
    }

    fun zoningChanged(it: Pair<Int, Int>?) {
        viewModelScope.launch {
            zoningChangedFlow.eventEmit(it)
        }
    }

    fun onShortBreakStatusChanged(it: ShortBreakStatus?) {
        viewModelScope.launch {
            _shortBreakStatus.emit(it)
        }
    }

    fun saveZoneFlow(it: String) {
        viewModelScope.launch {
            _zoneFlow.emit(it)
        }
    }

    fun updateBackPressed(result: Boolean) {
        backPressed = result
    }

    fun checkCustomerCallAvailable(currentDispatch: InfoDispatchModel?) {
        CoroutineScope(Dispatchers.IO).launch {
            Logs.d(TAG, "checkCustomerCallAvailable currentDispatch: $currentDispatch")
            val customerPhoneNumber = currentDispatch?.customerPhoneNumber
            val customerPhoneNumberExit = currentDispatch?.customerPhoneNumberExit?.toIntOrNull()

            val isCustomerCallAvailable = customerPhoneNumberExit?.let {
                it == 1 || it == 3 || it == 4
            } ?: false

            if (!isCustomerCallAvailable) {
                _customerCallAvailable.emit(false)
                Logs.d(
                    TAG,
                    "checkCustomerCallAvailable: customerPhoneNumberExit is not 1, 3, or 4, emitting false"
                )
                return@launch
            }

            val hasValidPhoneNumber = (customerPhoneNumber?.length ?: 0) > 1
            val isBridgeCallActive = bravoStateFlow.value.bridgeCallState == 2

            val isCallAvailable = hasValidPhoneNumber || isBridgeCallActive
            _customerCallAvailable.emit(isCallAvailable)
            Logs.d(TAG, "checkCustomerCallAvailable: Emitting $isCallAvailable")
            _customerCallButtonStateAvailable.emit(!countdownManagerUseCase.isCountdownRunning(CountdownId.CLIENT_CALL_BUTTON))
            Logs.d(TAG, "_customerCallButtonStateAvailable: Emitting $isCallAvailable")
        }
    }

    fun checkKeepScreenOnConfig() {
        viewModelScope.launch {
            val option = userPreferencesUseCase.getUserPreferences()?.turnOffScreenPosition
            if (option != null) {
                _keepScreenOnFlow.emit(option)
            }
        }
    }



    fun updateMessageUI() {
        viewModelScope.launch {
            _updateMessageUI.emit(true)
        }
    }

    fun updateLastITopState(it: ITopMeterStatus?) {
        viewModelScope.launch {
            _lastITopStateReceived.emit(it)
        }
    }

    fun iTopMeterBreakChange(breakStarted: Boolean) {
        viewModelScope.launch {
            _lastITopMeterBreak.emit(breakStarted)
        }
    }

    fun isVacant(): Boolean {
        val shiftStatus = shiftStatusFlow.value?.currentStatus
        return if (shiftStatus == null) {
            false
        } else {
            shiftStatusUseCase.isVacant(shiftStatus)
        }
    }

    fun currentBluetoothIsITop(): Boolean {
        return bluetoothLocalUseCase.isCurrentBluetoothItop()
    }

    fun changeTextTimeControlTopBar(text: String?) {
        viewModelScope.launch {
            _timeControlFlow.emit(text)
        }
    }

    fun onCourtesyLightChanged(it: Boolean?) {
        viewModelScope.launch {
            Logs.d(TAG, "onCourtesyLightChanged: $it")
            _courtesyLightFlow.emit(it)
        }
    }

    fun statusHandler(data: String?) {
        viewModelScope.launch {
            val lightOn = data?.get(0) == '1'
            val volumeNumberOfDigits = tools.myAtoi(data?.get(1).toString())
            val volume = tools.myAtoi(data?.substring(2, 2 + volumeNumberOfDigits))

            val displayIntensityBegin = 3 + volumeNumberOfDigits
            val displayIntensityNumberOfDigits =
                tools.myAtoi(data?.get(displayIntensityBegin - 1).toString())
            val displayIntensity = tools.myAtoi(
                data?.substring(
                    displayIntensityBegin,
                    displayIntensityBegin + displayIntensityNumberOfDigits
                )
            )

            val configCourtesy = tools.myAtoi(data?.get(data.length - 1).toString())
            Logs.d(TAG, "statusHandler: lightOn: $lightOn, volume: $volume, displayIntensity: $displayIntensity, configCourtesy: $configCourtesy")

            //Esto es mentira, de momento el Skyglass no nos notifica el estado actual del luminoso
            //lightSkyGlassUseCase.setCourtesyLightState(lightOn)
        }
    }

    fun saveITopMeterBreakStatus(status: Boolean?) {
        viewModelScope.launch {
            _iTopMeterBreakStatus.emit(status)
        }
    }

    fun showMessageInDialog() {
        viewModelScope.launch {
            messageUseCase.getLastMessage()?.let { lastMessage ->
                val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                    when (response.buttonPressed) {
                        ButtonType.DELETE -> {
                            stopAutoCloseMessage()
                            val callback: (CustomDialog.CustomDialogResponse) -> Unit =
                                { inDialogResponse ->
                                    when (inDialogResponse.buttonPressed) {
                                        ButtonType.ACCEPT -> {
                                            viewModelScope.launch {
                                                messageUseCase.deleteMessage(lastMessage)
                                            }
                                        }

                                        ButtonType.CANCEL -> {
                                            viewModelScope.launch {
                                                lastMessage.isRead = true
                                                messageUseCase.updateMessage(lastMessage)
                                                _updateMessageUI.emit(true)
                                            }
                                        }

                                        else -> {}
                                    }
                                }

                            openDialog(
                                CustomDialog.CustomDialogModel(
                                    title = context.getString(R.string.dialog_delete_message_title),
                                    description = context.getString(R.string.dialog_delete_message_desc),
                                    buttons = arrayListOf(
                                        ButtonType.CANCEL,
                                        ButtonType.ACCEPT
                                    )
                                ),
                                callback
                            )
                        }

                        ButtonType.ACCEPT -> {
                            stopAutoCloseMessage()
                            // MARCAR COMO LEIDO
                            viewModelScope.launch {
                                lastMessage.isRead = true
                                messageUseCase.updateMessage(lastMessage)
                                _updateMessageUI.emit(true)
                            }
                        }

                        else -> {}
                    }
                }

                openDialog(
                    CustomDialog.CustomDialogModel(
                        title = context.getString(R.string.message_receive),
                        description = lastMessage.text,
                        buttons = arrayListOf(ButtonType.DELETE, ButtonType.ACCEPT),
                        dialogTAG = CustomDialog.CustomDialogTAG.URGENT_MESSAGE_DIALOG,
                    ), callback
                )

                val messageClosureTimes =
                    mapOf(0 to null, 1 to 5, 2 to 10, 3 to 15, 4 to 20, 5 to 25, 6 to 30, 7 to 60)

                val selectedSeconds =
                    messageClosureTimes[userPreferencesUseCase.getUserPreferences()?.secondsToCloseMsgPosition]
                selectedSeconds?.let {
                    startAutoCloseMessageTimer(selectedSeconds)
                }
            }
        }
    }

    private fun startAutoCloseMessageTimer(seconds: Int) {
        viewModelScope.launch {
            val countdown = CountdownModel(
                idCountdown = CountdownId.CLOSE_MESSAGE,
                idCallback = CountdownIdCallback.CLOSE_MESSAGE,
                timeInSeconds = seconds.toLong(),
            )

            countdownManagerUseCase.createOrUpdateCountdown(countdown)
            countdownManagerUseCase.startCountdown(countdown)
            countdownManagerUseCase.setCountdownFinishCallback(
                idCountdown = countdown.idCountdown
            ) {
                viewModelScope.launch(Dispatchers.IO) {
                    _closeDialogFlow.emit(CustomDialog.CustomDialogTAG.URGENT_MESSAGE_DIALOG)
                    countdownManagerUseCase.deleteCountdownById(
                        CountdownId.CLOSE_MESSAGE
                    )
                }
            }
        }
    }

    private fun stopAutoCloseMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            if (countdownManagerUseCase.getCountdownById(CountdownId.CLOSE_MESSAGE) != null) {
                countdownManagerUseCase.stopCountdownById(CountdownId.CLOSE_MESSAGE)
                countdownManagerUseCase.deleteCountdownById(CountdownId.CLOSE_MESSAGE)
            }
        }
    }

    fun setLastShortBreakWasForced(value: Boolean) {
        viewModelScope.launch {
            _lastShortBreakForced.emit(value)
        }
    }

    fun taximeterPrimeConfigReceived(primeConfiguration: TaximeterPrimeConfiguration?) {
        viewModelScope.launch {
            primeConfigurationFlow.emit(primeConfiguration)
        }
    }

    fun taximeterPrimeTripTypeReceived(tripType: Int) {
        /**
         * Cerrar dialogo al recibir tipo de servicio seleccionado desde el taximetro.
         * Flag a false para no volver a mostrar dialogo en pantalla importes
         */
        viewModelScope.launch {
            PrimeManager.showTripTypeSelectionDialog = false
            _closeDialogFlow.emit(CustomDialog.CustomDialogTAG.PRIME_PAYMENT_DIALOG)
        }
    }

    fun getIngenicoIntentData(intent: Intent) {
        viewModelScope.launch(Dispatchers.Main) {
            val ingenicoResponse = ingenicoUseCase.getIntentData(intent)
            Logs.d(TAG, "getIngenicoIntentData: $ingenicoResponse")
            _ingenicoResponseFlow.emit(ingenicoResponse)
        }
    }

    fun updatePhoneCallErrorFlow(it: PhoneCallError?) {
        viewModelScope.launch {
            Logs.d(TAG, "updatePhoneCallErrorFlow: Updating phone call error flow with: $it")
            _phoneCallErrorFlow.emit(it)
        }
    }

    fun updateCanMakeCalls(it: Boolean) {
        viewModelScope.launch {
            Logs.d(TAG, "updateCanMakeCalls: Updating can make calls flow with: $it")
            _canMakeCallsFlow.emit(it)
        }
    }

    fun updateButtomTimerState(btnState: ButtonTimerState?) {
        viewModelScope.launch {
            _buttonTimerState.emit(btnState)
        }
    }

    fun disableReceivedDispatchButtonsFlow(disable: Boolean) {
        viewModelScope.launch {
            _disableDispatchButtonFlow.emit(disable)
        }
    }

    fun emitManualZoningNavigation(isManual: Boolean) {
        viewModelScope.launch {
            manualZoningNavigationEventFlow.eventEmit(isManual)
        }
    }

    fun saveZoningScrollPosition(scrollMode: ScrollModeEnum) {
        viewModelScope.launch {
            _zoningScrollPositionFlow.emit(scrollMode)
        }
    }

    fun sentResultRedsysPayment(result: MainActivity.RedsysPaymentResult) {
        Logs.d(TAG, "sentResultRedsysPayment")
        viewModelScope.launch {
            _redsysPaymentResultFlow.emit(result)
        }
    }

    fun refreshCabId() {
        viewModelScope.launch {
            val vehicleTag = bravoConfigurationDao.getBravoConfiguration()?.vehicleTag
            Logs.d(TAG, "refreshCabId: vehicleTag: $vehicleTag")

            if (!vehicleTag.isNullOrBlank()) {
                Logs.d(TAG, "refreshCabId: setting RemoteLog.setCabId(): $vehicleTag")
                RemoteLog.setCabId(vehicleTag)
            } else {
                Logs.e(TAG, "refreshCabId: No vehicle tag found")
            }
        }
    }

    fun loadTotalizators(totalizersRetrievedEvent: TotalizersRetrievedEvent?) {
        viewModelScope.launch {
            if (totalizersRetrievedEvent != null) {
                _taximeterTotalizersFlow.emit(totalizersRetrievedEvent)
            } else {
                Logs.e(TAG, "loadTotalizators: totalizersRetrievedEvent is null")
            }
        }
    }

    fun setRoofLightDelocation(value: Boolean) {
        viewModelScope.launch {
            _roofLightDelocation.emit(value)
        }
    }
}