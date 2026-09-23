package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.CropImageUiEvent
import ifac.td.taxi.ui.screen.components.CropImageUiState
import ifac.td.taxi.ui.screen.components.CropImageUiEffect
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 115-5: import android.app.Application
class CropImageComposeViewModel(
    context: Application,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val tripUseCase: TripUseCase,
    private val subscriberUseCase: SubscriberUseCase,
    private val sendImageUseCase: SendImageUseCase,
    private val ticketUseCase: TicketUseCase
) : BaseViewModel(context) {
    private val TAG = "CropImageViewModel"
    private val _uiState = MutableStateFlow(CropImageUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<CropImageUiEffect>()
    val uiEffect: SharedFlow<CropImageUiEffect> = _uiEffect.asSharedFlow()
    private var fileName: String = ""
    fun setServiceId(serviceId: String?) {
        _uiState.update { it.copy(serviceId = serviceId) }
    }
    fun setImageUri(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri) }
    }
    fun onEvent(event: CropImageUiEvent) {
        when (event) {
            is CropImageUiEvent.ServiceIdChanged -> setServiceId(event.serviceId)
            is CropImageUiEvent.ImageSelected -> saveFileFromUri(event.uri)
            CropImageUiEvent.AcceptClicked -> onAcceptClicked()
            CropImageUiEvent.CropClicked -> onCropClicked()
            CropImageUiEvent.SelectImageClicked -> emitEffect(CropImageUiEffect.RequestImageSelect)
            CropImageUiEvent.DialogDismissed -> _uiState.update { it.copy(dialogState = CropImageCustomDialogState.Hidden) }
            CropImageUiEvent.DialogConfirmed -> _uiState.update { it.copy(dialogState = CropImageCustomDialogState.Hidden) }
        }
    }
    fun onAcceptClicked() {
        val uri = uiState.value.imageUri
        if (uri == null) {
            viewModelScope.launch {
                _uiEffect.emit(CropImageUiEffect.ShowToast(R.string.dialog_voucher_required))
            }
            return
        }
        sendImageToAlpha(uiState.value.serviceId)
    }
    fun onCropClicked() {
        viewModelScope.launch {
            _uiEffect.emit(CropImageUiEffect.RequestCropImage(uiState.value.imageUri))
        }
    }
    fun saveFileFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "saveFileFromUri: Starting for URI: $uri")
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
            Logs.d(TAG, "saveFileFromUri: File saved successfully")
        }
    }
    fun scaleBitmap(bitmap: Bitmap, newHeight: Int): Bitmap? {
        return try {
            val originalWidth = bitmap.width
            val originalHeight = bitmap.height
            val scaleFactor = newHeight.toFloat() / originalHeight
            val newWidth = (originalWidth * scaleFactor).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } catch (e: Exception) {
            Logs.e(TAG, "scaleBitmap: Error scaling bitmap: $e")
            null
        }
    }
    fun generateFileName(isBill: Boolean, numSubscriber: String): String {
        val nameType = if (isBill) "FACTURA" else "VALE"
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH_mm_ss", Locale.US).format(Date())
        return "${nameType}#${numSubscriber}#${currentDate}#canon.png"
    }
    fun getBitmapFromUri(uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }
    fun sendImageToAlpha(serviceId: String?) {
        viewModelScope.launch {
            val bitmap = uiState.value.imageUri?.let { getBitmapFromUri(it) }
            if (bitmap == null) {
                _uiEffect.emit(CropImageUiEffect.ShowToast(R.string.dialog_error_send_image_description))
                return@launch
            }
            sendImageUseCase.sendImageToAlfa(
                serviceId = serviceId,
                bitmap = bitmap,
                fileName = fileName,
                callback = _uploadResult
            )
        }
    }
    private val _uploadResult = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    init {
        viewModelScope.launch {
            _uploadResult.collect { success ->
                handleUploadResult(success)
            }
        }
    }
    private suspend fun handleUploadResult(success: Boolean) {
        if (success) {
            if (uiState.value.serviceId == null) {
                // keep behavior flexible; navigation handled by host/shared VM in old code
            }
            _uiEffect.emit(CropImageUiEffect.ShowToast(R.string.dialog_send_image_description))
            _uiEffect.emit(CropImageUiEffect.NavigateBack)
        } else {
            _uiEffect.emit(CropImageUiEffect.ShowToast(R.string.dialog_error_send_image_description))
        }
    }
    fun isPayment(currentStatus: Int): Boolean = shiftStatusUseCase.isPayment(currentStatus)
    fun isHired(currentStatus: Int): Boolean = shiftStatusUseCase.isHired(currentStatus)
    fun dispatchSubscriberNextStep(
        dispatchValue: InfoDispatchModel,
        trip: Trip,
        callback: (Trip) -> Unit
    ) {
        viewModelScope.launch {
            if (dispatchValue.requireQr) {
                navigateTo(PaymentDirections.goToScannerQRFragment(dispatchValue.longDispatchNumber))
            } else {
                tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
                shiftStatusUseCase.getStatus()?.let {
                    ticketUseCase.prepareTicket(trip, dispatchValue, it)
                }
                val status = shiftStatusUseCase.checkForSubStateModifications(com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_FOR_HIRE)
                shiftStatusUseCase.setStatus(status, true)
            }
        }
    }
    fun sendSubscriberAuth(serviceId: String?, tripId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            val accountPaymentRequest = subscriberUseCase.getAccountPaymentRequest(tripId)
            accountPaymentRequest?.serviceId = serviceId
            subscriberUseCase.sendAccountPaymentRequest(accountPaymentRequest)
        }
    }
    fun emitEffect(effect: CropImageUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}
