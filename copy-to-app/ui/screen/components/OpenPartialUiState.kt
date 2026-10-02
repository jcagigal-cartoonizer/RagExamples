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
// # Block 5-1: import androidx.annotation.StringRes
data class OpenPartialUiState(
    val isLoading: Boolean = true,
    val rawPartialContent: String? = null,
    val ticketContent: String = "",
    val buttons: OpenPartialButtonsState = OpenPartialButtonsState(),
    val dialog: OpenPartialDialogState? = null,
)
data class OpenPartialButtonsState(
    val back: ButtonUiState = ButtonUiState.visibleEnabled(),
    val print: ButtonUiState = ButtonUiState.visibleEnabled(),
    val close: ButtonUiState = ButtonUiState.gone(),
    val totalizers: ButtonUiState = ButtonUiState.gone(),
)
data class ButtonUiState(
    val visible: Boolean,
    val enabled: Boolean,
    val style: ButtonStyle,
) {
    companion object {
        fun visibleEnabled(style: ButtonStyle = ButtonStyle.ENABLE) =
            ButtonUiState(visible = true, enabled = true, style = style)
        fun visibleDisabled(style: ButtonStyle = ButtonStyle.DISABLE) =
            ButtonUiState(visible = true, enabled = false, style = style)
        fun gone() =
            ButtonUiState(visible = false, enabled = false, style = ButtonStyle.DISABLE)
    }
}
enum class ButtonStyle {
    ENABLE, DISABLE
}
data class OpenPartialDialogState(
    @StringRes val titleRes: Int = R.string.alert,
    @StringRes val messageRes: Int,
    val buttons: List<OpenPartialCustomDialogDialogButtonType> = listOf(OpenPartialCustomDialogDialogButtonType.CANCEL, OpenPartialCustomDialogDialogButtonType.ACCEPT)
)
enum class OpenPartialCustomDialogDialogButtonType {
    CANCEL, ACCEPT
}
sealed interface OpenPartialUiEvent {
    data object OnBackClicked : OpenPartialUiEvent
    data object OnPrintClicked : OpenPartialUiEvent
    data object OnCloseClicked : OpenPartialUiEvent
    data object OnTotalizersClicked : OpenPartialUiEvent
    data object OnDialogDismissed : OpenPartialUiEvent
    data object OnDialogAccepted : OpenPartialUiEvent
}
sealed interface OpenPartialUiEffect {
    data object NavigateBack : OpenPartialUiEffect
    data object NavigateToTotalizers : OpenPartialUiEffect
    data object ShowToastNoPartialsToPrint : OpenPartialUiEffect
    data object RequestClosePartialsConfirmation : OpenPartialUiEffect
    data object ClosePartialsAndNavigate : OpenPartialUiEffect
}
