package ifac.td.taxi.ui.screen.compose
import kotlinx.coroutines.flow.MutableStateFlow
import com.github.gcacace.signaturepad.views.SignaturePad
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.domain.usecase.TripUseCase
import kotlinx.coroutines.flow.asSharedFlow
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import kotlinx.coroutines.launch
import ifac.td.taxi.PaymentDirections
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.SignatureUseCase
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.update
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.viewinterop.AndroidView
import android.app.Application
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import androidx.lifecycle.lifecycleScope
import ifac.td.taxi.domain.usecase.SubscriberUseCase
import ifac.td.taxi.domain.model.PaymentMethod
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import ifac.td.taxi.ui.custom.button.CustomButton
import kotlinx.coroutines.flow.MutableSharedFlow
import ifac.td.taxi.R


// -----------------------------------------------------------------------------
// UI STATE / EVENTS / EFFECTS
// -----------------------------------------------------------------------------

data class SignatureUiState(
    val hasSigned: Boolean = false,
    val serviceId: String? = null,
    val buttons: SignatureButtonsState = SignatureButtonsState()
)

data class SignatureButtonsState(
    val clear: SignatureSignatureButtonVisualState = SignatureSignatureButtonVisualState(
        style = CustomButton.StyleButton.ENABLE,
        enabled = true,
        visible = true,
        background = CustomButton.BackgroundButtonColor.RED,
        textRes = R.string.clear,
        iconRes = R.drawable.trash,
        textVisible = true,
        iconVisible = true
    ),
    val accept: SignatureSignatureButtonVisualState = SignatureSignatureButtonVisualState(
        style = CustomButton.StyleButton.ENABLE,
        enabled = true,
        visible = true,
        background = CustomButton.BackgroundButtonColor.GREEN,
        textRes = R.string.btn_accept,
        iconRes = R.drawable.baseline_check_192,
        textVisible = true,
        iconVisible = true
    )
)

data class SignatureSignatureButtonVisualState(
    val style: CustomButton.StyleButton,
    val enabled: Boolean,
    val visible: Boolean,
    val background: CustomButton.BackgroundButtonColor,
    val textRes: Int,
    val iconRes: Int,
    val textVisible: Boolean,
    val iconVisible: Boolean
)

sealed interface SignatureUiEvent {
    data object OnStartSigning : SignatureUiEvent
    data object OnSigned : SignatureUiEvent
    data object OnCleared : SignatureUiEvent
    data object OnClearClicked : SignatureUiEvent
    data class OnAcceptClicked(
        val bitmap: Bitmap?,
        val serviceId: String
    ) : SignatureUiEvent
}

sealed interface SignatureUiEffect {
    data class ShowToast(val messageResId: Int) : SignatureUiEffect
}

// -----------------------------------------------------------------------------
// REDUCER
// Mirrors the fragment button logic one-by-one.
// -----------------------------------------------------------------------------

fun SignatureButtonsState.reduce(
    hasSigned: Boolean,
    acceptLoading: Boolean = false
): SignatureButtonsState {
    return copy(
        clear = clear.copy(
            style = CustomButton.StyleButton.ENABLE,
            enabled = true,
            visible = true,
            background = CustomButton.BackgroundButtonColor.RED,
            textRes = R.string.clear,
            iconRes = R.drawable.trash,
            textVisible = true,
            iconVisible = true
        ),
        accept = accept.copy(
            style = if (acceptLoading) CustomButton.StyleButton.LOADING else CustomButton.StyleButton.ENABLE,
            enabled = hasSigned,
            visible = true,
            background = CustomButton.BackgroundButtonColor.GREEN,
            textRes = R.string.btn_accept,
            iconRes = R.drawable.baseline_check_192,
            textVisible = true,
            iconVisible = true
        )
    )
}

// -----------------------------------------------------------------------------
// VIEWMODEL
// -----------------------------------------------------------------------------

open class BaseViewModel(val app: Application)

