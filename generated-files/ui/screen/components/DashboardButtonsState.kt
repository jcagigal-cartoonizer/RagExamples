package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 311-3: import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
@Immutable
data class DashboardButtonsState(
    val onStop: HeaderButtonState = HeaderButtonState(),
    val onZone: HeaderButtonState = HeaderButtonState(),
    val hired: HeaderButtonState = HeaderButtonState(),
    val trips: HeaderButtonState = HeaderButtonState(),
) {
    companion object {
        fun fromAvailableColumns(dataTypes: String): DashboardButtonsState {
            val columns = if (dataTypes.isBlank()) emptySet() else parseColumns(dataTypes)
            val onStopVisible = columns.contains(AvailableColumnsEnum.STAND_VEHICLES)
            val onZoneVisible = columns.contains(AvailableColumnsEnum.ZONE_VEHICLES)
            val hiredVisible = columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) ||
                    columns.contains(AvailableColumnsEnum.BOOKED_TRIPS)
            val tripsVisible = columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS) ||
                    columns.contains(AvailableColumnsEnum.TOTAL_TRIPS) ||
                    (columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) &&
                            columns.contains(AvailableColumnsEnum.BOOKED_TRIPS))
            return DashboardButtonsState(
                onStop = HeaderButtonState(
                    visible = onStopVisible,
                    text = "Stop",
                    textColor = Color.White,
                    backgroundColor = Color(0xFF1976D2),
                    iconVisible = false
                ),
                onZone = HeaderButtonState(
                    visible = onZoneVisible,
                    text = "Zone",
                    textColor = Color.White,
                    backgroundColor = Color(0xFF1976D2),
                    iconVisible = false
                ),
                hired = HeaderButtonState(
                    visible = hiredVisible,
                    text = if (columns.contains(AvailableColumnsEnum.HIRED_VEHICLES)) "Hired" else "Ub servicios",
                    textColor = Color.White,
                    backgroundColor = Color(0xFF1976D2),
                    iconVisible = false
                ),
                trips = HeaderButtonState(
                    visible = tripsVisible,
                    text = when {
                        columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS) -> "PreBooked"
                        columns.contains(AvailableColumnsEnum.TOTAL_TRIPS) -> "Total"
                        else -> "Trips"
                    },
                    textColor = Color.White,
                    backgroundColor = Color(0xFF1976D2),
                    iconVisible = false
                )
            )
        }
        fun parseColumns(dataTypes: String): Set<AvailableColumnsEnum> {
            // adapt this parser to your real backend string format
            return AvailableColumnsEnum.entries.filter { dataTypes.contains(it.name) }.toSet()
        }
    }
}
@Immutable
data class HeaderButtonState(
    val visible: Boolean = false,
    val text: String = "",
    val textColor: Color = Color.White,
    val backgroundColor: Color = Color(0xFF1976D2),
    val iconVisible: Boolean = false,
    val enabled: Boolean = true
)
