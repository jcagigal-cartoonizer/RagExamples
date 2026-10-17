package ifac.td.taxi.ui.screen.compose
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase as FrameworkBravoCentralUseCase
import ifac.td.taxi.compose.viewmodel.ComposeSharedViewModel
import androidx.compose.material3.TextButton
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.PaymentDirections
import androidx.compose.ui.Modifier
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.trimUserFromString
import ifac.td.taxi.domain.usecase.BravoCentralUseCase
import ifac.td.taxi.compose.viewmodel.BaseComposeViewModel
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import androidx.compose.material3.AlertDialog
import ifac.td.taxi.domain.usecase.SubscriberUseCase
import androidx.navigation.NavController
import ifac.td.taxi.domain.model.PaymentMethod
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import kotlinx.coroutines.flow.*
import androidx.compose.material3.MaterialTheme
import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.domain.usecase.TripUseCase
import kotlinx.coroutines.withContext
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import android.content.Intent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import ifac.td.taxi.domain.model.SubscriberQR
import androidx.navigation.NavDirections
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import com.google.gson.JsonSyntaxException
import ifac.td.taxi.domain.model.Trip
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import ifac.td.taxi.domain.model.VoucherQR
import android.app.Application
import androidx.navigation.NavDeepLinkRequest
import androidx.core.net.toUri
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.Subscriber


/* ------------------------------------------------------------
 * 1) UI STATE / EVENT / EFFECT / DIALOG / BUTTON VISUAL STATE
 * ------------------------------------------------------------ */

data class SubscriberPaymentUiState(
    val abn: String = "",
    val usr: String = "",
    val aut: String = "",
    val pin: String = "",
    val editableAbn: Boolean = true,
    val editableUsr: Boolean = true,
    val editableAut: Boolean = true,
    val editablePin: Boolean = true,
    val buttonsState: SubscriberPaymentButtonsState = SubscriberPaymentButtonsState(),
    val dialogState: SubscriberPaymentDialogState = SubscriberPaymentDialogState(),
    val actualTrip: Trip? = null,
    val actualDispatch: InfoDispatchModel? = null,
    val subscriberPetitionSent: Boolean = false,
    val scannedCompanyId: Int? = null,
    val hasScannedQR: Boolean = false,
    val isLoading: Boolean = false,
)

data class SubscriberPaymentDialogState(
    val isVisible: Boolean = false,
    val title: String = "",
    val description: String = "",
    val buttons: List<ButtonType> = emptyList(),
    val tag: String? = null
)

enum class SubscriberPaymentButtonStyle { ENABLE, DISABLE, LOADING }
enum class SubscriberPaymentButtonBackgroundColor { GREEN, RED, BLUE, YELLOW, ORANGE, DEFAULT, BLACK, TRANSPARENT }

data class SubscriberPaymentButtonVisualState(
    val text: String = "",
    val style: SubscriberPaymentButtonStyle = SubscriberPaymentButtonStyle.ENABLE,
    val backgroundColor: SubscriberPaymentButtonBackgroundColor = SubscriberPaymentButtonBackgroundColor.BLUE,
    val visible: Boolean = true
)

data class SubscriberPaymentButtonsState(
    val showQrButton: Boolean = true,
    val acceptButton: SubscriberPaymentButtonVisualState = SubscriberPaymentButtonVisualState(
        text = "ACCEPT",
        style = SubscriberPaymentButtonStyle.ENABLE,
        backgroundColor = SubscriberPaymentButtonBackgroundColor.GREEN
    ),
    val cancelButton: SubscriberPaymentButtonVisualState = SubscriberPaymentButtonVisualState(
        text = "CANCEL",
        style = SubscriberPaymentButtonStyle.ENABLE,
        backgroundColor = SubscriberPaymentButtonBackgroundColor.RED
    )
)

