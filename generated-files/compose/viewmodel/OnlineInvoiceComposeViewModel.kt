package ifac.td.taxi.compose.viewmodel
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
import ifac.td.taxi.viewmodel.OnlineInvoiceViewModel
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.OnlineInvoiceScreen
// # Block 147-2: import android.app.Application
class OnlineInvoiceComposeViewModel(
    context: Application,
    private val tripUseCase: TripUseCase,
    private val dispatchUseCase: DispatchUseCase,
    private val bravoRestInvoiceUseCase: BravoRestInvoiceUseCase
) : BaseViewModel(context) {
    fun fromViewModel(viewModel: OnlineInvoiceViewModel): OnlineInvoiceComposeViewModel {
        val composeViewModel = OnlineInvoiceComposeViewModel(
            context = viewModel.context,
            dispatchUseCase = viewModel.dispatchUseCase,
            tripUseCase = viewModel.tripUseCase,
            bravoRestInvoiceUseCase = viewModel.bravoRestInvoiceUseCase,
        )
        return composeViewModel
    }

    lateinit var buttonsState : OnlineInvoiceButtonsState
    lateinit var dialogState : OnlineInvoiceDialogState
    private val _uiState = MutableStateFlow(OnlineInvoiceUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<OnlineInvoiceUiEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()
    fun onTripIdReceived(id: Long) {
        getTrip(id)
    }
    fun onFiscalIdChanged(value: String) {
        _uiState.update { it.copy(fiscalId = value) }
    }
    fun onCompanyNameChanged(value: String) { _uiState.update { it.copy(companyName = value) } }
    fun onStreetNameChanged(value: String) { _uiState.update { it.copy(streetName = value) } }
    fun onNumberChanged(value: String) { _uiState.update { it.copy(number = value) } }
    fun onCityChanged(value: String) { _uiState.update { it.copy(city = value) } }
    fun onPostalCodeChanged(value: String) { _uiState.update { it.copy(postalCode = value) } }
    fun onProvinceChanged(value: String) { _uiState.update { it.copy(province = value) } }
    fun onCountryChanged(value: String) { _uiState.update { it.copy(country = value) } }
    fun onEmailChanged(value: String) { _uiState.update { it.copy(email = value) } }
    fun onAcceptClicked() {
        val state = _uiState.value
        if (state.fiscalId.isBlank() || state.areRequiredFieldsEmpty) {
            if (state.fiscalId.isNotBlank() && state.areRequiredFieldsEmpty) {
                getFiscalData(state.fiscalId)
            } else {
                emitEffect(OnlineInvoiceUiEffect.ShowToast(R.string.toast_complete_todos_los_campos))
            }
            return
        }
        generateInvoice(state.toFiscalData())
    }
    fun onCancelClicked() {
        emitEffect(OnlineInvoiceUiEffect.NavigateBack)
    }
    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = null) }
    }
    fun confirmDialog() {
        _uiState.value.dialogState?.onAccept?.invoke()
        dismissDialog()
    }
    fun getTrip(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val trip = tripUseCase.getTripById(id)
            if (trip != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trip = trip,
                        fiscalId = trip.nif.orEmpty(),
                        areFiscalFieldsVisible = trip.nif?.isNotBlank() == true
                    )
                }
                if (!trip.nif.isNullOrBlank()) {
                    getFiscalData(trip.nif)
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
                emitError(SmartTDException.DATABASE_ERROR)
            }
        }
    }
    fun getFiscalData(fiscalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = bravoRestInvoiceUseCase.getFiscalData(fiscalId)) {
                is Resource.Success -> {
                    result.data?.let { data ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                areFiscalFieldsVisible = true,
                                companyName = data.companyName,
                                streetName = data.streetName,
                                number = data.number,
                                city = data.city,
                                postalCode = data.postalCode,
                                province = data.province,
                                country = data.country,
                                email = data.email
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    result.exception?.let { emitError(it) }
                }
            }
        }
    }
    fun generateInvoice(userFiscalData: FiscalData) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val trip = _uiState.value.trip
            if (trip == null) {
                _uiState.update { it.copy(isLoading = false) }
                emitError(SmartTDException.DATABASE_ERROR)
                return@launch
            }
            var dispatch: Dispatch? = null
            if (trip.fromDispatch && trip.pkDispatchId != null) {
                dispatch = dispatchUseCase.getDispatchById(trip.pkDispatchId!!)
            }
            when (val result = bravoRestInvoiceUseCase.generateInvoice(
                trip,
                dispatch?.toInfoDispatchModel(),
                fiscalData = userFiscalData
            )) {
                is Resource.Success -> {
                    updateTripInvoiceStatus(trip)
                    _uiState.update { it.copy(isLoading = false) }
                    emitEffect(OnlineInvoiceUiEffect.OpenDialog(
                        OnlineInvoiceDialogState(
                            titleRes = R.string.dialog_success,
                            descriptionRes = R.string.online_invoice_sent_success,
                            buttons = listOf(OnlineInvoiceDialogButton.Accept),
                            onAccept = { emitEffect(OnlineInvoiceUiEffect.NavigateBack) }
                        )
                    ))
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    result.exception?.let { emitError(it) }
                }
            }
        }
    }
    private suspend fun updateTripInvoiceStatus(trip: Trip) {
        trip.invoiceAlreadyGenerated = true
        tripUseCase.updateTripInvoiceFlag(trip.id, trip.invoiceAlreadyGenerated)
    }
    fun emitError(exception: Throwable) {
        val uiException = SmartTDUIException.fromException(exception)
        emitEffect(
            OnlineInvoiceUiEffect.OpenDialog(
                OnlineInvoiceDialogState(
                    titleRes = R.string.dialog_error_title,
                    descriptionRes = uiException.stringResId,
                    buttons = listOf(OnlineInvoiceDialogButton.Accept),
                    onAccept = { emitEffect(OnlineInvoiceUiEffect.NavigateBack) }
                )
            )
        )
    }
    fun emitEffect(effect: OnlineInvoiceUiEffect) {
        _effects.tryEmit(effect)
    }
}
