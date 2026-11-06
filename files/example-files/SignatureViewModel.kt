package ifac.td.taxi.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.PaymentDirections
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.SignatureUseCase
import ifac.td.taxi.domain.usecase.SubscriberUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SignatureViewModel(
    private val signatureUseCase: SignatureUseCase,
    private val subscriberUseCase: SubscriberUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val tripUseCase: TripUseCase,
    private val ticketUseCase: TicketUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "SignatureViewModel"

    private val _signatureResponseFlow = MutableSharedFlow<Boolean>()
    val signatureResponseFlow = _signatureResponseFlow.asSharedFlow()

    private var hasSigned = false

    fun hasSigned(): Boolean {
        Logs.d(TAG, "hasSigned: $hasSigned")
        return hasSigned
    }

    fun signedFlag(flag: Boolean) {
        Logs.d(TAG, "signedFlag: Setting hasSigned to $flag")
        hasSigned = flag
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
}
