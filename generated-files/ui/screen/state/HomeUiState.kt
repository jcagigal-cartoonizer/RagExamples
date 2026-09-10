package ifac.td.taxi.ui.screen.state
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
// // ## 4) Full Compose-friendly `HomeUiState`


import ifac.td.taxi.viewmodel.model.MessageUIEnum
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus

data class HomeUiState(
    val buttons: HomeButtonsState = HomeButtonsState(),
    val hasMessages: MessageUIEnum? = null,
    val locationEnabled: Pair<Boolean, Boolean> = true to true,
    val roofLightState: Pair<Boolean?, Boolean?>? = null,
    val showLocationButton: Boolean? = null,
    val pendingServicesButton: Boolean? = null,
    val locatedOnStop: Boolean = false,
    val locationType: String = "",
    val hasTaximeterConnection: Boolean = false,
    val orangeBtnPending: Boolean? = null,
    val shortBreakStatus: ShortBreakStatus? = null,
    val keepScreenOn: Boolean = false,
    val dashboardVisible: Boolean = false,
    val fixedPriceVisible: Boolean = false,
    val zoningEnabled: Boolean = true,
    val pendingButtonEnabled: Boolean = false,
    val manualTripAllowed: Boolean = false,
    val dialog: HomeDialogSpec? = null,
)


