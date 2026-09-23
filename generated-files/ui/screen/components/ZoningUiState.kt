package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ZoningUiState
import ifac.td.taxi.ui.screen.components.ZoningButtonsState = ZoningButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