sealed interface SubscriberPaymentUiEvent {
    data class Init(val user: String?, val subscriber: String?) : SubscriberPaymentUiEvent
    data class SharedStateChanged(val sharedState: SubscriberPaymentMainActivityUiState) : SubscriberPaymentUiEvent
    data class AbnChanged(val value: String) : SubscriberPaymentUiEvent
    data class UsrChanged(val value: String) : SubscriberPaymentUiEvent
    data class AutChanged(val value: String) : SubscriberPaymentUiEvent
    data class PinChanged(val value: String) : SubscriberPaymentUiEvent
    data object QrClicked : SubscriberPaymentUiEvent
    data object AcceptClicked : SubscriberPaymentUiEvent
    data object CancelClicked : SubscriberPaymentUiEvent
    data class ScannerResult(val result: String) : SubscriberPaymentUiEvent
    data class DialogButtonPressed(val button: ButtonType) : SubscriberPaymentUiEvent
    data object DialogDismissed : SubscriberPaymentUiEvent
}

sealed interface SubscriberPaymentUiEffect {
    data class ShowToast(val messageRes: Int) : SubscriberPaymentUiEffect
    data object NavigateBack : SubscriberPaymentUiEffect
    data object NavigateHome : SubscriberPaymentUiEffect
    data class NavigateSignature(val dispatchLongNumber: Long) : SubscriberPaymentUiEffect
    data class NavigateVoucher(val dispatchLongNumber: Long) : SubscriberPaymentUiEffect
    data class NavigateQr(val dispatchLongNumber: Long) : SubscriberPaymentUiEffect
    data class OpenScanner(val cameraPosition: Int) : SubscriberPaymentUiEffect
    data class ShowDialog(val dialog: SubscriberPaymentDialogState) : SubscriberPaymentUiEffect
}

data class SubscriberPaymentMainActivityUiState(
    val trip: Trip? = null,
    val dispatch: InfoDispatchModel? = null,
    val loading: Boolean = false
)

/* ------------------------------------------------------------
 * 2) REDUCER
 * ------------------------------------------------------------ */

fun reduceSubscriberPaymentButtonsState(
    sharedLoading: Boolean,
    actualTrip: Trip?,
    actualDispatch: InfoDispatchModel?
): SubscriberPaymentButtonsState {
    val isFromDispatch = actualTrip?.fromDispatch == true && actualDispatch?.subscriber?.isNotEmpty() == true

    return if (isFromDispatch) {
        SubscriberPaymentButtonsState(
            showQrButton = false,
            acceptButton = SubscriberPaymentButtonVisualState(
                text = "ACCEPT",
                style = if (sharedLoading) SubscriberPaymentButtonStyle.LOADING else SubscriberPaymentButtonStyle.ENABLE,
                backgroundColor = SubscriberPaymentButtonBackgroundColor.GREEN,
                visible = true
            ),
            cancelButton = SubscriberPaymentButtonVisualState(
                text = "CANCEL",
                style = SubscriberPaymentButtonStyle.ENABLE,
                backgroundColor = SubscriberPaymentButtonBackgroundColor.RED,
                visible = true
            )
        )
    } else {
        SubscriberPaymentButtonsState(
            showQrButton = true,
            acceptButton = SubscriberPaymentButtonVisualState(
                text = "ACCEPT",
                style = if (sharedLoading) SubscriberPaymentButtonStyle.LOADING else SubscriberPaymentButtonStyle.ENABLE,
                backgroundColor = SubscriberPaymentButtonBackgroundColor.GREEN,
                visible = true
            ),
            cancelButton = SubscriberPaymentButtonVisualState(
                text = "CANCEL",
                style = SubscriberPaymentButtonStyle.ENABLE,
                backgroundColor = SubscriberPaymentButtonBackgroundColor.RED,
                visible = true
            )
        )
    }
}

/* ------------------------------------------------------------
 * 3) VIEWMODEL
 * ------------------------------------------------------------ */

