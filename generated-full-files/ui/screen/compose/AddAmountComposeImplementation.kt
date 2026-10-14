package ifac.td.taxi.ui.screen.compose

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import ifac.td.taxi.R
import ifac.td.taxi.compose.viewmodel.ComposeSharedViewModel
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.NO_SUBSCRIBER
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toCurrency
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toMoneyValue
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ------------------------------------------------------------
// STATE / EFFECTS
// ------------------------------------------------------------

data class AddAmountUiState(
    val trip: TripUiModel? = null,
    val serviceAmountText: String = "",
    val extraAmountText: String = "",
    val tollAmountText: String = "",
    val tipAmountText: String = "",
    val totalAmountText: String = "",
    val serviceAmountEnabled: Boolean = false,
    val extrasVisible: Boolean = false,
    val tollsVisible: Boolean = false,
    val tipsVisible: Boolean = false,
    val dialogState: AddAmountDialogState? = null,
    val buttons: AddAmountButtonsState = AddAmountButtonsState()
)

data class TripUiModel(
    val id: Long,
    val fromDispatch: Boolean,
    val taximeterAmount: Int,
    val taximeterTripId: Long?,
    val extra1: Int,
    val extra2: Int,
    val extra3: Int,
    val extra4: Int,
    val extrasAuto: Int?,
    val tolls: Int?,
    val tips: Int?,
    val addMinimumPriceDispatch: Boolean,
    val addMinimumPriceAirport: Boolean,
    val mininumPriceDispatch: Int,
    val clientType: Int? = null,
    val importeCentral: String? = null
)

