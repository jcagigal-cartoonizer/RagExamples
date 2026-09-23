package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 114-2: import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.repository.room.dao.BravoConfigurationVariableDao
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toDispatchReceivedModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class DispatchReceivedComposeViewModel(
    private val dispatchUseCase: DispatchUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    private val licensingUsecase: LicensingUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DispatchReceivedUiState())
    val uiState: StateFlow<DispatchReceivedUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<DispatchReceivedUiEffect>()
    val effects: SharedFlow<DispatchReceivedUiEffect> = _effects.asSharedFlow()
    private var actualDispatch: DispatchReceivedModel? = null
    private var idDispatch: Long? = null
    init {
        viewModelScope.launch {
            val loginConfig = bravoConfigurationVariableDao.getBravoConfigurationVariable()
            _uiState.update { it.copy(showPickupAddressByLogin = loginConfig?.showPickupAddress == true) }
        }
    }
    fun onEvent(event: DispatchReceivedUiEvent) {
        when (event) {
            is DispatchReceivedUiEvent.LoadDispatch -> loadDispatch(event.id)
            DispatchReceivedUiEvent.CheckCancelDialogPermission -> checkCancelDialogPermission()
            DispatchReceivedUiEvent.AcceptClicked -> acceptDispatch()
            DispatchReceivedUiEvent.CancelClicked -> rejectManualDispatch()
            DispatchReceivedUiEvent.PhoneClicked -> handlePhoneClick()
            DispatchReceivedUiEvent.RequestCancelConfirmation -> requestCancelConfirmation()
            DispatchReceivedUiEvent.CheckAddressAcceptance -> checkIsDirRecogidaAceptaDespacho()
            is DispatchReceivedUiEvent.CloseExternalDialog -> {
                viewModelScope.launch { _effects.emit(DispatchReceivedUiEffect.CloseDialog(event.tag)) }
            }
        }
    }
    fun loadDispatch(id: Long) {
        viewModelScope.launch {
            idDispatch = id
            val dispatch = dispatchUseCase.getDispatchById(id)
            if (dispatch != null) {
                actualDispatch = dispatch.toDispatchReceivedModel(id)
                _uiState.update {
                    it.copy(
                        dispatch = actualDispatch,
                        hour = actualDispatch?.pickUpTime,
                        pickUpZone = actualDispatch?.pickUpZone,
                        simpleAddress = actualDispatch?.smallPickUpAdress,
                        city = actualDispatch?.city,
                        passengerName = actualDispatch?.passengerName,
                    )
                }
            }
        }
    }
    fun checkCancelDialogPermission() {
        viewModelScope.launch {
            val preferences = userPreferencesUseCase.getUserPreferences()
            _uiState.update { it.copy(showRejectConfirmDialog = preferences?.showConfRejectDispatch == true) }
        }
    }
    fun handlePhoneClick() {
        viewModelScope.launch {
            _effects.emit(DispatchReceivedUiEffect.RequestPhonePermission)
        }
    }
    fun requestCancelConfirmation() {
        viewModelScope.launch {
            _effects.emit(
                DispatchReceivedUiEffect.OpenPermissionDialog(
                    DispatchReceivedDialogModel(
                        title = "Aviso",
                        description = "¿Quieres rechazar este despacho?",
                        buttons = listOf(DispatchReceivedDialogButtonSpec.CancelAccept)
                    )
                )
            )
        }
    }
    fun acceptDispatch() {
        viewModelScope.launch {
            stopCallTimer()
        }
    }
    fun rejectManualDispatch() {
        viewModelScope.launch {
            stopCallTimer()
        }
    }
    fun stopCallTimer() {
        // keep your existing timer stop implementation here
    }
    fun isNumberPhoneIn() {
        // keep your existing implementation here
    }
    fun checkIsDirRecogidaAceptaDespacho() {
        viewModelScope.launch {
            val preferences = licensingUsecase.getLicensingParameters()
            _uiState.update { it.copy(addressAcceptsDispatch = preferences?.isAddressTakenAcceptsDispatch == true) }
        }
    }
    fun showPickupAddress(): Boolean {
        return _uiState.value.showPickupAddressByLogin && _uiState.value.addressAcceptsDispatch
    }
    fun buttonsState(state: DispatchReceivedUiState): DispatchReceivedButtonsState {
        return DispatchReceivedButtonsState.from(
            showRejectConfirmDialog = state.showRejectConfirmDialog,
            hasDispatch = state.dispatch != null
        )
    }
}
data class DispatchReceivedUiState(
    val dispatch: DispatchReceivedModel? = null,
    val hour: String? = null,
    val pickUpZone: String? = null,
    val simpleAddress: String? = null,
    val city: String? = null,
    val passengerName: String? = null,
    val showRejectConfirmDialog: Boolean = false,
    val showPickupAddressByLogin: Boolean = false,
    val addressAcceptsDispatch: Boolean = false,
) {
    val showCallButton: Boolean = true
}
sealed interface DispatchReceivedUiEvent {
    data class LoadDispatch(val id: Long) : DispatchReceivedUiEvent
    data object CheckCancelDialogPermission : DispatchReceivedUiEvent
    data object AcceptClicked : DispatchReceivedUiEvent
    data object CancelClicked : DispatchReceivedUiEvent
    data object PhoneClicked : DispatchReceivedUiEvent
    data object RequestCancelConfirmation : DispatchReceivedUiEvent
    data object CheckAddressAcceptance : DispatchReceivedUiEvent
    data class CloseExternalDialog(val tag: String) : DispatchReceivedUiEvent
}
sealed interface DispatchReceivedUiEffect {
    data class NavigateToInfoDispatch(val tripId: Long) : DispatchReceivedUiEffect
    data class OpenPermissionDialog(val dialog: DispatchReceivedDialogModel) : DispatchReceivedUiEffect
    data object RequestPhonePermission : DispatchReceivedUiEffect
    data object ShowRejectConfirmDialog : DispatchReceivedUiEffect
    data class CloseDialog(val tag: String) : DispatchReceivedUiEffect
    data class ShowToast(val message: String) : DispatchReceivedUiEffect
}
