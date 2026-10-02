package ifac.td.taxi.compose.viewmodel
import  android.app.Application
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// # Block 43-2: import android.app.Application
class CropImageComposeViewModel(
    context: Application,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val tripUseCase: TripUseCase,
    private val subscriberUseCase: SubscriberUseCase,
    private val sendImageUseCase: SendImageUseCase,
    private val ticketUseCase: TicketUseCase
) : BaseViewModel(context) {
    private val _uiState = MutableStateFlow(CropImageUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<CropImageUiEffect>()
    val uiEffect: SharedFlow<CropImageUiEffect> = _uiEffect.asSharedFlow()
    private var fileName: String = ""
    fun onEvent(event: CropImageUiEvent) {
        when (event) {
            is CropImageUiEvent.ImageSelected -> {
                _uiState.update { it.copy(imageUri = event.uri) }
            }
            CropImageUiEvent.ClickCrop -> {
                viewModelScope.launch { _uiEffect.emit(CropImageUiEffect.OpenCropper) }
            }
            CropImageUiEvent.ClickSelectImage -> {
                viewModelScope.launch { _uiEffect.emit(CropImageUiEffect.OpenImageSelector) }
            }
            CropImageUiEvent.ClickAccept -> {
                val currentUri = uiState.value.imageUri
                if (currentUri == null) {
                    viewModelScope.launch {
                        _uiEffect.emit(CropImageUiEffect.ShowToast(R.string.dialog_voucher_required))
                    }
                } else {
                    sendImageToAlpha(uiState.value.serviceId)
                }
            }
            CropImageUiEvent.DismissDialog -> {
                _uiState.update { it.copy(showConfirmSendDialog = false, showErrorDialog = false) }
            }
            CropImageUiEvent.ConfirmSend -> {
                _uiState.update { it.copy(showConfirmSendDialog = false) }
                sendImageToAlpha(uiState.value.serviceId)
            }
            CropImageUiEvent.PermissionGranted -> Unit
        }
    }
    fun setServiceId(serviceId: String?) {
        _uiState.update { it.copy(serviceId = serviceId) }
    }
    fun setImageUri(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri) }
    }
    fun saveFileFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            setImageUri(uri)
            val numSubscriber = subscriberUseCase.getSubscriber()?.numberSubscriberFromW2C
            fileName = generateFileName(false, numSubscriber ?: "")
            val bitmap = getBitmapFromUri(uri)
            val rescaledBitmap = bitmap?.let { scaleBitmap(it, 560) }
            val directory = context.getExternalFilesDir(null)
            val outputFile = File(directory, fileName)
            FileOutputStream(outputFile).use { outputStream ->
                rescaledBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
        }
    }
    fun sendImageToAlpha(serviceId: String?) {
        viewModelScope.launch {
            val bitmap = uiState.value.imageUri?.let { getBitmapFromUri(it) }
                ?: return@launch
            sendImageUseCase.sendImageToAlfa(serviceId, bitmap, fileName, uploadResult = null)
            // If your use case already emits callback flow, keep that flow handling here.
        }
    }
    fun handleUploadResult(value: Boolean) {
        viewModelScope.launch {
            if (value) {
                val currentStatus = shiftStatusUseCase.getStatus()?.currentStatus ?: 0
                val hired = isHired(currentStatus)
                if (isVacant() || hired) {
                    _uiEffect.emit(CropImageUiEffect.NavigateBack)
                }
                val dispatch = sharedDispatch()
                val trip = sharedTrip()
                if (uiState.value.serviceId == dispatch?.longDispatchNumber?.toString()) {
                    if (dispatch != null && trip != null) {
                        dispatchSubscriberNextStep(dispatch, trip, sharedUpdatePrintCallback())
                    }
                } else if (isPayment(currentStatus)) {
                    sendSubscriberAuth(uiState.value.serviceId, trip?.id)
                }
            } else {
                _uiEffect.emit(CropImageUiEffect.ShowToast(R.string.dialog_error_send_image_description))
            }
        }
    }
    fun requestCameraPermission() {
        viewModelScope.launch { _uiEffect.emit(CropImageUiEffect.RequestCameraPermission) }
    }
    fun openCropper() {
        viewModelScope.launch { _uiEffect.emit(CropImageUiEffect.OpenCropper) }
    }
    fun openImageSelector() {
        viewModelScope.launch { _uiEffect.emit(CropImageUiEffect.OpenImageSelector) }
    }
    fun isPayment(currentStatus: Int): Boolean = shiftStatusUseCase.isPayment(currentStatus)
    fun isHired(currentStatus: Int): Boolean = shiftStatusUseCase.isHired(currentStatus)
    fun scaleBitmap(bitmap: Bitmap, newHeight: Int): Bitmap? = try {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val scaleFactor = newHeight.toFloat() / originalHeight
        val newWidth = (originalWidth * scaleFactor).toInt()
        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    } catch (_: Exception) { null }
    fun generateFileName(isBill: Boolean, numSubscriber: String): String {
        val nameType = if (isBill) "FACTURA" else "VALE"
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH_mm_ss", Locale.US).format(Date())
        return "${nameType}#${numSubscriber}#${currentDate}#canon.png"
    }
    fun getBitmapFromUri(uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    }
    fun dispatchSubscriberNextStep(
        dispatchValue: InfoDispatchModel,
        trip: Trip,
        callback: (Trip) -> Unit
    ) {
        viewModelScope.launch {
            if (dispatchValue.requireQr) {
                _uiEffect.emit(CropImageUiEffect.OpenScannerQr(dispatchValue.longDispatchNumber))
            } else {
                tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
                shiftStatusUseCase.getStatus()?.let {
                    ticketUseCase.prepareTicket(trip = trip, dispatch = dispatchValue, shiftStatus = it)
                }
                val status = shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
                shiftStatusUseCase.setStatus(status, true)
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
    fun isVacant(): Boolean = sharedShiftStatus() == 0
    fun sharedShiftStatus(): Int = shiftStatusUseCase.getStatus()?.currentStatus ?: 0
    fun sharedDispatch() = sharedDispatchFlowValue()
    fun sharedTrip() = sharedTripFlowValue()
    fun sharedUpdatePrintCallback(): (Trip) -> Unit = { }
    fun sharedDispatchFlowValue(): InfoDispatchModel? = null
    fun sharedTripFlowValue(): Trip? = null
}
