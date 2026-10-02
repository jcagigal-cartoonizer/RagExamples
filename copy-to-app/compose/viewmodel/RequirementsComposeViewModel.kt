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
