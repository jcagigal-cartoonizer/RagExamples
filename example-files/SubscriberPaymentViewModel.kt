package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.PaymentDirections
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.domain.model.ShiftStatus
import ifac.td.taxi.domain.model.Subscriber
import ifac.td.taxi.domain.model.SubscriberQR
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.model.VoucherQR
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.SubscriberUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.dialog.CustomDialog.CustomDialogTAG.SUBSCRIBER_RESPONSE_SUCCESS_DIALOG
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.trimUserFromString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SubscriberPaymentViewModel(
    private val subscriberUseCase: SubscriberUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val tripUseCase: TripUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val ticketUseCase: TicketUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "SubscriberPaymentViewModel"

    var shiftStatus: ShiftStatus? = null

    private val _subscriberFlow = MutableStateFlow<Subscriber?>(null)
    val subscriberFlow: StateFlow<Subscriber?> = _subscriberFlow.asStateFlow()

    private val _checkSubscriberCreditLimitFlow = MutableSharedFlow<String>()
    val checkSubscriberCreditLimitFlow = _checkSubscriberCreditLimitFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            shiftStatus = shiftStatusUseCase.getStatus()
            _subscriberFlow.emit(subscriberUseCase.getSubscriber())
        }
    }

    fun insertSubscriber(subscriber: Subscriber) {
        viewModelScope.launch {
            subscriberUseCase.registerSubscriberAndAssignToTrip(subscriber)
        }
    }

    fun subscriberLogic(tripId: Long, subscriber: Subscriber, isFromIngenico: Boolean = false) {
        viewModelScope.launch {
            if (!hasScannedQR) {
                Logs.d(TAG, "subscriberLogic: hasScannedQR is false, saving null voucher QR")
                subscriberUseCase.saveVoucherQR(null)
            }

            val accountPaymentRequest = subscriberUseCase.getAccountPaymentRequest(tripId, subscriber)
            Logs.d(TAG, "subscriberLogic: Account payment request created")

            if (isFromIngenico) {
                accountPaymentRequest?._abonadoConTarjeta = true
                Logs.d(TAG, "subscriberLogic: Setting _abonadoConTarjeta to true (from Ingenico)")
            }

            subscriberUseCase.sendAccountPaymentRequest(accountPaymentRequest)
            Logs.d(TAG, "subscriberLogic: Account payment request sent")
        }
    }


    fun subscriberFromDispatch(actualDispatch: InfoDispatchModel, actualTrip: Trip, subscriber: Subscriber?) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "subscriberFromDispatch: Starting subscriberFromDispatch with checkCreditLimit: ${actualDispatch.checkCreditLimit}")

            if (!hasScannedQR) subscriberUseCase.saveVoucherQR(null)

            val manualSubscriberId = actualDispatch.subscriber?.toIntOrNull()
            val manualUserId =
                actualDispatch.subscriberUser?.let { trimUserFromString(it).toIntOrNull() }

            val validSubscriber: Subscriber? =
                if (manualSubscriberId != null && manualUserId != null && manualSubscriberId > 0 && manualUserId > 0) {
                    Logs.d(TAG, "subscriberFromDispatch: Using manual data from actualDispatch.")
                    Subscriber(
                        manualSubscriber = manualSubscriberId,
                        manualUser = manualUserId,
                        tripId = actualTrip.id
                    )
                } else if (subscriber?.manualSubscriber != null && subscriber?.manualUser != null && subscriber.manualSubscriber > 0 && subscriber.manualUser > 0) {
                    Logs.d(TAG, "subscriberFromDispatch: Using data from subscriber parameter.")
                    subscriber.copy(tripId = actualTrip.id)
                } else {
                    Logs.d(TAG, "subscriberFromDispatch: No valid subscriber data found.")
                    null
                }

            if (validSubscriber != null) {
                Logs.d(TAG, "subscriberFromDispatch: Processing subscriber: $validSubscriber")
                subscriberUseCase.updateSubscriber(validSubscriber)

                if (actualDispatch.checkCreditLimit) {
                    Logs.d(TAG, "subscriberFromDispatch: Checking credit limit for amount: ${actualTrip.totalAmount}")
                    subscriberUseCase.checkCreditLimit(
                        actualTrip.totalAmount,
                        _checkSubscriberCreditLimitFlow
                    )
                } else {
                    Logs.d(TAG, "subscriberFromDispatch: Skipping credit check, proceeding to next step")
                    dispatchSubscriberNextStep(actualDispatch, actualTrip)
                }
            } else {
                Logs.e(TAG, "subscriberFromDispatch: Invalid subscriber or user ID. Payment cannot proceed.")

                showDialog(
                    Pair(
                        CustomDialog.CustomDialogModel(
                            title = context.getString(R.string.dialog_error_subscriber),
                            buttons = arrayListOf(ButtonType.ACCEPT),
                            dialogTAG = CustomDialog.CustomDialogTAG.SUBSCRIBER_RESPONSE_ERROR_DIALOG,
                            isCancellable = false,
                        ),
                    ) { response ->
                    }
                )
            }
        }
    }

    suspend fun dispatchRequireSignature(dispatch: InfoDispatchModel) {
        navigateTo(PaymentDirections.goToSignatureFragment(dispatch.longDispatchNumber))
    }

    suspend fun dispatchRequireVoucher(dispatch: InfoDispatchModel) {
        navigateTo(PaymentDirections.goToCropImageViewFragment(dispatch.longDispatchNumber))
    }

    suspend fun dispatchRequireQR(dispatch: InfoDispatchModel) {
        navigateTo(PaymentDirections.goToScannerQRFragment(dispatch.longDispatchNumber))
    }

    fun validCredit(trip: Trip?, callback: (Trip) -> Unit) {
        viewModelScope.launch {
            trip?.let {
                withContext(Dispatchers.IO) {
                    tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
                }

                shiftStatusUseCase.getStatus()?.let { status ->
                    ticketUseCase.prepareTicket(
                        trip = trip,
                        dispatch = null,
                        shiftStatus = status,
                    )
                }

                //Navigation
                val status =
                    shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
                status?.let { statusWithSubStatus ->
                    shiftStatusUseCase.setStatus(statusWithSubStatus, true)
                }
            }
        }
    }

    private fun dispatchSubscriberNextStep(dispatchValue: InfoDispatchModel, trip: Trip) {
        viewModelScope.launch {
            if (dispatchValue.requireSignature) {
                Logs.d(TAG, "dispatchSubscriberNextStep: Requires signature")
                dispatchRequireSignature(dispatchValue)
            } else if (dispatchValue.requireVoucher) {
                Logs.d(TAG, "dispatchSubscriberNextStep: Requires voucher")
                dispatchRequireVoucher(dispatchValue)
            } else if (dispatchValue.requireQr) {
                Logs.d(TAG, "dispatchSubscriberNextStep: Requires QR")
                dispatchRequireQR(dispatchValue)
            } else {
                Logs.d(TAG, "dispatchSubscriberNextStep: Ending trip")
                tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)

                shiftStatusUseCase.getStatus()?.let { status ->
                    ticketUseCase.prepareTicket(
                        trip = trip,
                        dispatch = dispatchValue,
                        shiftStatus = status,
                    )
                }

                val status = shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
                status?.let { statusWithSubStatus ->
                    Logs.d(TAG, "dispatchSubscriberNextStep: Setting status")
                    shiftStatusUseCase.setStatus(statusWithSubStatus, true)
                }

                showDialog(
                    Pair(
                        CustomDialog.CustomDialogModel(
                            title = context.getString(R.string.operation_authorized),
                            buttons = arrayListOf(ButtonType.ACCEPT),
                            dialogTAG = SUBSCRIBER_RESPONSE_SUCCESS_DIALOG,
                            isCancellable = false,
                        ),
                    ) { response ->

                    }
                )
                
                
                Logs.d(TAG, "dispatchSubscriberNextStep: Navigating to home fragment")
                navigateTo(PaymentDirections.goToHomeFragment())
            }
        }
    }


    private var hasScannedQR = false

    var scannedCompanyId: Int? = null

    fun saveVoucherQR(voucherQR: VoucherQR?) {
        scannedCompanyId = voucherQR?.companyId
        viewModelScope.launch {
            hasScannedQR = true
            Logs.d(TAG, "Saving voucherQR: $voucherQR")
            subscriberUseCase.saveVoucherQR(voucherQR)
        }
    }

    fun saveSubscriberQR(subscriberQR: SubscriberQR?) {
        scannedCompanyId = subscriberQR?.companyId
        Logs.d(TAG, "saveSubscriberQR: companyId = $scannedCompanyId")
    }

    fun cancelSubscriberPetition() {
        viewModelScope.launch {
            Logs.d(TAG, "cancelSubscriberPetition: Canceling subscriber petition")
            bravoCentralUseCase.cancelCredit()
        }
    }
}