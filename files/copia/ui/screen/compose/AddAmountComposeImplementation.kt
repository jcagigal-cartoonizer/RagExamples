package ifac.td.taxi.ui.screen.compose

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import ifac.td.taxi.R
import ifac.td.taxi.compose.viewmodel.ComposeSharedViewModel
import ifac.td.taxi.domain.model.PaymentModifiers
import ifac.td.taxi.domain.model.PaymentModifiers.ModifyImports.ALWAYS
import ifac.td.taxi.domain.model.PaymentModifiers.ModifyImports.NOT_ALLOWED
import ifac.td.taxi.domain.model.PaymentModifiers.ModifyImports.ONLY_IMPORT_0
import ifac.td.taxi.domain.model.TTSType
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.NO_SUBSCRIBER
import ifac.td.taxi.domain.usecase.TTSUseCase
import ifac.td.taxi.domain.usecase.TTSUseCaseImpl
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toCurrency
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toMoneyValue
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ------------------------------------------------------------
// EFFECTS / STATE
// ------------------------------------------------------------

data class AddAmountDialogState(
    val title: String,
    val description: String,
    val acceptText: String = "OK"
)

sealed interface AddAmountUiEffect {
    data object NavigateBack : AddAmountUiEffect
    data class ShowDialog(val dialog: AddAmountDialogState) : AddAmountUiEffect
}

sealed interface AddAmountUiEvent {
    data class ServiceAmountChanged(val value: String) : AddAmountUiEvent
    data class ExtraAmountChanged(val value: String) : AddAmountUiEvent
    data class TollAmountChanged(val value: String) : AddAmountUiEvent
    data class TipAmountChanged(val value: String) : AddAmountUiEvent

    data object ServiceAmountDone : AddAmountUiEvent
    data object ExtraAmountDone : AddAmountUiEvent
    data object TollAmountDone : AddAmountUiEvent
    data object TipAmountDone : AddAmountUiEvent

    data object ServiceAmountFocusLost : AddAmountUiEvent
    data object ExtraAmountFocusLost : AddAmountUiEvent
    data object TollAmountFocusLost : AddAmountUiEvent
    data object TipAmountFocusLost : AddAmountUiEvent

    data object AcceptClicked : AddAmountUiEvent
    data object CancelClicked : AddAmountUiEvent
    data object DialogAccepted : AddAmountUiEvent
}

data class AddAmountButtonsState(
    val acceptVisible: Boolean = true,
    val cancelVisible: Boolean = true,
    val acceptEnabled: Boolean = true,
    val cancelEnabled: Boolean = true,
    val acceptContainerColor: Color = Color(0xFF1E88E5),
    val acceptContentColor: Color = Color.White,
    val acceptDisabledContainerColor: Color = Color(0xFFBDBDBD),
    val acceptDisabledContentColor: Color = Color.White,
    val cancelContainerColor: Color = Color.Transparent,
    val cancelContentColor: Color = Color(0xFF1E88E5),
    val cancelDisabledContainerColor: Color = Color.Transparent,
    val cancelDisabledContentColor: Color = Color(0xFFBDBDBD),
    val acceptText: String = "Accept",
    val cancelText: String = "Cancel",
    val showDivider: Boolean = false,
    val showLoading: Boolean = false
)

data class AddAmountUiState(
    val isLoading: Boolean = false,
    val tripId: Long? = null,
    val fromDispatch: Boolean = false,
    val isSubscriber: Boolean = false,
    val isAmountFromDispatch: Boolean = false,

    val serviceAmountCents: Int = 0,
    val extraAmountCents: Int = 0,
    val tollAmountCents: Int = 0,
    val tipAmountCents: Int = 0,
    val totalAmountCents: Int = 0,

    val originalServiceAmountCents: Int = 0,
    val originalExtraAmountCents: Int = 0,

    val serviceAmountText: String = "",
    val extraAmountText: String = "",
    val tollAmountText: String = "",
    val tipAmountText: String = "",

    val serviceAmountHint: String = "",
    val extraAmountHint: String = "",
    val tollAmountHint: String = "",
    val tipAmountHint: String = "",

    val serviceAmountEnabled: Boolean = false,
    val extrasEnabled: Boolean = false,
    val tollsEnabled: Boolean = false,
    val tipsEnabled: Boolean = false,

    val showExtras: Boolean = false,
    val showTolls: Boolean = false,
    val showTips: Boolean = false,

    val totalAmountText: String = "",
    val buttonsState: AddAmountButtonsState = AddAmountButtonsState(),
    val dialogState: AddAmountDialogState? = null
)

