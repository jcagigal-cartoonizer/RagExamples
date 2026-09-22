package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.annotation.StringRes
data class HomeUiState(
    val buttons: HomeButtonsState = HomeButtonsState(),
    val dialog: HomeDialogState? = null
)
sealed interface HomeUiEvent {
    data object ScreenResumed : HomeUiEvent
    data object ZoningClicked : HomeUiEvent
    data object PendingClicked : HomeUiEvent
    data object LocationClicked : HomeUiEvent
    data object ReceiptsClicked : HomeUiEvent
    data object MessagesClicked : HomeUiEvent
    data object CentralClicked : HomeUiEvent
    data object DashboardClicked : HomeUiEvent
    data object FixedPriceClicked : HomeUiEvent
    data object RoofLightClicked : HomeUiEvent
    data object LocateStandClicked : HomeUiEvent
    data object DialogDismissed : HomeUiEvent
    data object DialogCancelled : HomeUiEvent
    data object DialogConfirmed : HomeUiEvent
}
sealed interface HomeUiEffect {
    data class Navigate(val destination: HomeNavigation) : HomeUiEffect
    data class ShowToast(@StringRes val messageRes: Int) : HomeUiEffect
    data class Beep(val tone: Int) : HomeUiEffect
    data class ShowDialog(val dialog: HomeDialogState) : HomeUiEffect
    data object HideDialog : HomeUiEffect
}
sealed interface HomeNavigation {
    data object Back : HomeNavigation
    data class DeepLink(val uri: String) : HomeNavigation
    data class Id(val actionId: Int) : HomeNavigation
}
data class HomeDialogState(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int? = null,
    val buttons: List<HomeDialogButton> = listOf(HomeDialogButton.Cancel, HomeDialogButton.Accept)
)
sealed interface HomeDialogButton {
    data object Cancel : HomeDialogButton
    data object Accept : HomeDialogButton
}
// // # Block 174-3: import androidx.annotation.StringRes
// // import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
data class HomeButtonsState(
    val zoning: HomeButtonUiState = HomeButtonUiState(),
    val pending: HomeButtonUiState = HomeButtonUiState(),
    val location: HomeButtonUiState = HomeButtonUiState(),
    val receipts: HomeButtonUiState = HomeButtonUiState(),
    val messages: HomeButtonUiState = HomeButtonUiState(),
    val central: HomeButtonUiState = HomeButtonUiState(),
    val dashboard: HomeButtonUiState = HomeButtonUiState(),
    val fixedPrice: HomeButtonUiState = HomeButtonUiState(),
    val roofLight: HomeButtonUiState = HomeButtonUiState(),
    val locateStand: HomeButtonUiState = HomeButtonUiState()
)
data class HomeButtonUiState(
    val visible: Boolean = true,
    val enabled: Boolean = false,
    val loading: Boolean = false,
    @StringRes val textRes: Int = 0,
    val background: HomeButtonBackground = HomeButtonBackground.Red,
    val style: HomeButtonStyle = HomeButtonStyle.Disabled
)
enum class HomeButtonBackground(val color: Long) {
    Red(0xFFE53935),
    Green(0xFF43A047),
    Blue(0xFF1E88E5),
    Orange(0xFFF57C00),
    Gray(0xFF9E9E9E),
    Transparent(0x00000000)
}
enum class HomeButtonStyle {
    Disabled,
    Enabled,
    Loading,
    Empty
}
fun HomeButtonUiState.toComposeColors(): Pair<Color, Color> {
    val bg = when (background) {
        HomeButtonBackground.Red -> Color(0xFFE53935)
        HomeButtonBackground.Green -> Color(0xFF43A047)
        HomeButtonBackground.Blue -> Color(0xFF1E88E5)
        HomeButtonBackground.Orange -> Color(0xFFF57C00)
        HomeButtonBackground.Gray -> Color(0xFF9E9E9E)
        HomeButtonBackground.Transparent -> Color.Transparent
    }
    val content = if (background == HomeButtonBackground.Orange) Color.White else Color.White
    return bg to content
}
object HomeButtonsStateFactory {
    fun defaultState() = HomeButtonsState(
        zoning = HomeButtonUiState(
            visible = true, enabled = false, textRes = R.string.btn_zoning,
            background = HomeButtonBackground.Blue, style = HomeButtonStyle.Disabled
        ),
        pending = HomeButtonUiState(
            visible = true, enabled = false, textRes = R.string.btn_pending,
            background = HomeButtonBackground.Blue, style = HomeButtonStyle.Disabled
        ),
        location = HomeButtonUiState(
            visible = true, enabled = false, textRes = R.string.btn_location_on,
            background = HomeButtonBackground.Red, style = HomeButtonStyle.Disabled
        ),
        receipts = HomeButtonUiState(
            visible = true, enabled = true, textRes = R.string.btn_receipts,
            background = HomeButtonBackground.Blue, style = HomeButtonStyle.Enabled
        ),
        messages = HomeButtonUiState(
            visible = true, enabled = false, textRes = R.string.btn_messages,
            background = HomeButtonBackground.Blue, style = HomeButtonStyle.Disabled
        ),
        central = HomeButtonUiState(
            visible = true, enabled = true, textRes = R.string.btn_central,
            background = HomeButtonBackground.Blue, style = HomeButtonStyle.Enabled
        ),
        dashboard = HomeButtonUiState(
            visible = false, enabled = true, textRes = R.string.btn_dashboard,
            background = HomeButtonBackground.Blue, style = HomeButtonStyle.Enabled
        ),
        fixedPrice = HomeButtonUiState(
            visible = false, enabled = true, textRes = R.string.btn_fixed_price,
            background = HomeButtonBackground.Blue, style = HomeButtonStyle.Enabled
        ),
        roofLight = HomeButtonUiState(
            visible = true, enabled = false, textRes = R.string.btn_roof_light,
            background = HomeButtonBackground.Gray, style = HomeButtonStyle.Disabled
        ),
        locateStand = HomeButtonUiState(
            visible = true, enabled = false, textRes = R.string.btn_locate_stop,
            background = HomeButtonBackground.Red, style = HomeButtonStyle.Disabled
        )
    )
}
