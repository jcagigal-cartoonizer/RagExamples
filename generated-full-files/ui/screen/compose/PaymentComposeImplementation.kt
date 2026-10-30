package ifac.td.taxi.ui.screen.compose
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.TextButton
import ifac.td.taxi.repository.room.dao.DispatchExtraDataDao
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import androidx.compose.material3.Scaffold
import androidx.annotation.DrawableRes
import androidx.compose.runtime.LaunchedEffect
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase as FrameworkLicensingUseCase
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.ui.unit.dp
import ifac.td.taxi.domain.usecase.TicketUseCase
import androidx.compose.foundation.layout.Box
import ifac.td.taxi.framework.sdk.usecase.TaximeterManagerUseCase as FrameworkTaximeterManagerUseCase
import ifac.td.taxi.domain.usecase.IngenicoUseCase
import ifac.td.taxi.domain.usecase.TTSUseCase
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import ifac.td.taxi.domain.usecase.SubscriberUseCase
import androidx.navigation.NavController
import ifac.td.taxi.domain.model.PaymentMethod
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import ifac.td.taxi.viewmodel.model.RedSysPaymentState
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.RedSysUseCase
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.domain.usecase.TripUseCase
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.BackToDispatchedUseCase
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.domain.usecase.CountdownManagerUseCase
import ifac.td.taxi.domain.usecase.LicensingUseCase
import androidx.compose.runtime.setValue
import android.content.Intent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.background
import ifac.td.taxi.domain.usecase.LightSkyGlassUseCase
import ifac.td.taxi.domain.usecase.SumUpUseCase
import ifac.td.taxi.framework.sdk.usecase.SoundManagerUseCase
import ifac.td.taxi.domain.model.PaymentModifiers
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.TaximeterManagerUseCase
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import android.app.Application
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.ui.custom.button.CustomButton
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.material3.ButtonDefaults
import ifac.td.taxi.R
import androidx.compose.foundation.layout.Column


// -----------------------------
// UI STATE / EFFECT / EVENT
// -----------------------------

data class PaymentUiState(
    val trip: Trip? = null,
    val dispatch: InfoDispatchModel? = null,
    val amountText: String = "",
    val showAmount: Boolean = false,
    val showAddButton: Boolean = false,
    val showAppPaymentOverlay: Boolean = false,
    val showBackToDispatch: Boolean = false,
    val showLocution: Boolean = false,
    val showCourtesyLight: Boolean = false,
    val showUvLight: Boolean = false,
    val showBizum: Boolean = false,
    val appPaymentTitle: String = "Esperando Respuesta",
    val isWaitingAppPaymentResponse: Boolean = true,
    val paymentModifiers: PaymentModifiers? = null,
    val conditions: Pair<BluetoothInfo?, Int>? = null,
    val redSysPaymentState: RedSysPaymentState = RedSysPaymentState.IDLE,
    val buttons: PaymentButtonsState = PaymentButtonsState.default(),
    val dialog: PaymentDialogState? = null,
)

