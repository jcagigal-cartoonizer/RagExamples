package ifac.td.taxi.ui.screen.compose
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Spacer
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.util.Logs
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Arrangement
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.NavGraphDirections
import androidx.compose.ui.Modifier
import ifac.td.taxi.domain.usecase.PortugalUseCase
import kotlinx.coroutines.flow.StateFlow
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ifac.td.taxi.viewmodel.BaseViewModel
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import ifac.td.taxi.domain.usecase.SubscriberUseCase
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import ifac.td.taxi.domain.usecase.PrinterUseCase
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.domain.usecase.TripUseCase
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.withContext
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.navigation.NavDirections
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import ifac.td.taxi.domain.model.Trip
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import ifac.td.taxi.domain.model.VoucherQR
import android.app.Application
import androidx.navigation.NavDeepLinkRequest
import ifac.td.taxi.ui.custom.button.CustomButton
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.material3.ButtonDefaults
import ifac.td.taxi.R
import androidx.compose.foundation.layout.Column


// ---------------------------------------------------------------------------------------------
// ScannerQr state models
// ---------------------------------------------------------------------------------------------

data class ScannerQrUiState(
    val serviceId: String? = null,
    val buttonsState: ScannerQrButtonsState = ScannerQrButtonsState(),
    val dialogState: ScannerQrDialogState = ScannerQrDialogState(),
    val isLoadingUpload: Boolean = false,
)

sealed interface ScannerQrUiEvent {
    data object CancelClicked : ScannerQrUiEvent
    data object ScannerClicked : ScannerQrUiEvent
    data class CameraSelected(val button: ButtonType) : ScannerQrUiEvent
    data class QrScanned(val rawResult: String) : ScannerQrUiEvent
    data object DialogDismissed : ScannerQrUiEvent
}

sealed interface ScannerQrUiEffect {
    data object NavigateBack : ScannerQrUiEffect
    data object NavigateHome : ScannerQrUiEffect
    data object OpenScannerFront : ScannerQrUiEffect
    data object OpenScannerBack : ScannerQrUiEffect
    data class ShowCustomDialog(val model: CustomDialog.CustomDialogModel) : ScannerQrUiEffect
    data object EnableScannerButton : ScannerQrUiEffect
    data object SetScannerLoading : ScannerQrUiEffect
}

data class ScannerQrDialogState(
    val isVisible: Boolean = false,
    val model: CustomDialog.CustomDialogModel? = null,
)

data class ScannerQrButtonVisualState(
    val text: String = "",
    val iconRes: Int? = null,
    val background: CustomButton.BackgroundButtonColor = CustomButton.BackgroundButtonColor.BLUE,
    val style: CustomButton.StyleButton = CustomButton.StyleButton.ENABLE,
    val visible: Boolean = true,
    val onlyText: String? = null,
    val textOnly: Boolean = false,
    val maxLines: Int = 1,
)

data class ScannerQrButtonsState(
    val cancel: ScannerQrButtonVisualState = ScannerQrButtonVisualState(
        text = "CANCEL",
        iconRes = R.drawable.cerrar,
        background = CustomButton.BackgroundButtonColor.RED,
        style = CustomButton.StyleButton.ENABLE,
        visible = true
    ),
    val scanner: ScannerQrButtonVisualState = ScannerQrButtonVisualState(
        text = "SCANNER",
        iconRes = R.drawable.baseline_qr_code_scanner_192,
        background = CustomButton.BackgroundButtonColor.YELLOW,
        style = CustomButton.StyleButton.ENABLE,
        visible = true
    )
)

fun ScannerQrButtonsState.reduce(effect: ScannerQrUiEffect): ScannerQrButtonsState {
    return when (effect) {
        ScannerQrUiEffect.SetScannerLoading -> copy(
            scanner = scanner.copy(style = CustomButton.StyleButton.LOADING)
        )

        ScannerQrUiEffect.EnableScannerButton -> copy(
            scanner = scanner.copy(style = CustomButton.StyleButton.ENABLE)
        )

        else -> this
    }
}

// ---------------------------------------------------------------------------------------------
// ScannerQr ViewModel
// ---------------------------------------------------------------------------------------------

