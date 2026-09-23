package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.PortugalInvoiceUiEffect
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 58-2: import android.app.Application
class PortugalInvoiceComposeViewModel(
    context: Application,
    private val portugalUseCase: PortugalUseCase,
    private val tripUseCase: TripUseCase,
    private val ticketUseCase: TicketUseCase,
    private val dispatchUseCase: DispatchUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val printerUseCase: PrinterUseCase,
) : BaseViewModel(context) {
    private val _uiState = MutableStateFlow(
        PortugalInvoiceUiState(
            countryOptions = CountryUtils.arrayCountries.toList()
        )
    )
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<PortugalInvoiceUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    fun onCancelClick() {
        viewModelScope.launch {
            _uiEffect.emit(PortugalInvoiceUiEffect.NavigateBack)
        }
    }
    fun onCountrySelected(index: Int) {
        _uiState.update { it.copy(selectedCountryIndex = index) }
    }
    fun onNifChanged(value: String) {
        _uiState.update { it.copy(nif = value) }
    }
    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value) }
    }
    fun onLocalidadeChanged(value: String) {
        _uiState.update { it.copy(localidade = value) }
    }
    fun onAcceptClicked() {
        val state = _uiState.value
        if (state.isAcceptLoading) return
        _uiState.update { it.copy(isAcceptLoading = true) }
        viewModelScope.launch {
            val selectedCountry = state.selectedCountry
            val isPortugalSelected = selectedCountryIndexIsPortugal(state.selectedCountryIndex)
            if (isPortugalSelected && !state.externalCustomer) {
                if (isValidPortugalNif(state.nif)) {
                    acceptInvoice(
                        nif = state.nif,
                        name = state.name,
                        localidade = state.localidade,
                        country = selectedCountry
                    )
                } else {
                    _uiState.update { it.copy(isAcceptLoading = false, showExternalCustomerDialog = true) }
                    _uiEffect.emit(PortugalInvoiceUiEffect.ShowExternalCustomerDialog)
                }
            } else {
                acceptInvoice(
                    nif = state.nif,
                    name = state.name,
                    localidade = state.localidade,
                    country = selectedCountry
                )
            }
        }
    }
    fun onExternalCustomerDialogResult(accepted: Boolean) {
        viewModelScope.launch {
            if (accepted) {
                _uiState.update {
                    it.copy(
                        externalCustomer = true,
                        showExternalCustomerDialog = false,
                        isAcceptLoading = false
                    )
                }
                _uiEffect.emit(PortugalInvoiceUiEffect.HideExternalCustomerDialog)
            } else {
                _uiState.update {
                    it.copy(
                        showExternalCustomerDialog = false,
                        isAcceptLoading = false
                    )
                }
                _uiEffect.emit(PortugalInvoiceUiEffect.HideExternalCustomerDialog)
            }
        }
    }
    fun setExternalCustomer(externalCustomer: Boolean) {
        _uiState.update { it.copy(externalCustomer = externalCustomer) }
    }
    fun selectedCountryIndexIsPortugal(index: Int): Boolean = index == 0
    fun isValidPortugalNif(nif: String): Boolean {
        // preserve original FieldType.NIF_PORTUGAL behavior with your validation rule
        return nif.length == 9 && nif.all { it.isDigit() }
    }
    fun acceptInvoice(
        nif: String,
        name: String,
        localidade: String,
        country: String
    ) {
        viewModelScope.launch {
            val getLastTrip = tripUseCase.getLastTrip()
            getLastTrip?.let { trip ->
                trip.nif = nif
                trip.name = name
                trip.localidade = localidade
                trip.country = country
                trip.iso = CountryUtils.arrayISOCountries[CountryUtils.arrayCountries.indexOf(country)]
                tripUseCase.updateTripPortugalThings(
                    trip.id, trip.nif, trip.name, trip.localidade, trip.country, trip.iso
                )
                portugalUseCase.calculateHashPortugal(true, trip)
                prepareTicket(trip)
            }
            _uiState.update { it.copy(isAcceptLoading = false) }
        }
    }
    private suspend fun prepareTicket(trip: Trip) {
        val dispatch: InfoDispatchModel? = if (trip.fromDispatch) {
            dispatchUseCase.getDispatchByTripId(trip.id)?.let {
                it.id?.let { _ -> it.toInfoDispatchModel() }
            }
        } else {
            null
        }
        val callback = object : TicketUseCaseImpl.PrepareTicketCallback {
            override fun onSuccess() {
                viewModelScope.launch {
                    _uiEffect.emit(PortugalInvoiceUiEffect.NavigateToReceiptHistory(-1L))
                }
            }
        }
        shiftStatusUseCase.getStatus()?.let { shiftStatus ->
            ticketUseCase.prepareTicket(
                trip = trip,
                dispatch = dispatch,
                shiftStatus = shiftStatus,
                portugalByPass = true,
                ticketPrinted = true,
                callback = callback
            )
        }
    }
}