data class PaymentButtonsState(
    val cash: PaymentButtonVisualState = PaymentButtonVisualState(),
    val subscriber: PaymentButtonVisualState = PaymentButtonVisualState(),
    val card: PaymentButtonVisualState = PaymentButtonVisualState(),
    val bizum: PaymentButtonVisualState = PaymentButtonVisualState(visible = false),
    val appPayment: PaymentButtonVisualState = PaymentButtonVisualState(visible = false),
    val othersPayment: PaymentButtonVisualState = PaymentButtonVisualState(visible = false),
    val cancel: PaymentButtonVisualState = PaymentButtonVisualState(visible = false),
) {
    companion object {
        fun default(): PaymentButtonsState = PaymentButtonsState(
            cash = PaymentButtonVisualState(
                visible = true,
                enabled = true,
                background = CustomButton.BackgroundButtonColor.GREEN,
                buttonType = PaymentButtonType.CONTADO
            ),
            subscriber = PaymentButtonVisualState(
                visible = true,
                enabled = true,
                background = CustomButton.BackgroundButtonColor.RED,
                buttonType = PaymentButtonType.ABONADO
            ),
            card = PaymentButtonVisualState(
                visible = true,
                enabled = true,
                background = CustomButton.BackgroundButtonColor.BLUE,
                buttonType = PaymentButtonType.TARJETA
            ),
            bizum = PaymentButtonVisualState(
                visible = false,
                enabled = true,
                background = CustomButton.BackgroundButtonColor.RED,
                buttonType = PaymentButtonType.BIZUM
            ),
            appPayment = PaymentButtonVisualState(
                visible = false,
                enabled = true,
                background = CustomButton.BackgroundButtonColor.BLUE,
                buttonType = PaymentButtonType.APP_PAYMENT
            ),
            othersPayment = PaymentButtonVisualState(
                visible = false,
                enabled = true,
                background = CustomButton.BackgroundButtonColor.RED,
                buttonType = PaymentButtonType.OTHERS
            ),
            cancel = PaymentButtonVisualState(
                visible = false,
                enabled = true,
                background = CustomButton.BackgroundButtonColor.RED,
                buttonType = PaymentButtonType.CANCEL
            )
        )
    }
}

data class PaymentButtonVisualState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val text: String? = null,
    @DrawableRes val iconRes: Int? = null,
    val background: CustomButton.BackgroundButtonColor = CustomButton.BackgroundButtonColor.BLUE,
    val buttonType: PaymentButtonType = PaymentButtonType.CONTADO,
)

enum class PaymentButtonType {
    CONTADO, ABONADO, TARJETA, BIZUM, APP_PAYMENT, OTHERS, CANCEL
}

data class PaymentDialogState(
    val title: String,
    val description: String,
    val buttons: List<PaymentDialogButton>,
    val centerText: Boolean = false,
    val cancellable: Boolean = true,
    val checkBoxText: String? = null,
    val icon: Int? = null,
    val editTextMaxLength: Int? = null,
    val hint: String? = null,
)

enum class PaymentDialogButton {
    ACCEPT, CANCEL, RETRY, OTHERS
}

sealed interface PaymentUiEvent {
    data object OnInit : PaymentUiEvent
    data object OnResume : PaymentUiEvent
    data object OnCashClicked : PaymentUiEvent
    data object OnCardClicked : PaymentUiEvent
    data object OnSubscriberClicked : PaymentUiEvent
    data object OnAddClicked : PaymentUiEvent
    data object OnBackToDispatchClicked : PaymentUiEvent
    data object OnAppPaymentClicked : PaymentUiEvent
    data object OnOthersPaymentClicked : PaymentUiEvent
    data object OnCancelAppPaymentClicked : PaymentUiEvent
    data object OnLocutionClicked : PaymentUiEvent
    data object OnUvLightClicked : PaymentUiEvent
    data object OnCourtesyLightClicked : PaymentUiEvent
    data class OnDialogButtonClicked(val button: PaymentDialogButton) : PaymentUiEvent
}

sealed interface PaymentUiEffect {
    data class Navigate(val route: Any) : PaymentUiEffect
    data class OpenActivity(val intent: Intent, val requestCode: Int) : PaymentUiEffect
    data class ShowToast(val messageRes: Int) : PaymentUiEffect
    data class OpenDialog(val dialog: PaymentDialogState) : PaymentUiEffect
    data object CloseDialog : PaymentUiEffect
    data class StartSumUpPayment(val amount: Int) : PaymentUiEffect
    data class ProcessRedSysPayment(val amount: Int, val ticket: String?) : PaymentUiEffect
    data object GoToNextInfoDispatch : PaymentUiEffect
    data object BackToDispatched : PaymentUiEffect
}

// -----------------------------
// EXACT BUTTON REDUCER
// -----------------------------

