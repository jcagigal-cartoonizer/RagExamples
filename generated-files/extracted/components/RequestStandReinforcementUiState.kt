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
// # Block 246-3: import androidx.compose.runtime.Immutable
@Immutable
data class RequestStandReinforcementUiState(
    val idMacrozone: Int = 0,
    val idZone: Int = 0,
    val isInFavourites: Boolean = false,
    val buttons: RequestStandReinforcementButtonsState = RequestStandReinforcementButtonsState(),
    val dialog: RequestStandReinforcementCustomDialogCustomDialogState = RequestStandReinforcementCustomDialogCustomDialogState()
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
