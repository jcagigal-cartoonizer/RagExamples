package ifac.td.taxi.ui.screen.compose
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.Stable
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import ifac.td.taxi.domain.model.Dispatch
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.mutableStateOf
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toBoolean1or0
import androidx.compose.foundation.layout.Spacer
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.util.Logs
import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.TextButton
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.PaymentDirections
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow
import ifac.td.taxi.viewmodel.model.UtilsModel
import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toInfoDispatchModel
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.height
import ifac.td.taxi.domain.usecase.SubscriberUseCase
import androidx.navigation.NavController
import ifac.td.taxi.domain.model.PaymentMethod
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import ifac.td.taxi.domain.usecase.TripUseCase
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.collectAsState
import ifac.td.taxi.domain.usecase.DispatchUseCase
import kotlinx.coroutines.withContext
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import android.content.Intent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import ifac.td.taxi.framework.sdk.usecase.NavigatorUseCase
import androidx.navigation.NavDirections
import androidx.navigation.compose.rememberNavController
import ifac.td.taxi.domain.model.Trip
import androidx.compose.ui.Alignment
import androidx.compose.runtime.DisposableEffect
import android.app.Application
import androidx.navigation.NavDeepLinkRequest
import ifac.td.taxi.ui.custom.button.CustomButton
import kotlinx.coroutines.flow.MutableSharedFlow
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.Subscriber
import androidx.compose.foundation.layout.Column


// ----------------------------
// UI STATE / EVENTS / EFFECTS
// ----------------------------

data class TccUiState(
    val isLoading: Boolean = true,
    val dispatch: Dispatch? = null,
    val attributes: TccAttributesState = TccAttributesState(),
    val buttonsState: TccButtonsState = TccButtonsState(),
    val dialogState: TccDialogState = TccDialogState.Hidden,
)

data class TccButtonsState(
    val accept: TccButtonVisualState = TccButtonVisualState(
        text = "ACCEPT",
        iconRes = R.drawable.baseline_check_192,
        background = CustomButton.BackgroundButtonColor.GREEN,
        style = CustomButton.StyleButton.ENABLE,
        visible = true,
        enabled = true
    )
)

data class TccButtonVisualState(
    val text: String,
    val iconRes: Int,
    val background: CustomButton.BackgroundButtonColor,
    val style: CustomButton.StyleButton,
    val visible: Boolean,
    val enabled: Boolean
)

sealed interface TccDialogState {
    data object Hidden : TccDialogState
    data class Error(@StringRes val messageRes: Int) : TccDialogState
}

sealed interface TccUiEvent {
    data object ScreenStarted : TccUiEvent
    data object AcceptClicked : TccUiEvent
    data class SpinnerChanged(
        val att1: Int?,
        val att2: Int?,
        val att3: Int?,
        val att4: Int?
    ) : TccUiEvent
}

sealed interface TccUiEffect {
    data class ShowToast(@StringRes val messageRes: Int) : TccUiEffect
    data object NavigateToSignature : TccUiEffect
    data object NavigateToVoucher : TccUiEffect
    data object NavigateToQr : TccUiEffect
    data object FinishPaymentFlow : TccUiEffect
}

data class TccAttributeUi(
    val title: String,
    val selectedIndex: Int = 1,
    val visible: Boolean = true
)

data class TccAttributesState(
    val att1: TccAttributeUi? = null,
    val att2: TccAttributeUi? = null,
    val att3: TccAttributeUi? = null,
    val att4: TccAttributeUi? = null
)

fun reduceTccButtonsState(dispatch: Dispatch?, orientation: Int): TccButtonsState {
    return TccButtonsState(
        accept = TccButtonVisualState(
            text = "ACCEPT",
            iconRes = R.drawable.baseline_check_192,
            background = CustomButton.BackgroundButtonColor.GREEN,
            style = CustomButton.StyleButton.ENABLE,
            visible = true,
            enabled = true
        )
    )
}

