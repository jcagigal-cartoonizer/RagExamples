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
// # Block 497-3: import androidx.compose.foundation.shape.RoundedCornerShape
data class DashboardButtonsState(
    val onStop: DashboardButtonState = DashboardButtonState.hidden(),
    val onZone: DashboardButtonState = DashboardButtonState.hidden(),
    val hired: DashboardButtonState = DashboardButtonState.hidden(),
    val trips: DashboardButtonState = DashboardButtonState.hidden(),
) {
    companion object {
        fun fromDataTypes(dataTypes: String): DashboardButtonsState {
            val columns = ZoneUtils.parseStringDataTypes(dataTypes)
            val stopVisible = columns.contains(AvailableColumnsEnum.STAND_VEHICLES)
            val zoneVisible = columns.contains(AvailableColumnsEnum.ZONE_VEHICLES)
            val hiredVisible = columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) ||
                columns.contains(AvailableColumnsEnum.BOOKED_TRIPS)
            val tripsVisible = columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS) ||
                columns.contains(AvailableColumnsEnum.TOTAL_TRIPS) ||
                (columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) && columns.contains(AvailableColumnsEnum.BOOKED_TRIPS))
            return DashboardButtonsState(
                onStop = DashboardButtonState(
                    visible = stopVisible,
                    textRes = R.string.abrevStandVehicles,
                    style = DashboardButtonStyle.blue()
                ),
                onZone = DashboardButtonState(
                    visible = zoneVisible,
                    textRes = R.string.abrevUbZonaVehicles,
                    style = DashboardButtonStyle.blue()
                ),
                hired = DashboardButtonState(
                    visible = hiredVisible,
                    textRes = if (columns.contains(AvailableColumnsEnum.HIRED_VEHICLES)) {
                        R.string.abrevUbServicios
                    } else {
                        R.string.abrevUbServiciosWithoutDots
                    },
                    style = DashboardButtonStyle.red()
                ),
                trips = DashboardButtonState(
                    visible = tripsVisible,
                    textRes = when {
                        columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS) -> R.string.abbrevPreBookedTrips
                        columns.contains(AvailableColumnsEnum.TOTAL_TRIPS) -> R.string.abbrevTotalTrips
                        else -> R.string.abbrevTotalTrips
                    },
                    style = DashboardButtonStyle.red()
                )
            )
        }
    }
}
data class DashboardButtonState(
    val visible: Boolean,
    val textRes: Int,
    val style: DashboardButtonStyle,
) {
    val text: String = "" // resolved in Compose via stringResource
    companion object {
        fun hidden() = DashboardButtonState(
            visible = false,
            textRes = android.R.string.empty,
            style = DashboardButtonStyle.hidden()
        )
    }
}
