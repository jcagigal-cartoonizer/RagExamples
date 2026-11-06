package ifac.td.taxi.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.PaymentDirections
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.Dispatch
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.domain.model.Subscriber
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.SubscriberUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import ifac.td.taxi.viewmodel.model.UtilsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TccViewModel(
    context: Application,
    private val dispatchUseCase: DispatchUseCase,
    private val subscriberUseCase: SubscriberUseCase,
    private val tripUseCase: TripUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val ticketUseCase: TicketUseCase,
) : BaseViewModel(context) {

    private val _dispatchModelFlow = MutableSharedFlow<Dispatch?>()
    val dispatchModelFlow = _dispatchModelFlow.asSharedFlow()

    private val _checkSubscriberCreditLimitFlow = MutableSharedFlow<String>()
    val checkSubscriberCreditLimitFlow = _checkSubscriberCreditLimitFlow.asSharedFlow()

    private val TAG = "TccViewModel"

    fun getDispatch(id: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            id?.let {
                try {
                    val dispatch = dispatchUseCase.getDispatchByTripId(it)
                    Logs.d(TAG, "getDispatch $dispatch")
                    _dispatchModelFlow.emit(dispatch)
                } catch (e: Exception) {
                    Logs.e(TAG, "Error fetching dispatch")
                }
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
                            Logs.d(TAG, "Checking credit limit")
                            handleCreditLimit(dispatchValue, trip)
                        } else {
                            Logs.d(TAG, "Not checking credit limit")
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
        Logs.d(TAG, "Checking subscriber credit limit")
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
            Logs.d(TAG, "Inserted subscriber: $subscriber")
            subscriberUseCase.checkCreditLimit(trip.totalAmount, _checkSubscriberCreditLimitFlow)
        }
    }

    private suspend fun proceedWithDispatch(
        dispatchValue: InfoDispatchModel,
        trip: Trip,
        callback: (Trip) -> Unit,
    ) {
        Logs.d(TAG, "Proceeding with dispatch")
        if (dispatchValue.requireSignature) {
            dispatchRequireSignature(dispatchValue)
        } else if (dispatchValue.requireVoucher) {
            dispatchRequireVoucher(dispatchValue)
        } else if (dispatchValue.requireQr) {
            dispatchRequireQR(dispatchValue)
        } else {
            endTrip(trip, dispatchValue, callback)
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

    private suspend fun endTrip(
        trip: Trip,
        dispatchValue: InfoDispatchModel,
        callback: (Trip) -> Unit,
    ) {
        Logs.d(TAG, "Ending trip and navigating post-payment")
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

    fun updateDispatch(dispatch: Dispatch) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Logs.d(TAG, "Updating Dispatch")
                dispatchUseCase.updateDispatch(dispatch)
            } catch (e: Exception) {
                Logs.e(TAG, "Error updating dispatch")
            }
        }
    }

    suspend fun updateTCCDispatch(dispatchId: Long, att1: String?, att2: String?, att3: String?, att4: String?) {
       try {
           Logs.d(TAG, "Updating TCC attributes for dispatch $dispatchId")
           dispatchUseCase.updateDispatchTCCAttributes(dispatchId, att1, att2, att3, att4)
       } catch (e: Exception) {
           Logs.e(TAG, "Error updating TCC attributes")
       }
    }

    fun validCredit(trip: Trip?, dispatch: InfoDispatchModel, callback: (Trip) -> Unit) {
        trip?.let {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    Logs.d(TAG, "Handling valid credit for trip: ${trip.id}")
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
}