fun reduceTccAttributesState(dispatch: Dispatch?, orientation: Int): TccAttributesState {
    fun attr(title: String?): TccAttributeUi? = title?.takeIf { it.isNotEmpty() }?.let {
        TccAttributeUi(title = it, selectedIndex = 1, visible = true)
    }
    return TccAttributesState(
        att1 = attr(dispatch?.attribute1Title),
        att2 = attr(dispatch?.attribute2Title),
        att3 = attr(dispatch?.attribute3Title),
        att4 = attr(dispatch?.attribute4Title),
    )
}

// ----------------------------
// COMPOSE SCREEN
// ----------------------------

@Composable
fun TccRoute(
    tripId: Long?,
    navController: NavController,
    viewModel: TccViewModel,
    sharedViewModel: TccMainActivityViewModel,
    onShowToast: (Int) -> Unit = { res -> Toast.makeText(LocalContext.current, res, Toast.LENGTH_SHORT).show() },
    onBackPressed: () -> Unit = { navController.popBackStack() },
) {
    TccScreen(
        tripId = tripId,
        navController = navController,
        viewModel = viewModel,
        sharedViewModel = sharedViewModel,
        onShowToast = onShowToast,
        onBackPressed = onBackPressed
    )
}

@Composable
fun TccScreen(
    tripId: Long?,
    navController: NavController,
    viewModel: TccViewModel,
    sharedViewModel: TccMainActivityViewModel,
    onShowToast: (Int) -> Unit,
    onBackPressed: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation
    val dispatch by viewModel.dispatch.collectAsStateWithLifecycle()

    val uiState = remember(dispatch, orientation) {
        TccUiState(
            isLoading = dispatch == null,
            dispatch = dispatch,
            attributes = reduceTccAttributesState(dispatch, orientation),
            buttonsState = reduceTccButtonsState(dispatch, orientation),
            dialogState = TccDialogState.Hidden
        )
    }

    LaunchedEffect(tripId) {
        viewModel.getDispatch(tripId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is TccUiEffect.ShowToast -> onShowToast(effect.messageRes)
                TccUiEffect.NavigateToSignature -> {
                    dispatch?.toInfoDispatchModel()?.let {
                        navController.navigate(PaymentDirections.goToSignatureFragment(it.longDispatchNumber))
                    }
                }
                TccUiEffect.NavigateToVoucher -> {
                    dispatch?.toInfoDispatchModel()?.let {
                        navController.navigate(PaymentDirections.goToCropImageViewFragment(it.longDispatchNumber))
                    }
                }
                TccUiEffect.NavigateToQr -> {
                    dispatch?.toInfoDispatchModel()?.let {
                        navController.navigate(PaymentDirections.goToScannerQRFragment(it.longDispatchNumber))
                    }
                }
                TccUiEffect.FinishPaymentFlow -> onBackPressed()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                TccAttributesSection(
                    attributesState = uiState.attributes,
                    onAttributeSelected = { a1, a2, a3, a4 ->
                        // no-op here; selection is read directly from state holders below
                    },
                    modifier = Modifier.weight(1f)
                )

                TccAcceptButton(
                    dispatch = dispatch,
                    tripId = tripId,
                    viewModel = viewModel,
                    sharedViewModel = sharedViewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }

        if (uiState.dialogState is TccDialogState.Error) {
            TccSimpleErrorDialog(
                messageRes = (uiState.dialogState as TccDialogState.Error).messageRes,
                onDismiss = { /* handled by state in VM if needed */ }
            )
        }
    }
}

@Composable
private fun TccAttributesSection(
    attributesState: TccAttributesState,
    onAttributeSelected: (Int?, Int?, Int?, Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(8.dp)) {
        attributesState.att1?.let { TccAttributeDropdownRow(it) }
        attributesState.att2?.let { TccAttributeDropdownRow(it) }
        attributesState.att3?.let { TccAttributeDropdownRow(it) }
        attributesState.att4?.let { TccAttributeDropdownRow(it) }
    }
}

@Composable
private fun TccAttributeDropdownRow(attribute: TccAttributeUi) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableIntStateOf(attribute.selectedIndex) }
    val items = remember { (0..9).toList() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = attribute.title)
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(text = selected.toString())
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                items.forEach { value ->
                    DropdownMenuItem(
                        text = { Text(value.toString()) },
                        onClick = {
                            selected = value
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TccAcceptButton(
    dispatch: Dispatch?,
    tripId: Long?,
    viewModel: TccViewModel,
    sharedViewModel: TccMainActivityViewModel,
    modifier: Modifier = Modifier
) {
    var buttonStyle by remember { mutableStateOf(CustomButton.StyleButton.ENABLE) }

    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier,
        factory = { context ->
            CustomButton(context).apply {
                setButtonType(ButtonType.ACCEPT.value)
                setButtonStyle(CustomButton.StyleButton.ENABLE)
                setAction {
                    buttonStyle = CustomButton.StyleButton.LOADING
                    setButtonStyle(CustomButton.StyleButton.LOADING)

                    val att1 = if (!dispatch?.attribute1Title.isNullOrEmpty()) "1" else null
                    val att2 = if (!dispatch?.attribute2Title.isNullOrEmpty()) "1" else null
                    val att3 = if (!dispatch?.attribute3Title.isNullOrEmpty()) "1" else null
                    val att4 = if (!dispatch?.attribute4Title.isNullOrEmpty()) "1" else null

                    dispatch?.let {
                        viewModel.onAcceptClicked(
                            tripId = tripId,
                            dispatch = it,
                            att1 = att1,
                            att2 = att2,
                            att3 = att3,
                            att4 = att4,
                            callback = sharedViewModel.updatePrintFlowCallback
                        )
                    }
                }
            }
        },
        update = { button ->
            button.setButtonStyle(buttonStyle)
        }
    )
}

@Composable
private fun TccSimpleErrorDialog(
    @StringRes messageRes: Int,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "OK")
            }
        },
        title = { Text(text = "Error") },
        text = { Text(text = LocalContext.current.getString(messageRes)) }
    )
}

