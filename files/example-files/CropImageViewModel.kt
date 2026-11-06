package ifac.td.taxi.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.PaymentDirections
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.SendImageUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.SubscriberUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CropImageViewModel(
    context: Application,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val tripUseCase: TripUseCase,
    private val subscriberUseCase: SubscriberUseCase,
    private val sendImageUseCase: SendImageUseCase,
    private val ticketUseCase: TicketUseCase
) : BaseViewModel(context) {

    private val TAG = "CropImageViewModel"

    private val _uriImageFlow = MutableStateFlow<Uri?>(null)
    val uriImageFlow = _uriImageFlow.asStateFlow()

    private val _uploadImageCallbackFlow = MutableSharedFlow<Boolean>()
    val uploadImageCallbackFlow = _uploadImageCallbackFlow.asSharedFlow()

    private var fileName: String = ""

    fun setImageUri(uri: Uri) { _uriImageFlow.value = uri }

    fun saveFileFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "saveFileFromUri: Starting for URI: $uri")
            setImageUri(uri)

            val numSubscriber = subscriberUseCase.getSubscriber()?.numberSubscriberFromW2C
            Logs.d(TAG, "saveFileFromUri: numSubscriber: $numSubscriber")

            fileName = generateFileName(false, numSubscriber ?: "")
            Logs.d(TAG, "saveFileFromUri: filename: $fileName")

            val bitmap = getBitmapFromUri(uri)
            Logs.d(TAG, "saveFileFromUri: Retrieved bitmap: ${bitmap != null}")

            val rescaledBitmap = bitmap?.let { scaleBitmap(it, 560) }
            Logs.d(TAG, "saveFileFromUri: Rescaled bitmap: ${rescaledBitmap != null}")

            val directory = context.getExternalFilesDir(null)
            val outputFile = File(directory, fileName)
            Logs.d(TAG, "saveFileFromUri: Saving to: ${outputFile.absolutePath}")

            FileOutputStream(outputFile).use { outputStream ->
                rescaledBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }

            Logs.d(TAG, "saveFileFromUri: File saved successfully")
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, newHeight: Int): Bitmap? {
        return try {
            Logs.d(TAG, "scaleBitmap: Scaling bitmap to height: $newHeight")

            val originalWidth = bitmap.width
            val originalHeight = bitmap.height

            val scaleFactor = newHeight.toFloat() / originalHeight

            val newWidth = (originalWidth * scaleFactor).toInt()
            Logs.d(TAG, "scaleBitmap: New dimensions: Width = $newWidth, Height = $newHeight")

            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } catch (e: Exception) {
            Logs.e(TAG, "scaleBitmap: Error scaling bitmap: $e")
            null
        }
    }


    private fun generateFileName(isBill: Boolean, numSubscriber: String): String {
        val nameType = if (isBill) "FACTURA" else "VALE"

        val currentDate = SimpleDateFormat("yyyy-MM-dd HH_mm_ss", Locale.US).format(Date())

        val filename = "${nameType}#${numSubscriber}#${currentDate}#canon.png"
        Logs.d(TAG, "generateFileName: Generated filename: $filename")

        return filename
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        Logs.d(TAG, "getBitmapFromUri: Getting bitmap from URI: $uri")
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }

    fun sendImageToAlpha(serviceId: String?) {
        viewModelScope.launch {
            Logs.d(TAG, "sendImageToAlpha: Sending image for serviceId: $serviceId")
            val bitmap = uriImageFlow.value?.let { getBitmapFromUri(it) } ?: run {
                Logs.e(TAG, "sendImageToAlpha: No image URI set, cannot send image")
                return@launch
            }

            Logs.d(TAG, "sendImageToAlpha: Retrieved bitmap: ${bitmap != null}")

            sendImageUseCase.sendImageToAlfa(serviceId, bitmap, fileName, _uploadImageCallbackFlow)
            Logs.d(TAG, "sendImageToAlpha: Image sent to Alfa")
        }
    }

    fun dispatchSubscriberNextStep(
        dispatchValue: InfoDispatchModel,
        trip: Trip,
        callback: (Trip) -> Unit
    ) {
        viewModelScope.launch {
            Logs.d(TAG, "dispatchSubscriberNextStep: Dispatch value: $dispatchValue, Trip ID: ${trip.id}")
            if (dispatchValue.requireQr) {
                Logs.d(TAG, "dispatchSubscriberNextStep: QR required, navigating to scanner")
                dispatchRequireQR(dispatchValue)
            } else {
                Logs.d(TAG, "dispatchSubscriberNextStep: Ending trip with subscriber payment method")
                tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)

                shiftStatusUseCase.getStatus()?.let {
                    Logs.d(TAG, "dispatchSubscriberNextStep: Preparing ticket...")
                    ticketUseCase.prepareTicket(
                        trip = trip,
                        dispatch = dispatchValue,
                        shiftStatus = it
                    )
                    Logs.d(TAG, "dispatchSubscriberNextStep: Ticket prepared")
                }

                val status =
                    shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)

                Logs.d(TAG, "dispatchSubscriberNextStep: Setting shift status: $status")
                shiftStatusUseCase.setStatus(status, true)
            }
        }
    }

    private suspend fun dispatchRequireQR(dispatch: InfoDispatchModel) {
        Logs.d(TAG, "dispatchRequireQR: Navigating to QR scanner for dispatch: ${dispatch.longDispatchNumber}")
        navigateTo(PaymentDirections.goToScannerQRFragment(dispatch.longDispatchNumber))
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

    fun isPayment(currentStatus: Int): Boolean {
         return shiftStatusUseCase.isPayment(currentStatus)
    }

    fun isHired(currentStatus: Int): Boolean {
        return shiftStatusUseCase.isHired(currentStatus)
    }

}
