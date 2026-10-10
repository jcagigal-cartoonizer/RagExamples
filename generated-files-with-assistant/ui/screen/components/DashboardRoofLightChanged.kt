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
// # Block 797-9: import com.interfacom.sdk.taximeter.bravocomm.ifConstants
object HomeButtonsReducer {
    fun reduce(
        previous: HomeButtonsState,
        action: HomeButtonsAction
    ): HomeButtonsState {
        return when (action) {
            is HomeButtonsAction.RoofLightChanged -> {
                previous.copy(isRoofLightOn = action.enabled)
            }
            is HomeButtonsAction.ZoneChanged -> {
                previous.copy(zoneText = action.zone)
            }
            is HomeButtonsAction.LocationEnabledChanged -> {
                previous.copy(locationEnabled = action.value)
            }
            is HomeButtonsAction.ShortBreakStatusChanged -> {
                previous.copy(shortBreakStatus = action.status)
            }
            is HomeButtonsAction.HasTaximeterConnectionChanged -> {
                previous.copy(hasTaximeterConnection = action.connected)
            }
            is HomeButtonsAction.ManualStateChanged -> {
                previous.copy(isManual = action.isManual)
            }
            is HomeButtonsAction.ShiftStatusChanged -> {
                val newState = previous.copy(currentShiftStatus = action.status)
                newState.withTopBarRightState()
            }
            is HomeButtonsAction.DispatchChanged -> {
                previous.copy(dispatch = action.dispatch).withNotificationDialogButtons()
            }
            is HomeButtonsAction.TripIdChanged -> {
                previous.copy(tripId = action.tripId)
            }
            HomeButtonsAction.EvaluateTopBarRight -> {
                previous.withTopBarRightState()
            }
            HomeButtonsAction.OpenNotificationDialog -> {
                previous.withNotificationDialogButtons()
            }
            HomeButtonsAction.HideNotificationDialog -> {
                previous.copy(notificationDialog = NotificationDashboardDialogState.Hidden)
            }
        }
    }
    fun HomeButtonsState.withNotificationDialogButtons(): HomeButtonsState {
        val dispatch = dispatch
        val buttons = buildList {
            if (dispatch?.isAtDoorNotificationEnabled == true && dispatch.isAtDoorNotificationSent == false) {
                add(NotificationButtonType.AT_DOOR)
            }
            if (dispatch?.riderInCab == true && dispatch.isRiderInCabNotificationSent == false) {
                add(NotificationButtonType.RIDER_IN_CAB)
            }
        }
        return copy(
            notificationDialog = if (buttons.isEmpty()) {
                NotificationDashboardDialogState.Hidden
            } else {
                NotificationDashboardDialogState.SelectNotification(buttons)
            }
        )
    }
    fun HomeButtonsState.withTopBarRightState(): HomeButtonsState {
        val taximeterConnection = hasTaximeterConnection
        val status = currentShiftStatus
        val showNext = when {
            !taximeterConnection && isHired(status) -> true
            status == null -> false
            isManual -> true
            isHired(status) && isTaximeterWithoutProtocol() && !hasITopTaximeterConnected() -> true
            status == ifConstants.STATE_DISPATCHED && canGoToHiredManual(taximeterConnection) -> true
            else -> false
        }
        return copy(
            topBarRight = if (showNext) {
                TopBarRightState.Next(enabled = true)
            } else {
                TopBarRightState.Hidden
            }
        )
    }
    fun isHired(status: String?): Boolean {
        return status == ifConstants.STATE_HIRED ||
                status == ifConstants.STATE_HIRED_DISPATCHED ||
                status == ifConstants.STATE_HIRED_NO_CENTRAL
    }
    fun isTaximeterWithoutProtocol(): Boolean {
        return true
    }
    fun hasITopTaximeterConnected(): Boolean {
        return true
    }
    fun canGoToHiredManual(taximeterConnection: Boolean): Boolean {
        return !taximeterConnection
    }
}
sealed interface HomeButtonsAction {
    data class RoofLightChanged(val enabled: Boolean?) : HomeButtonsAction
    data class ZoneChanged(val zone: String?) : HomeButtonsAction
    data class LocationEnabledChanged(val value: Pair<Boolean, Boolean>) : HomeButtonsAction
    data class ShortBreakStatusChanged(val status: String?) : HomeButtonsAction
    data class HasTaximeterConnectionChanged(val connected: Boolean) : HomeButtonsAction
    data class ManualStateChanged(val isManual: Boolean) : HomeButtonsAction
    data class ShiftStatusChanged(val status: String?) : HomeButtonsAction
    data class DispatchChanged(val dispatch: InfoDispatchUiModel?) : HomeButtonsAction
    data class TripIdChanged(val tripId: Long?) : HomeButtonsAction
    data object EvaluateTopBarRight : HomeButtonsAction
    data object OpenNotificationDialog : HomeButtonsAction
    data object HideNotificationDialog : HomeButtonsAction
}