class ScannerQrViewModel(
    application: Application,
    val subscriberUseCase: SubscriberUseCase,
    val ticketUseCase: TicketUseCase,
) : BaseViewModel(application) {

    val TAG = "ScannerQrViewModel"

    val _uiState = MutableStateFlow(ScannerQrUiState())
    val uiState: StateFlow<ScannerQrUiState> = _uiState.asStateFlow()

    val _uiEffect = MutableSharedFlow<ScannerQrUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<ScannerQrUiEffect> = _uiEffect.asSharedFlow()

    fun init(serviceId: String?) {
        _uiState.update { it.copy(serviceId = serviceId) }
    }

    fun onEvent(event: ScannerQrUiEvent) {
        when (event) {
            ScannerQrUiEvent.CancelClicked -> {
                _uiEffect.tryEmit(ScannerQrUiEffect.NavigateBack)
            }

            ScannerQrUiEvent.ScannerClicked -> {
                _uiEffect.tryEmit(
                    ScannerQrUiEffect.ShowCustomDialog(
                        CustomDialog.CustomDialogModel(
                            title = context.getString(R.string.dialog_open_camera),
                            description = context.getString(R.string.dialog_select_camera),
                            buttons = arrayListOf(ButtonType.FRONT_CAMERA, ButtonType.BACK_CAMERA)
                        )
                    )
                )
            }

            is ScannerQrUiEvent.CameraSelected -> {
                when (event.button) {
                    ButtonType.FRONT_CAMERA -> _uiEffect.tryEmit(ScannerQrUiEffect.OpenScannerFront)
                    ButtonType.BACK_CAMERA -> _uiEffect.tryEmit(ScannerQrUiEffect.OpenScannerBack)
                    else -> Unit
                }
            }

            is ScannerQrUiEvent.QrScanned -> handleResult(event.rawResult)
            ScannerQrUiEvent.DialogDismissed -> Unit
        }
    }

    fun onScannerStarted() {
        _uiEffect.tryEmit(ScannerQrUiEffect.SetScannerLoading)
        _uiState.update { it.copy(buttonsState = it.buttonsState.reduce(ScannerQrUiEffect.SetScannerLoading)) }
    }

    fun onUploadFailed() {
        _uiEffect.tryEmit(ScannerQrUiEffect.EnableScannerButton)
        _uiState.update { it.copy(buttonsState = it.buttonsState.reduce(ScannerQrUiEffect.EnableScannerButton)) }
    }

    fun handleResult(result: String) {
        try {
            val qrVoucher = Gson().fromJson(result, VoucherQR::class.java)
            Logs.d(TAG, "handleResult: QR parsed successfully: $qrVoucher")

            uploadVoucherId(
                serviceId = uiState.value.serviceId,
                qrVoucher = qrVoucher,
                infoDispatchModel = null,
                trip = null,
                updatePrintFlowCallback = {},
                navigateFunction = { isSuccess ->
                    if (isSuccess) {
                        _uiEffect.tryEmit(ScannerQrUiEffect.NavigateHome)
                    } else {
                        onUploadFailed()
                    }
                }
            )
        } catch (e: Exception) {
            Logs.e(TAG, "handleResult: Error reading QR: ${e.message}")
            onUploadFailed()
        }
    }

    fun uploadVoucherId(
        serviceId: String?,
        qrVoucher: VoucherQR?,
        infoDispatchModel: InfoDispatchModel?,
        trip: Trip?,
        updatePrintFlowCallback: suspend (Trip) -> Unit,
        navigateFunction: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            Logs.d(
                TAG,
                "uploadVoucherId: Starting voucher upload for serviceId: $serviceId, tripId: ${trip?.id}, voucherId: ${qrVoucher?.id}"
            )

            subscriberUseCase.uploadVoucherId(
                serviceId,
                qrVoucher,
                infoDispatchModel,
                trip,
                navigateFunction,
                { trip2, dispatch, status ->
                    viewModelScope.launch {
                        if (trip2 != null) {
                            Logs.d(TAG, "uploadVoucherId: Preparing ticket for trip: ${trip2.id}")
                            ticketUseCase.prepareTicket(trip2, dispatch, status)
                        } else {
                            Logs.d(TAG, "uploadVoucherId: Trip is null, skipping ticket preparation")
                        }
                    }
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// ScannerQr MainActivity shared bridge
// ---------------------------------------------------------------------------------------------

class ScannerQrMainActivityViewModel(
    application: Application
) : BaseViewModel(application) {

    val _dispatchFlow = MutableStateFlow<InfoDispatchModel?>(null)
    val dispatchFlow: StateFlow<InfoDispatchModel?> = _dispatchFlow.asStateFlow()

    val _tripFlow = MutableStateFlow<Trip?>(null)
    val tripFlow: StateFlow<Trip?> = _tripFlow.asStateFlow()

    var updatePrintFlowCallback: suspend (Trip) -> Unit = {}

    fun setDispatch(value: InfoDispatchModel?) {
        _dispatchFlow.value = value
    }

    fun setTrip(value: Trip?) {
        _tripFlow.value = value
    }

    fun setUpdatePrintCallback(callback: suspend (Trip) -> Unit) {
        updatePrintFlowCallback = callback
    }
}

// ---------------------------------------------------------------------------------------------
// TripHistory ViewModel (included because you asked for a single compiled file with previous step)
// ---------------------------------------------------------------------------------------------

class TripHistoryViewModel(
    context: Application,
    val tripUseCase: TripUseCase,
    val printerUseCase: PrinterUseCase,
    val portugalUseCase: PortugalUseCase,
) : BaseViewModel(context) {

    val TAG = "TripHistoryViewModel"

    val _tripListFlow = MutableStateFlow<List<Trip>?>(null)
    val tripListFlow = _tripListFlow.asStateFlow()

    val _loadingFlow = MutableStateFlow(false)
    val loadingFlow = _loadingFlow.asStateFlow()

    var isLastPage = false
    val PAGE_SIZE = 25

    fun getTrips(page: Int) {
        viewModelScope.launch {
            try {
                Logs.d(TAG, "getTrips: page: $page")
                if (isLastPage) return@launch

                _loadingFlow.value = true
                val loadedItems = page * PAGE_SIZE
                Logs.d(TAG, "getTrips: loadedItems $loadedItems")
                val newTrips = tripUseCase.getAllTripsFinishedPaged(loadedItems)
                Logs.d(TAG, "getTrips: newTrips ${newTrips.size}")
                val currentTrips = _tripListFlow.value ?: emptyList()
                Logs.d(TAG, "getTrips: currentTrips ${currentTrips.size}")

                if (newTrips.size < PAGE_SIZE) {
                    isLastPage = true
                }

                Logs.d(TAG, "getTrips: isLastPage $isLastPage")

                val combinedTrips = (currentTrips + newTrips).distinctBy { it.id }
                _tripListFlow.emit(combinedTrips)
            } catch (e: Exception) {
                Logs.e(TAG, "getTrips: error: ${e.message}")
            } finally {
                _loadingFlow.value = false
            }
        }
    }

    fun deleteSelectedTrips(selectedTrips: Set<Trip>, callback: () -> Unit) {
        viewModelScope.launch {
            if (selectedTrips.isNotEmpty()) {
                val selectedTripsList = selectedTrips.toList()
                for (trip in selectedTripsList) {
                    tripUseCase.deleteTrip(trip)
                    Logs.d("TripHistoryViewModel", "trip ${trip.id} deleted ")
                    callback.invoke()
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_trips_eliminados_correctamente),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_seleccione_algun_trip),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun printTicket(trip: Trip) {
        viewModelScope.launch {
            portugalUseCase.increaseTicketPrinted(trip)
            printerUseCase.printTicket(trip, true)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// ComposeSharedViewModel (included from your provided code, trimmed to compile in one file)
// ---------------------------------------------------------------------------------------------

class ComposeSharedViewModel(
    application: Application
) : BaseComposeViewModel(application) {

    val _hasTaximeterConnectionFlow = MutableStateFlow(false)
    val hasTaximeterConnectionFlow = _hasTaximeterConnectionFlow.asStateFlow()

    val _navigationDirectionsFlow = MutableSharedFlow<NavDirections>()
    val navigationDirectionsFlow = _navigationDirectionsFlow.asSharedFlow()

    val _navigationDeepLinkFlow = MutableSharedFlow<NavDeepLinkRequest>()
    val navigationDeepLinkFlow = _navigationDeepLinkFlow.asSharedFlow()

    val _pendingNavigationFlow = MutableStateFlow<Boolean?>(null)
    val pendingNavigationFlow = _pendingNavigationFlow.asStateFlow()

    val _xmppStatusFlow = MutableStateFlow(0)
    val xmppStatusFlow = _xmppStatusFlow.asStateFlow()

    val _gpsStatusFlow = MutableStateFlow(0)
    val gpsStatusFlow = _gpsStatusFlow.asStateFlow()

    val _showLegalTextFlow = MutableStateFlow<Boolean?>(null)
    val showLegalTextFlow = _showLegalTextFlow.asStateFlow()

    val _roofLightFlow = MutableStateFlow<Boolean?>(null)
    val roofLightFlow = _roofLightFlow.asStateFlow()

    val _userLoggedFlow = MutableStateFlow<Boolean?>(null)
    val userLoggedFlow = _userLoggedFlow.asStateFlow()

    val _clientButtonValueFlow = MutableStateFlow(false)
    val clientButtonValueFlow = _clientButtonValueFlow.asStateFlow()

    val _zoneFlow = MutableStateFlow<String?>(null)
    val zoneFlow = _zoneFlow.asStateFlow()

    val _locationEnabledFlow = MutableStateFlow(Pair(true, true))
    val locationEnabledFlow = _locationEnabledFlow.asStateFlow()

    val _locationManuallyDeLocatedFlow = MutableStateFlow(false)
    val locationManuallyDeLocatedFlow = _locationManuallyDeLocatedFlow.asStateFlow()

    val _locationType = MutableStateFlow("")
    val locationType = _locationType.asStateFlow()

    val _timeControlFlow = MutableStateFlow<String?>(null)
    val timeControlFlow = _timeControlFlow.asStateFlow()

    val _currentAmountFlow = MutableStateFlow<Int?>(null)
    val currentAmountFlow = _currentAmountFlow.asStateFlow()

    val _dispatchFlow = MutableStateFlow<InfoDispatchModel?>(null)
    val dispatchFlow = _dispatchFlow.asStateFlow()

    val _showDialogFlow = MutableSharedFlow<String>()
    val showDialogFlow = _showDialogFlow.asSharedFlow()

    val _loadingStateFlow = MutableSharedFlow<Boolean>()
    val loadingStateFlow = _loadingStateFlow.asSharedFlow()

    val _printTicketFlow = MutableSharedFlow<String>()
    val printTicketFlow = _printTicketFlow.asSharedFlow()

    val _cropImageFlow = MutableSharedFlow<android.net.Uri?>()
    val cropImageFlow = _cropImageFlow.asSharedFlow()

    val _sumUpFlow = MutableSharedFlow<Int>()
    val sumUpFlow = _sumUpFlow.asSharedFlow()

    val _sumUpResponseFlow = MutableSharedFlow<Pair<Int, android.content.Intent?>>()
    val sumUpResponseFlow = _sumUpResponseFlow.asSharedFlow()

    val _dispatchMinimumPriceFlow = MutableStateFlow<Int?>(null)
    val dispatchMinimumPriceFlow = _dispatchMinimumPriceFlow.asStateFlow()

    val _locatedOnStop = MutableStateFlow(false)
    val locatedOnStop = _locatedOnStop.asStateFlow()

    val _reconnectionFlow = MutableStateFlow(false)
    val reconnectionFlow = _reconnectionFlow.asStateFlow()

    val _orangeBtnPendingFlow = MutableStateFlow<Boolean?>(null)
    val orangeBtnPendingFlow = _orangeBtnPendingFlow.asStateFlow()

    val _shortBreakStatus = MutableStateFlow<String?>(null)
    val shortBreakStatus = _shortBreakStatus.asStateFlow()

    val _customerCallAvailable = MutableStateFlow(false)
    val customerCallAvailable = _customerCallAvailable.asStateFlow()

    val _customerCallButtonStateAvailable = MutableStateFlow(true)
    val customerCallButtonStateAvailable = _customerCallButtonStateAvailable.asStateFlow()

    val _lastITopStateReceived = MutableStateFlow<String?>(null)
    val lastITopStateReceived = _lastITopStateReceived.asStateFlow()
}

// ---------------------------------------------------------------------------------------------
// BaseComposeViewModel placeholder to keep this file self-contained
// ---------------------------------------------------------------------------------------------

open class BaseComposeViewModel(application: Application) : BaseViewModel(application)

// ---------------------------------------------------------------------------------------------
// Compose UI
// ---------------------------------------------------------------------------------------------

@Composable
fun ScannerQrRoute(
    serviceId: String?,
    navController: NavController,
    viewModel: ScannerQrViewModel,
    sharedViewModel: ScannerQrMainActivityViewModel,
    onShowHeader: (Boolean) -> Unit,
    onOpenScanner: (cameraPosition: Int, onResult: (String) -> Unit) -> Unit,
    onOpenDialog: (CustomDialog.CustomDialogModel, (CustomDialog.CustomDialogResponse) -> Unit) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(serviceId) {
        viewModel.init(serviceId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                ScannerQrUiEffect.NavigateBack -> onNavigateBack()
                ScannerQrUiEffect.NavigateHome -> navController.navigate(NavGraphDirections.goToHomeFragment())
                ScannerQrUiEffect.OpenScannerFront -> onOpenScanner(1) { result ->
                    viewModel.onScannerStarted()
                    viewModel.handleResult(result)
                }
                ScannerQrUiEffect.OpenScannerBack -> onOpenScanner(0) { result ->
                    viewModel.onScannerStarted()
                    viewModel.handleResult(result)
                }
                is ScannerQrUiEffect.ShowCustomDialog -> onOpenDialog(effect.model) { response ->
                    when (response.buttonPressed) {
                        ButtonType.FRONT_CAMERA, ButtonType.BACK_CAMERA -> {
                            viewModel.onEvent(ScannerQrUiEvent.CameraSelected(response.buttonPressed))
                        }
                        else -> Unit
                    }
                }
                ScannerQrUiEffect.EnableScannerButton -> Unit
                ScannerQrUiEffect.SetScannerLoading -> Unit
            }
        }
    }

    ScannerQrScreen(
        uiState = uiState,
        onCancelClick = {
            viewModel.onEvent(ScannerQrUiEvent.CancelClicked)
        },
        onScannerClick = {
            viewModel.onEvent(ScannerQrUiEvent.ScannerClicked)
        }
    )
}

@Composable
fun ScannerQrScreen(
    uiState: ScannerQrUiState,
    onCancelClick: () -> Unit,
    onScannerClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.dialog_voucher_required),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(24.dp))
                Image(
                    painter = painterResource(R.drawable.qr_scanner),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .background(Color.Black)
                .padding(1.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ScannerQrButton(
                state = uiState.buttonsState.cancel,
                modifier = Modifier.weight(1f),
                onClick = onCancelClick
            )
            ScannerQrButton(
                state = uiState.buttonsState.scanner,
                modifier = Modifier.weight(1f),
                onClick = onScannerClick
            )
        }
    }
}

@Composable
fun ScannerQrButton(
    state: ScannerQrButtonVisualState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val enabled = state.style == CustomButton.StyleButton.ENABLE
    val backgroundColor = when (state.background) {
        CustomButton.BackgroundButtonColor.GREEN -> Color(0xFF2E7D32)
        CustomButton.BackgroundButtonColor.RED -> Color(0xFFC62828)
        CustomButton.BackgroundButtonColor.BLUE -> Color(0xFF1565C0)
        CustomButton.BackgroundButtonColor.YELLOW -> Color(0xFFF9A825)
        CustomButton.BackgroundButtonColor.ORANGE -> Color(0xFFEF6C00)
        CustomButton.BackgroundButtonColor.BLACK -> Color.Black
        CustomButton.BackgroundButtonColor.TRANSPARENT -> Color.Transparent
        CustomButton.BackgroundButtonColor.DEFAULT -> Color(0xFF1565C0)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxSize(),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = Color(0xFF9E9E9E)
        )
    ) {
        if (state.style == CustomButton.StyleButton.LOADING) {
            CircularProgressIndicator()
        } else {
            Text(text = state.text)
        }
    }
}
