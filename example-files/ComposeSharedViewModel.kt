package ifac.td.taxi.compose.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ComposeSharedViewModel(
    application: Application
) : BaseComposeViewModel(application) {

    private val _hasTaximeterConnectionFlow = MutableStateFlow(false)
    val hasTaximeterConnectionFlow = _hasTaximeterConnectionFlow.asStateFlow()

    private val _navigationDirectionsFlow = MutableSharedFlow<NavDirections>()
    val navigationDirectionsFlow = _navigationDirectionsFlow.asSharedFlow()

    private val _navigationDeepLinkFlow = MutableSharedFlow<NavDeepLinkRequest>()
    val navigationDeepLinkFlow = _navigationDeepLinkFlow.asSharedFlow()

    private val _pendingNavigationFlow = MutableStateFlow<Boolean?>(null)
    val pendingNavigationFlow = _pendingNavigationFlow.asStateFlow()

    private val _xmppStatusFlow = MutableStateFlow(0)
    val xmppStatusFlow = _xmppStatusFlow.asStateFlow()

    private val _gpsStatusFlow = MutableStateFlow(0)
    val gpsStatusFlow = _gpsStatusFlow.asStateFlow()

    private val _showLegalTextFlow = MutableStateFlow<Boolean?>(null)
    val showLegalTextFlow = _showLegalTextFlow.asStateFlow()

    private val _roofLightFlow = MutableStateFlow<Boolean?>(null)
    val roofLightFlow = _roofLightFlow.asStateFlow()

    private val _userLoggedFlow = MutableStateFlow<Boolean?>(null)
    val userLoggedFlow = _userLoggedFlow.asStateFlow()

    private val _clientButtonValueFlow = MutableStateFlow(false)
    val clientButtonValueFlow = _clientButtonValueFlow.asStateFlow()

    private val _zoneFlow = MutableStateFlow<String?>(null)
    val zoneFlow = _zoneFlow.asStateFlow()

    private val _locationEnabledFlow = MutableStateFlow(Pair(true, true))
    val locationEnabledFlow = _locationEnabledFlow.asStateFlow()

    private val _locationManuallyDeLocatedFlow = MutableStateFlow(false)
    val locationManuallyDeLocatedFlow = _locationManuallyDeLocatedFlow.asStateFlow()

    private val _locationType = MutableStateFlow("")
    val locationType = _locationType.asStateFlow()

    private val _timeControlFlow = MutableStateFlow<String?>(null)
    val timeControlFlow = _timeControlFlow.asStateFlow()

    private val _currentAmountFlow = MutableStateFlow<Int?>(null)
    val currentAmountFlow = _currentAmountFlow.asStateFlow()

    private val _dispatchFlow = MutableStateFlow<InfoDispatchModel?>(null)
    val dispatchFlow = _dispatchFlow.asStateFlow()

    private val _showDialogFlow = MutableSharedFlow<String>()
    val showDialogFlow = _showDialogFlow.asSharedFlow()

    private val _loadingStateFlow = MutableSharedFlow<Boolean>()
    val loadingStateFlow = _loadingStateFlow.asSharedFlow()

    private val _printTicketFlow = MutableSharedFlow<String>()
    val printTicketFlow = _printTicketFlow.asSharedFlow()

    private val _cropImageFlow = MutableSharedFlow<Uri?>()
    val cropImageFlow = _cropImageFlow.asSharedFlow()

    private val _sumUpFlow = MutableSharedFlow<Int>()
    val sumUpFlow = _sumUpFlow.asSharedFlow()

    private val _sumUpResponseFlow = MutableSharedFlow<Pair<Int, Intent?>>()
    val sumUpResponseFlow = _sumUpResponseFlow.asSharedFlow()

    private val _dispatchMinimumPriceFlow = MutableStateFlow<Int?>(null)
    val dispatchMinimumPriceFlow = _dispatchMinimumPriceFlow.asStateFlow()

    private val _locatedOnStop = MutableStateFlow(false)
    val locatedOnStop = _locatedOnStop.asStateFlow()

    private val _reconnectionFlow = MutableStateFlow(false)
    val reconnectionFlow = _reconnectionFlow.asStateFlow()

    private val _orangeBtnPendingFlow = MutableStateFlow<Boolean?>(null)
    val orangeBtnPendingFlow = _orangeBtnPendingFlow.asStateFlow()

    private val _shortBreakStatus = MutableStateFlow<String?>(null)
    val shortBreakStatus = _shortBreakStatus.asStateFlow()

    private val _customerCallAvailable = MutableStateFlow(false)
    val customerCallAvailable = _customerCallAvailable.asStateFlow()

    private val _customerCallButtonStateAvailable = MutableStateFlow(true)
    val customerCallButtonStateAvailable = _customerCallButtonStateAvailable.asStateFlow()

    private val _lastITopStateReceived = MutableStateFlow<String?>(null)
    val lastITopStateReceived = _lastITopStateReceived.asStateFlow()

    private val _lastITopMeterBreak = MutableStateFlow<Boolean?>(null)
    val lastITopMeterBreak = _lastITopMeterBreak.asStateFlow()

    private val _updateMessageUI = MutableSharedFlow<Boolean>()
    val updateMessageUI = _updateMessageUI.asSharedFlow()

    private val _courtesyLightFlow = MutableStateFlow<Boolean?>(null)
    val courtesyLightFlow = _courtesyLightFlow.asStateFlow()

    private val _canMakeCallsFlow = MutableStateFlow(true)
    val canMakeCallsFlow = _canMakeCallsFlow.asStateFlow()

    private val _buttonTimerState = MutableStateFlow<String?>(null)
    val buttonTimerState = _buttonTimerState.asStateFlow()

    private val _zoningScrollPositionFlow = MutableStateFlow("FOLLOW_SELECTED")
    val zoningScrollPositionFlow = _zoningScrollPositionFlow.asStateFlow()

    private val _roofLightDelocation = MutableStateFlow(false)
    val roofLightDelocation = _roofLightDelocation.asStateFlow()

    private val _panicButtonAllow = MutableStateFlow(0x36)
    val panicButtonAllow = _panicButtonAllow.asStateFlow()

    private val _isExternalGpsFlow = MutableStateFlow(true)
    val isExternalGpsFlow = _isExternalGpsFlow.asStateFlow()

    private val _taximeterTotalizersFlow = MutableStateFlow<String?>(null)
    val taximeterTotalizersFlow = _taximeterTotalizersFlow.asStateFlow()

    private val _redsysPaymentResultFlow = MutableSharedFlow<String>()
    val redsysPaymentResultFlow = _redsysPaymentResultFlow.asSharedFlow()

    var backPressed = false
    var permissionBluetoothCallBack: () -> Unit = {}

    fun updateHasTaximeterConnectionFlow(isConnected: Boolean) {
        viewModelScope.launch { _hasTaximeterConnectionFlow.emit(isConnected) }
    }

    fun navigateTo(directions: NavDirections) {
        viewModelScope.launch { _navigationDirectionsFlow.emit(directions) }
    }

    fun navigateTo(deepLink: NavDeepLinkRequest) {
        viewModelScope.launch { _navigationDeepLinkFlow.emit(deepLink) }
    }

    fun savePendingNavigation() {
        viewModelScope.launch { _pendingNavigationFlow.emit(true) }
    }

    fun clearPendingNavigation() {
        viewModelScope.launch { _pendingNavigationFlow.emit(null) }
    }

    override fun navigateBack() {
        super.navigateBack()
    }

    fun updateBackCallback(invoked: (() -> Unit)?) {
        // In Compose, keep the callback in state if needed
    }

    fun updateGpsExternal(isExternal: Boolean) {
        _isExternalGpsFlow.value = isExternal
    }

    fun updateLocationEnabledFlow(enabled: Boolean, canEnable: Boolean) {
        viewModelScope.launch { _locationEnabledFlow.emit(Pair(enabled, canEnable)) }
    }

    fun updateLocationManuallyDeLocatedFlow(enabled: Boolean) {
        viewModelScope.launch { _locationManuallyDeLocatedFlow.emit(enabled) }
    }

    fun updateClientButtonFlow(shouldShowButton: Boolean) {
        viewModelScope.launch { _clientButtonValueFlow.emit(shouldShowButton) }
    }

    fun updateTimeControlFlow(value: String) {
        viewModelScope.launch { _timeControlFlow.emit(value) }
    }

    fun updateTripFlow(trip: Any?) {
        // Keep as placeholder if you later add your domain Trip model
    }

    fun updateDispatchFlow(dispatch: InfoDispatchModel?) {
        viewModelScope.launch {
            _dispatchFlow.emit(dispatch)
        }
    }

    fun resetDispatchFlow() {
        viewModelScope.launch { _dispatchFlow.emit(null) }
    }

    fun updateCurrentAmount(amount: Int?) {
        viewModelScope.launch { _currentAmountFlow.emit(amount) }
    }

    fun updateLocatedOnStop(value: Boolean) {
        viewModelScope.launch { _locatedOnStop.emit(value) }
    }

    fun updateLocationType(type: String) {
        viewModelScope.launch { _locationType.emit(type) }
    }

    fun updateRoofLightFlow(value: Boolean?) {
        viewModelScope.launch { _roofLightFlow.emit(value) }
    }

    fun updateZoneFlow(value: String) {
        viewModelScope.launch { _zoneFlow.emit(value) }
    }

    fun updateRoofLightDelocation(value: Boolean) {
        viewModelScope.launch { _roofLightDelocation.emit(value) }
    }

    fun updateCanMakeCalls(value: Boolean) {
        viewModelScope.launch { _canMakeCallsFlow.emit(value) }
    }

    fun sentResultRedsysPayment(result: String) {
        viewModelScope.launch { _redsysPaymentResultFlow.emit(result) }
    }

    fun updatePanicButtonAllow(value: Int) {
        viewModelScope.launch { _panicButtonAllow.emit(value) }
    }

    fun updateBackPressed(result: Boolean) {
        backPressed = result
    }

    fun requestBluetoothPermission() {
        permissionBluetoothCallBack.invoke()
    }
}