// ----------------------------
// VIEWMODEL: TCC
// ----------------------------

class TccViewModel(
    context: Application,
    val dispatchUseCase: DispatchUseCase,
    val subscriberUseCase: SubscriberUseCase,
    val tripUseCase: TripUseCase,
    val shiftStatusUseCase: ShiftStatusUseCase,
    val ticketUseCase: TicketUseCase,
) : BaseViewModel(context) {

    val TAG = "TccViewModel"

    val _dispatch = MutableStateFlow<Dispatch?>(null)
    val dispatch: StateFlow<Dispatch?> = _dispatch.asStateFlow()

    val _uiEffects = MutableSharedFlow<TccUiEffect>()
    val uiEffects: SharedFlow<TccUiEffect> = _uiEffects.asSharedFlow()

    val _checkSubscriberCreditLimitFlow = MutableSharedFlow<String>()
    val checkSubscriberCreditLimitFlow = _checkSubscriberCreditLimitFlow.asSharedFlow()

    fun getDispatch(id: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            id?.let {
                try {
                    val dispatch = dispatchUseCase.getDispatchByTripId(it)
                    Logs.d(TAG, "getDispatch $dispatch")
                    _dispatch.value = dispatch
                } catch (e: Exception) {
                    Logs.e(TAG, "Error fetching dispatch")
                    _dispatch.value = null
                    _uiEffects.emit(TccUiEffect.ShowToast(R.string.dialog_error_title))
                }
            }
        }
    }

    fun onAcceptClicked(
        tripId: Long?,
        dispatch: Dispatch,
        att1: String?,
        att2: String?,
        att3: String?,
        att4: String?,
        callback: (Trip) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dispatch.id?.toLong()?.let { id ->
                    updateTCCDispatch(id, att1, att2, att3, att4)
                }

                val requiresSignature = dispatch.requisiteSignature == "1"
                val dispatchId = dispatch.id?.toLong()

                if (requiresSignature) {
                    Logs.d(TAG, "finishTccService: TCC opening Signature View")
                    dispatchId?.let { _uiEffects.emit(TccUiEffect.NavigateToSignature) }
                } else {
                    Logs.d(TAG, "finishTccService: TCC Signature not required")
                    finishTccService(tripId, dispatch.toInfoDispatchModel(), callback)
                }
            } catch (e: Exception) {
                Logs.e(TAG, "Error handling accept click")
                _uiEffects.emit(TccUiEffect.ShowToast(R.string.dialog_error_title))
            }
        }
    }

    fun finishTccService(
        tripId: Long?,
        dispatch: InfoDispatchModel?,
        callback: (Trip) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trip = tripId?.let { tripUseCase.getTripById(it) }
                trip?.let {
                    dispatch?.let { dispatchValue ->
                        if (dispatchValue.checkCreditLimit) {
                            handleCreditLimit(dispatchValue, trip)
                        } else {
                            proceedWithDispatch(dispatchValue, trip, callback)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.not_checking_credit_limit),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Logs.e(TAG, "Error finishing TCC service")
            }
        }
    }

    private suspend fun handleCreditLimit(dispatchValue: InfoDispatchModel, trip: Trip) {
        dispatchValue.subscriber?.let { manualSubscriber ->
            dispatchValue.subscriberUser?.let { manualUser ->
                Subscriber(
                    manualSubscriber = manualSubscriber.toInt(),
                    manualUser = UtilsModel.trimUserFromString(manualUser).toInt(),
                    tripId = trip.id
                )
            }
        }?.let { subscriber ->
            subscriberUseCase.registerSubscriberAndAssignToTrip(subscriber)
            subscriberUseCase.checkCreditLimit(trip.totalAmount, _checkSubscriberCreditLimitFlow)
        }
    }

    private suspend fun proceedWithDispatch(
        dispatchValue: InfoDispatchModel,
        trip: Trip,
        callback: (Trip) -> Unit,
    ) {
        if (dispatchValue.requireSignature) {
            dispatchRequireSignature(dispatchValue)
        } else if (dispatchValue.requireVoucher) {
            dispatchRequireVoucher(dispatchValue)
        } else if (dispatchValue.requireQr) {
            dispatchRequireQR(dispatchValue)
        } else {
            validCredit(trip, dispatchValue, callback)
        }
    }

    fun dispatchRequireSignature(dispatch: InfoDispatchModel) {
        viewModelScope.launch {
            navigateTo(PaymentDirections.goToSignatureFragment(dispatch.longDispatchNumber))
        }
    }

    fun dispatchRequireVoucher(dispatch: InfoDispatchModel) {
        viewModelScope.launch {
            navigateTo(PaymentDirections.goToCropImageViewFragment(dispatch.longDispatchNumber))
        }
    }

    fun dispatchRequireQR(dispatch: InfoDispatchModel) {
        viewModelScope.launch {
            navigateTo(PaymentDirections.goToScannerQRFragment(dispatch.longDispatchNumber))
        }
    }

    fun updateDispatch(dispatch: Dispatch) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dispatchUseCase.updateDispatch(dispatch)
            } catch (e: Exception) {
                Logs.e(TAG, "Error updating dispatch")
            }
        }
    }

    suspend fun updateTCCDispatch(
        dispatchId: Long,
        att1: String?,
        att2: String?,
        att3: String?,
        att4: String?
    ) {
        try {
            dispatchUseCase.updateDispatchTCCAttributes(dispatchId, att1, att2, att3, att4)
        } catch (e: Exception) {
            Logs.e(TAG, "Error updating TCC attributes")
        }
    }

    fun validCredit(trip: Trip?, dispatch: InfoDispatchModel, callback: (Trip) -> Unit) {
        trip?.let {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
                    shiftStatusUseCase.getStatus()?.let { status ->
                        ticketUseCase.prepareTicket(
                            trip = trip,
                            dispatch = dispatch,
                            shiftStatus = status
                        )
                    }
                    navigatePostPayment()
                } catch (e: Exception) {
                    Logs.e(TAG, "Error validating credit")
                }
            }
        }
    }

    private fun navigatePostPayment() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val status =
                    shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
                status?.let { shiftStatusUseCase.setStatus(it, true) }
            } catch (e: Exception) {
                Logs.e(TAG, "Error navigating post-payment")
            }
        }
    }
}

