package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.model.VoucherQR
import ifac.td.taxi.domain.usecase.SubscriberUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.extension.EventFlow
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.launch

class ScannerQrViewModel(
    context: Application,
    private val subscriberUseCase: SubscriberUseCase,
    private val ticketUseCase: TicketUseCase,
) : BaseViewModel(context) {

    private val TAG = "ScannerQrViewModel"

    val openDialogQrFlow =
        EventFlow<Pair<CustomDialog.CustomDialogModel, (CustomDialog.CustomDialogResponse) -> Unit>>()

    fun uploadVoucherId(
        serviceId: String?,
        qrVoucher: VoucherQR?,
        infoDispatchModel: InfoDispatchModel?,
        trip: Trip?,
        updatePrintFlowCallback: suspend (Trip) -> Unit,
        navigateFunction: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            Logs.d(TAG, "uploadVoucherId: Starting voucher upload for serviceId: $serviceId, tripId: ${trip?.id}, voucherId: ${qrVoucher?.id}")

            subscriberUseCase.uploadVoucherId(
                serviceId,
                qrVoucher,
                infoDispatchModel,
                trip,
                navigateFunction,
                { trip, dispatch, status ->
                    viewModelScope.launch {
                        if (trip != null) {
                            Logs.d(TAG, "uploadVoucherId: Preparing ticket for trip: ${trip.id}")
                            ticketUseCase.prepareTicket(
                                trip,
                                dispatch,
                                status,
                            )
                        } else {
                            Logs.d(TAG, "uploadVoucherId: Trip is null, skipping ticket preparation")
                        }
                    }
                },
                openDialogQrFlow
            )
        }
    }
}