fun reducePaymentButtonsState(
    dispatch: InfoDispatchModel?,
    isWaitingAppPaymentResponse: Boolean,
    hasMoneiAccount: Boolean,
    subscriberButtonAllowed: Boolean,
    isConcertedOrMax: Boolean,
): PaymentButtonsState {
    var state = PaymentButtonsState.default()

    when (dispatch?.clientType) {
        DispatchUseCase.SUBSCRIBER_CASH_NO_CARD -> {
            state = state.copy(card = state.card.copy(enabled = false))
        }

        DispatchUseCase.SUBSCRIBER_CASH_CARD -> {
            state = state.copy(card = state.card.copy(enabled = false))
        }

        DispatchUseCase.SUBSCRIBER_CREDIT_NO_CARD -> {
            state = if (!ifac.td.taxi.domain.utils.StaticConfiguration.subscriberFailPin) {
                state.copy(
                    cash = state.cash.copy(enabled = false),
                    card = state.card.copy(enabled = false)
                )
            } else {
                state.copy(
                    cash = state.cash.copy(enabled = true),
                    card = state.card.copy(enabled = true)
                )
            }
        }

        DispatchUseCase.SUBSCRIBER_CREDIT_CARD -> {
            state = if (!ifac.td.taxi.domain.utils.StaticConfiguration.subscriberFailPin) {
                state.copy(
                    cash = state.cash.copy(enabled = false),
                    card = state.card.copy(enabled = false)
                )
            } else {
                state.copy(
                    cash = state.cash.copy(enabled = true),
                    card = state.card.copy(enabled = true)
                )
            }
        }

        DispatchUseCase.SUBSCRIBER_CREDIT_CARD_COMPULSORY -> {
            state = if (!ifac.td.taxi.domain.utils.StaticConfiguration.subscriberFailPin) {
                state.copy(
                    cash = state.cash.copy(enabled = false),
                    card = state.card.copy(enabled = false)
                )
            } else {
                state.copy(
                    cash = state.cash.copy(enabled = true),
                    card = state.card.copy(enabled = true)
                )
            }
        }

        DispatchUseCase.SUBSCRIBER_TCC -> {
            state = if (!ifac.td.taxi.domain.utils.StaticConfiguration.subscriberFailPin) {
                state.copy(
                    cash = state.cash.copy(enabled = false),
                    card = state.card.copy(enabled = false)
                )
            } else {
                state.copy(
                    cash = state.cash.copy(enabled = true),
                    card = state.card.copy(enabled = true)
                )
            }
        }

        else -> Unit
    }

    if (dispatch?.isPaymentByApp == true && isWaitingAppPaymentResponse) {
        state = state.copy(
            bizum = state.bizum.copy(visible = false),
            subscriber = state.subscriber.copy(visible = false),
            card = state.card.copy(visible = false),
            cash = state.cash.copy(visible = false),
            appPayment = state.appPayment.copy(
                visible = true,
                enabled = true,
                loading = false,
                background = CustomButton.BackgroundButtonColor.BLUE,
                buttonType = PaymentButtonType.APP_PAYMENT
            ),
            othersPayment = state.othersPayment.copy(
                visible = true,
                enabled = !isConcertedOrMax,
                background = CustomButton.BackgroundButtonColor.RED
            ),
            cancel = state.cancel.copy(visible = false)
        )
    } else {
        state = state.copy(
            appPayment = state.appPayment.copy(visible = false),
            othersPayment = state.othersPayment.copy(visible = false),
            cancel = state.cancel.copy(visible = false)
        )
    }

    if (hasMoneiAccount && (dispatch?.clientType == null || dispatch.clientType <= 0 || ifac.td.taxi.domain.utils.StaticConfiguration.subscriberFailPin)) {
        state = state.copy(bizum = state.bizum.copy(visible = true))
    }

    state = state.copy(
        subscriber = state.subscriber.copy(enabled = subscriberButtonAllowed)
    )

    return state
}

// -----------------------------
// MAIN ACTIVITY BRIDGE VIEWMODEL
// -----------------------------

class PaymentMainActivityViewModel : ViewModel() {
    val _tripFlow = MutableStateFlow<Trip?>(null)
    val tripFlow: StateFlow<Trip?> = _tripFlow.asStateFlow()

    val _dispatchFlow = MutableStateFlow<InfoDispatchModel?>(null)
    val dispatchFlow: StateFlow<InfoDispatchModel?> = _dispatchFlow.asStateFlow()