class SignatureViewModel(
    val signatureUseCase: SignatureUseCase,
    val subscriberUseCase: SubscriberUseCase,
    val shiftStatusUseCase: ShiftStatusUseCase,
    val tripUseCase: TripUseCase,
    val ticketUseCase: TicketUseCase,
    context: Application,
) : BaseViewModel(context) {

    val TAG = "SignatureViewModel"

    val _uiState = MutableStateFlow(SignatureUiState())
    val uiState: StateFlow<SignatureUiState> = _uiState.asStateFlow()

    val _signatureResponseFlow = MutableSharedFlow<Boolean>()
    val signatureResponseFlow: SharedFlow<Boolean> = _signatureResponseFlow.asSharedFlow()

    val _effects = MutableSharedFlow<SignatureUiEffect>()
    val effects: SharedFlow<SignatureUiEffect> = _effects.asSharedFlow()

    fun onEvent(event: SignatureUiEvent) {
        when (event) {
            SignatureUiEvent.OnStartSigning -> {
                Logs.d(TAG, "onEvent: OnStartSigning")
            }

            SignatureUiEvent.OnSigned -> {
                Logs.d(TAG, "onEvent: OnSigned")
                _uiState.update { it.copy(hasSigned = true, buttons = it.buttons.reduce(hasSigned = true)) }
            }

            SignatureUiEvent.OnCleared -> {
                Logs.d(TAG, "onEvent: OnCleared")
                _uiState.update { it.copy(hasSigned = false, buttons = it.buttons.reduce(hasSigned = false)) }
            }

            SignatureUiEvent.OnClearClicked -> {
                Logs.d(TAG, "onEvent: OnClearClicked")
                _uiState.update { it.copy(hasSigned = false, buttons = it.buttons.reduce(hasSigned = false)) }
            }

            is SignatureUiEvent.OnAcceptClicked -> {
                Logs.d(TAG, "onEvent: OnAcceptClicked")
                if (hasSigned()) {
                    _uiState.update {
                        it.copy(
                            serviceId = event.serviceId,
                            buttons = it.buttons.copy(
                                accept = it.buttons.accept.copy(style = CustomButton.StyleButton.LOADING)
                            )
                        )
                    }
                    sendSignature(
                        signatureBitmap = event.bitmap,
                        dispatchId = event.serviceId,
                        fromDispatch = false
                    )
                } else {
                    viewModelScope.launch {
                        _effects.emit(SignatureUiEffect.ShowToast(R.string.toast_sign_first))
                    }
                }
            }
        }
    }

    private var hasSigned = false

    fun hasSigned(): Boolean {
        Logs.d(TAG, "hasSigned: $hasSigned")
        return hasSigned
    }

    fun signedFlag(flag: Boolean) {
        Logs.d(TAG, "signedFlag: Setting hasSigned to $flag")
        hasSigned = flag
        _uiState.update { it.copy(hasSigned = flag, buttons = it.buttons.reduce(hasSigned = flag)) }
    }

    fun sendSignature(signatureBitmap: Bitmap?, dispatchId: String?, fromDispatch: Boolean) {
        viewModelScope.launch {
            Logs.d(TAG, "sendSignature: Sending signature for dispatchId: $dispatchId, fromDispatch: $fromDispatch")
            signatureUseCase.sendSignature(signatureBitmap, dispatchId, _signatureResponseFlow, fromDispatch)
            Logs.d(TAG, "sendSignature: Signature sent")
        }
    }

    fun sendSubscriberAuth(serviceId: String?, tripId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "sendSubscriberAuth: Starting subscriber authentication for serviceId: $serviceId")
            val accountPaymentRequest = subscriberUseCase.getAccountPaymentRequest(tripId)
            Logs.d(TAG, "sendSubscriberAuth: Retrieved AccountPaymentRequest: $accountPaymentRequest")
            accountPaymentRequest?.serviceId = serviceId
            subscriberUseCase.sendAccountPaymentRequest(accountPaymentRequest)
            Logs.d(TAG, "sendSubscriberAuth: Account payment request sent")
        }
    }

    fun dispatchSubscriberNextStep(dispatchValue: InfoDispatchModel, trip: Trip) {
        viewModelScope.launch {
            Logs.d(TAG, "dispatchSubscriberNextStep: Processing next step for dispatch: ${dispatchValue.longDispatchNumber}, tripId: ${trip.id}")

            if (dispatchValue.requireVoucher) {
                Logs.d(TAG, "dispatchSubscriberNextStep: Dispatch requires voucher, navigating to CropImageView")
                dispatchRequireVoucher(dispatchValue)
            } else if (dispatchValue.requireQr) {
                Logs.d(TAG, "dispatchSubscriberNextStep: Dispatch requires QR, navigating to ScannerQRFragment")
                dispatchRequireQR(dispatchValue)
            } else {
                Logs.d(TAG, "dispatchSubscriberNextStep: Ending trip with SUBSCRIBER payment method")
                tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)

                shiftStatusUseCase.getStatus()?.let { status ->
                    Logs.d(TAG, "dispatchSubscriberNextStep: Preparing ticket...")
                    ticketUseCase.prepareTicket(
                        trip = trip,
                        dispatch = dispatchValue,
                        shiftStatus = status,
                    )
                    Logs.d(TAG, "dispatchSubscriberNextStep: Ticket prepared")
                }

                val status = shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
                status?.let { statusWithSubStatus ->
                    Logs.d(TAG, "dispatchSubscriberNextStep: Setting shift status to: $statusWithSubStatus")
                    shiftStatusUseCase.setStatus(statusWithSubStatus, true)
                }
            }
        }
    }

    private suspend fun dispatchRequireVoucher(dispatch: InfoDispatchModel) {
        Logs.d(TAG, "dispatchRequireVoucher: Navigating to CropImageViewFragment for dispatch: ${dispatch.longDispatchNumber}")
        navigateTo(PaymentDirections.goToCropImageViewFragment(dispatch.longDispatchNumber))
    }

    private suspend fun dispatchRequireQR(dispatch: InfoDispatchModel) {
        Logs.d(TAG, "dispatchRequireQR: Navigating to ScannerQRFragment for dispatch: ${dispatch.longDispatchNumber}")
        navigateTo(PaymentDirections.goToScannerQRFragment(dispatch.longDispatchNumber))
    }

    fun onSignatureValidationResult(
        value: Boolean,
        serviceId: String?,
        dispatchFlow: InfoDispatchModel?,
        trip: Trip?
    ) {
        viewModelScope.launch {
            Logs.d(TAG, "onSignatureValidationResult: value=$value serviceId=$serviceId")
            if (value) {
                if (serviceId == dispatchFlow?.longDispatchNumber) {
                    Logs.d(TAG, "onSignatureValidationResult: Dispatching subscriber for serviceId: $serviceId")
                    if (dispatchFlow != null && trip != null) {
                        dispatchSubscriberNextStep(dispatchFlow, trip)
                    }
                } else {
                    Logs.d(TAG, "onSignatureValidationResult: Sending subscriber auth for serviceId: $serviceId")
                    sendSubscriberAuth(serviceId, trip?.id)
                }
            } else {
                Logs.d(TAG, "onSignatureValidationResult: Signature validation failed, setting subscriberFailPin to true")
                ifac.td.taxi.domain.utils.StaticConfiguration.subscriberFailPin = true
            }

            _effects.emit(
                SignatureUiEffect.ShowToast(
                    if (value) R.string.dialog_signature_valid_desc else R.string.dialog_signature_invalid_desc
                )
            )
        }
    }
}

