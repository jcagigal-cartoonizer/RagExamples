package ifac.td.taxi.compose.viewmodel
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
// # Block 922-10: import androidx.lifecycle.ViewModel
class MainActivityComposeBridgeViewModel(
    private val sharedViewModel: ComposeSharedViewModel
) : ViewModel() {
    private val _homeButtonsState = MutableStateFlow(HomeButtonsState())
    val homeButtonsState = _homeButtonsState.asStateFlow()
    fun bind() {
        viewModelScope.launch {
            sharedViewModel.roofLightFlow.collect { roofLight ->
                reduce(HomeButtonsAction.RoofLightChanged(roofLight))
            }
        }
        viewModelScope.launch {
            sharedViewModel.zoneFlow.collect { zone ->
                reduce(HomeButtonsAction.ZoneChanged(zone))
            }
        }
        viewModelScope.launch {
            sharedViewModel.locationEnabledFlow.collect { locationEnabled ->
                reduce(HomeButtonsAction.LocationEnabledChanged(locationEnabled))
            }
        }
        viewModelScope.launch {
            sharedViewModel.shortBreakStatus.collect { status ->
                reduce(HomeButtonsAction.ShortBreakStatusChanged(status))
            }
        }
        viewModelScope.launch {
            sharedViewModel.hasTaximeterConnectionFlow.collect { connected ->
                reduce(HomeButtonsAction.HasTaximeterConnectionChanged(connected))
            }
        }
        viewModelScope.launch {
            sharedViewModel.dispatchFlow.collect { dispatch ->
                reduce(
                    HomeButtonsAction.DispatchChanged(
                        dispatch = dispatch?.toUiModel()
                    )
                )
            }
        }
        viewModelScope.launch {
            sharedViewModel.shiftStatusFlow.collect { shift ->
                reduce(HomeButtonsAction.ShiftStatusChanged(shift?.currentStatus))
            }
        }
        viewModelScope.launch {
            sharedViewModel.userLoggedFlow.collect {
                reduce(HomeButtonsAction.EvaluateTopBarRight)
            }
        }
        viewModelScope.launch {
            sharedViewModel.locationManuallyDeLocatedFlow.collect {
                reduce(HomeButtonsAction.EvaluateTopBarRight)
            }
        }
    }
    fun onNotificationDialogDismiss() {
        reduce(HomeButtonsAction.HideNotificationDialog)
    }
    fun onOpenNotificationDialog() {
        reduce(HomeButtonsAction.OpenNotificationDialog)
    }
    fun onTopBarRightClicked() {
        val state = _homeButtonsState.value
        when (val top = state.topBarRight) {
            is TopBarRightState.Next -> {
                if (state.currentShiftStatus == com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_DISPATCHED) {
                    reduce(HomeButtonsAction.OpenNotificationDialog)
                } else {
                    // trigger manual payment / hired manual flow
                }
            }
            TopBarRightState.Hidden -> Unit
        }
    }
    fun reduce(action: HomeButtonsAction) {
        _homeButtonsState.update { current ->
            HomeButtonsReducer.reduce(current, action)
        }
    }
}
fun ifac.td.taxi.compose.home.InfoDispatchModel?.toUiModel(): InfoDispatchUiModel? {
    return this?.let {
        InfoDispatchUiModel(
            isAtDoorNotificationEnabled = it.isAtDoorNotificationEnabled(),
            isAtDoorNotificationSent = it.isAtDoorNotificationSent,
            riderInCab = it.riderInCab,
            isRiderInCabNotificationSent = it.isRiderInCabNotificationSent
        )
    }
}
Your `ComposeSharedViewModel` already exposes the flows.  
What you need now is a screen-level collector that mirrors the fragment’s `repeatOnLifecycle { launch { collect {} } }`.
Here’s the Compose version.