    val _currentAmountFlow = MutableStateFlow<Int?>(null)
    val currentAmountFlow: StateFlow<Int?> = _currentAmountFlow.asStateFlow()

    val _sumUpResponseFlow = MutableStateFlow<Pair<Any?, Intent?>?>(null)
    val sumUpResponseFlow: StateFlow<Pair<Any?, Intent?>?> = _sumUpResponseFlow.asStateFlow()

    val _redsysPaymentResultFlow = MutableStateFlow<Any?>(null)
    val redsysPaymentResultFlow: StateFlow<Any?> = _redsysPaymentResultFlow.asStateFlow()

    val _inAppPaymentResponseFlow = MutableStateFlow<Pair<Boolean, String?>?>(null)
    val inAppPaymentResponseFlow: StateFlow<Pair<Boolean, String?>?> = _inAppPaymentResponseFlow.asStateFlow()

    fun setTrip(trip: Trip?) { _tripFlow.value = trip }
    fun setDispatch(dispatch: InfoDispatchModel?) { _dispatchFlow.value = dispatch }
    fun resetCurrentAmountFlow() { _currentAmountFlow.value = null }

    fun sendAppPaymentAuthRequest(amount: Int) {}
    fun processRedSysPayment(amount: Int, ticket: String?) {}
    fun stopAppPaymentCounter() {}
    fun stopPaymentCounter() {}
    fun resetBottomBarText() {}
    fun changeTextTimerTopBar(text: String?) {}
    fun goToNextInfoDispatch() {}
    fun backToDispatched(id: Long) {}
    fun printTicket(ticket: String) {}
    fun showHeader(show: Boolean) {}
    fun customBackPressed() {}
    fun configureIconsTopBar(icon: Any, show: Boolean, onClick: () -> Unit) {}
}

// -----------------------------
// COMPOSE VIEWMODEL
// -----------------------------