// ----------------------------
// VIEWMODEL: SHARED BRIDGE
// ----------------------------

class TccMainActivityViewModel : ViewModel() {

    val _dispatchFlow = MutableStateFlow<InfoDispatchModel?>(null)
    val dispatchFlow: StateFlow<InfoDispatchModel?> = _dispatchFlow.asStateFlow()

    val _tripFlow = MutableStateFlow<Trip?>(null)
    val tripFlow: StateFlow<Trip?> = _tripFlow.asStateFlow()

    val _uiEffects = MutableSharedFlow<TccUiEffect>()
    val uiEffects: SharedFlow<TccUiEffect> = _uiEffects.asSharedFlow()

    val updatePrintFlowCallback: (Trip) -> Unit = { trip ->
        viewModelScope.launch {
            _tripFlow.value = trip
        }
    }

    fun setDispatch(dispatch: InfoDispatchModel?) {
        _dispatchFlow.value = dispatch
    }

    fun setTrip(trip: Trip?) {
        _tripFlow.value = trip
    }
}

// ----------------------------
// OPTIONAL: DESTINATION MAP VIEWMODEL
// ----------------------------

class DestinationMapViewModel(
    val dispatchUseCase: DispatchUseCase,
    val navigatorUseCase: NavigatorUseCase,
    context: Application,
) : BaseViewModel(context) {

    val TAG = "DestinationMapViewModel"

    val _navigatorFlow = MutableSharedFlow<Intent?>()
    val navigatorFlow = _navigatorFlow.asSharedFlow()

    fun openNavigatorApp(coordinates: String, street: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "openNavigatorApp: Coordinates = $coordinates, Street = $street")

            val navigatorIntent = when {
                !coordinates.isNullOrEmpty() && !street.isNullOrEmpty() -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with coordinates ($coordinates) and street: $street")
                    navigatorUseCase.openNavigatorWithCoordinatesAndStreetName(coordinates, street)
                }
                !coordinates.isNullOrEmpty() -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with coordinates ($coordinates)")
                    navigatorUseCase.openNavigatorWithCoordinates(coordinates)
                }
                !street.isNullOrEmpty() -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with street: $street")
                    navigatorUseCase.openNavigatorApp(street)
                }
                else -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator without specific parameters")
                    navigatorUseCase.openNavigatorApp()
                }
            }

            _navigatorFlow.emit(navigatorIntent)
        }
    }
}