// ------------------------------------------------------------
// BUTTON REDUCER
// ------------------------------------------------------------

object AddAmountButtonsReducer {
    fun reduce(
        serviceAmountEnabled: Boolean,
        showTolls: Boolean,
        showTips: Boolean,
        showExtras: Boolean,
        isFromDispatch: Boolean,
        isSubscriber: Boolean,
        isAmountFromDispatch: Boolean,
        isLoading: Boolean
    ): AddAmountButtonsState {
        val acceptEnabled = !isLoading
        val cancelEnabled = !isLoading

        return AddAmountButtonsState(
            acceptVisible = true,
            cancelVisible = true,
            acceptEnabled = acceptEnabled,
            cancelEnabled = cancelEnabled,
            acceptContainerColor = if (acceptEnabled) Color(0xFF1E88E5) else Color(0xFFBDBDBD),
            acceptContentColor = Color.White,
            cancelContainerColor = Color.Transparent,
            cancelContentColor = if (cancelEnabled) Color(0xFF1E88E5) else Color(0xFFBDBDBD),
            showDivider = showTolls || showTips || showExtras,
            showLoading = isLoading
        )
    }
}

// ------------------------------------------------------------
// SCREEN VIEWMODEL
// ------------------------------------------------------------

class AddAmountComposeViewModel(
    private val tripUseCase: TripUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val ttsUseCase: TTSUseCase,
    application: Application,
) : AndroidViewModel(application) {

    private var paymentModifiers: PaymentModifiers = PaymentModifiers()

    private val _uiState = MutableStateFlow(AddAmountUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AddAmountUiEffect>()
    val uiEffect = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            paymentModifiers = licensingUseCase.getPaymentModifiers()
        }
    }

    fun onEvent(event: AddAmountUiEvent) {
        when (event) {
            is AddAmountUiEvent.ServiceAmountChanged -> {
                _uiState.value = _uiState.value.copy(serviceAmountText = event.value)
                recalculate()
            }
            is AddAmountUiEvent.ExtraAmountChanged -> {
                _uiState.value = _uiState.value.copy(extraAmountText = event.value)
                recalculate()
            }
            is AddAmountUiEvent.TollAmountChanged -> {
                _uiState.value = _uiState.value.copy(tollAmountText = event.value)
                recalculate()
            }
            is AddAmountUiEvent.TipAmountChanged -> {
                _uiState.value = _uiState.value.copy(tipAmountText = event.value)
                recalculate()
            }

            AddAmountUiEvent.ServiceAmountDone,
            AddAmountUiEvent.ExtraAmountDone,
            AddAmountUiEvent.TollAmountDone,
            AddAmountUiEvent.TipAmountDone,
            AddAmountUiEvent.ServiceAmountFocusLost,
            AddAmountUiEvent.ExtraAmountFocusLost,
            AddAmountUiEvent.TollAmountFocusLost,
            AddAmountUiEvent.TipAmountFocusLost -> recalculate()

            AddAmountUiEvent.AcceptClicked -> collectAmount()
            AddAmountUiEvent.CancelClicked -> viewModelScope.launch { _effects.emit(AddAmountUiEffect.NavigateBack) }
            AddAmountUiEvent.DialogAccepted -> _uiState.value = _uiState.value.copy(dialogState = null)
        }
    }

    fun loadTrip(trip: Trip, dispatch: InfoDispatchModel?) {
        val isSubscriber = dispatch?.clientType != null && dispatch.clientType != NO_SUBSCRIBER
        val isAmountFromDispatch = trip.fromDispatch

        val serviceEnabled = allowsModifyCash(
            amountIsZero = (trip.taximeterAmount ?: 0) == 0,
            isManual = trip.taximeterTripId == null,
            isSubscriber = isSubscriber,
            isAmountFromDispatch = isAmountFromDispatch
        )

        val showTolls = allowsTolls(dispatch)
        val showTips = allowsTips(dispatch)

        val serviceAmount = if (trip.fromDispatch && (dispatch?.amount ?: 0) > 0) dispatch?.amount ?: 0 else (trip.taximeterAmount ?: 0)
        val extraAmount = trip.extra1 + trip.extra2 + trip.extra3 + trip.extra4 + (trip.extrasAuto ?: 0)
        val tollAmount = trip.tolls ?: 0
        val tipAmount = trip.tips ?: 0
        val totalAmount = serviceAmount + extraAmount + tollAmount + tipAmount

        _uiState.value = _uiState.value.copy(
            tripId = trip.id,
            fromDispatch = trip.fromDispatch,
            isSubscriber = isSubscriber,
            isAmountFromDispatch = isAmountFromDispatch,
            serviceAmountCents = serviceAmount,
            extraAmountCents = extraAmount,
            tollAmountCents = tollAmount,
            tipAmountCents = tipAmount,
            totalAmountCents = totalAmount,
            originalServiceAmountCents = serviceAmount,
            originalExtraAmountCents = extraAmount,
            serviceAmountText = serviceAmount.toMoneyValue(),
            extraAmountText = extraAmount.toMoneyValue(),
            tollAmountText = tollAmount.toMoneyValue(),
            tipAmountText = tipAmount.toMoneyValue(),
            serviceAmountHint = serviceAmount.toMoneyValue(),
            extraAmountHint = extraAmount.toMoneyValue(),
            tollAmountHint = tollAmount.toMoneyValue(),
            tipAmountHint = tipAmount.toMoneyValue(),
            serviceAmountEnabled = serviceEnabled,
            extrasEnabled = extraAmount == 0,
            tollsEnabled = true,
            tipsEnabled = true,
            showExtras = extraAmount != 0,
            showTolls = showTolls,
            showTips = showTips,
            totalAmountText = totalAmount.toCurrency()
        )

        updateButtons()
        checkIfWorksWithoutTx()
        useTTSForAmount(serviceAmount, extraAmount, tollAmount, tipAmount, totalAmount)
    }

    private fun recalculate() {
        val current = _uiState.value

        val serviceAmountLocal = current.serviceAmountText.toCents()
        val extraAmountLocal = if (current.originalExtraAmountCents != 0) {
            val priceModified = current.serviceAmountEnabled &&
                serviceAmountLocal != current.originalServiceAmountCents
            if (priceModified) 0 else current.originalExtraAmountCents
        } else {
            current.extraAmountText.toCents()
        }

        val tollAmountLocal = current.tollAmountText.toCents()
        val tipAmountLocal = current.tipAmountText.toCents()
        val totalAmountLocal = serviceAmountLocal + extraAmountLocal + tollAmountLocal + tipAmountLocal

        _uiState.value = current.copy(
            serviceAmountCents = serviceAmountLocal,
            extraAmountCents = extraAmountLocal,
            tollAmountCents = tollAmountLocal,
            tipAmountCents = tipAmountLocal,
            totalAmountCents = totalAmountLocal,
            extraAmountText = extraAmountLocal.toMoneyValue(),
            showExtras = extraAmountLocal != 0,
            totalAmountText = totalAmountLocal.toCurrency()
        )

        updateButtons()
    }

    private fun updateButtons() {
        val current = _uiState.value
        _uiState.value = current.copy(
            buttonsState = AddAmountButtonsReducer.reduce(
                serviceAmountEnabled = current.serviceAmountEnabled,
                showTolls = current.showTolls,
                showTips = current.showTips,
                showExtras = current.showExtras,
                isFromDispatch = current.fromDispatch,
                isSubscriber = current.isSubscriber,
                isAmountFromDispatch = current.isAmountFromDispatch,
                isLoading = current.isLoading
            )
        )
    }

    private fun collectAmount() {
        val state = _uiState.value
        val tripId = state.tripId ?: return

        viewModelScope.launch {
            val trip = tripUseCase.getTripById(tripId) ?: return@launch

            val maxAmountManual = maximumAmountManual()
            val maxAmountTips = maximumAmountTips()
            val maxAmountTolls = maximumAmountTolls()

            if (maxAmountManual > 0 && ((state.serviceAmountCents - (trip.taximeterAmount ?: 0)) > maxAmountManual)) {
                _effects.emit(
                    AddAmountUiEffect.ShowDialog(
                        AddAmountDialogState(
                            title = getApplication<Application>().getString(R.string.warning),
                            description = getApplication<Application>().getString(R.string.toastMaxService)
                        )
                    )
                )
                return@launch
            }

            if (maxAmountTips > 0 && ((state.tipAmountCents - (trip.tips ?: 0)) > maxAmountTips)) {
                _effects.emit(
                    AddAmountUiEffect.ShowDialog(
                        AddAmountDialogState(
                            title = getApplication<Application>().getString(R.string.warning),
                            description = getApplication<Application>().getString(R.string.toastMaxTips)
                        )
                    )
                )
                return@launch
            }

            if (maxAmountTolls > 0 && ((state.tollAmountCents - (trip.tolls ?: 0)) > maxAmountTolls)) {
                _effects.emit(
                    AddAmountUiEffect.ShowDialog(
                        AddAmountDialogState(
                            title = getApplication<Application>().getString(R.string.warning),
                            description = getApplication<Application>().getString(R.string.toastMaxTolls)
                        )
                    )
                )
                return@launch
            }

            updateTrip(
                trip = trip,
                serviceAmount = state.serviceAmountCents,
                tollAmount = state.tollAmountCents,
                tipAmount = state.tipAmountCents,
                totalAmount = state.totalAmountCents,
                extraAmount = state.extraAmountCents
            )
        }
    }

    fun updateTrip(
        trip: Trip?,
        serviceAmount: Int,
        tollAmount: Int,
        tipAmount: Int,
        totalAmount: Int,
        extraAmount: Int,
    ) {
        viewModelScope.launch {
            trip?.let {
                it.taximeterAmount = serviceAmount
                it.tolls = tollAmount
                it.tips = tipAmount
                it.totalAmount = totalAmount
                it.extras = extraAmount

                if (extraAmount == 0) {
                    it.extra1 = 0
                    it.extra2 = 0
                    it.extra3 = 0
                    it.extra4 = 0
                    it.extrasAuto = 0
                }

                if (trip.addMinimumPriceDispatch && (trip.mininumPriceDispatch != it.taximeterAmount)) {
                    trip.addMinimumPriceDispatch = false
                }

                licensingUseCase.getLicensingParameters()?.minAirportAmount?.let { minAirportAmount ->
                    if (trip.addMinimumPriceAirport && (trip.taximeterAmount != minAirportAmount)) {
                        trip.addMinimumPriceAirport = false
                    }
                }

                tripUseCase.updateTripAmountsAndExtras(
                    it.id,
                    it.taximeterAmount,
                    it.tolls,
                    it.tips,
                    it.totalAmount,
                    it.extras,
                    it.extra1,
                    it.extra2,
                    it.extra3,
                    it.extra4,
                    it.extrasAuto,
                    it.addMinimumPriceDispatch,
                    it.addMinimumPriceAirport
                )

                _effects.emit(AddAmountUiEffect.NavigateBack)
            }
        }
    }

    fun allowsModifyCash(amountIsZero: Boolean, isManual: Boolean, isSubscriber: Boolean, isAmountFromDispatch: Boolean): Boolean {
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

    fun checkIfWorksWithoutTx() {
        viewModelScope.launch {
            val bluetoothResult = bluetoothLocalUseCase.getLocalBluetooth()
            val value = bluetoothResult == null
            _uiState.value = _uiState.value.copy(serviceAmountEnabled = _uiState.value.serviceAmountEnabled || value)
            updateButtons()
        }
    }

    fun maximumAmountManual(): Int = paymentModifiers.maxManualAmount
    fun maximumAmountTips(): Int = paymentModifiers.maxTipAmount
    fun maximumAmountTolls(): Int = paymentModifiers.maxTollsAmount

    fun allowsTips(dispatch: InfoDispatchModel?): Boolean {
        return if ((dispatch?.importeCentral?.toIntOrNull() ?: 0) > 0) false else paymentModifiers.isAllowTips
    }

    fun allowsTolls(dispatch: InfoDispatchModel?): Boolean {
        return if ((dispatch?.importeCentral?.toIntOrNull() ?: 0) > 0) false else paymentModifiers.isAllowTolls
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
            text += getApplication<Application>().resources.getString(R.string.tts_service_amount) + " " + serviceAmountLocal.toCurrency() + "\n"
            if (extraAmountLocal != 0) text += getApplication<Application>().resources.getString(R.string.tts_extra_amount) + " " + extraAmountLocal.toCurrency() + "\n"
            if (tollAmountLocal != 0) text += getApplication<Application>().resources.getString(R.string.tts_toll_amount) + " " + tollAmountLocal.toCurrency() + "\n"
            if (tipAmountLocal != 0) text += getApplication<Application>().resources.getString(R.string.tts_tips_amount) + " " + tipAmountLocal.toCurrency() + "\n"
            if ((totalAmountLocal != 0) && (totalAmountLocal != serviceAmountLocal)) {
                text += getApplication<Application>().resources.getString(R.string.tts_total_amount) + " " + totalAmountLocal.toCurrency() + "\n"
            }

            when (userPreferencesUseCase.getUserPreferences()?.blindLocutionId) {
                TTSUseCaseImpl.TTSBlindOption.AUTO.value,
                TTSUseCaseImpl.TTSBlindOption.ONLY_PAYMENT.value -> {
                    ttsUseCase.speakText(text.trim(), TTSType.Amount)
                }
            }
        }
    }

    private fun String.toCents(): Int = replace(".", "").replace(",", "").trim().toIntOrNull() ?: 0
}