class PaymentComposeViewModel(
    application: Application,
    val tripUseCase: TripUseCase,
    val shiftStatusUseCase: ShiftStatusUseCase,
    val countdownManagerUseCase: CountdownManagerUseCase,
    val bluetoothLocalUseCase: BluetoothLocalUseCase,
    val lightSkyGlassUseCase: LightSkyGlassUseCase,
    val licensingUseCase: FrameworkLicensingUseCase,
    val subscriberUseCase: SubscriberUseCase,
    val ticketUseCase: TicketUseCase,
    val userPreferencesUseCase: UserPreferencesUseCase,
    val sumUpUseCase: SumUpUseCase,
    val redSysUseCase: RedSysUseCase,
    val dispatchExtraDataDao: DispatchExtraDataDao,
    val ttsUseCase: TTSUseCase,
    val bravoConfigurationUseCase: BravoCentralConfigurationUseCase,
    val bravoCentralUseCase: BravoCentralUseCase,
    val backToDispatchedUseCase: BackToDispatchedUseCase,
    val ingenicoUseCase: IngenicoUseCase,
    val dispatchUseCase: DispatchUseCase,
    val taximeterManagerUseCase: FrameworkTaximeterManagerUseCase,
    val soundManagerUseCase: SoundManagerUseCase,
    val redSysSessionState: ifac.td.taxi.domain.model.RedSysSessionState,
) : ViewModel() {

    val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    val _uiEffect = MutableSharedFlow<PaymentUiEffect>()
    val uiEffect: SharedFlow<PaymentUiEffect> = _uiEffect.asSharedFlow()

    fun onEvent(event: PaymentUiEvent) {
        when (event) {
            PaymentUiEvent.OnInit -> initPaymentView()
            PaymentUiEvent.OnResume -> onResume()
            PaymentUiEvent.OnCashClicked -> handlePayment(PaymentMethod.CASH)
            PaymentUiEvent.OnCardClicked -> handlePayment(PaymentMethod.CARD)
            PaymentUiEvent.OnSubscriberClicked -> handlePayment(PaymentMethod.SUBSCRIBER)
            PaymentUiEvent.OnAddClicked -> navigateWithAmount()
            PaymentUiEvent.OnBackToDispatchClicked -> backToDispatch()
            PaymentUiEvent.OnAppPaymentClicked -> handlePayment(PaymentMethod.APP)
            PaymentUiEvent.OnOthersPaymentClicked -> onOthersPayment()
            PaymentUiEvent.OnCancelAppPaymentClicked -> onCancelAppPayment()
            PaymentUiEvent.OnLocutionClicked -> speakTTS(auto = false)
            PaymentUiEvent.OnUvLightClicked -> uvLight()
            PaymentUiEvent.OnCourtesyLightClicked -> turnCourtesyLight()
            is PaymentUiEvent.OnDialogButtonClicked -> Unit
        }
    }

    private fun initPaymentView() {
        viewModelScope.launch {
            _uiState.update { it.copy(paymentModifiers = licensingUseCase.getPaymentModifiers()) }
        }
    }

    private fun onResume() {
        val dispatch = _uiState.value.dispatch
        val buttons = reducePaymentButtonsState(
            dispatch = dispatch,
            isWaitingAppPaymentResponse = _uiState.value.isWaitingAppPaymentResponse,
            hasMoneiAccount = false,
            subscriberButtonAllowed = true,
            isConcertedOrMax = dispatch?.let { it.isConcertedPrice == true || it.isConcertedMaximumPrice == true } == true
        )
        _uiState.update { it.copy(buttons = buttons) }
    }

    private fun handlePayment(paymentType: PaymentMethod) {
        viewModelScope.launch {
            stopCountdown()
            when (paymentType) {
                PaymentMethod.CARD -> {
                    _uiEffect.emit(PaymentUiEffect.StartSumUpPayment(_uiState.value.trip?.totalAmount ?: 0))
                }
                PaymentMethod.CASH -> {
                    finishPayment(_uiState.value.trip, paymentType, _uiState.value.dispatch)
                }
                PaymentMethod.APP -> {
                    finishPayment(_uiState.value.trip, paymentType, _uiState.value.dispatch, navigate = false)
                }
                PaymentMethod.SUBSCRIBER -> {
                    val dispatch = _uiState.value.dispatch
                    val trip = _uiState.value.trip
                    if (dispatch?.askAuthCode == true && dispatch.typedAuthCode.isBlank()) {
                        emitDialog(
                            PaymentDialogState(
                                title = "Ask auth code",
                                description = "Enter auth code",
                                buttons = listOf(PaymentDialogButton.CANCEL, PaymentDialogButton.ACCEPT),
                                editTextMaxLength = if (trip?.fromDispatch == true) 40 else 14,
                                hint = "Auth code"
                            )
                        )
                    } else if (trip?.fromDispatch == true && !dispatch?.subscriber.isNullOrBlank()) {
                        useSubscriberAttempt(trip)
                        subscriberFromDispatch(trip, dispatch)
                    } else {
                        subscriberPayment(trip)
                    }
                }
                PaymentMethod.MONEI, PaymentMethod.EMPTY -> Unit
            }
            turnCourtesyLightOff()
            _uiEffect.emit(PaymentUiEffect.CloseDialog)
        }
    }

    private fun finishPayment(
        trip: Trip?,
        paymentMethod: PaymentMethod,
        dispatch: InfoDispatchModel?,
        navigate: Boolean = true,
    ) {
        viewModelScope.launch {
            if (trip != null) {
                tripUseCase.endTrip(trip, paymentMethod)
                if (navigate) navigatePostPayment(dispatch)
            }
        }
    }

    private fun subscriberPayment(trip: Trip?) {}
    private fun subscriberFromDispatch(trip: Trip, dispatch: InfoDispatchModel?) {}
    private fun useSubscriberAttempt(trip: Trip) {}
    private fun navigatePostPayment(dispatch: InfoDispatchModel?) {}
    private fun navigateWithAmount() {}
    private fun backToDispatch() {}
    private fun onOthersPayment() {}
    private fun onCancelAppPayment() {}
    private fun uvLight() {}
    private fun turnCourtesyLight() {}
    private fun turnCourtesyLightOff() {}
    private fun stopCountdown() {}
    private fun speakTTS(auto: Boolean) {}

    private fun emitDialog(dialog: PaymentDialogState) {
        viewModelScope.launch { _uiEffect.emit(PaymentUiEffect.OpenDialog(dialog)) }
    }

    fun setTrip(trip: Trip?) { _uiState.update { it.copy(trip = trip) } }
    fun setDispatch(dispatch: InfoDispatchModel?) { _uiState.update { it.copy(dispatch = dispatch) } }
    fun updateAmount(amountText: String, show: Boolean) { _uiState.update { it.copy(amountText = amountText, showAmount = show) } }
    fun updateButtonsFromDispatch(dispatch: InfoDispatchModel?, isWaitingAppPaymentResponse: Boolean, hasMoneiAccount: Boolean, subscriberButtonAllowed: Boolean) {
        _uiState.update {
            it.copy(
                dispatch = dispatch,
                isWaitingAppPaymentResponse = isWaitingAppPaymentResponse,
                buttons = reducePaymentButtonsState(
                    dispatch = dispatch,
                    isWaitingAppPaymentResponse = isWaitingAppPaymentResponse,
                    hasMoneiAccount = hasMoneiAccount,
                    subscriberButtonAllowed = subscriberButtonAllowed,
                    isConcertedOrMax = dispatch?.let { d -> d.isConcertedPrice == true || d.isConcertedMaximumPrice == true } == true
                )
            )
        }
    }
}

