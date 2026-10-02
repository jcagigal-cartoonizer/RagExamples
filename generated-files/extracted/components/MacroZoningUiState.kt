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
// # Block 436-3: import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
data class MacroZoningUiState(
    val macroZones: List<MacroZoneModelUi> = emptyList(),
    val listOrder: OrderOptions = OrderOptions.NONE,
    val dataTypes: String? = null,
    val scrollMode: ScrollModeEnum = ScrollModeEnum.FOLLOW_SELECTED,
    val refreshProgress: Int = 0,
    val selectedZoneId: Int? = null,
    val shortBreakStatus: ShortBreakStatus? = null,
    val currentZone: String? = null,
    val hiredZone: String? = null,
    val buttonsState: MacroZoningButtonsState = MacroZoningButtonsState(),
    val dialogState: MacroZoningCustomActionButtonCustomDialogState? = null
)
sealed interface MacroZoningUiEvent {
    data object OnResume : MacroZoningUiEvent
    data object OnPause : MacroZoningUiEvent
    data object OnFilterClick : MacroZoningUiEvent
    data class OnOrderSelected(val order: OrderOptions) : MacroZoningUiEvent
    data object OnPendingClick : MacroZoningUiEvent
    data object OnPreReservationClick : MacroZoningUiEvent
    data class OnMacroZoneClick(val macroZoneId: Int) : MacroZoningUiEvent
    data object OnDialogDismiss : MacroZoningUiEvent
    data object OnDialogConfirm : MacroZoningUiEvent
}
sealed interface MacroZoningUiEffect {
    data class NavigateToZoning(val macroZoneId: Int) : MacroZoningUiEffect
    data object NavigateToPendingTrips : MacroZoningUiEffect
    data object NavigateToPreReservationTrips : MacroZoningUiEffect
    data class ShowToast(val message: String) : MacroZoningUiEffect
    data class ShowDialog(val dialogState: MacroZoningCustomActionButtonCustomDialogState) : MacroZoningUiEffect
}
