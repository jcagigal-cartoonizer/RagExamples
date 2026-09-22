package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 4-1: import android.app.Application
// import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.model.UserPreferences
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.usecase.ReceiptUseCase
import ifac.td.taxi.framework.util.tickets.TicketsPrinter
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toInfoDispatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
data class OfflineInvoiceUiState(
    val tripId: Long = -1L,
    val driverDirection: String = "",
    val driverPostalCode: String = "",
    val driverCity: String = "",
    val clientNameAndSurname: String = "",
    val clientNIF: String = "",
    val clientDirection: String = "",
    val clientPostalCode: String = "",
    val clientCity: String = "",
    val showDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isInvoiceRequestInProgress: Boolean = false,
    val isCopyDialogVisible: Boolean = false,
    val dialogTitle: String = "",
    val dialogDescription: String = ""
)
sealed interface OfflineInvoiceUiEvent {
    data class TripIdChanged(val tripId: Long) : OfflineInvoiceUiEvent
    data class DriverDirectionChanged(val value: String) : OfflineInvoiceUiEvent
    data class DriverPostalCodeChanged(val value: String) : OfflineInvoiceUiEvent
    data class DriverCityChanged(val value: String) : OfflineInvoiceUiEvent
    data class ClientNameChanged(val value: String) : OfflineInvoiceUiEvent
    data class ClientNifChanged(val value: String) : OfflineInvoiceUiEvent
    data class ClientDirectionChanged(val value: String) : OfflineInvoiceUiEvent
    data class ClientPostalCodeChanged(val value: String) : OfflineInvoiceUiEvent
    data class ClientCityChanged(val value: String) : OfflineInvoiceUiEvent
    data object AcceptClicked : OfflineInvoiceUiEvent
    data object CancelClicked : OfflineInvoiceUiEvent
    data object DialogCopyAcceptClicked : OfflineInvoiceUiEvent
    data object DialogCopyCancelClicked : OfflineInvoiceUiEvent
    data object LoadPreferences : OfflineInvoiceUiEvent
}
sealed interface OfflineInvoiceUiEffect {
    data class PrintInvoice(
        val receipt: String,
        val brokenDownTaxTicket: String
    ) : OfflineInvoiceUiEffect
    data class ShowToast(val messageRes: Int) : OfflineInvoiceUiEffect
    data object NavigateBack : OfflineInvoiceUiEffect
    data class OpenCopyDialog(
        val title: String,
        val description: String
    ) : OfflineInvoiceUiEffect
}
class OfflineInvoiceComposeViewModel(
    application: Application,
    private val receiptUseCase: ReceiptUseCase,
    private val tripUseCase: TripUseCase,
    private val dispatchUseCase: DispatchUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(OfflineInvoiceUiState())
    val uiState: StateFlow<OfflineInvoiceUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<OfflineInvoiceUiEffect>()
    val uiEffect: SharedFlow<OfflineInvoiceUiEffect> = _uiEffect.asSharedFlow()
    fun onEvent(event: OfflineInvoiceUiEvent) {
        when (event) {
            is OfflineInvoiceUiEvent.TripIdChanged ->
                _uiState.update { it.copy(tripId = event.tripId) }
            is OfflineInvoiceUiEvent.DriverDirectionChanged ->
                _uiState.update { it.copy(driverDirection = event.value) }
            is OfflineInvoiceUiEvent.DriverPostalCodeChanged ->
                _uiState.update { it.copy(driverPostalCode = event.value) }
            is OfflineInvoiceUiEvent.DriverCityChanged ->
                _uiState.update { it.copy(driverCity = event.value) }
            is OfflineInvoiceUiEvent.ClientNameChanged ->
                _uiState.update { it.copy(clientNameAndSurname = event.value) }
            is OfflineInvoiceUiEvent.ClientNifChanged ->
                _uiState.update { it.copy(clientNIF = event.value) }
            is OfflineInvoiceUiEvent.ClientDirectionChanged ->
                _uiState.update { it.copy(clientDirection = event.value) }
            is OfflineInvoiceUiEvent.ClientPostalCodeChanged ->
                _uiState.update { it.copy(clientPostalCode = event.value) }
            is OfflineInvoiceUiEvent.ClientCityChanged ->
                _uiState.update { it.copy(clientCity = event.value) }
            OfflineInvoiceUiEvent.LoadPreferences -> loadPreferences()
            OfflineInvoiceUiEvent.CancelClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(OfflineInvoiceUiEffect.NavigateBack)
                }
            }
            OfflineInvoiceUiEvent.AcceptClicked -> validateAndGenerateInvoice()
            OfflineInvoiceUiEvent.DialogCopyAcceptClicked -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isCopyDialogVisible = false) }
                    _uiEffect.emit(OfflineInvoiceUiEffect.NavigateBack)
                }
            }
            OfflineInvoiceUiEvent.DialogCopyCancelClicked -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isCopyDialogVisible = false) }
                    _uiEffect.emit(OfflineInvoiceUiEffect.NavigateBack)
                }
            }
        }
    }
    fun loadPreferences() {
        viewModelScope.launch {
            val preferences = userPreferencesUseCase.getUserPreferences()
            if (preferences != null) {
                _uiState.update {
                    it.copy(
                        driverCity = preferences.invoiceIssuerCity.takeIf { v -> v.isNotEmpty() } ?: it.driverCity,
                        driverDirection = preferences.invoiceIssuerAddress.takeIf { v -> v.isNotEmpty() } ?: it.driverDirection,
                        driverPostalCode = preferences.invoiceIssuerZipCode.takeIf { v -> v.isNotEmpty() } ?: it.driverPostalCode
                    )
                }
            }
        }
    }
    fun validateAndGenerateInvoice() {
        val state = _uiState.value
        if (state.clientNIF.isBlank() && areFieldsEmpty(state)) {
            viewModelScope.launch {
                _uiEffect.emit(OfflineInvoiceUiEffect.ShowToast(ifac.td.taxi.R.string.toast_complete_todos_los_campos))
            }
            return
        }
        if (state.clientNIF.isNotBlank() && !areFieldsEmpty(state)) {
            viewModelScope.launch {
                val isValidNif = isValidDni(state.clientNIF)
                if (!isValidNif) {
                    _uiEffect.emit(OfflineInvoiceUiEffect.ShowToast(ifac.td.taxi.R.string.check_fields))
                    return@launch
                }
                doBilling(state)
            }
            return
        }
        viewModelScope.launch {
            _uiEffect.emit(OfflineInvoiceUiEffect.ShowToast(ifac.td.taxi.R.string.check_fields))
        }
    }
    fun doBilling(state: OfflineInvoiceUiState) {
        viewModelScope.launch {
            tripUseCase.getTripById(state.tripId)?.let { trip ->
                val receipt = receiptUseCase.processReceipt(
                    trip = trip,
                    driverDirection = state.driverDirection,
                    driverPostalCode = state.driverPostalCode,
                    driverCity = state.driverCity,
                    clientNameAndSurname = state.clientNameAndSurname,
                    clientNIF = state.clientNIF,
                    clientDirection = state.clientDirection,
                    clientPostalCode = state.clientPostalCode,
                    clientCity = state.clientCity,
                )
                val tripDispatch = dispatchUseCase.getDispatchByTripId(state.tripId)
                val brokenDownTaxTicket =
                    TicketsPrinter.getBrokenDownTax(trip, getApplication(), tripDispatch?.toInfoDispatchModel())
                trip.invoiceAlreadyGenerated = true
                tripUseCase.updateTripInvoiceFlag(trip.id, trip.invoiceAlreadyGenerated)
                _uiEffect.emit(
                    OfflineInvoiceUiEffect.PrintInvoice(
                        receipt = receipt,
                        brokenDownTaxTicket = brokenDownTaxTicket
                    )
                )
                _uiEffect.emit(
                    OfflineInvoiceUiEffect.OpenCopyDialog(
                        title = "Invoice",
                        description = "Do you want to print a copy?"
                    )
                )
                _uiState.update { it.copy(isCopyDialogVisible = true) }
            }
        }
    }
    fun areFieldsEmpty(state: OfflineInvoiceUiState): Boolean {
        return listOf(
            state.driverDirection,
            state.driverPostalCode,
            state.driverCity,
            state.clientNameAndSurname,
            state.clientNIF,
            state.clientDirection,
            state.clientPostalCode,
            state.clientCity
        ).any { it.isBlank() }
    }
    fun isValidDni(value: String): Boolean {
        return value.length >= 8
    }
}
