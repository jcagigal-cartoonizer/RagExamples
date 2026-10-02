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
// # Block 4-1: import androidx.annotation.StringRes
@Immutable
data class HomeUiState(
    val buttons: HomeButtonsState = HomeButtonsState(),
    val dialog: HomeDialogState? = null,
    val isKeepScreenOn: Boolean = true,
)
sealed interface HomeUiEffect {
    data class NavigateToRes(val resId: Int) : HomeUiEffect
    data class NavigateDeepLink(val uri: String) : HomeUiEffect
    data class ShowToast(@StringRes val resId: Int) : HomeUiEffect
    data class Beep(val tone: Int) : HomeUiEffect
    data object ResetMainActivityFlows : HomeUiEffect
    data object OpenPendingTrips : HomeUiEffect
    data object OpenMessages : HomeUiEffect
    data object OpenReceiptHistory : HomeUiEffect
    data object OpenContactCentral : HomeUiEffect
    data object OpenDashboard : HomeUiEffect
    data object OpenFixedPrice : HomeUiEffect
    data object LogoffAndGoToWelcome : HomeUiEffect
}
@Immutable
data class HomeDialogState(
    val type: HomeDialogType,
    val title: String,
    val description: String? = null,
    val showCancel: Boolean = true,
    val showAccept: Boolean = true,
)
enum class HomeDialogType {
    ACTIVATE_LOCATION,
    DEACTIVATE_LOCATION,
    EXIT_SHIFT,
    MANUAL_TRIP,
    LOCATE_STOP_EXIT,
}
@Immutable
data class HomeButtonsState(
    val location: HomeComposeButtonState = HomeComposeButtonState.Disabled(textRes = R.string.btn_location_on),
    val zoning: HomeComposeButtonState = HomeComposeButtonState.Disabled(),
    val pending: HomeComposeButtonState = HomeComposeButtonState.Disabled(),
    val locateStand: HomeComposeButtonState = HomeComposeButtonState.Disabled(textRes = R.string.btn_locate_stop),
    val central: HomeComposeButtonState = HomeComposeButtonState.Disabled(),
    val receipts: HomeComposeButtonState = HomeComposeButtonState.Enabled(),
    val messages: HomeComposeButtonState = HomeComposeButtonState.Disabled(),
    val roofLight: HomeComposeButtonState = HomeComposeButtonState.Disabled(),
    val dashboard: HomeComposeButtonState = HomeComposeButtonState.Hidden(),
    val fixedPrice: HomeComposeButtonState = HomeComposeButtonState.Hidden(),
) {
    companion object {
        fun initial() = HomeButtonsState(
            messages = HomeComposeButtonState.Disabled(),
            locateStand = HomeComposeButtonState.Disabled(textRes = R.string.btn_locate_stop),
        )
    }
}
@Immutable
data class HomeComposeButtonState(
    val enabled: Boolean,
    val visible: Boolean = true,
    val loading: Boolean = false,
    val textRes: Int? = null,
    val background: ButtonColor = ButtonColor.Blue,
    val textColor: Color = Color.White,
    val style: ButtonVisualStyle = ButtonVisualStyle.Default,
) {
    companion object {
        fun Enabled(
            textRes: Int? = null,
            background: ButtonColor = ButtonColor.Blue,
            textColor: Color = Color.White,
        ) = HomeComposeButtonState(true, true, false, textRes, background, textColor)
        fun Disabled(
            textRes: Int? = null,
            background: ButtonColor = ButtonColor.Blue,
            textColor: Color = Color.White,
        ) = HomeComposeButtonState(false, true, false, textRes, background, textColor)
        fun Hidden() = HomeComposeButtonState(false, false, false)
        fun Loading() = HomeComposeButtonState(true, true, true)
    }
}
enum class ButtonColor {
    Blue, Red, Green, Orange, Gray
}
enum class ButtonVisualStyle {
    Default, Empty
}
@Immutable
data class HomeRuntimeState(
    val locationEnabled: Boolean = false,
    val canEnableLocation: Boolean = false,
    val locationPermissionAllowed: Boolean = false,
    val shortBreakStatus: ShortBreakStatus? = null,
    val zone: String? = null,
    val locatedOnStop: Boolean = false,
    val locationType: String = "",
    val orangePending: Boolean = false,
    val hasMessages: MessageUIEnum? = null,
    val roofLightState: Boolean? = null,
    val roofLightAvailable: Boolean = false,
    val roofLightDisableLuminous: Boolean? = null,
    val pendingServicesButton: Boolean? = null,
    val pendingTripsEmpty: Boolean? = null,
    val hasTaximeterConnection: Boolean = false,
    val showDashboardButton: Boolean = false,
    val showEstimateFixedPrice: Boolean = false,
)
