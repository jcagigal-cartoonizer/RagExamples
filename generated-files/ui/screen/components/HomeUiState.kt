package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.HomeUiState
import ifac.td.taxi.ui.screen.components.HomeDialogState
import ifac.td.taxi.ui.screen.components.HomeNavigation
import ifac.td.taxi.ui.screen.components.HomeButtonsState = HomeButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 125-2: import androidx.annotation.StringRes
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