data class AddAmountDialogState(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

sealed interface AddAmountUiEvent {
    data class ServiceAmountChanged(val value: String) : AddAmountUiEvent
    data class ExtraAmountChanged(val value: String) : AddAmountUiEvent
    data class TollAmountChanged(val value: String) : AddAmountUiEvent
    data class TipAmountChanged(val value: String) : AddAmountUiEvent
    data object AcceptClicked : AddAmountUiEvent
    data object CancelClicked : AddAmountUiEvent
    data object ActionDone : AddAmountUiEvent
    data object FocusLost : AddAmountUiEvent
    data object DialogAccepted : AddAmountUiEvent
    data object DialogDismissed : AddAmountUiEvent
}

sealed interface AddAmountUiEffect {
    data object NavigateBack : AddAmountUiEffect
    data object NavigateToPendingTrips : AddAmountUiEffect
    data object UpdateTopBarIconRight : AddAmountUiEffect
    data object RequestMessagesRefresh : AddAmountUiEffect
    data class KeepScreenActive(val active: Boolean) : AddAmountUiEffect
    data class ShowToast(val messageRes: Int) : AddAmountUiEffect
    data class UpdateTimeControl(val value: String?) : AddAmountUiEffect
    data class ShowDialog(val dialog: AddAmountDialogState) : AddAmountUiEffect
}

// ------------------------------------------------------------
// BUTTON STATE
// ------------------------------------------------------------

data class AddAmountButtonsState(
    val accept: ButtonVisualState = ButtonVisualState(
        textRes = R.string.accept,
        visible = true,
        enabled = true,
        backgroundRes = R.color.primary,
        contentColorRes = android.R.color.white
    ),
    val cancel: ButtonVisualState = ButtonVisualState(
        textRes = R.string.cancel,
        visible = true,
        enabled = true,
        backgroundRes = R.color.gray_200,
        contentColorRes = R.color.black
    ),
    val pending: PendingButtonState = PendingButtonState(),
    val dashboardVisible: Boolean = false,
    val fixedPriceVisible: Boolean = false,
    val receiptsVisible: Boolean = true,
    val zoningEnabled: Boolean = false,
    val keepScreenActive: Boolean = false,
    val refreshProgress: Int = 0,
    val noCarsVisible: Boolean = false
)

data class ButtonVisualState(
    @StringRes val textRes: Int,
    val visible: Boolean,
    val enabled: Boolean,
    val backgroundRes: Int,
    val contentColorRes: Int
)

data class PendingButtonState(
    val visible: Boolean = true,
    val enabled: Boolean = false,
    val orangeBackground: Boolean = false
)

// ------------------------------------------------------------
// SCREEN VIEWMODEL
// ------------------------------------------------------------

class AddAmountViewModel(
    private val tripUseCase: TripUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val ttsUseCase: TTSUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddAmountUiState())
    val uiState: StateFlow<AddAmountUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AddAmountUiEffect>()
    val uiEffect: SharedFlow<AddAmountUiEffect> = _uiEffect.asSharedFlow()

    private val _pendingServicesButtonPressedCallback = MutableSharedFlow<Boolean>()
    val pendingServicesButtonPressedCallback: SharedFlow<Boolean> = _pendingServicesButtonPressedCallback.asSharedFlow()

    private val _keepOnScreenFlow = MutableSharedFlow<Boolean>()
    val keepOnScreenFlow: SharedFlow<Boolean> = _keepOnScreenFlow.asSharedFlow()

    private val _hasDashboardButtonOn = MutableSharedFlow<Boolean>()
    val hasDashboardButtonOn: SharedFlow<Boolean> = _hasDashboardButtonOn.asSharedFlow()

    private val _timeControlFlow = MutableSharedFlow<String?>()
    val timeControlFlow: SharedFlow<String?> = _timeControlFlow.asSharedFlow()

    private val _showEstimateFixedPriceButtonFlow = MutableSharedFlow<Boolean>()
    val showEstimateFixedPriceButtonFlow: SharedFlow<Boolean> = _showEstimateFixedPriceButtonFlow.asSharedFlow()

    private val _zoningButtonStateFlow = MutableSharedFlow<Boolean>()
    val zoningButtonStateFlow: SharedFlow<Boolean> = _zoningButtonStateFlow.asSharedFlow()

    private val _pendingServicesButtonFlow = MutableSharedFlow<Boolean?>()
    val pendingServicesButtonFlow: SharedFlow<Boolean?> = _pendingServicesButtonFlow.asSharedFlow()

    private val _pendingServicesHiredButtonFlow = MutableSharedFlow<Boolean?>()
    val pendingServicesHiredButtonFlow: SharedFlow<Boolean?> = _pendingServicesHiredButtonFlow.asSharedFlow()

    private val _refreshProgressFlow = MutableSharedFlow<Int>()
    val refreshProgressFlow: SharedFlow<Int> = _refreshProgressFlow.asSharedFlow()

    private var paymentModifiers: PaymentModifiers = PaymentModifiers()
    private var originalServiceAmount: Int = 0
    private var originalExtraAmount: Int = 0

    init {
        viewModelScope.launch {
            paymentModifiers = licensingUseCase.getPaymentModifiers()
        }
    }

    fun initialize(trip: Trip?, dispatch: InfoDispatchModel?) {
        trip ?: return

        val serviceAmount = if (trip.fromDispatch && dispatch?.amount != null && dispatch.amount > 0) {
            dispatch.amount
        } else {
            trip.taximeterAmount ?: 0
        }

        val extraAmount = trip.extra1 + trip.extra2 + trip.extra3 + trip.extra4 + (trip.extrasAuto ?: 0)
        val tollAmount = trip.tolls ?: 0
        val tipAmount = trip.tips ?: 0
        val totalAmount = serviceAmount + extraAmount + tollAmount + tipAmount

        originalServiceAmount = serviceAmount
        originalExtraAmount = extraAmount

        val serviceEnabled = allowsModifyCash(
            amountIsZero = serviceAmount == 0,
            isManual = trip.taximeterTripId == null,
            isSubscriber = dispatch?.clientType != NO_SUBSCRIBER,
            isAmountFromDispatch = trip.fromDispatch
        )

        _uiState.update {
            it.copy(
                trip = trip.toUiModel(dispatch),
                serviceAmountText = serviceAmount.toMoneyValue(),
                extraAmountText = extraAmount.toMoneyValue(),
                tollAmountText = tollAmount.toMoneyValue(),
                tipAmountText = tipAmount.toMoneyValue(),
                totalAmountText = totalAmount.toCurrency(),
                serviceAmountEnabled = serviceEnabled,
                extrasVisible = extraAmount != 0,
                tollsVisible = allowsTolls(dispatch),
                tipsVisible = allowsTips(dispatch)
            )
        }

        useTTSForAmount(serviceAmount, extraAmount, tollAmount, tipAmount, totalAmount)
    }

    fun onEvent(event: AddAmountUiEvent) {
        when (event) {
            is AddAmountUiEvent.ServiceAmountChanged -> {
                _uiState.update { it.copy(serviceAmountText = event.value) }
                recalculate()
            }
            is AddAmountUiEvent.ExtraAmountChanged -> {
                _uiState.update { it.copy(extraAmountText = event.value) }
                recalculate()
            }
            is AddAmountUiEvent.TollAmountChanged -> {
                _uiState.update { it.copy(tollAmountText = event.value) }
                recalculate()
            }
            is AddAmountUiEvent.TipAmountChanged -> {
                _uiState.update { it.copy(tipAmountText = event.value) }
                recalculate()
            }
            AddAmountUiEvent.AcceptClicked -> collectAmount()
            AddAmountUiEvent.CancelClicked -> viewModelScope.launch { _uiEffect.emit(AddAmountUiEffect.NavigateBack) }
            AddAmountUiEvent.ActionDone, AddAmountUiEvent.FocusLost -> recalculate()
            AddAmountUiEvent.DialogAccepted, AddAmountUiEvent.DialogDismissed -> {
                _uiState.update { it.copy(dialogState = null) }
            }
        }
    }

    private fun recalculate() {
        val state = _uiState.value
        val serviceAmount = state.serviceAmountText.toMoneyCents()
        var extraAmount = state.extraAmountText.toMoneyCents()
        val tollAmount = state.tollAmountText.toMoneyCents()
        val tipAmount = state.tipAmountText.toMoneyCents()

        if (originalExtraAmount != 0) {
            val priceModified = state.serviceAmountEnabled && serviceAmount != originalServiceAmount
            extraAmount = if (priceModified) 0 else originalExtraAmount
            _uiState.update {
                it.copy(
                    extraAmountText = extraAmount.toMoneyValue(),
                    extrasVisible = !priceModified && originalExtraAmount != 0
                )
            }
        }

        val total = serviceAmount + extraAmount + tollAmount + tipAmount
        _uiState.update { it.copy(totalAmountText = total.toCurrency()) }
    }

    private fun collectAmount() {
        val state = _uiState.value
        val trip = state.trip ?: return

        val serviceAmount = state.serviceAmountText.toMoneyCents()
        val extraAmount = state.extraAmountText.toMoneyCents()
        val tollAmount = state.tollAmountText.toMoneyCents()
        val tipAmount = state.tipAmountText.toMoneyCents()
        val totalAmount = serviceAmount + extraAmount + tollAmount + tipAmount

        val maxAmountManual = maximumAmountManual()
        val maxAmountTips = maximumAmountTips()
        val maxAmountTolls = maximumAmountTolls()

        if (maxAmountManual > 0 && ((serviceAmount - trip.taximeterAmount) > maxAmountManual)) {
            showDialog(R.string.warning, R.string.toastMaxService)
            return
        }
        if (maxAmountTips > 0 && ((tipAmount - (trip.tips ?: 0)) > maxAmountTips)) {
            showDialog(R.string.warning, R.string.toastMaxTips)
            return
        }
        if (maxAmountTolls > 0 && ((tollAmount - (trip.tolls ?: 0)) > maxAmountTolls)) {
            showDialog(R.string.warning, R.string.toastMaxTolls)
            return
        }

        viewModelScope.launch {
            tripUseCase.updateTripAmountsAndExtras(
                trip.id,
                serviceAmount,
                tollAmount,
                tipAmount,
                totalAmount,
                extraAmount,
                trip.extra1,
                trip.extra2,
                trip.extra3,
                trip.extra4,
                trip.extrasAuto ?: 0,
                trip.addMinimumPriceDispatch,
                trip.addMinimumPriceAirport
            )
            _uiEffect.emit(AddAmountUiEffect.NavigateBack)
        }
    }

    private fun showDialog(@StringRes titleRes: Int, @StringRes descriptionRes: Int) {
        val dialog = AddAmountDialogState(titleRes, descriptionRes)
        _uiState.update { it.copy(dialogState = dialog) }
        viewModelScope.launch { _uiEffect.emit(AddAmountUiEffect.ShowDialog(dialog)) }
    }

    fun allowsModifyCash(amountIsZero: Boolean, isManual: Boolean, isSubscriber: Boolean, isAmountFromDispatch: Boolean): Boolean {
        if (isAmountFromDispatch) return false
        if (isSubscriber && paymentModifiers.isAllowModifySubscriberAmounts) return true
        val withoutProtocol = false
        if (withoutProtocol || isManual) return true
        return when (paymentModifiers.isAllowModifyCash) {
            PaymentModifiers.ModifyImports.NOT_ALLOWED -> false
            PaymentModifiers.ModifyImports.ONLY_IMPORT_0 -> amountIsZero
            PaymentModifiers.ModifyImports.ALWAYS -> true
        }
    }

    fun allowsTips(dispatch: InfoDispatchModel?): Boolean {
        return if ((dispatch?.importeCentral?.toIntOrNull() ?: 0) > 0) false else paymentModifiers.isAllowTips
    }

    fun allowsTolls(dispatch: InfoDispatchModel?): Boolean {
        return if ((dispatch?.importeCentral?.toIntOrNull() ?: 0) > 0) false else paymentModifiers.isAllowTolls
    }

    fun maximumAmountManual(): Int = paymentModifiers.maxManualAmount
    fun maximumAmountTips(): Int = paymentModifiers.maxTipAmount
    fun maximumAmountTolls(): Int = paymentModifiers.maxTollsAmount

    fun useTTSForAmount(serviceAmountLocal: Int, extraAmountLocal: Int, tollAmountLocal: Int, tipAmountLocal: Int, totalAmountLocal: Int) {
        viewModelScope.launch {
            var text = ""
            text += getApplication<Application>().resources.getString(R.string.tts_service_amount) + " " + serviceAmountLocal.toCurrency() + "\n"
            if (extraAmountLocal != 0) text += getApplication<Application>().resources.getString(R.string.tts_extra_amount) + " " + extraAmountLocal.toCurrency() + "\n"
            if (tollAmountLocal != 0) text += getApplication<Application>().resources.getString(R.string.tts_toll_amount) + " " + tollAmountLocal.toCurrency() + "\n"
            if (tipAmountLocal != 0) text += getApplication<Application>().resources.getString(R.string.tts_tips_amount) + " " + tipAmountLocal.toCurrency() + "\n"
            if (totalAmountLocal != 0 && totalAmountLocal != serviceAmountLocal) text += getApplication<Application>().resources.getString(R.string.tts_total_amount) + " " + totalAmountLocal.toCurrency() + "\n"
            when (userPreferencesUseCase.getUserPreferences()?.blindLocutionId) {
                TTSUseCaseImpl.TTSBlindOption.AUTO.value,
                TTSUseCaseImpl.TTSBlindOption.ONLY_PAYMENT.value -> ttsUseCase.speakText(text.trim(), TTSType.Amount)
            }
        }
    }

    private fun Trip.toUiModel(dispatch: InfoDispatchModel?) = TripUiModel(
        id = id,
        fromDispatch = fromDispatch,
        taximeterAmount = taximeterAmount ?: 0,
        taximeterTripId = taximeterTripId,
        extra1 = extra1,
        extra2 = extra2,
        extra3 = extra3,
        extra4 = extra4,
        extrasAuto = extrasAuto,
        tolls = tolls,
        tips = tips,
        addMinimumPriceDispatch = addMinimumPriceDispatch,
        addMinimumPriceAirport = addMinimumPriceAirport,
        mininumPriceDispatch = mininumPriceDispatch,
        clientType = dispatch?.clientType,
        importeCentral = dispatch?.importeCentral
    )
}