// ------------------------------------------------------------
// SHARED VM ADAPTER (REAL MAIN ACTIVITY VM)
// ------------------------------------------------------------

class AddAmountSharedVmAdapter(
    private val mainActivityViewModel: MainActivityViewModel
) : ViewModel() {

    val tripFlow = mainActivityViewModel.tripFlow
    val dispatchFlow = mainActivityViewModel.dispatchFlow
    val hasTaximeterConnectionFlow = mainActivityViewModel.hasTaximeterConnectionFlow
    val zoneFlow = mainActivityViewModel.zoneFlow
    val shortBreakStatusFlow = mainActivityViewModel.shortBreakStatus
    val orangeBtnPendingFlow = mainActivityViewModel.orangeBtnPendingFlow

    fun navigateBack() = mainActivityViewModel.navigateBack()

    fun navigateTo(directions: NavDirections) = mainActivityViewModel.navigateTo(directions)

    fun navigateTo(deepLink: NavDeepLinkRequest) = mainActivityViewModel.navigateTo(deepLink)

    fun updateBackCallback(invoked: (() -> Unit)?) = mainActivityViewModel.updateBackCallback(invoked)

    fun showHeader(visible: Boolean) {
        mainActivityViewModel.showHeader(visible)
    }
}

// ------------------------------------------------------------
// COMPOSE SCREEN
// ------------------------------------------------------------

