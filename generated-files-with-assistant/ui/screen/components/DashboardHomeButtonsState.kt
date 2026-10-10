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
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.DashboardScreen
// # Block 748-8: import androidx.compose.runtime.Immutable
@Immutable
data class HomeButtonsState(
    val topBarRight: TopBarRightState = TopBarRightState.Hidden,
    val notificationDialog: NotificationDashboardDialogState = NotificationDashboardDialogState.Hidden,
    val isRoofLightOn: Boolean? = null,
    val zoneText: String? = null,
    val locationEnabled: Pair<Boolean, Boolean> = true to true,
    val shortBreakStatus: String? = null,
    val hasTaximeterConnection: Boolean = false,
    val isManual: Boolean = false,
    val currentShiftStatus: String? = null,
    val dispatch: InfoDispatchUiModel? = null,
    val tripId: Long? = null,
)
@Immutable
sealed interface TopBarRightState {
    data object Hidden : TopBarRightState
    data class Next(
        val enabled: Boolean,
        val showConfirmManualDialog: Boolean = false
    ) : TopBarRightState
}
@Immutable
sealed interface NotificationDashboardDialogState {
    data object Hidden : NotificationDashboardDialogState
    data class SelectNotification(
        val buttons: List<NotificationButtonType>
    ) : NotificationDashboardDialogState
}
enum class NotificationButtonType {
    AT_DOOR,
    RIDER_IN_CAB,
    CANCEL,
    ACCEPT
}
@Immutable
data class InfoDispatchUiModel(
    val isAtDoorNotificationEnabled: Boolean = false,
    val isAtDoorNotificationSent: Boolean = false,
    val riderInCab: Boolean = false,
    val isRiderInCabNotificationSent: Boolean = false,
)