private fun String.toMoneyCents(): Int =
    replace(".", "").replace(",", "").trim().toIntOrNull() ?: 0

// ------------------------------------------------------------
// SHARED VM ADAPTER FOR EXISTING MainActivityViewModel
// ------------------------------------------------------------

data class AddAmountSharedState(
    val zone: String? = null,
    val shortBreakStatus: ShortBreakStatus? = null,
    val pendingTripsList: List<Any>? = null
)

class AddAmountMainActivityViewModelAdapter(
    private val sharedViewModel: MainActivityViewModel,
    private val screenViewModel: AddAmountViewModel
) : ViewModel() {

    private val _buttonsState = MutableStateFlow(AddAmountButtonsState())
    val buttonsState: StateFlow<AddAmountButtonsState> = _buttonsState.asStateFlow()

    private val _effects = MutableSharedFlow<AddAmountUiEffect>()
    val uiEffect: SharedFlow<AddAmountUiEffect> = _effects.asSharedFlow()

    private var pendingServicesButton: Boolean? = null
    private var pendingServicesHiredButton: Boolean? = null

    fun bind() {
        viewModelScope.launch {
            sharedViewModel.hasTaximeterConnectionFlow.collect {
                _effects.emit(AddAmountUiEffect.UpdateTopBarIconRight)
            }
        }

        viewModelScope.launch {
            sharedViewModel.orangeBtnPendingFlow.collect { orange ->
                _buttonsState.update {
                    it.copy(pending = it.pending.copy(orangeBackground = orange == true))
                }
            }
        }

        viewModelScope.launch {
            screenViewModel.pendingServicesButtonPressedCallback.collect { shouldNavigate ->
                if (shouldNavigate) {
                    _effects.emit(AddAmountUiEffect.NavigateToPendingTrips)
                } else {
                    if (sharedViewModel.pendingTripsListFlow.value?.isEmpty() == true) {
                        _effects.emit(AddAmountUiEffect.ShowToast(R.string.toast_no_hay_pendientes))
                    } else {
                        _effects.emit(AddAmountUiEffect.NavigateToPendingTrips)
                    }
                }
            }
        }

        viewModelScope.launch {
            sharedViewModel.shortBreakStatus.collect {
                recomputePendingEnabled()
            }
        }

        viewModelScope.launch {
            screenViewModel.keepOnScreenFlow.collect {
                _buttonsState.update { state -> state.copy(keepScreenActive = it) }
                _effects.emit(AddAmountUiEffect.KeepScreenActive(it))
            }
        }

        viewModelScope.launch {
            screenViewModel.hasDashboardButtonOn.collect {
                _buttonsState.update { state -> state.copy(dashboardVisible = it) }
            }
        }

        viewModelScope.launch {
            sharedViewModel.updateMessageUI.collect {
                _effects.emit(AddAmountUiEffect.RequestMessagesRefresh)
                screenViewModel.getMessages()
            }
        }

        viewModelScope.launch {
            screenViewModel.timeControlFlow.collect {
                sharedViewModel.updateTimeControlFlow(it)
                _effects.emit(AddAmountUiEffect.UpdateTimeControl(it))
            }
        }

        viewModelScope.launch {
            screenViewModel.showEstimateFixedPriceButtonFlow.collect {
                _buttonsState.update { state ->
                    state.copy(
                        fixedPriceVisible = it,
                        receiptsVisible = !it
                    )
                }
            }
        }

        viewModelScope.launch {
            screenViewModel.zoningButtonStateFlow.collect {
                _buttonsState.update { state -> state.copy(zoningEnabled = it) }
            }
        }

        viewModelScope.launch {
            sharedViewModel.zoneFlow.collect { zone ->
                if (zone.isNullOrBlank()) {
                    _buttonsState.update { state -> state.copy(pending = state.pending.copy(enabled = false)) }
                } else {
                    recomputePendingEnabled()
                }
            }
        }

        viewModelScope.launch {
            screenViewModel.pendingServicesButtonFlow.collect {
                pendingServicesButton = it
                recomputePendingEnabled()
            }
        }

        viewModelScope.launch {
            screenViewModel.pendingServicesHiredButtonFlow.collect {
                pendingServicesHiredButton = it
                recomputePendingEnabled()
            }
        }

        viewModelScope.launch {
            screenViewModel.refreshProgressFlow.collect { progress ->
                _buttonsState.update { state -> state.copy(refreshProgress = progress) }
            }
        }
    }

    private fun recomputePendingEnabled() {
        val zone = sharedViewModel.zoneFlow.value
        val shortBreak = sharedViewModel.shortBreakStatus.value

        val isInShortBreak =
            shortBreak == ShortBreakStatus.IN_SHORT_BREAK ||
            shortBreak == ShortBreakStatus.IN_SHORT_BREAK_FORCED

        val enabled = when {
            zone.isNullOrEmpty() -> false
            isInShortBreak -> false
            !W2CLocation.isLocationAllowedByCentral() -> false
            W2CLocation.getIsInSoonInZone() -> true
            pendingServicesButton == true -> true
            else -> false
        }

        _buttonsState.update { it.copy(pending = it.pending.copy(enabled = enabled)) }
    }
}