// -----------------------------------------------------------------------------
// SHARED / ACTIVITY VIEWMODEL BRIDGE
// -----------------------------------------------------------------------------

class SignatureMainActivityViewModel(
    context: Application
) : BaseViewModel(context) {

    val _toastEvents = MutableSharedFlow<Int>()
    val toastEvents: SharedFlow<Int> = _toastEvents.asSharedFlow()

    fun showToast(messageResId: Int) {
        viewModelScope.launch {
            _toastEvents.emit(messageResId)
        }
    }
}

// -----------------------------------------------------------------------------
// COMPOSE SCREEN
// -----------------------------------------------------------------------------

@Composable
fun SignatureScreen(
    uiState: SignatureUiState,
    onEvent: (SignatureUiEvent) -> Unit,
    sharedViewModel: SignatureMainActivityViewModel,
    serviceId: String,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var signaturePadRef by remember { mutableStateOf<SignaturePad?>(null) }

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.toastEvents.collect { /* host bridge only */ }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.customer_signature),
                    modifier = Modifier.fillMaxWidth()
                )

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    factory = { ctx ->
                        SignaturePad(ctx).apply {
                            setPenColor(android.graphics.Color.BLACK)
                            setOnSignedListener(object : SignaturePad.OnSignedListener {
                                override fun onStartSigning() {
                                    onEvent(SignatureUiEvent.OnStartSigning)
                                }

                                override fun onSigned() {
                                    onEvent(SignatureUiEvent.OnSigned)
                                }

                                override fun onClear() {
                                    onEvent(SignatureUiEvent.OnCleared)
                                }
                            })
                            signaturePadRef = this
                        }
                    },
                    update = { pad ->
                        signaturePadRef = pad
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(5f / 2f)
                .background(Color.Black)
        ) {
            SignatureCustomButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(1.dp),
                customFunction = CustomButton.ButtonType.CLEAR,
                style = uiState.buttons.clear.style,
                enabled = uiState.buttons.clear.enabled,
                onClick = {
                    onEvent(SignatureUiEvent.OnClearClicked)
                    signaturePadRef?.clear()
                }
            )

            SignatureCustomButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(1.dp),
                customFunction = CustomButton.ButtonType.ACCEPT,
                style = uiState.buttons.accept.style,
                enabled = uiState.buttons.accept.enabled,
                onClick = {
                    val bitmap = signaturePadRef?.signatureBitmap
                    onEvent(SignatureUiEvent.OnAcceptClicked(bitmap, serviceId))
                }
            )
        }
    }
}

@Composable
private fun SignatureCustomButton(
    modifier: Modifier = Modifier,
    customFunction: CustomButton.ButtonType,
    style: CustomButton.StyleButton,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            CustomButton(ctx).apply {
                setButtonType(customFunction.value)
                setButtonStyle(style)
                setAction { onClick() }
            }
        },
        update = { button ->
            button.setButtonType(customFunction.value)
            button.setButtonStyle(style)
            button.setAction { onClick() }
        }
    )
}

// -----------------------------------------------------------------------------
// OPTIONAL ROUTE
// -----------------------------------------------------------------------------

@Composable
fun SignatureRoute(
    viewModel: SignatureViewModel,
    sharedViewModel: SignatureMainActivityViewModel,
    serviceId: String
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SignatureScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        sharedViewModel = sharedViewModel,
        serviceId = serviceId
    )
}
