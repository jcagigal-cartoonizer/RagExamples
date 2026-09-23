package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PreReservationTripsButtonStyle
import ifac.td.taxi.ui.screen.components.PreReservationTripsButtonState
import ifac.td.taxi.ui.screen.components.PreReservationTripsUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 538-7: import androidx.compose.ui.graphics.Color
data class PreReservationTripsButtonStyle(
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color? = null,
    val enabled: Boolean = true
)
fun PreReservationTripsButtonState.toStyle(
    primaryColor: Color,
    secondaryColor: Color,
    warningColor: Color,
    disabledColor: Color,
    onPrimary: Color,
    onSecondary: Color,
    onWarning: Color,
    onDisabled: Color
): PreReservationTripsButtonStyle {
    return when (kind) {
        PreReservationTripsButtonKind.Primary -> PreReservationTripsButtonStyle(
            backgroundColor = primaryColor,
            contentColor = onPrimary,
            enabled = enabled
        )
        PreReservationTripsButtonKind.Secondary -> PreReservationTripsButtonStyle(
            backgroundColor = secondaryColor,
            contentColor = onSecondary,
            enabled = enabled
        )
        PreReservationTripsButtonKind.Warning -> PreReservationTripsButtonStyle(
            backgroundColor = warningColor,
            contentColor = onWarning,
            enabled = enabled
        )
        PreReservationTripsButtonKind.Disabled -> PreReservationTripsButtonStyle(
            backgroundColor = disabledColor,
            contentColor = onDisabled,
            enabled = false
        )
    }
}
In Compose, the equivalent of your `repeatOnLifecycle(STARTED)` is:
In the sample above, I used `uiState.trips.first()` as a placeholder when confirming the dialog. In a real implementation you should store the selected trip:
data class PreReservationTripsUiState(
    val trips: List<Prereservation> = emptyList(),
    val selectedTrip: Prereservation? = null,
    val dialog: PreReservationTripsDialogState? = null,
    ...
)
Then set it on click and use it on confirm. That is the Compose equivalent of your fragment’s closure-based dialog callback.
To fully mirror the original ViewModel, keep these calls:
But in Compose, emit only:
That fully replaces multiple flows and fragment-side callbacks.
1. a **complete ViewModel implementation with selected-trip support**,  
2. a **Compose version of the list item matching the RecyclerView row**, and  
3. a **drop-in Fragment hosting a ComposeView** so you can migrate gradually.