@Composable
fun AddAmountScreen(
    navController: NavController,
    viewModel: AddAmountComposeViewModel,
    sharedVm: AddAmountSharedVmAdapter,
    onShowHeader: (Boolean) -> Unit,
    onShowDialog: (AddAmountDialogState, onAccept: () -> Unit) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val trip by sharedVm.tripFlow.collectAsStateWithLifecycle(initialValue = null)
    val dispatch by sharedVm.dispatchFlow.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(Unit) {
        onShowHeader(true)
    }

    LaunchedEffect(trip, dispatch) {
        if (trip != null) {
            viewModel.loadTrip(trip!!, dispatch)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                AddAmountUiEffect.NavigateBack -> navController.popBackStack()
                is AddAmountUiEffect.ShowDialog -> onShowDialog(effect.dialog) {
                    viewModel.onEvent(AddAmountUiEvent.DialogAccepted)
                }
            }
        }
    }

    AddAmountContent(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun AddAmountContent(
    state: AddAmountUiState,
    onEvent: (AddAmountUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AmountField(
            label = "Service amount",
            value = state.serviceAmountText,
            hint = state.serviceAmountHint,
            enabled = state.serviceAmountEnabled,
            onValueChange = { onEvent(AddAmountUiEvent.ServiceAmountChanged(it)) },
            onDone = { onEvent(AddAmountUiEvent.ServiceAmountDone) },
            onFocusLost = { onEvent(AddAmountUiEvent.ServiceAmountFocusLost) }
        )

        if (state.showExtras) {
            Spacer(Modifier.height(12.dp))
            AmountField(
                label = "Extras",
                value = state.extraAmountText,
                hint = state.extraAmountHint,
                enabled = false,
                onValueChange = { onEvent(AddAmountUiEvent.ExtraAmountChanged(it)) },
                onDone = { onEvent(AddAmountUiEvent.ExtraAmountDone) },
                onFocusLost = { onEvent(AddAmountUiEvent.ExtraAmountFocusLost) }
            )
        }

        if (state.showTolls) {
            Spacer(Modifier.height(12.dp))
            AmountField(
                label = "Tolls",
                value = state.tollAmountText,
                hint = state.tollAmountHint,
                enabled = state.tollsEnabled,
                onValueChange = { onEvent(AddAmountUiEvent.TollAmountChanged(it)) },
                onDone = { onEvent(AddAmountUiEvent.TollAmountDone) },
                onFocusLost = { onEvent(AddAmountUiEvent.TollAmountFocusLost) }
            )
        }

        if (state.showTips) {
            Spacer(Modifier.height(12.dp))
            AmountField(
                label = "Tips",
                value = state.tipAmountText,
                hint = state.tipAmountHint,
                enabled = state.tipsEnabled,
                onValueChange = { onEvent(AddAmountUiEvent.TipAmountChanged(it)) },
                onDone = { onEvent(AddAmountUiEvent.TipAmountDone) },
                onFocusLost = { onEvent(AddAmountUiEvent.TipAmountFocusLost) }
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Total: ${state.totalAmountText}",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(24.dp))

        AddAmountButtons(
            state = state.buttonsState,
            onAccept = { onEvent(AddAmountUiEvent.AcceptClicked) },
            onCancel = { onEvent(AddAmountUiEvent.CancelClicked) }
        )
    }
}

@Composable
fun AmountField(
    label: String,
    value: String,
    hint: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    onFocusLost: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (focused && !it.isFocused) onFocusLost()
                focused = it.isFocused
            },
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label) },
        placeholder = { Text(hint) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        singleLine = true
    )
}

