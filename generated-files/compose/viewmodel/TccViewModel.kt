package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.TccButtonsState
import ifac.td.taxi.ui.screen.components.TccUiState
import ifac.td.taxi.ui.screen.components.TccUiEffect
import ifac.td.taxi.ui.screen.components.TccButtonState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 65-2: import android.app.Application
class TccComposeViewModel(
    context: Application,
    private val dispatchUseCase: DispatchUseCase,
    private val subscriberUseCase: SubscriberUseCase,
    private val tripUseCase: TripUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val ticketUseCase: TicketUseCase,
) : BaseViewModel(context) {
    private val TAG = "TccViewModel"
    private val _uiState = MutableStateFlow(TccUiState())
    val uiState: StateFlow<TccUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<TccUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<TccUiEffect> = _effects.asSharedFlow()
    fun loadDispatch(tripId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val dispatch = tripId?.let { dispatchUseCase.getDispatchByTripId(it) }
                if (dispatch == null) {
                    _effects.tryEmit(TccUiEffect.ToastRes(R.string.dialog_error_title))
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }
                val buttons = buildButtonsState(dispatch)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dispatch = dispatch,
                    buttonsState = buttons,
                    dialogState = TccDialogState.Hidden,
                    errorMessageRes = null
                )
            } catch (e: Exception) {
                Logs.e(TAG, "Error fetching dispatch")
                _uiState.value = _uiState.value.copy(isLoading = false)
                _effects.tryEmit(TccUiEffect.ToastRes(R.string.dialog_error_title))
            }
        }
    }
    fun onAcceptClicked(
        tripId: Long?,
        dispatch: Dispatch,
        att1: Int?,
        att2: Int?,
        att3: Int?,
        att4: Int?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                buttonsState = _uiState.value.buttonsState.copy(
                    accept = _uiState.value.buttonsState.accept.copy(loading = true, enabled = false)
                )
            )
            try {
                dispatch.attribute1Value = att1?.toString()
                dispatch.attribute2Value = att2?.toString()
                dispatch.attribute3Value = att3?.toString()
                dispatch.attribute4Value = att4?.toString()
                val dispatchId = dispatch.id?.toLong()
                if (dispatchId != null) {
                    updateTCCDispatch(dispatchId, att1?.toString(), att2?.toString(), att3?.toString(), att4?.toString())
                }
                val requiresSignature = dispatch.requisiteSignature == "1"
                val infoDispatch = dispatch.toInfoDispatchModel()
                if (requiresSignature) {
                    _effects.tryEmit(TccUiEffect.NavigateToSignature(infoDispatch.longDispatchNumber))
                } else {
                    finishTccService(
                        tripId = tripId,
                        dispatch = infoDispatch,
                    )
                }
            } catch (e: Exception) {
                Logs.e(TAG, "Error on accept clicked")
                _uiState.value = _uiState.value.copy(
                    buttonsState = _uiState.value.buttonsState.copy(
                        accept = _uiState.value.buttonsState.accept.copy(loading = false, enabled = true)
                    )
                )
                _effects.tryEmit(TccUiEffect.ToastRes(R.string.dialog_error_title))
            }
        }
    }
    fun onCreditLimitResult(
        validCredit: Boolean,
        trip: Trip?,
        dispatch: InfoDispatchModel
    ) {
        viewModelScope.launch {
            if (validCredit) {
                if (dispatch.requireSignature) {
                    dispatchRequireSignature(dispatch)
                } else if (dispatch.requireVoucher) {
                    dispatchRequireVoucher(dispatch)
                } else if (dispatch.requireQr) {
                    dispatchRequireQR(dispatch)
                } else {
                    validCredit(trip, dispatch)
                }
            } else {
                _effects.tryEmit(TccUiEffect.ToastRes(R.string.toast_not_enough_credit))
                _uiState.value = _uiState.value.copy(
                    buttonsState = _uiState.value.buttonsState.copy(
                        accept = _uiState.value.buttonsState.accept.copy(loading = false, enabled = true)
                    )
                )
            }
        }
    }
    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(dialogState = TccDialogState.Hidden)
    }
    fun buildButtonsState(dispatch: Dispatch): TccButtonsState {
        return TccButtonsState(
            accept = TccButtonState(visible = true, enabled = true, loading = false),
            cancel = TccButtonState(visible = true, enabled = true, loading = false),
            attributes1 = attributeState(dispatch.attribute1Title),
            attributes2 = attributeState(dispatch.attribute2Title),
            attributes3 = attributeState(dispatch.attribute3Title),
            attributes4 = attributeState(dispatch.attribute4Title),
        )
    }
    fun attributeState(title: String?): AttributeSpinnerState {
        val visible = !title.isNullOrEmpty()
        return AttributeSpinnerState(
            visible = visible,
            title = title.orEmpty(),
            value = 1
        )
    }
    fun getDispatch(tripId: Long?) = loadDispatch(tripId)
    private suspend fun updateTCCDispatch(
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
    fun finishTccService(
        tripId: Long?,
        dispatch: InfoDispatchModel?,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trip = tripId?.let { tripUseCase.getTripById(it) }
                trip?.let {
                    dispatch?.let { dispatchValue ->
                        if (dispatchValue.checkCreditLimit) {
                            handleCreditLimit(dispatchValue, trip)
                        } else {
                            proceedWithDispatch(dispatchValue, trip)
                        }
                    }
                }
            } catch (e: Exception) {
                Logs.e(TAG, "Error finishing TCC service")
                _uiState.value = _uiState.value.copy(
                    buttonsState = _uiState.value.buttonsState.copy(
                        accept = _uiState.value.buttonsState.accept.copy(loading = false, enabled = true)
                    )
                )
            }
        }
    }
    private suspend fun handleCreditLimit(dispatchValue: InfoDispatchModel, trip: Trip) {
        dispatchValue.subscriber?.let { manualSubscriber ->
            dispatchValue.subscriberUser?.let { manualUser ->
                val subscriber = ifac.td.taxi.domain.model.Subscriber(
                    manualSubscriber = manualSubscriber.toInt(),
                    manualUser = UtilsModel.trimUserFromString(manualUser).toInt(),
                    tripId = trip.id
                )
                subscriberUseCase.registerSubscriberAndAssignToTrip(subscriber)
                subscriberUseCase.checkCreditLimit(trip.totalAmount, object : kotlinx.coroutines.flow.MutableSharedFlow<String>() {})
            }
        }
    }
    private suspend fun proceedWithDispatch(dispatchValue: InfoDispatchModel, trip: Trip) {
        if (dispatchValue.requireSignature) {
            dispatchRequireSignature(dispatchValue)
        } else if (dispatchValue.requireVoucher) {
            dispatchRequireVoucher(dispatchValue)
        } else if (dispatchValue.requireQr) {
            dispatchRequireQR(dispatchValue)
        } else {
            endTrip(trip, dispatchValue)
        }
    }
    fun dispatchRequireSignature(dispatch: InfoDispatchModel) {
        viewModelScope.launch {
            _effects.emit(TccUiEffect.NavigateToSignature(dispatch.longDispatchNumber))
        }
    }
    fun dispatchRequireVoucher(dispatch: InfoDispatchModel) {
        viewModelScope.launch {
            _effects.emit(TccUiEffect.NavigateToVoucher(dispatch.longDispatchNumber))
        }
    }
    fun dispatchRequireQR(dispatch: InfoDispatchModel) {
        viewModelScope.launch {
            _effects.emit(TccUiEffect.NavigateToQr(dispatch.longDispatchNumber))
        }
    }
    private suspend fun endTrip(trip: Trip, dispatchValue: InfoDispatchModel) {
        tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
        shiftStatusUseCase.getStatus()?.let { status ->
            ticketUseCase.prepareTicket(
                trip = trip,
                dispatch = dispatchValue,
                shiftStatus = status
            )
        }
        navigatePostPayment()
    }
    fun navigatePostPayment() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val status = shiftStatusUseCase.checkForSubStateModifications(com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_FOR_HIRE)
                status?.let { shiftStatusUseCase.setStatus(it, true) }
            } catch (e: Exception) {
                Logs.e(TAG, "Error navigating post-payment")
            }
        }
    }
    fun validCredit(trip: Trip?, dispatch: InfoDispatchModel) {
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
                } finally {
                    _uiState.value = _uiState.value.copy(
                        buttonsState = _uiState.value.buttonsState.copy(
                            accept = _uiState.value.buttonsState.accept.copy(loading = false, enabled = true)
                        )
                    )
                }
            }
        }
    }
}
