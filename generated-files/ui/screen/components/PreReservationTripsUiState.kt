package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PreReservationTripsButtonsState = PreReservationTripsButtonsState
import ifac.td.taxi.ui.screen.components.PreReservationTripsUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 6-1: import com.interfacom.sdk.taximeter.bravocomm.models.prereservations.Prereservation
data class PreReservationTripsUiState(
    val isLoading: Boolean = false,
    val trips: List<Prereservation> = emptyList(),
    val dialog: PreReservationTripsDialogState? = null,
    val buttonsState: PreReservationTripsButtonsState = PreReservationTripsButtonsState(),
    val showHeader: Boolean = true,
)
sealed interface PreReservationTripsUiEvent {
    data object ScreenStarted : PreReservationTripsUiEvent
    data object ScreenPaused : PreReservationTripsUiEvent
    data class TripClicked(val trip: Prereservation) : PreReservationTripsUiEvent
    data object DialogAcceptClicked : PreReservationTripsUiEvent
    data object DialogCancelClicked : PreReservationTripsUiEvent
}
sealed interface PreReservationTripsUiEffect {
    data class ShowToast(val messageRes: Int) : PreReservationTripsUiEffect
    data class ShowDialog(
        val dialog: PreReservationTripsDialogState
    ) : PreReservationTripsUiEffect
    data object DismissDialog : PreReservationTripsUiEffect
    data object NavigateBack : PreReservationTripsUiEffect
}
data class PreReservationTripsDialogState(
    val title: String,
    val description: String,
    val acceptLabel: String,
    val cancelLabel: String? = null,
    val isDestructive: Boolean = false,
)
