package ifac.td.taxi.ui.screen.components
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
// # Block 13-1: import androidx.compose.runtime.Immutable
@Immutable
data class RequirementsUiState(
    val isLoading: Boolean = false,
    val driverRequirements: List<String> = emptyList(),
    val vehicleRequirements: List<String> = emptyList(),
    val showDriverNoData: Boolean = false,
    val showVehicleNoData: Boolean = false,
    val buttonsState: RequirementsButtonsState = RequirementsButtonsState(),
    val dialogState: RequirementsDialogState = RequirementsDialogState.Hidden
)
sealed interface RequirementsUiEvent {
    data object LoadRequirements : RequirementsUiEvent
    data object Retry : RequirementsUiEvent
    data object OnDriverPrimaryClicked : RequirementsUiEvent
    data object OnVehiclePrimaryClicked : RequirementsUiEvent
    data object OnDialogConfirmClicked : RequirementsUiEvent
    data object OnDialogDismissClicked : RequirementsUiEvent
}
sealed interface RequirementsUiEffect {
    data class ShowToast(val message: String) : RequirementsUiEffect
    data object NavigateBack : RequirementsUiEffect
    data class OpenDialog(val dialogState: RequirementsDialogState) : RequirementsUiEffect
    data object CloseDialog : RequirementsUiEffect
}
sealed class RequirementsDialogState {
    data object Hidden : RequirementsDialogState()
    data class Info(
        val title: String,
        val message: String,
        val confirmText: String = "OK",
        val dismissText: String? = null
    ) : RequirementsDialogState()
}
@Immutable
data class RequirementsButtonsState(
    val driverPrimary: RequirementsButtonStyleHelperButtonVisualState = RequirementsButtonStyleHelperButtonVisualState(),
    val vehiclePrimary: RequirementsButtonStyleHelperButtonVisualState = RequirementsButtonStyleHelperButtonVisualState()
) {
    companion object {
        fun initial(
            hasDriverData: Boolean,
            hasVehicleData: Boolean
        ): RequirementsButtonsState {
            return RequirementsButtonsState(
                driverPrimary = RequirementsButtonStyleHelperButtonVisualState.visible(
                    enabled = hasDriverData,
                    background = if (hasDriverData) Color(0xFF1E88E5) else Color(0xFFE0E0E0),
                    contentColor = if (hasDriverData) Color.White else Color(0xFF9E9E9E)
                ),
                vehiclePrimary = RequirementsButtonStyleHelperButtonVisualState.visible(
                    enabled = hasVehicleData,
                    background = if (hasVehicleData) Color(0xFF1E88E5) else Color(0xFFE0E0E0),
                    contentColor = if (hasVehicleData) Color.White else Color(0xFF9E9E9E)
                )
            )
        }
    }
}
@Immutable
data class RequirementsButtonStyleHelperButtonVisualState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val background: Color = Color(0xFF1E88E5),
    val contentColor: Color = Color.White,
    val borderColor: Color = Color.Transparent,
    val elevation: Int = 0
) {
    companion object {
        fun visible(
            enabled: Boolean = true,
            background: Color = Color(0xFF1E88E5),
            contentColor: Color = Color.White
        ) = RequirementsButtonStyleHelperButtonVisualState(
            visible = true,
            enabled = enabled,
            background = background,
            contentColor = contentColor
        )
        fun hidden() = RequirementsButtonStyleHelperButtonVisualState(visible = false)
    }
}
