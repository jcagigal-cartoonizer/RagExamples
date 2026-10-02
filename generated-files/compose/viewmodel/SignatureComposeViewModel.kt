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
// # Block 79-2: import android.app.Application
class SignatureComposeViewModel(
    private val signatureUseCase: SignatureUseCase,
    private val subscriberUseCase: SubscriberUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val tripUseCase: TripUseCase,
    private val ticketUseCase: TicketUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SignatureUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<SignatureUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    private val _signatureResponse = MutableSharedFlow<Boolean>()
    val signatureResponse = _signatureResponse.asSharedFlow()
    fun setServiceId(serviceId: String?) {
        _uiState.update { it.copy(serviceId = serviceId) }
    }
    fun onEvent(event: SignatureUiEvent) {
        when (event) {
            is SignatureUiEvent.OnSignatureChanged -> {
                _uiState.update {
                    it.copy(
                        hasSigned = event.hasSigned,
                        signatureButtonsState = it.signatureButtonsState.copy(
                            accept = it.signatureButtonsState.accept.copy(enabled = event.hasSigned)
                        )
                    )
                }
            }
            SignatureUiEvent.OnClearClicked -> {
                _uiState.update {
                    it.copy(
                        hasSigned = false,
                        signatureButtonsState = it.signatureButtonsState.copy(
                            accept = it.signatureButtonsState.accept.copy(enabled = false, isLoading = false)
                        )
                    )
                }
            }
            SignatureUiEvent.OnAcceptClicked -> submitSignature()
            SignatureUiEvent.OnDialogDismissed -> {
                _uiState.update { it.copy(showClearDialog = false, dialogMessageResId = null, dialogType = null) }
            }
            SignatureUiEvent.OnDialogConfirmClicked -> {
                _uiState.update { it.copy(showClearDialog = false, dialogMessageResId = null, dialogType = null) }
            }
            is SignatureUiEvent.OnSignatureBitmapReady -> Unit
            is SignatureUiEvent.OnServiceIdResolved -> setServiceId(event.serviceId)
        }
    }
    fun submitSignature(signatureBitmap: Bitmap?, fromDispatch: Boolean) {
        val serviceId = uiState.value.serviceId
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    signatureButtonsState = it.signatureButtonsState.copy(
                        accept = it.signatureButtonsState.accept.copy(
                            isLoading = true,
                            enabled = false
                        ),
                        clear = it.signatureButtonsState.clear.copy(
                            enabled = false
                        )
                    )
                )
            }
            signatureUseCase.sendSignature(signatureBitmap, serviceId, _signatureResponse, fromDispatch)
        }
    }
    fun collectSignatureResult(
        isValid: Boolean,
        trip: Trip?,
        dispatch: InfoDispatchModel?,
        navigateToCrop: suspend (String) -> Unit,
        navigateToQr: suspend (String) -> Unit,
        showToast: suspend (Int) -> Unit,
    ) {
        viewModelScope.launch {
            if (isValid) {
                val serviceId = uiState.value.serviceId
                if (serviceId != null && dispatch != null && trip != null) {
                    if (serviceId == dispatch.longDispatchNumber) {
                        if (dispatch.requireVoucher) {
                            navigateToCrop(dispatch.longDispatchNumber)
                        } else if (dispatch.requireQr) {
                            navigateToQr(dispatch.longDispatchNumber)
                        } else {
                            tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
                            shiftStatusUseCase.getStatus()?.let { status ->
                                ticketUseCase.prepareTicket(trip = trip, dispatch = dispatch, shiftStatus = status)
                            }
                            shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
                                ?.let { shiftStatusUseCase.setStatus(it, true) }
                        }
                    } else {
                        sendSubscriberAuth(serviceId, trip.id)
                    }
                }
                showToast(ifac.td.taxi.R.string.dialog_signature_valid_desc)
            } else {
                showToast(ifac.td.taxi.R.string.dialog_signature_invalid_desc)
            }
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    signatureButtonsState = it.signatureButtonsState.copy(
                        accept = it.signatureButtonsState.accept.copy(isLoading = false, enabled = it.hasSigned),
                        clear = it.signatureButtonsState.clear.copy(enabled = true)
                    )
                )
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
    fun dispatchSubscriberNextStep(dispatchValue: InfoDispatchModel, trip: Trip) {
        viewModelScope.launch {
            if (dispatchValue.requireVoucher) {
                _uiEffect.emit(SignatureUiEffect.NavigateTo(PaymentDirections.goToCropImageViewFragment(dispatchValue.longDispatchNumber)))
            } else if (dispatchValue.requireQr) {
                _uiEffect.emit(SignatureUiEffect.NavigateTo(PaymentDirections.goToScannerQRFragment(dispatchValue.longDispatchNumber)))
            } else {
                tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
                shiftStatusUseCase.getStatus()?.let { status ->
                    ticketUseCase.prepareTicket(trip = trip, dispatch = dispatchValue, shiftStatus = status)
                }
                shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
                    ?.let { shiftStatusUseCase.setStatus(it, true) }
            }
        }
    }
}
