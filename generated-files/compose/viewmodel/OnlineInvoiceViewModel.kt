package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.model.Dispatch
import ifac.td.taxi.domain.model.SmartTDException
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.BravoRestInvoiceUseCase
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.repository.connections.rest.invoice.Resource
import ifac.td.taxi.ui.model.FiscalData
import ifac.td.taxi.ui.model.SmartTDUIException
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toInfoDispatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class OnlineInvoiceComposeViewModel(
    context: Application,
    private val tripUseCase: TripUseCase,
    private val dispatchUseCase: DispatchUseCase,
    private val bravoRestInvoiceUseCase: BravoRestInvoiceUseCase
) : BaseViewModel(context) {
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
