package ifac.td.taxi.ui.screen.state
import ifac.td.taxi.ui.screen.state.DashboardButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // ## 2) DashboardButtonsState with exact visibility/color behavior

// This is the state object that replaces the fragment button logic and the XML visibility toggles.


import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum

@Immutable
data class DashboardButtonsState(
    val showOnStop: Boolean = false,
    val showOnZone: Boolean = false,
    val showHired: Boolean = false,
    val showTrips: Boolean = false,

    val onStopTextRes: Int = R.string.abrevUbParada,
    val onZoneTextRes: Int = R.string.abrevUbZona,
    val hiredTextRes: Int = R.string.abrevUbServicios, // default text
    val tripsTextRes: Int = R.string.abbrevPreBookedTrips,

    val onStopSelected: Boolean = false,
    val onZoneSelected: Boolean = false,
    val hiredSelected: Boolean = false,
    val tripsSelected: Boolean = false,

    val onStopSortVisible: Boolean = false,
    val onZoneSortVisible: Boolean = false,
    val hiredSortVisible: Boolean = false,
    val tripsSortVisible: Boolean = false,

    val onStopSortAsc: Boolean = false,
    val onZoneSortAsc: Boolean = false,
    val hiredSortAsc: Boolean = false,
    val tripsSortAsc: Boolean = false,

    val locateButtonVisible: Boolean = true,
    val locateButtonEnabled: Boolean = true,
    val locateButtonTextRes: Int = R.string.btn_soon_to_clear,
    val locateButtonType: DashboardButtonType = DashboardButtonType.Blue
)

enum class DashboardButtonType {
    Blue, Red, Gray, Disable
}

