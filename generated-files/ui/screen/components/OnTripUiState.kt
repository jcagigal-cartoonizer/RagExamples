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
// # Block 12-1: import androidx.annotation.DrawableRes
@Immutable
data class OnTripUiState(
    val buttons: OnTripButtonsState = OnTripButtonsState(),
    val showDialog: OnTripDialogState? = null,
    val topBarRightVisible: Boolean = false,
    val topBarRightEnabled: Boolean = false,
)
@Immutable
data class OnTripDialogState(
    val title: String,
    val description: String? = null,
    val buttons: List<OnTripDialogButton> = emptyList()
)
enum class OnTripDialogButton {
    CANCEL,
    ACCEPT,
    AT_DOOR,
    RIDER_IN_CAB
}
sealed interface OnTripUiEffect {
    data object NavigateToEstimateFixedPrice : OnTripUiEffect
    data object NavigateToReceiptHistory : OnTripUiEffect
    data object NavigateToMessage : OnTripUiEffect
    data object NavigateToContactCentral : OnTripUiEffect
    data object NavigateToDispatchInfo : OnTripUiEffect
    data object NavigateToDirections : OnTripUiEffect
    data object NavigateToMacroZoning : OnTripUiEffect
    data class NavigateToDeepLink(val deepLinkUri: String) : OnTripUiEffect
    data class OpenExternalIntent(val intentAction: String) : OnTripUiEffect
    data object ShowSelectNotificationDialog : OnTripUiEffect
    data object ShowConfirmNoClientDialog : OnTripUiEffect
    data object ShowConfirmHiredManualDialog : OnTripUiEffect
    data object ShowTopBarNext : OnTripUiEffect
    data object HideTopBarNext : OnTripUiEffect
    data object ShowToastItopRestricted : OnTripUiEffect
    data object NoEffect : OnTripUiEffect
}
@Immutable
data class OnTripButtonsState(
    val fixedPrice: OnTripButtonState = OnTripButtonState.Hidden,
    val notifications: OnTripButtonState = OnTripButtonState.Hidden,
    val zoning: OnTripButtonState = OnTripButtonState.Hidden,
    val navigate: OnTripButtonState = OnTripButtonState.Hidden,
    val dispatchInfo: OnTripButtonState = OnTripButtonState.Hidden,
    val receipts: OnTripButtonState = OnTripButtonState.Hidden,
    val messages: OnTripButtonState = OnTripButtonState.Hidden,
    val central: OnTripButtonState = OnTripButtonState.Hidden,
    val client: OnTripButtonState = OnTripButtonState.Hidden,
    val roofLight: OnTripButtonState = OnTripButtonState.Hidden,
)
@Immutable
data class OnTripButtonState(
    val visible: Boolean = false,
    val enabled: Boolean = false,
    val text: String = "",
    val iconRes: Int? = null,
    val background: ButtonBackground = ButtonBackground.Default,
    val style: ButtonStyle = ButtonStyle.Disabled,
    val type: ButtonTypeUi = ButtonTypeUi.Default,
)
enum class ButtonStyle { Enabled, Disabled, Loading }
enum class ButtonTypeUi { Default, Empty, AtDoor, RiderInCab }
enum class ButtonBackground { Default, Red, Green, Blue, Orange, Gray }