// ----------------------------
// OPTIONAL: COMPOSE SHARED VIEWMODEL
// ----------------------------

class ComposeSharedViewModel(
    application: Application
) : BaseComposeViewModel(application) {

    val _hasTaximeterConnectionFlow = MutableStateFlow(false)
    val hasTaximeterConnectionFlow = _hasTaximeterConnectionFlow.asStateFlow()

    val _navigationDirectionsFlow = MutableSharedFlow<NavDirections>()
    val navigationDirectionsFlow = _navigationDirectionsFlow.asSharedFlow()

    val _navigationDeepLinkFlow = MutableSharedFlow<NavDeepLinkRequest>()
    val navigationDeepLinkFlow = _navigationDeepLinkFlow.asSharedFlow()

    val _pendingNavigationFlow = MutableStateFlow<Boolean?>(null)
    val pendingNavigationFlow = _pendingNavigationFlow.asStateFlow()

    val _xmppStatusFlow = MutableStateFlow(0)
    val xmppStatusFlow = _xmppStatusFlow.asStateFlow()

    val _gpsStatusFlow = MutableStateFlow(0)
    val gpsStatusFlow = _gpsStatusFlow.asStateFlow()

    val _showLegalTextFlow = MutableStateFlow<Boolean?>(null)
    val showLegalTextFlow = _showLegalTextFlow.asStateFlow()

    val _roofLightFlow = MutableStateFlow<Boolean?>(null)
    val roofLightFlow = _roofLightFlow.asStateFlow()

    val _userLoggedFlow = MutableStateFlow<Boolean?>(null)
    val userLoggedFlow = _userLoggedFlow.asStateFlow()

    val _clientButtonValueFlow = MutableStateFlow(false)
    val clientButtonValueFlow = _clientButtonValueFlow.asStateFlow()

    val _zoneFlow = MutableStateFlow<String?>(null)
    val zoneFlow = _zoneFlow.asStateFlow()

    val _locationEnabledFlow = MutableStateFlow(Pair(true, true))
    val locationEnabledFlow = _locationEnabledFlow.asStateFlow()

    val _locationManuallyDeLocatedFlow = MutableStateFlow(false)
    val locationManuallyDeLocatedFlow = _locationManuallyDeLocatedFlow.asStateFlow()

    val _locationType = MutableStateFlow("")
    val locationType = _locationType.asStateFlow()

    val _timeControlFlow = MutableStateFlow<String?>(null)
    val timeControlFlow = _timeControlFlow.asStateFlow()

    val _currentAmountFlow = MutableStateFlow<Int?>(null)
    val currentAmountFlow = _currentAmountFlow.asStateFlow()

    val _dispatchFlow = MutableStateFlow<InfoDispatchModel?>(null)
    val dispatchFlow = _dispatchFlow.asStateFlow()

    val _showDialogFlow = MutableSharedFlow<String>()
    val showDialogFlow = _showDialogFlow.asSharedFlow()

    val _loadingStateFlow = MutableSharedFlow<Boolean>()
    val loadingStateFlow = _loadingStateFlow.asSharedFlow()

    val _printTicketFlow = MutableSharedFlow<String>()
    val printTicketFlow = _printTicketFlow.asSharedFlow()

    val _cropImageFlow = MutableSharedFlow<android.net.Uri?>()
    val cropImageFlow = _cropImageFlow.asSharedFlow()

    val _sumUpFlow = MutableSharedFlow<Int>()
    val sumUpFlow = _sumUpFlow.asSharedFlow()

    val _sumUpResponseFlow = MutableSharedFlow<Pair<Int, Intent?>>()
    val sumUpResponseFlow = _sumUpResponseFlow.asSharedFlow()

    val _dispatchMinimumPriceFlow = MutableStateFlow<Int?>(null)
    val dispatchMinimumPriceFlow = _dispatchMinimumPriceFlow.asStateFlow()

    val _locatedOnStop = MutableStateFlow(false)
    val locatedOnStop = _locatedOnStop.asStateFlow()

    val _reconnectionFlow = MutableStateFlow(false)
    val reconnectionFlow = _reconnectionFlow.asStateFlow()

    val _orangeBtnPendingFlow = MutableStateFlow<Boolean?>(null)
    val orangeBtnPendingFlow = _orangeBtnPendingFlow.asStateFlow()

    val _shortBreakStatus = MutableStateFlow<String?>(null)
    val shortBreakStatus = _shortBreakStatus.asStateFlow()

    val _customerCallAvailable = MutableStateFlow(false)
    val customerCallAvailable = _customerCallAvailable.asStateFlow()

    val _customerCallButtonStateAvailable = MutableStateFlow(true)
    val customerCallButtonStateAvailable = _customerCallButtonStateAvailable.asStateFlow()

    val _lastITopStateReceived = MutableStateFlow<String?>(null)
    val lastITopStateReceived = _lastITopStateReceived.asStateFlow()
}
