package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.DestinationMapButtonsState
import ifac.td.taxi.ui.screen.components.DestinationMap
import ifac.td.taxi.ui.screen.components.DestinationMapUiState
import ifac.td.taxi.ui.screen.components.DestinationMapButtonsState = DestinationMapButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 5-1: import android.content.Intent
@Immutable
data class DestinationMapUiState(
    val routePoints: List<RoutePointModel> = emptyList(),
    val buttonsState: DestinationMapButtonsState = DestinationMapButtonsState(),
    val dialogState: DestinationMapDialogState = DestinationMapDialogState.Hidden,
    val isLoading: Boolean = false
)
sealed interface DestinationMapDialogState {
    data object Hidden : DestinationMapDialogState
    data class ConfirmOpenNavigator(
        val coordinates: String,
        val address: String? = null
    ) : DestinationMapDialogState
}
sealed interface DestinationMapUiEffect {
    data class OpenNavigatorIntent(val intent: Intent?) : DestinationMapUiEffect
    data class ShowToast(val messageRes: Int) : DestinationMapUiEffect
    data object HideKeyboard : DestinationMapUiEffect
}
@Immutable
data class DestinationMapButtonsState(
    val acceptVisible: Boolean = true,
    val acceptEnabled: Boolean = true,
    val cancelVisible: Boolean = true,
    val cancelEnabled: Boolean = true,
    val secondaryVisible: Boolean = false,
    val secondaryEnabled: Boolean = false,
    val acceptText: String = "",
    val cancelText: String = "",
    val secondaryText: String = "",
    val acceptStyle: DestinationMapButtonStyle = DestinationMapButtonStyle.Primary,
    val cancelStyle: DestinationMapButtonStyle = DestinationMapButtonStyle.Secondary,
    val secondaryStyle: DestinationMapButtonStyle = DestinationMapButtonStyle.Tertiary
) {
    companion object {
        fun defaultForDestinationMap(): DestinationMapButtonsState = DestinationMapButtonsState(
            acceptVisible = true,
            acceptEnabled = true,
            cancelVisible = true,
            cancelEnabled = true,
            secondaryVisible = false,
            secondaryEnabled = false,
            acceptText = "Open in navigator",
            cancelText = "Back",
            secondaryText = "",
            acceptStyle = DestinationMapButtonStyle.Primary,
            cancelStyle = DestinationMapButtonStyle.Secondary,
            secondaryStyle = DestinationMapButtonStyle.Tertiary
        )
    }
}
enum class DestinationMapButtonStyle {
    Primary,
    Secondary,
    Tertiary
}