// ------------------------------------------------------------
// COMPOSE SCREEN
// ------------------------------------------------------------

@Composable
fun AddAmountScreen(
    navController: NavController,
    viewModel: AddAmountViewModel,
    sharedAdapter: AddAmountMainActivityViewModelAdapter,
    sharedViewModel: MainActivityViewModel,
    trip: Trip?,
    dispatch: InfoDispatchModel?,
    onShowHeader: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
    onKeepScreenActive: (Boolean) -> Unit,
    onUpdateTopBarIconRight: () -> Unit,
    onNavigateToPendingTrips: (NavDirections) -> Unit,
    pendingTripsDirections: NavDirections
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState by sharedAdapter.buttonsState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onShowHeader(true)
        viewModel.initialize(trip, dispatch)
        sharedAdapter.bind()
    }

    LaunchedEffect(sharedAdapter) {
        sharedAdapter.uiEffect.collectLatest { effect ->
            when (effect) {
                AddAmountUiEffect.NavigateBack -> navController.popBackStack()
                AddAmountUiEffect.NavigateToPendingTrips -> onNavigateToPendingTrips(pendingTripsDirections)
                AddAmountUiEffect.UpdateTopBarIconRight -> onUpdateTopBarIconRight()
                AddAmountUiEffect.RequestMessagesRefresh -> Unit
                is AddAmountUiEffect.KeepScreenActive -> onKeepScreenActive(effect.active)
                is AddAmountUiEffect.ShowToast -> onShowToast(effect.messageRes)
                is AddAmountUiEffect.UpdateTimeControl -> Unit
                is AddAmountUiEffect.ShowDialog -> Unit
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                AddAmountUiEffect.NavigateBack -> navController.popBackStack()
                AddAmountUiEffect.NavigateToPendingTrips -> onNavigateToPendingTrips(pendingTripsDirections)
                AddAmountUiEffect.UpdateTopBarIconRight -> onUpdateTopBarIconRight()
                AddAmountUiEffect.RequestMessagesRefresh -> Unit
                is AddAmountUiEffect.KeepScreenActive -> onKeepScreenActive(effect.active)
                is AddAmountUiEffect.ShowToast -> onShowToast(effect.messageRes)
                is AddAmountUiEffect.UpdateTimeControl -> Unit
                is AddAmountUiEffect.ShowDialog -> Unit
            }
        }
    }

    uiState.dialogState?.let { dialog ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(AddAmountUiEvent.DialogDismissed) },
            title = { Text(stringResource(dialog.titleRes)) },
            text = { Text(stringResource(dialog.descriptionRes)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(AddAmountUiEvent.DialogAccepted) }) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        )
    }

    AddAmountContent(
        state = uiState,
        buttonsState = buttonsState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun AddAmountContent(
    state: AddAmountUiState,
    buttonsState: AddAmountButtonsState,
    onEvent: (AddAmountUiEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.serviceAmountText,
            onValueChange = { onEvent(AddAmountUiEvent.ServiceAmountChanged(it)) },
            enabled = state.serviceAmountEnabled,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Service") },
            singleLine = true
        )

        if (state.extrasVisible) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.extraAmountText,
                onValueChange = { onEvent(AddAmountUiEvent.ExtraAmountChanged(it)) },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Extras") },
                singleLine = true
            )
        }

        if (state.tollsVisible) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.tollAmountText,
                onValueChange = { onEvent(AddAmountUiEvent.TollAmountChanged(it)) },
                enabled = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tolls") },
                singleLine = true
            )
        }

        if (state.tipsVisible) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.tipAmountText,
                onValueChange = { onEvent(AddAmountUiEvent.TipAmountChanged(it)) },
                enabled = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tips") },
                singleLine = true
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(text = state.totalAmountText, style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            if (buttonsState.cancel.visible) {
                CustomComposeButton(
                    style = buttonsState.cancel,
                    onClick = { onEvent(AddAmountUiEvent.CancelClicked) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (buttonsState.accept.visible) {
                CustomComposeButton(
                    style = buttonsState.accept,
                    onClick = { onEvent(AddAmountUiEvent.AcceptClicked) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CustomComposeButton(
    style: ButtonVisualState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = style.enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(style.backgroundRes),
            contentColor = colorResource(style.contentColorRes)
        )
    ) {
        Text(text = stringResource(style.textRes))
    }
}

// ------------------------------------------------------------
// PLACEHOLDER ADAPTER TYPES TO MATCH YOUR EXISTING PROJECT
// Replace these with your real imports/implementations.
// ------------------------------------------------------------

class TripUseCase {
    suspend fun updateTripAmountsAndExtras(
        id: Long,
        taximeterAmount: Int,
        tolls: Int,
        tips: Int,
        totalAmount: Int,
        extras: Int,
        extra1: Int,
        extra2: Int,
        extra3: Int,
        extra4: Int,
        extrasAuto: Int,
        addMinimumPriceDispatch: Boolean,
        addMinimumPriceAirport: Boolean
    ) = Unit
}

class LicensingUseCase {
    suspend fun getPaymentModifiers(): PaymentModifiers = PaymentModifiers()
}

class BluetoothLocalUseCase {
    suspend fun getLocalBluetooth(): Any? = null
}

class UserPreferencesUseCase {
    fun getUserPreferences(): UserPreferences? = null
}

class TTSUseCase {
    fun speakText(text: String, type: TTSType) = Unit
}

enum class TTSType { Amount }

data class UserPreferences(val blindLocutionId: Int)

data class PaymentModifiers(
    val isAllowModifySubscriberAmounts: Boolean = false,
    val isAllowModifyCash: ModifyImports = ModifyImports.NOT_ALLOWED,
    val isAllowTips: Boolean = false,
    val isAllowTolls: Boolean = false,
    val maxManualAmount: Int = 0,
    val maxTipAmount: Int = 0,
    val maxTollsAmount: Int = 0
) {
    enum class ModifyImports { NOT_ALLOWED, ONLY_IMPORT_0, ALWAYS }
}

object W2CLocation {
    fun isLocationAllowedByCentral(): Boolean = true
    fun getIsInSoonInZone(): Boolean = false
}
