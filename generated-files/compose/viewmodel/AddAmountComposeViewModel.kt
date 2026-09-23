package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.AddAmountUiState
import ifac.td.taxi.ui.screen.components.AddAmountUiEffect
import ifac.td.taxi.ui.screen.components.AddAmountUiEvent
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 151-2: import androidx.lifecycle.ViewModel
class AddAmountComposeViewModel(
    private val tripUseCase: TripUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val ttsUseCase: TTSUseCase,
) : ViewModel() {
    private val TAG = "AddAmountComposeViewModel"
    private val _uiState = MutableStateFlow(AddAmountUiState())
    val uiState: StateFlow<AddAmountUiState> = _uiState.asStateFlow()
    private val _dialogState = MutableStateFlow<AddAmountDialogState?>(null)
    val dialogState: StateFlow<AddAmountDialogState?> = _dialogState.asStateFlow()
    private val _effects = MutableSharedFlow<AddAmountUiEffect>()
    val effects: SharedFlow<AddAmountUiEffect> = _effects.asSharedFlow()
    private var paymentModifiers: PaymentModifiers = PaymentModifiers()
    private var trip: Trip? = null
    private var originalServiceAmount: Int = 0
    private var originalExtraAmount: Int = 0
    init {
        viewModelScope.launch {
            paymentModifiers = licensingUseCase.getPaymentModifiers()
        }
    }
    fun onEvent(event: AddAmountUiEvent) {
        when (event) {
            is AddAmountUiEvent.Init -> bindTrip(event.trip)
            is AddAmountUiEvent.ServiceAmountChanged -> updateService(event.value)
            is AddAmountUiEvent.ExtraAmountChanged -> updateExtra(event.value)
            is AddAmountUiEvent.TollAmountChanged -> updateToll(event.value)
            is AddAmountUiEvent.TipAmountChanged -> updateTip(event.value)
            AddAmountUiEvent.AcceptClicked -> collectAmount()
            AddAmountUiEvent.CancelClicked -> emitBack()
            AddAmountUiEvent.DismissDialog -> _dialogState.value = null
            AddAmountUiEvent.DialogAccept -> _dialogState.value = null
        }
    }
    fun bindTrip(trip: Trip) {
        this.trip = trip
        val allowTolls = allowsTolls(null)
        val allowTips = allowsTips(null)
        val service = trip.taximeterAmount ?: 0
        val extras = trip.extra1 + trip.extra2 + trip.extra3 + trip.extra4 + (trip.extrasAuto ?: 0)
        val tolls = trip.tolls ?: 0
        val tips = trip.tips ?: 0
        val total = service + extras + tolls + tips
        originalServiceAmount = service
        originalExtraAmount = extras
        _uiState.value = _uiState.value.copy(
            serviceAmount = service,
            extraAmount = extras,
            tollAmount = tolls,
            tipAmount = tips,
            totalAmount = total,
            serviceAmountText = service.toCurrency(),
            extraAmountText = extras.toCurrency(),
            tollAmountText = tolls.toCurrency(),
            tipAmountText = tips.toCurrency(),
            totalAmountText = total.toCurrency(),
            serviceAmountHint = if (service == 0) service.toCurrency() else "",
            extraAmountHint = if (extras == 0) extras.toCurrency() else "",
            tollAmountHint = if (tolls == 0) tolls.toCurrency() else "",
            tipAmountHint = if (tips == 0) tips.toCurrency() else "",
            buttonsState = buildButtonsState(
                trip = trip,
                serviceEnabled = allowsModifyCash(
                    amountIsZero = service == 0,
                    isManual = trip.taximeterTripId == null,
                    isSubscriber = false,
                    isAmountFromDispatch = trip.fromDispatch
                ),
                tollsVisible = allowTolls,
                tipsVisible = allowTips
            )
        )
        checkIfWorksWithoutTx()
        useTTSForAmount(service, extras, tolls, tips, total)
    }
    fun updateService(value: String) {
        val cents = value.toCents()
        val current = _uiState.value
        val updatedExtras = if (originalExtraAmount != 0) {
            val priceModified = current.buttonsState.serviceEnabled && cents != originalServiceAmount
            if (priceModified) 0 else originalExtraAmount
        } else current.extraAmount
        val total = cents + updatedExtras + current.tollAmount + current.tipAmount
        _uiState.value = current.copy(
            serviceAmount = cents,
            extraAmount = updatedExtras,
            totalAmount = total,
            serviceAmountText = value,
            extraAmountText = updatedExtras.toCurrency(),
            totalAmountText = total.toCurrency(),
            buttonsState = current.buttonsState.copy(extraVisible = updatedExtras != 0)
        )
    }
    fun updateExtra(value: String) = recalcFromInputs(extraText = value)
    fun updateToll(value: String) = recalcFromInputs(tollText = value)
    fun updateTip(value: String) = recalcFromInputs(tipText = value)
    fun recalcFromInputs(extraText: String? = null, tollText: String? = null, tipText: String? = null) {
        val current = _uiState.value
        val extra = extraText?.toCents() ?: current.extraAmount
        val toll = tollText?.toCents() ?: current.tollAmount
        val tip = tipText?.toCents() ?: current.tipAmount
        val total = current.serviceAmount + extra + toll + tip
        _uiState.value = current.copy(
            extraAmount = extra,
            tollAmount = toll,
            tipAmount = tip,
            totalAmount = total,
            extraAmountText = extraText ?: extra.toCurrency(),
            tollAmountText = tollText ?: toll.toCurrency(),
            tipAmountText = tipText ?: tip.toCurrency(),
            totalAmountText = total.toCurrency()
        )
    }
    fun collectAmount() {
        val current = _uiState.value
        val trip = trip ?: return
        val maxAmountManual = maximumAmountManual()
        val maxAmountTips = maximumAmountTips()
        val maxAmountTolls = maximumAmountTolls()
        if (maxAmountManual > 0 && ((current.serviceAmount - (trip.taximeterAmount ?: 0)) > maxAmountManual)) {
            showWarning(R.string.toastMaxService)
            return
        }
        if (maxAmountTips > 0 && ((current.tipAmount - (trip.tips ?: 0)) > maxAmountTips)) {
            showWarning(R.string.toastMaxTips)
            return
        }
        if (maxAmountTolls > 0 && ((current.tollAmount - (trip.tolls ?: 0)) > maxAmountTolls)) {
            showWarning(R.string.toastMaxTolls)
            return
        }
        viewModelScope.launch {
            tripUseCase.updateTripAmountsAndExtras(
                trip.id,
                current.serviceAmount,
                current.tollAmount,
                current.tipAmount,
                current.totalAmount,
                current.extraAmount,
                trip.extra1,
                trip.extra2,
                trip.extra3,
                trip.extra4,
                trip.extrasAuto,
                trip.addMinimumPriceDispatch,
                trip.addMinimumPriceAirport
            )
            _effects.emit(AddAmountUiEffect.NavigateBack)
        }
    }
    fun showWarning(messageRes: Int) {
        _dialogState.value = AddAmountDialogState(
            visible = true,
            title = "Warning",
            descriptionRes = messageRes,
            acceptText = "OK"
        )
        viewModelScope.launch {
            _effects.emit(
                AddAmountUiEffect.ShowDialog(
                    _dialogState.value!!
                )
            )
        }
    }
    fun emitBack() {
        viewModelScope.launch { _effects.emit(AddAmountUiEffect.NavigateBack) }
    }
    fun allowsModifyCash(
        amountIsZero: Boolean,
        isManual: Boolean,
        isSubscriber: Boolean,
        isAmountFromDispatch: Boolean
    ): Boolean {
        if (isAmountFromDispatch) return false
        if (isSubscriber && paymentModifiers.isAllowModifySubscriberAmounts) return true
        val withoutProtocol = false
        if (withoutProtocol || isManual) return true
        return when (paymentModifiers.isAllowModifyCash) {
            NOT_ALLOWED -> false
            ONLY_IMPORT_0 -> amountIsZero
            ALWAYS -> true
        }
    }
    fun allowsTips(dispatch: Any?): Boolean = paymentModifiers.isAllowTips
    fun allowsTolls(dispatch: Any?): Boolean = paymentModifiers.isAllowTolls
    fun maximumAmountManual(): Int = paymentModifiers.maxManualAmount
    fun maximumAmountTips(): Int = paymentModifiers.maxTipAmount
    fun maximumAmountTolls(): Int = paymentModifiers.maxTollsAmount
    fun checkIfWorksWithoutTx() {
        viewModelScope.launch {
            val bluetoothResult = bluetoothLocalUseCase.getLocalBluetooth()
            Logs.d(TAG, "worksWithoutTx: ${bluetoothResult == null}")
        }
    }
    fun useTTSForAmount(
        serviceAmountLocal: Int,
        extraAmountLocal: Int,
        tollAmountLocal: Int,
        tipAmountLocal: Int,
        totalAmountLocal: Int
    ) {
        viewModelScope.launch {
            var text = ""
            text += "Service amount ${serviceAmountLocal.toCurrency()}\n"
            if (extraAmountLocal != 0) text += "Extra amount ${extraAmountLocal.toCurrency()}\n"
            if (tollAmountLocal != 0) text += "Toll amount ${tollAmountLocal.toCurrency()}\n"
            if (tipAmountLocal != 0) text += "Tips amount ${tipAmountLocal.toCurrency()}\n"
            if (totalAmountLocal != 0 && totalAmountLocal != serviceAmountLocal) text += "Total amount ${totalAmountLocal.toCurrency()}\n"
            when (userPreferencesUseCase.getUserPreferences()?.blindLocutionId) {
                TTSUseCaseImpl.TTSBlindOption.AUTO.value,
                TTSUseCaseImpl.TTSBlindOption.ONLY_PAYMENT.value -> {
                    ttsUseCase.speakText(text.trim(), TTSType.Amount)
                }
            }
        }
    }
}
fun String.toCents(): Int {
    return replace(".", "")
        .replace(",", "")
        .trim()
        .toIntOrNull() ?: 0
}
