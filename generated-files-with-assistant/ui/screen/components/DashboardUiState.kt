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
// # Block 1-1: import androidx.compose.runtime.Immutable
@Immutable
data class DashboardUiState(
    val isLoadingZones: Boolean = true,
    val nearbyZones: List<ZoneModel> = emptyList(),
    val farZones: List<ZoneModel> = emptyList(),
    val actualZones: List<ZoneModel> = emptyList(),
    val pendingTrips: List<PendingTrip> = emptyList(),
    val pendingTripsCount: Int = 0,
    val headerState: DashboardHeaderState = DashboardHeaderState(),
    val buttonsState: DashboardButtonsState = DashboardButtonsState(),
    val dialogState: DashboardDialogState = DashboardDialogState.Hidden,
)
@Immutable
data class DashboardHeaderState(
    val showStand: Boolean = false,
    val showZone: Boolean = false,
    val showHired: Boolean = false,
    val showTrips: Boolean = false,
    val standText: String = "",
    val zoneText: String = "",
    val hiredText: String = "",
    val tripsText: String = "",
)
sealed interface DashboardUiEvent {
    data object OnResume : DashboardUiEvent
    data object OnPause : DashboardUiEvent
    data object OnBackPressed : DashboardUiEvent
    data object OnShowAllTripsClicked : DashboardUiEvent
    data object OnShowRecentTripsClicked : DashboardUiEvent
    data object OnCancelClicked : DashboardUiEvent
    data object OnCloseClicked : DashboardUiEvent
    data object OnDismissDialog : DashboardUiEvent
    data object OnConfirmDialog : DashboardUiEvent
    data class OnZoneClicked(val zone: ZoneModel) : DashboardUiEvent
    data class OnZoneLongClicked(val macroZoneId: Int, val zoneId: Int) : DashboardUiEvent
}
sealed interface DashboardUiEffect {
    data object NavigateBack : DashboardUiEffect
    data object NavigateToHome : DashboardUiEffect
    data object NavigateToOnTrip : DashboardUiEffect
    data object OpenDelocateDialog : DashboardUiEffect
    data object CloseDialog : DashboardUiEffect
}