// -----------------------------
// COMPOSABLE SCREEN
// -----------------------------

@Composable
fun PaymentScreen(
    navController: NavController,
    viewModel: PaymentComposeViewModel,
    sharedViewModel: PaymentMainActivityViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<PaymentDialogState?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is PaymentUiEffect.Navigate -> navController.navigate(effect.route)
                is PaymentUiEffect.ShowToast -> Unit
                is PaymentUiEffect.OpenDialog -> dialogState = effect.dialog
                PaymentUiEffect.CloseDialog -> dialogState = null
                is PaymentUiEffect.StartSumUpPayment -> sharedViewModel.sendAppPaymentAuthRequest(effect.amount)
                is PaymentUiEffect.ProcessRedSysPayment -> sharedViewModel.processRedSysPayment(effect.amount, effect.ticket)
                PaymentUiEffect.GoToNextInfoDispatch -> sharedViewModel.goToNextInfoDispatch()
                PaymentUiEffect.BackToDispatched -> sharedViewModel.dispatchFlow.value?.id?.let { sharedViewModel.backToDispatched(it) }
                is PaymentUiEffect.OpenActivity -> Unit
            }
        }
    }

    LaunchedEffect(sharedViewModel.tripFlow.collectAsStateWithLifecycle().value) {
        viewModel.setTrip(sharedViewModel.tripFlow.value)
    }
    LaunchedEffect(sharedViewModel.dispatchFlow.collectAsStateWithLifecycle().value) {
        viewModel.setDispatch(sharedViewModel.dispatchFlow.value)
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            if (state.showAppPaymentOverlay) {
                AppPaymentOverlay(
                    title = state.appPaymentTitle,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                PaymentMainArea(
                    state = state,
                    onEvent = viewModel::onEvent,
                    modifier = Modifier.weight(1f)
                )
                PaymentButtonsGrid(
                    state = state.buttons,
                    onCash = { viewModel.onEvent(PaymentUiEvent.OnCashClicked) },
                    onSubscriber = { viewModel.onEvent(PaymentUiEvent.OnSubscriberClicked) },
                    onCard = { viewModel.onEvent(PaymentUiEvent.OnCardClicked) },
                    onBizum = { },
                    onAppPayment = { viewModel.onEvent(PaymentUiEvent.OnAppPaymentClicked) },
                    onOthersPayment = { viewModel.onEvent(PaymentUiEvent.OnOthersPaymentClicked) },
                    onCancel = { viewModel.onEvent(PaymentUiEvent.OnCancelAppPaymentClicked) }
                )
            }

            if (state.showBackToDispatch) {
                FloatingActionButton(
                    onClick = { viewModel.onEvent(PaymentUiEvent.OnBackToDispatchClicked) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) { Text("360") }
            }
        }
    }

    dialogState?.let { dialog ->
        PaymentDialog(
            state = dialog,
            onDismiss = { dialogState = null },
            onButtonClick = {
                viewModel.onEvent(PaymentUiEvent.OnDialogButtonClicked(it))
                dialogState = null
            }
        )
    }
}

