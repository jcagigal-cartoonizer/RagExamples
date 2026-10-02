package ifac.td.taxi.compose.navigation
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
data class ZoningUiState(
    val progress: Int = 0,
    val macroZoneName: String = "",
    val placeholderText: String = "",
    val buttonsState: ZoningButtonsState = ZoningButtonsState(),
    val headerVisibility: ZoningHeaderVisibility = ZoningHeaderVisibility(),
    val zones: List<ZoneModel> = emptyList(),
    val selectedZone: ZoneModel? = null,
    val dialog: ZoningDialogModel? = null,
    val isLoadingZones: Boolean = false,
    val currentFilter: FilterOptions = FilterOptions.ZONE_BY_MACROZONE,
    val canShowSoonInZone: Boolean = false,
    val isTablet: Boolean = false,
    val isLandscape: Boolean = false,
)
@Immutable
data class ZoningHeaderVisibility(
    val stand: Boolean = false,
    val zone: Boolean = false,
    val hired: Boolean = false,
    val trips: Boolean = false,
)
@Immutable
data class ZoningButtonsState(
    val locateOnHired: ComposeActionButtonState = ComposeActionButtonState.Hidden(),
    val soonInZone: ComposeActionButtonState = ComposeActionButtonState.Hidden(),
    val pending: ComposeActionButtonState = ComposeActionButtonState.Hidden(),
    val trips: ComposeActionButtonState = ComposeActionButtonState.Hidden(),
    val cars: ComposeActionButtonState = ComposeActionButtonState.Hidden(),
)
sealed class ComposeActionButtonState {
    data class Enabled(
        @StringRes val textRes: Int,
        val iconRes: Int,
        val color: ComposeButtonColor
    ) : ComposeActionButtonState()
    data class Disabled(
        @StringRes val textRes: Int,
        val iconRes: Int,
        val color: ComposeButtonColor
    ) : ComposeActionButtonState()
    data class Hidden(
        @StringRes val textRes: Int = 0,
        val iconRes: Int = 0,
        val color: ComposeButtonColor = ComposeButtonColor.Blue
    ) : ComposeActionButtonState()
}
enum class ComposeButtonColor {
    Blue, Green, Red, Orange
}
@Immutable
data class ZoningDialogModel(
    val title: String,
    val description: String? = null,
    val buttons: List<ZoningDialogButton> = emptyList(),
    val listOptions: Map<Int, String>? = null
)
@Immutable
data class ZoningDialogButton(
    val type: ButtonTypeUi,
    val label: String
)
sealed class ZoningUiEvent {
    data object ScreenResumed : ZoningUiEvent()
    data object ClickTrips : ZoningUiEvent()
    data object ClickCars : ZoningUiEvent()
    data object ClickPending : ZoningUiEvent()
    data object ClickLocateOnHired : ZoningUiEvent()
    data object ClickSoonInZone : ZoningUiEvent()
    data class SelectZone(val zone: ZoneModel) : ZoningUiEvent()
    data object ClearDialog : ZoningUiEvent()
    data class HeaderClicked(val order: OrderOptions) : ZoningUiEvent()
    data class FilterChanged(val filter: FilterOptions) : ZoningUiEvent()
    data class ButtonAction(val type: ButtonTypeUi, val selectedOption: ZoningDialogListOption? = null) : ZoningUiEvent()
}
sealed class ZoningUiEffect {
    data class NavigateToServices(val idMacroZone: Int, val idZone: Int) : ZoningUiEffect()
    data class NavigateToCars(val idMacroZone: Int, val idZone: Int) : ZoningUiEffect()
    data object NavigateToPendingTrips : ZoningUiEffect()
    data object NavigateToHomeAndOnTrip : ZoningUiEffect()
    data object NavigateToPointsOfInterest : ZoningUiEffect()
    data class ShowToast(val messageRes: Int) : ZoningUiEffect()
    data class OpenFilterSheet(val currentFilter: FilterOptions) : ZoningUiEffect()
    data class OpenDialog(val dialog: ZoningDialogModel) : ZoningUiEffect()
}
