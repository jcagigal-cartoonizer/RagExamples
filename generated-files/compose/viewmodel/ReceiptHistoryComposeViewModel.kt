package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.ReceiptHistoryButtonsState
import ifac.td.taxi.ui.screen.components.ReceiptHistoryUiState
import ifac.td.taxi.ui.screen.components.ReceiptHistoryUiEffect
import ifac.td.taxi.ui.screen.components.ReceiptHistoryUiEvent
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 368-7: import android.app.Application
class ReceiptHistoryComposeViewModel(
    application: Application,
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
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ReceiptHistoryUiState())
    val uiState: StateFlow<ReceiptHistoryUiState> = _uiState.asStateFlow()
    private val _buttonsState = MutableStateFlow(ReceiptHistoryButtonsState())
    val buttonsState: StateFlow<ReceiptHistoryButtonsState> = _buttonsState.asStateFlow()
    private val _dialogState = MutableStateFlow(ReceiptHistoryDialogState())
    val dialogState: StateFlow<ReceiptHistoryDialogState> = _dialogState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ReceiptHistoryUiEffect>()
    val uiEffect: SharedFlow<ReceiptHistoryUiEffect> = _uiEffect.asSharedFlow()
    private var currentTrip: Trip? = null
    fun onScreenStarted(ticketId: Long) {
        viewModelScope.launch {
            checkNavigateWithStatus(null)
            getTicket(ticketId)
            checkVoucherConfiguration()
            checkFiscalLicensing()
            loadLicensingInvoice()
        }
    }
    fun onEvent(event: ReceiptHistoryUiEvent) {
        when (event) {
            ReceiptHistoryUiEvent.ScreenStarted -> Unit
            ReceiptHistoryUiEvent.PrintClicked -> currentTrip?.let { printActualTicket(it) } ?: emitToast(R.string.error_no_trip_loaded)
            ReceiptHistoryUiEvent.PreviousClicked -> emitEffect(ReceiptHistoryUiEffect.NavigateToTripHistory)
            ReceiptHistoryUiEvent.PartialsClicked -> clickPartials()
            ReceiptHistoryUiEvent.CardClicked -> currentTrip?.let { checkCardInfo(it) } ?: emitToast(R.string.error_no_trip_loaded)
            ReceiptHistoryUiEvent.BillClicked -> onInvoiceClick()
            ReceiptHistoryUiEvent.PhotoClicked -> showPhotoDialog()
            ReceiptHistoryUiEvent.DialogDismissed -> _dialogState.value = _dialogState.value.copy(visible = false)
            is ReceiptHistoryUiEvent.DialogButtonClicked -> handleDialogButton(event.button)
        }
    }
    fun openDialog(state: ReceiptHistoryDialogState) {
        _dialogState.value = state.copy(visible = true)
    }
    fun handleDialogButton(button: ButtonType) {
        _dialogState.value = _dialogState.value.copy(visible = false)
    }
    fun emitToast(resId: Int) = viewModelScope.launch { _uiEffect.emit(ReceiptHistoryUiEffect.ToastRes(resId)) }
    fun emitEffect(effect: ReceiptHistoryUiEffect) = viewModelScope.launch { _uiEffect.emit(effect) }
    fun loadLicensingInvoice() {
        val params = com.interfacom.sdk.taximeter.licensing.Licensing.getParameters()
        val invoiceConfig = InvoiceConfig(
            isInvoiceEnabled = params?.isInvoiceEnabled ?: false,
            isShowInvoiceButton = params?.isShowInvoiceButton ?: false
        )
        _uiState.value = _uiState.value.copy(invoiceConfig = invoiceConfig)
    }
    fun getTicket(tripId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val lastFinishedTrip = tripUseCase.getLastTripFinished()
            val trip = if (tripId >= 0) tripUseCase.getTripById(tripId) else lastFinishedTrip
            currentTrip = trip
            val isLastTrip = trip != null && trip.id == lastFinishedTrip?.id
            _uiState.update {
                it.copy(
                    trip = trip,
                    isLastTrip = isLastTrip,
                    isReceiptEmpty = trip == null
                )
            }
            recomputeButtons()
        }
    }
    fun checkNavigateWithStatus(currentStatus: Int?) {
        _uiState.update { it }
    }
    fun checkFiscalLicensing() {
        viewModelScope.launch(Dispatchers.IO) {
            val fiscal = licensingUseCase.getLicensingParameters()?.isFiscalService ?: false
            _uiState.update { it.copy(licensingFiscal = fiscal) }
            recomputeButtons()
        }
    }
    fun checkVoucherConfiguration() {
        viewModelScope.launch {
            bravoConfigurationUseCase.getBravoConfigurationVariable()?.let { config ->
                val enabled = config.tripReceipt == "1" || config.subscriberVouchers != "0"
                recomputeButtons(voucherEnabled = enabled)
            }
        }
    }
    fun printActualTicket(trip: Trip) {
        viewModelScope.launch {
            if (_uiState.value.licensingFiscal) portugalUseCase.increaseTicketPrinted(trip)
            printerUseCase.printTicket(trip, true)
        }
    }
    fun clickPartials() {
        viewModelScope.launch {
            val activePartial = partialUseCase.getActivePartial()
            if (activePartial != null && activePartial.hasTrips()) {
                emitEffect(ReceiptHistoryUiEffect.NavigateToTripHistory)
            } else {
                emitEffect(ReceiptHistoryUiEffect.NavigateToTripHistory)
            }
        }
    }
    fun checkPinPadRedSysEnabled() {
        viewModelScope.launch {
            val prefs = userPreferencesUseCase.getUserPreferences()
            val serialNumber = prefs?.pinPadSerialNumber
            val username = redSysUseCase.getUsernameRedSys()
            val password = TemporalData.passwordRedSys
            val requirementOK = (serialNumber != null && serialNumber.length >= 6 && prefs.pinPadTypeId == 2)
            val userSaved = !username.isNullOrEmpty() && password.isNotEmpty()
            if (requirementOK && userSaved) {
                loginRedSys(null, null)
            } else if (requirementOK) {
                _uiEffect.emit(
                    ReceiptHistoryUiEffect.ShowDialog(
                        ReceiptHistoryDialogState(
                            visible = true,
                            title = getApplication<Application>().getString(R.string.red_sys_title),
                            description = getApplication<Application>().getString(R.string.red_sys_description),
                            isRedSysDialog = true,
                            redSysUsername = username,
                            redSysPassword = password,
                            buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                        )
                    )
                )
            } else {
                emitToast(R.string.not_match_requirements)
            }
        }
    }
    fun loginRedSys(user: String?, password: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            redSysUseCase.configureRedSys()
            val usernameRedSys = user ?: redSysUseCase.getUsernameRedSys()
            val passwordRedSys = password ?: TemporalData.passwordRedSys
            if (usernameRedSys != null) {
                when (val response = redSysUseCase.loginRedSys(usernameRedSys, passwordRedSys)) {
                    is ifac.td.taxi.domain.model.RedSysLoginResponse.Success -> {
                        TemporalData.passwordRedSys = passwordRedSys
                        val consulta = redSysUseCase.consultaRedSys(response.merchantList[0].terminalList[0], null)
                        val ops = redSysUseCase.parseConsulta(consulta)
                        emitEffect(ReceiptHistoryUiEffect.NavigateToReceiptRedSys(ops.toTypedArray()))
                    }
                    is ifac.td.taxi.domain.model.RedSysLoginResponse.Error -> {
                        emitToast(R.string.red_sys_login_error)
                    }
                }
            }
        }
    }
    fun checkCardInfo(trip: Trip?) {
        viewModelScope.launch {
            if (trip == null) {
                emitToast(R.string.error_no_trip_loaded)
                return@launch
            }
            val isLastTrip = trip.id == tripUseCase.getLastTripFinished()?.id
            if (isLastTrip) {
                hasMoneiInfo(trip)
            } else {
                checkPinPadRedSysEnabled()
            }
        }
    }
    fun hasMoneiInfo(trip: Trip) {
        viewModelScope.launch {
            if (!trip.moneiPaymentId.isNullOrBlank()) {
                emitEffect(ReceiptHistoryUiEffect.NavigateToRefundMonei(trip.id))
            } else {
                checkPinPadRedSysEnabled()
            }
        }
    }
    fun gotoOfflineInvoiceFragment(tripId: Long) {
        emitEffect(ReceiptHistoryUiEffect.NavigateToOfflineInvoice(tripId))
    }
    fun goToOnlineInvoiceFragment(tripId: Long) {
        emitEffect(ReceiptHistoryUiEffect.NavigateToOnlineInvoice(tripId))
    }
    fun sendPhotoVoucher(id: Long) {
        viewModelScope.launch {
            val dispatch = dispatchUseCase.getDispatchByTripId(id)?.longDispatchNumber
            if (dispatch != null) emitEffect(ReceiptHistoryUiEffect.NavigateToCropImage(dispatch))
        }
    }
    fun onInvoiceClick() {
        val trip = currentTrip ?: run {
            emitToast(R.string.error_no_trip_loaded)
            return
        }
        if (!canGenerateInvoice(trip)) {
            emitToast(R.string.cannot_generate_invoice)
            return
        }
        val invoiceConfig = _uiState.value.invoiceConfig
        when {
            invoiceConfig.isInvoiceEnabled && invoiceConfig.isShowInvoiceButton -> {
                _dialogState.value = ReceiptHistoryDialogState(
                    visible = true,
                    title = getApplication<Application>().getString(R.string.dialog_invoice_options_title),
                    description = getApplication<Application>().getString(R.string.dialog_invoice_options_desc),
                    buttons = listOf(ButtonType.EMAIL, ButtonType.IMPRIMIR)
                )
            }
            invoiceConfig.isInvoiceEnabled -> goToOnlineInvoiceFragment(trip.id)
            invoiceConfig.isShowInvoiceButton -> gotoOfflineInvoiceFragment(trip.id)
        }
    }
    fun showPhotoDialog() {
        val trip = currentTrip ?: run {
            emitToast(R.string.error_no_trip_loaded)
            return
        }
        _dialogState.value = ReceiptHistoryDialogState(
            visible = true,
            title = getApplication<Application>().getString(R.string.warning),
            description = getApplication<Application>().getString(R.string.subscriber_photo_confirmation),
            buttons = listOf(ButtonType.IMPRIMIR, ButtonType.ACCEPT)
        )
    }
    fun canGenerateInvoice(trip: Trip): Boolean {
        if (_uiState.value.licensingFiscal) {
            if (trip.id != tripUseCase.getLastTripFinished()?.id) return false
        }
        if (trip.invoiceAlreadyGenerated) return false
        return (_uiState.value.invoiceConfig.isInvoiceEnabled || _uiState.value.invoiceConfig.isShowInvoiceButton) &&
            trip.totalAmount > 0 &&
            trip.paymentMethod != PaymentMethod.SUBSCRIBER.paymentString
    }
    fun recomputeButtons(voucherEnabled: Boolean = _uiState.value.invoiceConfig.isShowInvoiceButton) {
        val trip = currentTrip
        val isSubscriberAndFromDispatch = trip?.paymentMethod == PaymentMethod.SUBSCRIBER.paymentString && trip.fromDispatch == true
        val canInvoice = trip != null && canGenerateInvoice(trip)
        val shouldPrint = trip != null && !_uiState.value.isReceiptEmpty
        _buttonsState.value = ReceiptHistoryButtonsState.from(
            trip = trip,
            isReceiptEmpty = _uiState.value.isReceiptEmpty,
            canGenerateInvoice = canInvoice,
            isVoucherEnabled = voucherEnabled,
            isSubscriberAndFromDispatch = isSubscriberAndFromDispatch,
            shouldPrint = shouldPrint
        )
    }
}