class SubscriberPaymentViewModel(
    val subscriberUseCase: SubscriberUseCase,
    val shiftStatusUseCase: ShiftStatusUseCase,
    val tripUseCase: TripUseCase,
    val bravoCentralUseCase: FrameworkBravoCentralUseCase,
    val ticketUseCase: TicketUseCase,
    application: Application,
) : BaseComposeViewModel(application) {

    val _uiState = MutableStateFlow(SubscriberPaymentUiState())
    val uiState: StateFlow<SubscriberPaymentUiState> = _uiState.asStateFlow()

    val _effects = MutableSharedFlow<SubscriberPaymentUiEffect>()
    val effects: SharedFlow<SubscriberPaymentUiEffect> = _effects.asSharedFlow()

    private var shiftStatus = null

    fun onEvent(event: SubscriberPaymentUiEvent) {
        when (event) {
            is SubscriberPaymentUiEvent.Init -> {
                _uiState.update {
                    it.copy(
                        usr = event.user.orEmpty().ifBlank { it.usr },
                        abn = event.subscriber.orEmpty().ifBlank { it.abn }
                    )
                }
            }

            is SubscriberPaymentUiEvent.SharedStateChanged -> {
                val trip = event.sharedState.trip
                val dispatch = event.sharedState.dispatch

                _uiState.update { it.copy(actualTrip = trip, actualDispatch = dispatch) }

                val isFromDispatch = trip?.fromDispatch == true && dispatch?.subscriber?.isNotEmpty() == true
                if (isFromDispatch) {
                    _uiState.update {
                        it.copy(
                            abn = dispatch?.subscriber.orEmpty(),
                            usr = dispatch?.user.orEmpty(),
                            aut = dispatch?.autorizacion.orEmpty(),
                            editableAbn = dispatch?.autorizacion.isNullOrEmpty(),
                            editableUsr = dispatch?.autorizacion.isNullOrEmpty(),
                            editableAut = true,
                            editablePin = true,
                            buttonsState = it.buttonsState.copy(showQrButton = false)
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(buttonsState = it.buttonsState.copy(showQrButton = true))
                    }
                }

                _uiState.update {
                    it.copy(
                        buttonsState = reduceSubscriberPaymentButtonsState(
                            sharedLoading = event.sharedState.loading,
                            actualTrip = trip,
                            actualDispatch = dispatch
                        )
                    )
                }
            }

            is SubscriberPaymentUiEvent.AbnChanged -> _uiState.update { it.copy(abn = event.value) }
            is SubscriberPaymentUiEvent.UsrChanged -> _uiState.update { it.copy(usr = event.value) }
            is SubscriberPaymentUiEvent.AutChanged -> _uiState.update { it.copy(aut = event.value) }
            is SubscriberPaymentUiEvent.PinChanged -> _uiState.update { it.copy(pin = event.value) }

            SubscriberPaymentUiEvent.QrClicked -> {
                viewModelScope.launch { _effects.emit(SubscriberPaymentUiEffect.OpenScanner(1)) }
            }

            SubscriberPaymentUiEvent.AcceptClicked -> {
                val state = _uiState.value
                val isFromDispatch = state.actualTrip?.fromDispatch == true && state.actualDispatch?.subscriber?.isNotEmpty() == true
                if (isFromDispatch) {
                    if (state.abn.isValidSubscriber() && state.usr.isValidSubscriber()) {
                        clickedOkSubscriberDispatch()
                    }
                } else {
                    if (state.abn.isValidSubscriber() && state.usr.isValidSubscriber()) {
                        clickedOkSubscriberStreet()
                    }
                }
            }

            SubscriberPaymentUiEvent.CancelClicked -> {
                if (_uiState.value.subscriberPetitionSent) cancelSubscriberPetition()
                viewModelScope.launch { _effects.emit(SubscriberPaymentUiEffect.NavigateBack) }
            }

            is SubscriberPaymentUiEvent.ScannerResult -> handleResult(event.result)

            is SubscriberPaymentUiEvent.DialogButtonPressed -> {
                _uiState.update { it.copy(dialogState = it.dialogState.copy(isVisible = false)) }
            }

            SubscriberPaymentUiEvent.DialogDismissed -> {
                _uiState.update { it.copy(dialogState = it.dialogState.copy(isVisible = false)) }
            }
        }
    }

    private fun String.isValidSubscriber(): Boolean = trim().toIntOrNull()?.let { it > 0 } == true

    private fun clickedOkSubscriberDispatch() {
        val state = _uiState.value
        if (state.pin == state.actualDispatch?.pin) {
            state.actualDispatch?.let { dispatch ->
                state.actualTrip?.let { trip ->
                    val subscriber = saveSubscriber()
                    if (subscriber != null) {
                        subscriberFromDispatch(dispatch, trip, subscriber)
                        _uiState.update { it.copy(subscriberPetitionSent = true) }
                    }
                }
            }
        } else {
            showErrorDialog(context.getString(R.string.pin_incorrecto))
        }
    }

    private fun clickedOkSubscriberStreet() {
        val subscriber = saveSubscriber()
        if (subscriber != null) {
            subscriberLogic(
                tripId = _uiState.value.actualTrip?.id ?: 0L,
                subscriber = subscriber,
                isFromIngenico = false
            )
            _uiState.update { it.copy(subscriberPetitionSent = true) }
        }
    }

    private fun saveSubscriber(): Subscriber? {
        val state = _uiState.value
        val subscriberValue = state.abn.trim().toIntOrNull()
        val userValue = state.usr.trim().toIntOrNull()

        if (subscriberValue != null && userValue != null && subscriberValue > 0 && userValue > 0) {
            val subscriber = Subscriber(
                tripId = state.actualTrip?.id,
                isManual = false,
                manualSubscriber = subscriberValue,
                manualUser = userValue,
                manualAuthorization = state.aut,
                manualPin = state.pin.ifBlank { "" },
                companyId = state.scannedCompanyId?.toString() ?: ""
            )
            insertSubscriber(subscriber)
            return subscriber
        }
        return null
    }

    private fun insertSubscriber(subscriber: Subscriber) {
        viewModelScope.launch { subscriberUseCase.registerSubscriberAndAssignToTrip(subscriber) }
    }

    fun subscriberLogic(tripId: Long, subscriber: Subscriber, isFromIngenico: Boolean = false) {
        viewModelScope.launch {
            subscriberUseCase.saveVoucherQR(null)
            val accountPaymentRequest = subscriberUseCase.getAccountPaymentRequest(tripId, subscriber)
            if (isFromIngenico) accountPaymentRequest?._abonadoConTarjeta = true
            subscriberUseCase.sendAccountPaymentRequest(accountPaymentRequest)
        }
    }

    fun subscriberFromDispatch(actualDispatch: InfoDispatchModel, actualTrip: Trip, subscriber: Subscriber?) {
        viewModelScope.launch(Dispatchers.IO) {
            val manualSubscriberId = actualDispatch.subscriber?.toIntOrNull()
            val manualUserId = actualDispatch.subscriberUser?.let { trimUserFromString(it).toIntOrNull() }

            val validSubscriber =
                if (manualSubscriberId != null && manualUserId != null && manualSubscriberId > 0 && manualUserId > 0) {
                    Subscriber(
                        manualSubscriber = manualSubscriberId,
                        manualUser = manualUserId,
                        tripId = actualTrip.id
                    )
                } else if (subscriber?.manualSubscriber != null && subscriber.manualUser != null && subscriber.manualSubscriber > 0 && subscriber.manualUser > 0) {
                    subscriber.copy(tripId = actualTrip.id)
                } else null

            if (validSubscriber != null) {
                subscriberUseCase.updateSubscriber(validSubscriber)

                if (actualDispatch.checkCreditLimit) {
                    subscriberUseCase.checkCreditLimit(
                        actualTrip.totalAmount,
                        MutableSharedFlow<String>().also { flow ->
                            viewModelScope.launch {
                                flow.collect { value ->
                                    val validCredit = value == "1"
                                    if (validCredit) {
                                        _effects.emit(SubscriberPaymentUiEffect.ShowToast(R.string.toast_valid_credit))
                                        when {
                                            actualDispatch.requireSignature -> dispatchRequireSignature(actualDispatch)
                                            actualDispatch.requireVoucher -> dispatchRequireVoucher(actualDispatch)
                                            actualDispatch.requireQr -> dispatchRequireQR(actualDispatch)
                                            else -> validCredit(actualTrip)
                                        }
                                    } else {
                                        _effects.emit(SubscriberPaymentUiEffect.ShowToast(R.string.toast_not_enough_credit))
                                    }
                                }
                            }
                        }
                    )
                } else {
                    dispatchSubscriberNextStep(actualDispatch, actualTrip)
                }
            } else {
                showDialog(
                    CustomDialog.CustomDialogModel(
                        title = context.getString(R.string.dialog_error_subscriber),
                        buttons = arrayListOf(ButtonType.ACCEPT),
                        dialogTAG = CustomDialog.CustomDialogTAG.SUBSCRIBER_RESPONSE_ERROR_DIALOG,
                        isCancellable = false,
                    )
                )
            }
        }
    }

    suspend fun dispatchRequireSignature(dispatch: InfoDispatchModel) {
        _effects.emit(SubscriberPaymentUiEffect.NavigateSignature(dispatch.longDispatchNumber))
    }

    suspend fun dispatchRequireVoucher(dispatch: InfoDispatchModel) {
        _effects.emit(SubscriberPaymentUiEffect.NavigateVoucher(dispatch.longDispatchNumber))
    }

    suspend fun dispatchRequireQR(dispatch: InfoDispatchModel) {
        _effects.emit(SubscriberPaymentUiEffect.NavigateQr(dispatch.longDispatchNumber))
    }

    fun validCredit(trip: Trip?) {
        viewModelScope.launch {
            trip?.let {
                withContext(Dispatchers.IO) {
                    tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
                }
                shiftStatusUseCase.getStatus()?.let { status ->
                    ticketUseCase.prepareTicket(trip = trip, dispatch = null, shiftStatus = status)
                }
                val status = shiftStatusUseCase.checkForSubStateModifications(com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_FOR_HIRE)
                status?.let { shiftStatusUseCase.setStatus(it, true) }
            }
        }
    }

    private fun dispatchSubscriberNextStep(dispatchValue: InfoDispatchModel, trip: Trip) {
        viewModelScope.launch {
            when {
                dispatchValue.requireSignature -> dispatchRequireSignature(dispatchValue)
                dispatchValue.requireVoucher -> dispatchRequireVoucher(dispatchValue)
                dispatchValue.requireQr -> dispatchRequireQR(dispatchValue)
                else -> {
                    tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
                    shiftStatusUseCase.getStatus()?.let { status ->
                        ticketUseCase.prepareTicket(trip = trip, dispatch = dispatchValue, shiftStatus = status)
                    }
                    val status = shiftStatusUseCase.checkForSubStateModifications(com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_FOR_HIRE)
                    status?.let { shiftStatusUseCase.setStatus(it, true) }

                    showDialog(
                        CustomDialog.CustomDialogModel(
                            title = context.getString(R.string.operation_authorized),
                            buttons = arrayListOf(ButtonType.ACCEPT),
                            dialogTAG = CustomDialog.CustomDialogTAG.SUBSCRIBER_RESPONSE_SUCCESS_DIALOG,
                            isCancellable = false,
                        )
                    )
                    _effects.emit(SubscriberPaymentUiEffect.NavigateHome)
                }
            }
        }
    }

    fun saveVoucherQR(voucherQR: VoucherQR?) {
        _uiState.update {
            it.copy(scannedCompanyId = voucherQR?.companyId, hasScannedQR = true)
        }
        viewModelScope.launch { subscriberUseCase.saveVoucherQR(voucherQR) }
    }

    fun saveSubscriberQR(subscriberQR: SubscriberQR?) {
        _uiState.update { it.copy(scannedCompanyId = subscriberQR?.companyId) }
    }

    fun cancelSubscriberPetition() {
        viewModelScope.launch { bravoCentralUseCase.cancelCredit() }
    }

    private fun showErrorDialog(description: String) {
        val dialog = SubscriberPaymentDialogState(
            isVisible = true,
            title = context.getString(R.string.dialog_error_title),
            description = description,
            buttons = listOf(ButtonType.ACCEPT),
            tag = CustomDialog.CustomDialogTAG.SUBSCRIBER_RESPONSE_ERROR_DIALOG.name
        )
        _uiState.update { it.copy(dialogState = dialog) }
        viewModelScope.launch { _effects.emit(SubscriberPaymentUiEffect.ShowDialog(dialog)) }
    }

    private fun showDialog(model: CustomDialog.CustomDialogModel) {
        val dialog = SubscriberPaymentDialogState(
            isVisible = true,
            title = model.title,
            description = model.description,
            buttons = model.buttons,
            tag = model.dialogTAG?.name
        )
        _uiState.update { it.copy(dialogState = dialog) }
        viewModelScope.launch { _effects.emit(SubscriberPaymentUiEffect.ShowDialog(dialog)) }
    }

    fun handleResult(result: String) {
        val gson = Gson()
        try {
            val subscriberQR = gson.fromJson(result, SubscriberQR::class.java)
            if (subscriberQR.type == 1) {
                saveSubscriberQR(subscriberQR)
                _uiState.update {
                    it.copy(abn = subscriberQR.accountId, usr = subscriberQR.userId?.toString() ?: "1")
                }
                return
            }
        } catch (_: JsonSyntaxException) { }

        try {
            val voucherQR = gson.fromJson(result, VoucherQR::class.java)
            saveVoucherQR(voucherQR)
            _uiState.update {
                it.copy(abn = voucherQR.accountId, usr = voucherQR.userId?.toString() ?: "1")
            }
        } catch (_: JsonSyntaxException) { }
    }
}

/* ------------------------------------------------------------
 * 4) COMPOSE SCREEN
 * ------------------------------------------------------------ */

@Composable
fun SubscriberPaymentScreen(
    state: SubscriberPaymentUiState,
    onEvent: (SubscriberPaymentUiEvent) -> Unit,
    effects: SharedFlow<SubscriberPaymentUiEffect>,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateSignature: (Long) -> Unit,
    onNavigateVoucher: (Long) -> Unit,
    onNavigateQr: (Long) -> Unit,
    onOpenScanner: (cameraPosition: Int, onResult: (String) -> Unit) -> Unit,
    onShowToast: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var dialogState by remember { mutableStateOf<SubscriberPaymentDialogState?>(null) }

    LaunchedEffect(effects, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            effects.collectLatest { effect ->
                when (effect) {
                    is SubscriberPaymentUiEffect.ShowToast -> onShowToast(effect.messageRes)
                    SubscriberPaymentUiEffect.NavigateBack -> onNavigateBack()
                    SubscriberPaymentUiEffect.NavigateHome -> onNavigateHome()
                    is SubscriberPaymentUiEffect.NavigateSignature -> onNavigateSignature(effect.dispatchLongNumber)
                    is SubscriberPaymentUiEffect.NavigateVoucher -> onNavigateVoucher(effect.dispatchLongNumber)
                    is SubscriberPaymentUiEffect.NavigateQr -> onNavigateQr(effect.dispatchLongNumber)
                    is SubscriberPaymentUiEffect.OpenScanner -> onOpenScanner(effect.cameraPosition) { result ->
                        onEvent(SubscriberPaymentUiEvent.ScannerResult(result))
                    }
                    is SubscriberPaymentUiEffect.ShowDialog -> dialogState = effect.dialog
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(45.dp))

                SubscriberPaymentTextField(
                    label = "ABN",
                    value = state.abn,
                    enabled = state.editableAbn,
                    keyboardType = KeyboardType.Number,
                    onValueChange = { onEvent(SubscriberPaymentUiEvent.AbnChanged(it)) }
                )

                Spacer(Modifier.height(12.dp))

                SubscriberPaymentTextField(
                    label = "USR",
                    value = state.usr,
                    enabled = state.editableUsr,
                    keyboardType = KeyboardType.Number,
                    onValueChange = { onEvent(SubscriberPaymentUiEvent.UsrChanged(it)) }
                )

                Spacer(Modifier.height(12.dp))

                SubscriberPaymentTextField(
                    label = "AUT",
                    value = state.aut,
                    enabled = state.editableAut,
                    keyboardType = KeyboardType.Text,
                    maxLength = 40,
                    onValueChange = { onEvent(SubscriberPaymentUiEvent.AutChanged(it)) }
                )

                Spacer(Modifier.height(12.dp))

                SubscriberPaymentTextField(
                    label = "PIN",
                    value = state.pin,
                    enabled = state.editablePin,
                    keyboardType = KeyboardType.Password,
                    onValueChange = { onEvent(SubscriberPaymentUiEvent.PinChanged(it)) }
                )

                Spacer(Modifier.height(16.dp))

                if (state.buttonsState.showQrButton) {
                    Box(
                        modifier = Modifier
                            .size(175.dp)
                            .align(Alignment.CenterHorizontally)
                            .clickable { onEvent(SubscriberPaymentUiEvent.QrClicked) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "QR", color = Color.White)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(Color.Black)
                    .padding(1.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SubscriberPaymentCustomButton(
                    modifier = Modifier.weight(1f),
                    visualState = state.buttonsState.cancelButton,
                    onClick = { onEvent(SubscriberPaymentUiEvent.CancelClicked) }
                )

                SubscriberPaymentCustomButton(
                    modifier = Modifier.weight(1f),
                    visualState = state.buttonsState.acceptButton,
                    onClick = { onEvent(SubscriberPaymentUiEvent.AcceptClicked) }
                )
            }
        }

        dialogState?.let { dialog ->
            AlertDialog(
                onDismissRequest = { onEvent(SubscriberPaymentUiEvent.DialogDismissed) },
                title = { Text(dialog.title) },
                text = { Text(dialog.description) },
                confirmButton = {
                    TextButton(onClick = {
                        onEvent(SubscriberPaymentUiEvent.DialogButtonPressed(dialog.buttons.firstOrNull() ?: ButtonType.ACCEPT))
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun SubscriberPaymentTextField(
    label: String,
    value: String,
    enabled: Boolean,
    keyboardType: KeyboardType,
    maxLength: Int? = null,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(text = label, color = Color.White)
        Spacer(Modifier.height(4.dp))
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(maxLength?.let { it2 -> it.take(it2) } ?: it) },
            enabled = enabled,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SubscriberPaymentCustomButton(
    modifier: Modifier = Modifier,
    visualState: SubscriberPaymentButtonVisualState,
    onClick: () -> Unit
) {
    val bg = when (visualState.backgroundColor) {
        SubscriberPaymentButtonBackgroundColor.GREEN -> Color(0xFF2E7D32)
        SubscriberPaymentButtonBackgroundColor.RED -> Color(0xFFC62828)
        SubscriberPaymentButtonBackgroundColor.BLUE -> Color(0xFF1565C0)
        SubscriberPaymentButtonBackgroundColor.YELLOW -> Color(0xFFF9A825)
        SubscriberPaymentButtonBackgroundColor.ORANGE -> Color(0xFFEF6C00)
        SubscriberPaymentButtonBackgroundColor.DEFAULT -> Color(0xFF1565C0)
        SubscriberPaymentButtonBackgroundColor.BLACK -> Color.Black
        SubscriberPaymentButtonBackgroundColor.TRANSPARENT -> Color.Transparent
    }

    val contentColor = when (visualState.style) {
        SubscriberPaymentButtonStyle.DISABLE -> Color(0xFFBDBDBD)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(bg)
            .clickable(enabled = visualState.style != SubscriberPaymentButtonStyle.DISABLE) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (visualState.style) {
            SubscriberPaymentButtonStyle.LOADING -> CircularProgressIndicator(color = Color.White)
            else -> Text(text = visualState.text, color = contentColor)
        }
    }
}

/* ------------------------------------------------------------
 * 5) COMPOSE BRIDGE FOR SHARED VIEWMODEL
 * ------------------------------------------------------------ */

@Composable
fun SubscriberPaymentRoute(
    viewModel: SubscriberPaymentViewModel,
    sharedViewModel: ComposeSharedViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateSignature: (Long) -> Unit,
    onNavigateVoucher: (Long) -> Unit,
    onNavigateQr: (Long) -> Unit,
    onOpenScanner: (cameraPosition: Int, onResult: (String) -> Unit) -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sharedTrip by sharedViewModel.dispatchFlow.collectAsStateWithLifecycle(initialValue = null)
    val loading by sharedViewModel.loadingStateFlow.collectAsStateWithLifecycle(initialValue = false)

    LaunchedEffect(sharedViewModel.dispatchFlow, sharedViewModel.loadingStateFlow) {
        snapshotFlow {
            SubscriberPaymentMainActivityUiState(
                trip = sharedViewModel.tripFlow.value,
                dispatch = sharedViewModel.dispatchFlow.value,
                loading = false
            )
        }.collectLatest {
            viewModel.onEvent(SubscriberPaymentUiEvent.SharedStateChanged(it.copy(loading = loading)))
        }
    }

    SubscriberPaymentScreen(
        state = state,
        onEvent = viewModel::onEvent,
        effects = viewModel.effects,
        onNavigateBack = onNavigateBack,
        onNavigateHome = onNavigateHome,
        onNavigateSignature = onNavigateSignature,
        onNavigateVoucher = onNavigateVoucher,
        onNavigateQr = onNavigateQr,
        onOpenScanner = onOpenScanner,
        onShowToast = onShowToast
    )
}
