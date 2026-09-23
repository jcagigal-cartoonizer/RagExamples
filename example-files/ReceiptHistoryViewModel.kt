package ifac.td.taxi.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import com.interfacom.sdk.taximeter.licensing.Licensing
import ifac.td.taxi.framework.util.Logs
import es.redsys.paysys.Utils.RedCLSErrorCodes
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.PartialModel.Companion.hasTrips
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.PartialUseCase
import ifac.td.taxi.domain.usecase.PortugalUseCase
import ifac.td.taxi.domain.model.RedSysOperation
import ifac.td.taxi.domain.model.RedSysLoginResponse
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.PrinterUseCase
import ifac.td.taxi.domain.usecase.RedSysUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.TemporalData
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.framework.sdk.usecase.ReceiptUseCase
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.dialog.redSysDialog.RedSysCustomDialog
import ifac.td.taxi.ui.screen.ReceiptHistoryFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReceiptHistoryViewModel(
    context: Application,
    private val receiptUseCase: ReceiptUseCase,
    private val printerUseCase: PrinterUseCase,
    private val tripUseCase: TripUseCase,
    private val partialUseCase: PartialUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val portugalUseCase: PortugalUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val redSysUseCase: RedSysUseCase,
    private val bravoConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val dispatchUseCase: DispatchUseCase,
) : BaseViewModel(context) {

    private val TAG = "ReceiptHistoryViewModel"

    private val _lastReceiptFlow = MutableSharedFlow<Pair<Trip?, Boolean>?>()
    val lastReceiptFlow = _lastReceiptFlow.asSharedFlow()

    private val _hasMoneiInfoCallbackFlow = MutableSharedFlow<Long>()
    val hasMoneiInfoCallbackFlow = _hasMoneiInfoCallbackFlow.asSharedFlow()

    private val _licensingFiscalFlow = MutableStateFlow<Boolean>(false)
    val licensingFiscalFlow = _licensingFiscalFlow.asStateFlow()

    private val _printReceiptFlow = MutableStateFlow<Boolean>(false)
    val printReceiptFlow = _printReceiptFlow.asStateFlow()

    private val _pinPadRedSysEnabledFlow = MutableSharedFlow<Pair<Boolean, Boolean>>()
    val pinPadRedSysEnabledFlow = _pinPadRedSysEnabledFlow.asSharedFlow()

    private val _loginRedSysFlow = MutableSharedFlow<List<RedSysOperation>?>()
    val loginRedSysFlow = _loginRedSysFlow.asSharedFlow()

    private val _voucherConfigurationFlow = MutableStateFlow<Boolean>(false)
    val voucherConfigurationFlow = _voucherConfigurationFlow.asStateFlow()

    private val _longDispatchFlow = MutableSharedFlow<String?>()
    val longDispatchFlow = _longDispatchFlow.asSharedFlow()

    private val _fromHistoryFragmentFlow = MutableStateFlow<Boolean>(false)
    val fromHistoryFragmentFlow = _fromHistoryFragmentFlow.asStateFlow()

    private val _stateNavigateFlow = MutableStateFlow<Int>(0)
    val stateNavigateFlow = _stateNavigateFlow.asStateFlow()

    private val _invoiceConfig = MutableStateFlow(InvoiceConfig())
    val invoiceConfig = _invoiceConfig.asStateFlow()

    data class InvoiceConfig(
        val isInvoiceEnabled: Boolean = false,
        val isShowInvoiceButton: Boolean = false
    )

    fun loadLicensingInvoice() {
        val params = Licensing.getParameters()

        _invoiceConfig.value = InvoiceConfig(
            isInvoiceEnabled = params?.isInvoiceEnabled ?: false,
            isShowInvoiceButton = params?.isShowInvoiceButton ?: false
        )
    }

    fun getTicket(tripId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "getTicket: tripId = $tripId")
            _fromHistoryFragmentFlow.value = tripId >= 0

            val lastFinishedTrip = tripUseCase.getLastTripFinished()
            Logs.d(TAG, "getTicket: lastFinishedTrip = $lastFinishedTrip")

            val trip = if (tripId >= 0) {
                tripUseCase.getTripById(tripId).also {
                    Logs.d(TAG, "getTicket: trip = $it")
                }
            } else {
                lastFinishedTrip.also {
                    Logs.d(TAG, "getTicket: Using lastFinishedTrip = $it")
                }
            }

            val isLastTrip = (trip != null && trip.id == lastFinishedTrip?.id)
            Logs.d(TAG, "getTicket: isLastTrip = $isLastTrip")

            _lastReceiptFlow.emit(Pair(trip, isLastTrip))
            Logs.d(TAG, "getTicket: Emitted Pair(trip, isLastTrip)")
        }
    }

    fun checkNavigateWithStatus(currentStatus: Int?) {
        viewModelScope.launch {
            when (currentStatus) {
                ifConstants.STATE_FOR_HIRE,
                ifConstants.STATE_FOR_HIRE_NO_CENTRAL -> {
                    _stateNavigateFlow.value = 1
                }

                ifConstants.STATE_DISPATCHED,
                ifConstants.STATE_HIRED,
                ifConstants.STATE_HIRED_NO_CENTRAL,
                ifConstants.STATE_HIRED_DISPATCHED -> {
                    _stateNavigateFlow.value = 2
                }

                else -> {
                    _stateNavigateFlow.value = 0
                }
            }
        }
    }

    fun printActualTicket(
        trip: Trip
    ) {
        viewModelScope.launch {
            if (licensingFiscalFlow.value) {
                portugalUseCase.increaseTicketPrinted(trip)
                Logs.d(TAG, "printActualTicket: increasing fiscal service ticket printed count")
            }

            Logs.d(TAG, "printActualTicket: printing ticket for tripId = ${trip.id}")
            printerUseCase.printTicket(trip, true)
        }
    }

    fun goToOnlineInvoiceFragment(tripId: Long) {
        viewModelScope.launch {
            navigateTo(
                ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToOnlineInvoiceFragment(
                    tripId
                )
            )
        }
    }

    fun hasMoneiInfo(trip: Trip) {
        viewModelScope.launch() {
            if (!trip.moneiPaymentId.isNullOrBlank()) {
                _hasMoneiInfoCallbackFlow.emit(trip.id)
            } else {
                _hasMoneiInfoCallbackFlow.emit(-1)
            }
        }
    }

    fun checkFiscalLicensing() {
        viewModelScope.launch(Dispatchers.IO) {
            val fiscal = licensingUseCase.getLicensingParameters()?.isFiscalService ?: false
            Logs.d(TAG, "checkLicensing isFiscalService $fiscal")
            _licensingFiscalFlow.emit(fiscal)
        }
    }

    fun isPrintableTicked(trip: Trip?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (trip != null) {
                val isLastTicket = trip.id == tripUseCase.getLastTripFinished()?.id
                Logs.d(TAG, "isPrintableTicked: isLastTicket = $isLastTicket")

                val shouldPrint = portugalUseCase.shouldPrintTicket(trip, isLastTicket, licensingFiscalFlow.value)
                _printReceiptFlow.emit(shouldPrint)
            } else {
                Logs.d(TAG, "isPrintableTicked: trip is null, disabling print")
                _printReceiptFlow.emit(false)
            }
        }
    }

    fun clickPartials() {
        viewModelScope.launch {
            val activePartial = partialUseCase.getActivePartial()
            if (activePartial != null && activePartial.hasTrips()) {
                Logs.d(TAG, "Navigating to active partials")
                navigateTo(R.id.action_receiptHistoryFragment_to_partialsFragment)
            } else {
                navigateTo(R.id.action_receiptHistoryFragment_to_closedPartialFragment)
            }
        }
    }

    fun checkPinPadRedSysEnabled() {
        viewModelScope.launch {
            val userPreferences = userPreferencesUseCase.getUserPreferences()
            val serialNumber = userPreferences?.pinPadSerialNumber

            val usernameRedSys = redSysUseCase.getUsernameRedSys()
            val passwordRedSys = TemporalData.passwordRedSys
            //PinPadRedsys.MIN_LEN_SERIAL_NUMBER = 6
            _pinPadRedSysEnabledFlow.emit(
                Pair(
                    ((serialNumber != null) && (serialNumber.length >= 6) && userPreferences.pinPadTypeId == 2),
                    (!usernameRedSys.isNullOrEmpty() && passwordRedSys.isNotEmpty())
                )
            ) // 2 -> RedSys
        }
    }

    fun loginRedSys(user: String?, password: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            redSysUseCase.configureRedSys()

            val usernameRedSys = user ?: redSysUseCase.getUsernameRedSys()
            val passwordRedSys = password ?: TemporalData.passwordRedSys

            usernameRedSys?.let { user ->
                val loginResponse = redSysUseCase.loginRedSys(username = user, password = passwordRedSys)

                when (loginResponse) {
                    is RedSysLoginResponse.Success -> {
                        val array = loginResponse.merchantList
                        TemporalData.passwordRedSys = passwordRedSys
                        val consultaResponse = redSysUseCase.consultaRedSys(array[0].terminalList[0], null)
                        Logs.d(TAG, "loginRedSys: consultaResponse = $consultaResponse")
                        _loginRedSysFlow.emit(redSysUseCase.parseConsulta(consultaResponse))
                    }

                    is RedSysLoginResponse.Error ->  {
                        Logs.d(TAG, "loginRedSys: Login RedSys failed or returned empty array = ${loginResponse.errorMessage}")

                        val result = RedCLSErrorCodes.getExceptionFromCode(loginResponse.errorCode, null)
                        Logs.d(TAG, "loginRedSys: ${result.message}")
                        Logs.d(TAG, "loginRedSys: ${result.msgReturn}")

                        showDialog(
                            Pair(
                                CustomDialog.CustomDialogModel(
                                    title = context.getString(R.string.red_sys_title),
                                    description = result?.message,
                                    buttons = arrayListOf(ButtonType.ACCEPT)
                                ),
                            ) { response ->
                                if (response.buttonPressed == ButtonType.ACCEPT) {
                                    viewModelScope.launch(Dispatchers.IO) {
                                        if (loginResponse.errorCode == RedCLSErrorCodes.STATUS_KO_FORMATO_RESP_LOGIN_PWD_CAD) {
                                            navigateTo(R.id.action_receiptHistoryFragment_to_changePasswordRedSysFragment2)
                                        }
                                    }
                                }
                            }
                        )

                        _loginRedSysFlow.emit(null)
                    }
                }
            }
        }
    }

    fun checkVoucherConfiguration() {
        viewModelScope.launch {
            bravoConfigurationUseCase.getBravoConfigurationVariable()?.let { config ->
                val tripReceipt = config.tripReceipt == "1"
                val subscriberVouchers = config.subscriberVouchers != "0"
                Logs.d(TAG, "checkVoucherConfiguration: $tripReceipt, $subscriberVouchers")
                _voucherConfigurationFlow.emit(tripReceipt || subscriberVouchers)
            }
        }
    }

    fun gotoOfflineInvoiceFragment(tripId: Long) {
        viewModelScope.launch {
            navigateTo(
                ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToOfflineInvoiceFragment(
                    tripId
                )
            )
        }
    }

     fun sendPhotoVoucher(id: Long) {
         viewModelScope.launch {
             val longDispatchNumber = dispatchUseCase.getDispatchByTripId(id)?.longDispatchNumber
             Logs.d(TAG, "sendPhotoVoucher: $longDispatchNumber")
             _longDispatchFlow.emit(longDispatchNumber)
         }
    }

    fun checkCardInfo(trip: Trip?) {
        viewModelScope.launch {
            if (trip == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.resources.getString(R.string.error_no_trip_loaded), Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val isLastTrip = trip.id == tripUseCase.getLastTripFinished()?.id
            if (isLastTrip) {
                hasMoneiInfo(trip)
            } else {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, context.resources.getString(R.string.warning_past_trip_refund), Toast.LENGTH_SHORT).show()
//                }
                checkPinPadRedSysEnabled()
            }
        }
    }

    fun showRedSysCustomDialog(unit: ((RedSysCustomDialog.RedSysCustomDialogResponse) -> Unit, String?, String?) -> Unit) {
        viewModelScope.launch {
            val callback: (RedSysCustomDialog.RedSysCustomDialogResponse) -> Unit = { response ->
                when (response.buttonPressed) {
                    ButtonType.ACCEPT -> {
                        viewModelScope.launch {
                            val user = response.etRedSysUsername
                            val password = response.etRedSysPassword

                            if (!user.isNullOrEmpty() && !password.isNullOrEmpty()) {
                                loginRedSys(user, password)
                            } else {
                                withContext(Dispatchers.Main) {
        //                            iMainActivity.showToast(it.getString(R.string.toast_complete_todos_los_campos))
                                    showDialog(
                                        Pair(
                                            CustomDialog.CustomDialogModel(
                                                title = context.getString(R.string.red_sys_title),
                                                description = context.getString(R.string.toast_complete_todos_los_campos),
                                                buttons = arrayListOf(ButtonType.ACCEPT)
                                            )
                                        ) {}
                                    )
                                }
                            }
                        }
                    }

                    ButtonType.CANCEL -> {}

                    else -> {}
                }
            }

            val usernameRedSys = redSysUseCase.getUsernameRedSys()
            val savedPasswordRedSys = TemporalData.passwordRedSys
            unit.invoke(callback, usernameRedSys, savedPasswordRedSys)
        }
    }

}
