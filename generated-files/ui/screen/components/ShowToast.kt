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
// # Block 6-1: import androidx.compose.runtime.Immutable
@Immutable
data class ContactCentralUiState(
    val hasPredefinedMessages: Boolean = false,
    val hasShortBreak: Boolean = false,
    val isITopTaximeter: Boolean = false,
    val shortBreakStatus: ShortBreakStatus? = null,
    val voiceValue: Boolean = false,
    val zone: String? = null,
    val currentShiftStatus: Int? = null,
    val buttons: ContactCentralButtonsState = ContactCentralButtonsState(),
    val dialog: ContactCentralDialogState = ContactCentralDialogState(),
)
@Immutable
data class ContactCentralDialogState(
    val isVisible: Boolean = false,
    val title: String = "",
)
sealed interface ContactCentralUiEvent {
    data object CancelClicked : ContactCentralUiEvent
    data object ShortBreakClicked : ContactCentralUiEvent
    data object VoiceCallClicked : ContactCentralUiEvent
    data object MessagesClicked : ContactCentralUiEvent
    data object InformationClicked : ContactCentralUiEvent
    data object DialogAccepted : ContactCentralUiEvent
    data object DialogCancelled : ContactCentralUiEvent
    data object ScreenStarted : ContactCentralUiEvent
}
sealed interface ContactCentralUiEffect {
    data object NavigateBack : ContactCentralUiEffect
    data object NavigateToPredefinedMessages : ContactCentralUiEffect
    data object NavigateToInformationMessages : ContactCentralUiEffect
    data object ShowVoiceRequestDialog : ContactCentralUiEffect
    data object HideVoiceRequestDialog : ContactCentralUiEffect
    data class ShowToast(val messageRes: Int) : ContactCentralUiEffect
    data object StartShortBreakLoading : ContactCentralUiEffect
    data object RequestShortBreakAction : ContactCentralUiEffect
    data class SendVoiceRequest(val value: Boolean) : ContactCentralUiEffect
}
