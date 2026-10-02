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
// # Block 31-2: import androidx.annotation.DrawableRes
data class ZoningUiState(
    val isLoading: Boolean = true,
    val progress: Int = 0,
    val macroZoneName: String = "",
    val placeholderText: String = "",
    val zones: List<ZoneModel> = emptyList(),
    val selectedZone: ZoneModel? = null,
    val listOrder: OrderOptions = OrderOptions.NONE,
    val listFilter: FilterOptions = FilterOptions.ZONE_BY_MACROZONE,
    val headerVisibility: ZoneHeaderVisibility = ZoneHeaderVisibility(),
    val buttonsState: ZoningButtonsState = ZoningButtonsState(),
    val dialog: ZoningDialogState? = null,
    val showZoneList: Boolean = true,
    val scrollMode: ScrollModeUi = ScrollModeUi.FOLLOW_SELECTED,
)
data class ZoneHeaderVisibility(
    val stand: Boolean = true,
    val zone: Boolean = true,
    val hired: Boolean = true,
    val trips: Boolean = true,
)
data class ZoningDialogState(
    val type: DialogType,
    val title: String,
    val description: String? = null,
    val buttons: List<ButtonSpec> = emptyList(),
    val listOptions: Map<Int, String>? = null,
)
enum class DialogType {
    Custom,
    Filter,
}
data class ButtonSpec(
    val type: ButtonTypeUi,
    @StringRes val textRes: Int,
)
enum class ButtonTypeUi {
    Cancel,
    Accept,
    AddFavourites,
    RemoveFavourites,
    GoingHome,
    OpenReinforcement,
    WithZone,
    WithoutZone,
    Poi,
}
enum class ScrollModeUi {
    JUMP_TO_TOP,
    FOLLOW_SELECTED,
    NO_SCROLL,
}
