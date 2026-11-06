package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDeepLinkRequest
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.DispatchNotificationUseCase
import ifac.td.taxi.domain.usecase.LocationUseCase
import ifac.td.taxi.domain.usecase.MessageUseCase
import ifac.td.taxi.domain.usecase.RoofLightUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.NavigatorUseCase
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.screen.OnTripFragmentDirections
import ifac.td.taxi.viewmodel.model.FilterOptions
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import ifac.td.taxi.viewmodel.model.LocationInHiredEnum
import ifac.td.taxi.viewmodel.model.MessageUIEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnTripViewModel(
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
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "OnTripViewModel"

    private val _hasMessagesFlow = MutableStateFlow<MessageUIEnum?>(null)
    val hasMessagesFlow = _hasMessagesFlow.asStateFlow()

    private val _isManualFlow = MutableSharedFlow<Boolean>()
    val isManualFlow = _isManualFlow.asSharedFlow()

    private val _noClientButtonFlow = MutableSharedFlow<Boolean?>()
    val noClientButtonFlow = _noClientButtonFlow.asSharedFlow()

    private val _notifyLocationOnHired = MutableStateFlow<Boolean?>(null)
    val notifyLocationOnHired = _notifyLocationOnHired.asStateFlow()

    private val _showEstimateFixedPriceButtonFlow = MutableStateFlow<Boolean?>(null)
    val showEstimateFixedPriceButtonFlow = _showEstimateFixedPriceButtonFlow.asStateFlow()

    private val _navigatorFlow = MutableSharedFlow<Intent?>()
    val navigatorFlow = _navigatorFlow.asSharedFlow()

    private val _roofLightFlow = MutableStateFlow<Pair<Boolean?, Boolean?>?>(null)
    val roofLightFlow = _roofLightFlow.asStateFlow()

    private var lastRoofLightState: Boolean? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val shiftStatus = shiftStatusUseCase.getStatus()
            shiftStatus?.let {
                _isManualFlow.emit(it.isManual && it.currentStatus != ifConstants.STATE_DISPATCHED)
            }
        }

        viewModelScope.launch {
            val url = bravoCentralConfigurationUseCase.getBravoConfigurationVariable()?.fixedPricesStreetTripsURL
            Logs.d(TAG, "url: $url")
            //change to url.notBlank
            _showEstimateFixedPriceButtonFlow.emit(url?.isNotBlank())
            //_showEstimateFixedPriceButtonFlow.emit(true)
        }
    }

    fun getMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = messageUseCase.getMessageByDriverId()
            if (messages.isNullOrEmpty()) {
                _hasMessagesFlow.emit(MessageUIEnum.NO_MESSAGES)
            } else {
                val hasToShowBadge = messages.find { !it.isUrgent && !it.isRead }
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
            navigateTo(R.id.action_onTripFragment_to_messageFragment)
        }
    }

    fun locateOnHired(lastIdMacrozone: Int, lastIdZone: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            locationUseCase.locateOnHired(lastIdMacrozone, lastIdZone)
        }
    }

    fun sendAtTheDoorNotification(dispatch: InfoDispatchModel?) {
        viewModelScope.launch {
            dispatch?.let {
                dispatchNotificationUseCase.atTheDoor(it.id)
            }
        }
    }

    fun sendInCabNotification(dispatch: InfoDispatchModel?) {
        viewModelScope.launch {
            dispatch?.let {
                dispatchNotificationUseCase.inCab(it.id)
            }
        }
    }

    fun navigateToReceiptHistory() {
        viewModelScope.launch {
            //Está -1L porque no se puede mandar null
            navigateTo(OnTripFragmentDirections.actionOnTripFragmentToReceiptHistoryFragment(-1L))
        }
    }

    fun checkNoClientButton() {
        viewModelScope.launch(Dispatchers.IO) {
            val parameters = licensingUseCase.getLicensingParameters()
            Logs.d(TAG, "isNoClientHired: ${parameters?.isNoClientHired}")
            _noClientButtonFlow.emit(parameters?.isNoClientHired)
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

    fun sendNoClientNotification(infoDispatch: Long?) {
        viewModelScope.launch {
            infoDispatch?.let {
                dispatchNotificationUseCase.noClient(it)
            }
        }
    }

    fun openNavigatorApp() {
        viewModelScope.launch {
            navigatorUseCase.openNavigatorApp()?.let {
                _navigatorFlow.emit(it)
            }
        }
    }

    fun navigateToContactCentral() {
        viewModelScope.launch {
            navigateTo(R.id.action_onTripFragment_to_contactCentralFragment)
        }
    }

    fun canGoToHiredManual(taximeterConnected: Boolean): Boolean {
        return licensingUseCase.canDoManualTrips(taximeterConnected)
    }

    fun goToHiredManual(id: Long) {
        viewModelScope.launch {
            shiftStatusUseCase.goToHiredManualFromDispatch(tripId = id)
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

    fun isHired(value: Int?): Boolean {
        if (value == null) {
            return false
        }

        return shiftStatusUseCase.isHired(value)
    }

    fun actionZoningFragment(locatedOnHired: Boolean) {
        viewModelScope.launch {
            licensingUseCase.getLicensingParameters()?.locationInHired?.let {
                if (it == LocationInHiredEnum.WITHOUT_ZONE_OPTION.value) {
                    if (!locatedOnHired) {
                        //locate
                        showDialogWithOutZone()
                    } else {
                        //delocate and set button to default
                        _notifyLocationOnHired.emit(false)
                        sendPositionStatic()
                    }
                } else {
                    openZoningScreen()
                }
            }
        }
    }

    private suspend fun openZoningScreen() {
        val defaultToMacrozone = userPreferencesUseCase.getUserPreferences()?.macroZoneQuery
        val zoneFilter = zoningUseCase.getZoneConfiguration().zoneFilter
        val filterNavigatesToZoning =  zoneFilter == FilterOptions.NEARNESS || zoneFilter == FilterOptions.ID_NO_HIERARCHY ||
                zoneFilter == FilterOptions.HOT_ZONES || zoneFilter == FilterOptions.FAVOURITES


        if (defaultToMacrozone == true && !filterNavigatesToZoning) {
            navigateTo(R.id.action_onTripFragment_to_macroZoning)
        } else if (W2CLocation.getLastIdZone() > 0 || filterNavigatesToZoning) {
            val idMacroZone = if (W2CLocation.getLastIdMacrozone() != 0) {
                W2CLocation.getLastIdMacrozone()
            } else {
                1
            }
            val navDeepLink =
                    NavDeepLinkRequest.Builder.fromUri("android-app://ifac.td.taxi/zoningFragment/$idMacroZone".toUri())
                        .build()
                navigateTo(navDeepLink)
        } else {
                navigateTo(R.id.action_onTripFragment_to_macroZoning)
        }
    }

    private suspend fun showDialogWithOutZone() {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                //locateInHired -> true
                viewModelScope.launch {
                    Logs.d(TAG, "showDialogWithOutZone: ACCEPT -> emit _notifyLocation")
                    _notifyLocationOnHired.emit(true)
                    sendPositionStatic()
                }
            }
        }
        showDialog(
            Pair(
                CustomDialog.CustomDialogModel(
                    title = context.getString(R.string.libre_en_breve),
                    buttons = arrayListOf(
                        ButtonType.ACCEPT,
                        ButtonType.CANCEL
                    )
                ),
                callback
            )
        )
    }

    private fun sendPositionStatic() {
        val EV_TRACKING = 'z'
        W2CLocation.sendEvent(EV_TRACKING, null)
    }

    fun navigateToEstimateFixedPriceScreen() {
        viewModelScope.launch {
            navigateTo(R.id.action_onTripFragment_to_FixedPriceMapFragment)
        }
    }

    fun changeStateToPaymentManual() {
        viewModelScope.launch {
            shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_PAYMENT)
                ?.let {
                    shiftStatusUseCase.setStatus(it, true)
                }
        }
    }

    fun hasITopTaximeterConnected(): Boolean {
        return bluetoothLocalUseCase.isCurrentBluetoothItop()
    }
}