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
// # Block 10-1: import androidx.compose.runtime.Immutable
@Immutable
data class LoginDriverUiState(
    val driverId: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val showNetworkDialog: Boolean = false,
    val showIncorrectCredentialsDialog: Boolean = false,
    val lastSessionDriverId: String = "",
    val buttons: LoginDriverButtonsState = LoginDriverButtonsState(),
    val canShowDriverContainer: Boolean = true,
    val startTurnMode: Int = 1,
    val connectionMode: Int = 0
)
@Immutable
data class LoginDriverButtonsState(
    val cancel: ButtonStyleState = ButtonStyleState.visibleEnabled(
        background = LoginButtonBackground.GRAY,
        textColor = LoginButtonColors.white()
    ),
    val conCentral: ButtonStyleState = ButtonStyleState.hidden(
        background = LoginButtonBackground.GREEN,
        textColor = LoginButtonColors.white()
    ),
    val sinCentral: ButtonStyleState = ButtonStyleState.hidden(
        background = LoginButtonBackground.BLUE,
        textColor = LoginButtonColors.white()
    ),
    val refuerzo: ButtonStyleState = ButtonStyleState.hidden(
        background = LoginButtonBackground.ORANGE,
        textColor = LoginButtonColors.white()
    ),
    val showFlowContainer: Boolean = true,
    val flowContainerRatio: String = "3:2"
)
@Immutable
data class ButtonStyleState(
    val visible: Boolean,
    val enabled: Boolean,
    val isLoading: Boolean,
    val background: LoginButtonBackground,
    val textColor: Color,
)
enum class LoginButtonBackground {
    GREEN, BLUE, ORANGE, GRAY
}
object LoginButtonColors {
    fun white() = Color.White
}
sealed interface LoginDriverUiEffect {
    data object NavigateBack : LoginDriverUiEffect
    data object NavigateToChangePin : LoginDriverUiEffect
    data object LoginWithoutCentralFinished : LoginDriverUiEffect
    data object ShowNetworkErrorToast : LoginDriverUiEffect
    data object ShowIncorrectLoginNoCredentialsToast : LoginDriverUiEffect
    data object ShowErrorLoginToast : LoginDriverUiEffect
    data class ShowIncorrectCredentials(val show: Boolean) : LoginDriverUiEffect
    data class SetLastSessionDriver(val driverId: String) : LoginDriverUiEffect
    data class SetButtonLoading(val button: LoginDriverButtonId) : LoginDriverUiEffect
    data class ResetButtons(val preserveVisibility: Boolean = true) : LoginDriverUiEffect
}
enum class LoginDriverButtonId {
    CON_CENTRAL, SIN_CENTRAL, REFORZO
}
