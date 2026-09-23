package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.RequirementsButtonVisualState = RequirementsButtonVisualState
import ifac.td.taxi.ui.screen.components.RequirementsDialogState
import ifac.td.taxi.ui.screen.components.RequirementsButtonVisualState
import ifac.td.taxi.ui.screen.components.RequirementsButtonsState
import ifac.td.taxi.ui.screen.components.RequirementsButtonsState = RequirementsButtonsState
import ifac.td.taxi.ui.screen.components.RequirementsUiEffect
import ifac.td.taxi.ui.screen.components.RequirementsUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
    val driverPrimary: RequirementsButtonVisualState = RequirementsButtonVisualState(),
    val vehiclePrimary: RequirementsButtonVisualState = RequirementsButtonVisualState()
) {
    companion object {
        fun initial(
            hasDriverData: Boolean,
            hasVehicleData: Boolean
        ): RequirementsButtonsState {
            return RequirementsButtonsState(
                driverPrimary = RequirementsButtonVisualState.visible(
                    enabled = hasDriverData,
                    background = if (hasDriverData) Color(0xFF1E88E5) else Color(0xFFE0E0E0),
                    contentColor = if (hasDriverData) Color.White else Color(0xFF9E9E9E)
                ),
                vehiclePrimary = RequirementsButtonVisualState.visible(
                    enabled = hasVehicleData,
                    background = if (hasVehicleData) Color(0xFF1E88E5) else Color(0xFFE0E0E0),
                    contentColor = if (hasVehicleData) Color.White else Color(0xFF9E9E9E)
                )
            )
        }
    }
}
@Immutable
data class RequirementsButtonVisualState(
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
        ) = RequirementsButtonVisualState(
            visible = true,
            enabled = enabled,
            background = background,
            contentColor = contentColor
        )
        fun hidden() = RequirementsButtonVisualState(visible = false)
    }
}
