package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.RequirementsUiEvent
import ifac.td.taxi.ui.screen.components.RequirementsButtonsState
import ifac.td.taxi.ui.screen.components.RequirementsUiEffect
import ifac.td.taxi.ui.screen.components.RequirementsUiState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 102-2: import android.app.Application
class RequirementsComposeViewModel(
    application: Application,
    private val bravoRestApiUseCase: BravoRestApiUseCase
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(RequirementsUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<RequirementsUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()
    init {
        onEvent(RequirementsUiEvent.LoadRequirements)
    }
    fun onEvent(event: RequirementsUiEvent) {
        when (event) {
            RequirementsUiEvent.LoadRequirements,
            RequirementsUiEvent.Retry -> loadRequirements()
            RequirementsUiEvent.OnDriverPrimaryClicked -> {
                val dialog = RequirementsDialogState.Info(
                    title = "Driver Requirements",
                    message = "Open driver requirements action.",
                    confirmText = "OK"
                )
                emitDialog(dialog)
            }
            RequirementsUiEvent.OnVehiclePrimaryClicked -> {
                val dialog = RequirementsDialogState.Info(
                    title = "Vehicle Requirements",
                    message = "Open vehicle requirements action.",
                    confirmText = "OK"
                )
                emitDialog(dialog)
            }
            RequirementsUiEvent.OnDialogConfirmClicked -> {
                hideDialog()
                emitEffect(RequirementsUiEffect.CloseDialog)
            }
            RequirementsUiEvent.OnDialogDismissClicked -> {
                hideDialog()
                emitEffect(RequirementsUiEffect.CloseDialog)
            }
        }
    }
    fun loadRequirements() {
        _uiState.update { it.copy(isLoading = true, dialogState = RequirementsDialogState.Hidden) }
        viewModelScope.launch(Dispatchers.IO) {
            bravoRestApiUseCase.getCarsAndDriverRequirements { requirements ->
                val driver = requirements?.driverReqs.orEmpty()
                val vehicle = requirements?.vehicleReqs.orEmpty()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        driverRequirements = driver,
                        vehicleRequirements = vehicle,
                        showDriverNoData = driver.isEmpty(),
                        showVehicleNoData = vehicle.isEmpty(),
                        buttonsState = RequirementsButtonsState.initial(
                            hasDriverData = driver.isNotEmpty(),
                            hasVehicleData = vehicle.isNotEmpty()
                        )
                    )
                }
                if (requirements == null) {
                    emitEffect(
                        RequirementsUiEffect.ShowToast("Failed to load requirements")
                    )
                }
            }
        }
    }
    fun emitDialog(dialogState: RequirementsDialogState.Info) {
        _uiState.update { it.copy(dialogState = dialogState) }
        emitEffect(RequirementsUiEffect.OpenDialog(dialogState))
    }
    fun hideDialog() {
        _uiState.update { it.copy(dialogState = RequirementsDialogState.Hidden) }
    }
    fun emitEffect(effect: RequirementsUiEffect) {
        _uiEffect.tryEmit(effect)
    }
}