@Composable
fun AddAmountButtons(
    state: AddAmountButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        if (state.acceptVisible) {
            Button(
                onClick = onAccept,
                enabled = state.acceptEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.acceptEnabled) state.acceptContainerColor else state.acceptDisabledContainerColor,
                    contentColor = if (state.acceptEnabled) state.acceptContentColor else state.acceptDisabledContentColor
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(state.acceptText)
            }
        }

        Spacer(Modifier.width(12.dp))

        if (state.cancelVisible) {
            OutlinedButton(
                onClick = onCancel,
                enabled = state.cancelEnabled,
                border = BorderStroke(
                    1.dp,
                    if (state.cancelEnabled) state.cancelContentColor else state.cancelDisabledContentColor
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (state.cancelEnabled) state.cancelContentColor else state.cancelDisabledContentColor
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(state.cancelText)
            }
        }
    }
}

@Composable
fun AddAmountDialogHost(
    dialogState: AddAmountDialogState?,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    if (dialogState != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(dialogState.title) },
            text = { Text(dialogState.description) },
            confirmButton = {
                TextButton(onClick = onAccept) {
                    Text(dialogState.acceptText)
                }
            }
        )
    }
}

### What changed in this refinement

- Replaced the placeholder shared VM adapter with **`AddAmountSharedVmAdapter`** that delegates to your real `MainActivityViewModel`
- Navigation now uses **`NavController.popBackStack()`** for back and **`NavDirections` / `NavDeepLinkRequest`** passthrough on the adapter
- The button composable is now a clean wrapper around a **custom-button-like API**:
  - primary `Button`
  - secondary `OutlinedButton`
  - explicit enabled/disabled colors
- The screen is still Compose-first, but now it compiles against the structure you showed

If you want, I can do one final pass and adapt the button layer to your exact `CustomButton` class signature if you paste that API.
