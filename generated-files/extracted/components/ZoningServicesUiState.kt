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
// # Block 320-3: import androidx.compose.runtime.Immutable
@Immutable
data class ZoningServicesUiState(
    val zoneName: String = "",
    val zoneTrips: List<ZoneTripModel> = emptyList(),
    val refreshProgress: Int = 0,
    val showLoading: Boolean = true,
    val currentShiftStatus: Int? = null,
    val tripId: Long? = null,
    val dialogState: ZoningServicesCustomDialogCustomDialogState = ZoningServicesCustomDialogCustomDialogState(),
    val buttonsState: ZoningServicesButtonsState = ZoningServicesButtonsState(),
    val zoneLoading: Boolean = false,
)
sealed interface ZoningServicesUiEvent {
    data class Init(val idMacroZone: Int, val idZone: Int) : ZoningServicesUiEvent
    data object ShowAllClicked : ZoningServicesUiEvent
    data object ShowRecentClicked : ZoningServicesUiEvent
    data object CancelClicked : ZoningServicesUiEvent
    data object CloseClicked : ZoningServicesUiEvent
    data object ShowCloseDialog : ZoningServicesUiEvent
    data object HideCloseDialog : ZoningServicesUiEvent
    data object DismissDialog : ZoningServicesUiEvent
    data object ConfirmDialog : ZoningServicesUiEvent
}
sealed interface ZoningServicesUiEffect {
    data object NavigateBack : ZoningServicesUiEffect
    data object NavigateToHome : ZoningServicesUiEffect
    data object NavigateToOnTrip : ZoningServicesUiEffect
}
@Immutable
data class ZoningServicesCustomDialogCustomDialogState(
    val visible: Boolean = false,
    val title: String = "Confirm",
    val message: String = "Do you want to continue?",
    val confirmText: String = "OK",
    val dismissText: String = "Cancel",
)
@Immutable
data class ZoningServicesButtonsState(
    val showAllVisible: Boolean = false,
    val showRecentVisible: Boolean = true,
    val showAllEnabled: Boolean = true,
    val showRecentEnabled: Boolean = true,
    val cancelVisible: Boolean = true,
    val closeVisible: Boolean = true,
    val showAllSelected: Boolean = false,
    val showRecentSelected: Boolean = false,
) {
    fun withShowAllHidden() = copy(
        showAllVisible = false,
        showRecentVisible = true,
        showAllSelected = true,
        showRecentSelected = false
    )
    fun withShowRecentHidden() = copy(
        showAllVisible = true,
        showRecentVisible = false,
        showAllSelected = false,
        showRecentSelected = true
    )
}
