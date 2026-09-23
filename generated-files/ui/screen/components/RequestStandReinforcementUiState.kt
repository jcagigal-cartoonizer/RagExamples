package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementButtonsState = RequestStandReinforcementButtonsState
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementUiState
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementCustomDialogState = RequestStandReinforcementCustomDialogState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 246-3: import androidx.compose.runtime.Immutable
@Immutable
data class RequestStandReinforcementUiState(
    val idMacrozone: Int = 0,
    val idZone: Int = 0,
    val isInFavourites: Boolean = false,
    val buttons: RequestStandReinforcementButtonsState = RequestStandReinforcementButtonsState(),
    val dialog: RequestStandReinforcementCustomDialogState = RequestStandReinforcementCustomDialogState()
)
sealed interface RequestStandReinforcementUiEffect {
    data object NavigateBack : RequestStandReinforcementUiEffect
    data class ShowToast(val messageRes: Int) : RequestStandReinforcementUiEffect
    data object HideDialog : RequestStandReinforcementUiEffect
    data object ShowDialog : RequestStandReinforcementUiEffect
}
sealed interface RequestStandReinforcementAction {
    data object CancelClicked : RequestStandReinforcementAction
    data object AddFavouriteClicked : RequestStandReinforcementAction
    data object RemoveFavouriteClicked : RequestStandReinforcementAction
    data class ReinforcementClicked(val numCustomers: Int) : RequestStandReinforcementAction
    data object DialogConfirmed : RequestStandReinforcementAction
    data object DialogDismissed : RequestStandReinforcementAction
}