@Composable
private fun PaymentMainArea(
    state: PaymentUiState,
    onEvent: (PaymentUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (state.showAmount) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = state.amountText, style = MaterialTheme.typography.displayMedium)
                if (state.showAddButton) {
                    Spacer(Modifier.size(8.dp))
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.baseline_add_circle_outline_24),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { onEvent(PaymentUiEvent.OnAddClicked) }
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (state.showLocution) {
                FloatingActionButton(onClick = { onEvent(PaymentUiEvent.OnLocutionClicked) }) { Text("TTS") }
            }
            if (state.showUvLight) {
                Spacer(Modifier.size(12.dp))
                FloatingActionButton(onClick = { onEvent(PaymentUiEvent.OnUvLightClicked) }) { Text("UV") }
            }
            if (state.showCourtesyLight) {
                Spacer(Modifier.size(12.dp))
                FloatingActionButton(onClick = { onEvent(PaymentUiEvent.OnCourtesyLightClicked) }) { Text("L") }
            }
        }
    }
}

@Composable
private fun AppPaymentOverlay(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFF111111)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White)
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun PaymentButtonsGrid(
    state: PaymentButtonsState,
    onCash: () -> Unit,
    onSubscriber: () -> Unit,
    onCard: () -> Unit,
    onBizum: () -> Unit,
    onAppPayment: () -> Unit,
    onOthersPayment: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(6.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            PaymentCustomButton(state.cash, onCash, Modifier.weight(1f))
            PaymentCustomButton(state.subscriber, onSubscriber, Modifier.weight(1f))
            PaymentCustomButton(state.card, onCard, Modifier.weight(1f))
            if (state.bizum.visible) PaymentCustomButton(state.bizum, onBizum, Modifier.weight(1f))
        }
        if (state.appPayment.visible || state.othersPayment.visible || state.cancel.visible) {
            Row(Modifier.fillMaxWidth()) {
                if (state.appPayment.visible) PaymentCustomButton(state.appPayment, onAppPayment, Modifier.weight(2f))
                if (state.othersPayment.visible) PaymentCustomButton(state.othersPayment, onOthersPayment, Modifier.weight(1f))
                if (state.cancel.visible) PaymentCustomButton(state.cancel, onCancel, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PaymentCustomButton(
    visualState: PaymentButtonVisualState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visualState.visible) return

    val bg = when (visualState.background) {
        CustomButton.BackgroundButtonColor.GREEN -> Color(0xFF2E7D32)
        CustomButton.BackgroundButtonColor.RED -> Color(0xFFC62828)
        CustomButton.BackgroundButtonColor.BLUE -> Color(0xFF1565C0)
        CustomButton.BackgroundButtonColor.YELLOW -> Color(0xFFF9A825)
        CustomButton.BackgroundButtonColor.ORANGE -> Color(0xFFEF6C00)
        CustomButton.BackgroundButtonColor.DEFAULT -> Color(0xFF1565C0)
        CustomButton.BackgroundButtonColor.BLACK -> Color.Black
        CustomButton.BackgroundButtonColor.TRANSPARENT -> Color.Transparent
    }

    Button(
        onClick = onClick,
        enabled = visualState.enabled,
        modifier = modifier
            .padding(1.dp)
            .height(72.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (visualState.enabled) bg else Color(0xFF666666),
            disabledContainerColor = Color(0xFF666666)
        )
    ) {
        Text(text = visualState.text ?: visualState.buttonType.name)
    }
}

@Composable
private fun PaymentDialog(
    state: PaymentDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (PaymentDialogButton) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = { Text(state.description) },
        confirmButton = {
            TextButton(onClick = { onButtonClick(PaymentDialogButton.ACCEPT) }) {
                Text("OK")
            }
        }
    )
}
