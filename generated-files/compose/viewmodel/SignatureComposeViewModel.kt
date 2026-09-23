package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.SignatureUiState
import ifac.td.taxi.ui.screen.components.SignatureUiEvent
import ifac.td.taxi.ui.screen.components.SignatureUiEffect
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 79-2: import android.app.Application
class SignatureComposeViewModel(
    private val signatureUseCase: SignatureUseCase,
    private val subscriberUseCase: SubscriberUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val tripUseCase: TripUseCase,
    private val ticketUseCase: TicketUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SignatureUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<SignatureUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    private val _signatureResponse = MutableSharedFlow<Boolean>()
    val signatureResponse = _signatureResponse.asSharedFlow()
    fun setServiceId(serviceId: String?) {
        _uiState.update { it.copy(serviceId = serviceId) }
    }
    fun onEvent(event: SignatureUiEvent) {
        when (event) {
            is SignatureUiEvent.OnSignatureChanged -> {
                _uiState.update {
                    it.copy(
                        hasSigned = event.hasSigned,
                        signatureButtonsState = it.signatureButtonsState.copy(
                            accept = it.signatureButtonsState.accept.copy(enabled = event.hasSigned)
                        )
                    )
                }
            }
            SignatureUiEvent.OnClearClicked -> {
                _uiState.update {
                    it.copy(
                        hasSigned = false,
                        signatureButtonsState = it.signatureButtonsState.copy(
                            accept = it.signatureButtonsState.accept.copy(enabled = false, isLoading = false)
                        )
                    )
                }
            }
            SignatureUiEvent.OnAcceptClicked -> submitSignature()
            SignatureUiEvent.OnDialogDismissed -> {
                _uiState.update { it.copy(showClearDialog = false, dialogMessageResId = null, dialogType = null) }
            }
            SignatureUiEvent.OnDialogConfirmClicked -> {
                _uiState.update { it.copy(showClearDialog = false, dialogMessageResId = null, dialogType = null) }
            }
            is SignatureUiEvent.OnSignatureBitmapReady -> Unit
            is SignatureUiEvent.OnServiceIdResolved -> setServiceId(event.serviceId)
        }
    }
    fun submitSignature(signatureBitmap: Bitmap?, fromDispatch: Boolean) {
        val serviceId = uiState.value.serviceId
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    signatureButtonsState = it.signatureButtonsState.copy(
                        accept = it.signatureButtonsState.accept.copy(
                            isLoading = true,
                            enabled = false
                        ),
                        clear = it.signatureButtonsState.clear.copy(
                            enabled = false
                        )
                    )
                )
            }
            signatureUseCase.sendSignature(signatureBitmap, serviceId, _signatureResponse, fromDispatch)
        }
    }
    fun collectSignatureResult(
        isValid: Boolean,
        trip: Trip?,
        dispatch: InfoDispatchModel?,
        navigateToCrop: suspend (String) -> Unit,
        navigateToQr: suspend (String) -> Unit,
        showToast: suspend (Int) -> Unit,
    ) {
        viewModelScope.launch {
            if (isValid) {
                val serviceId = uiState.value.serviceId
                if (serviceId != null && dispatch != null && trip != null) {
                    if (serviceId == dispatch.longDispatchNumber) {
                        if (dispatch.requireVoucher) {
                            navigateToCrop(dispatch.longDispatchNumber)
                        } else if (dispatch.requireQr) {
                            navigateToQr(dispatch.longDispatchNumber)
                        } else {
                            tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
                            shiftStatusUseCase.getStatus()?.let { status ->
                                ticketUseCase.prepareTicket(trip = trip, dispatch = dispatch, shiftStatus = status)
                            }
                            shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
                                ?.let { shiftStatusUseCase.setStatus(it, true) }
                        }
                    } else {
                        sendSubscriberAuth(serviceId, trip.id)
                    }
                }
                showToast(ifac.td.taxi.R.string.dialog_signature_valid_desc)
            } else {
                showToast(ifac.td.taxi.R.string.dialog_signature_invalid_desc)
            }
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    signatureButtonsState = it.signatureButtonsState.copy(
                        accept = it.signatureButtonsState.accept.copy(isLoading = false, enabled = it.hasSigned),
                        clear = it.signatureButtonsState.clear.copy(enabled = true)
                    )
                )
            }
        }
    }
    fun sendSubscriberAuth(serviceId: String?, tripId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = subscriberUseCase.getAccountPaymentRequest(tripId)
            request?.serviceId = serviceId
            subscriberUseCase.sendAccountPaymentRequest(request)
        }
    }
    fun dispatchSubscriberNextStep(dispatchValue: InfoDispatchModel, trip: Trip) {
        viewModelScope.launch {
            if (dispatchValue.requireVoucher) {
                _uiEffect.emit(SignatureUiEffect.NavigateTo(PaymentDirections.goToCropImageViewFragment(dispatchValue.longDispatchNumber)))
            } else if (dispatchValue.requireQr) {
                _uiEffect.emit(SignatureUiEffect.NavigateTo(PaymentDirections.goToScannerQRFragment(dispatchValue.longDispatchNumber)))
            } else {
                tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
                shiftStatusUseCase.getStatus()?.let { status ->
                    ticketUseCase.prepareTicket(trip = trip, dispatch = dispatchValue, shiftStatus = status)
                }
                shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
                    ?.let { shiftStatusUseCase.setStatus(it, true) }
            }
        }
    }
}
