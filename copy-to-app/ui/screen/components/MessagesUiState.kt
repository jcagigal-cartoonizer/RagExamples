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
// # Block 11-1: import ifac.td.taxi.repository.room.entities.message.MessageEntity
data class MessagesUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedMessageIds: Set<Long> = emptySet(),
    val showDeleteButton: Boolean = false,
    val showSelectButton: Boolean = true,
    val showMarkReadAction: Boolean = true,
    val dialogState: MessageDialogState? = null
)
data class MessageDialogState(
    val title: String,
    val description: String,
    val buttons: List<DialogButtonConfig>,
)
sealed class DialogButtonConfig {
    data object Cancel : DialogButtonConfig()
    data object Accept : DialogButtonConfig()
}
sealed interface MessagesUiEvent {
    data object Back : MessagesUiEvent
    data object DeleteSelected : MessagesUiEvent
    data object ToggleSelectionMode : MessagesUiEvent
    data object MarkAllReadRequest : MessagesUiEvent
    data object MarkAllReadConfirmed : MessagesUiEvent
    data object DismissDialog : MessagesUiEvent
    data class MessageClicked(val message: MessageEntity) : MessagesUiEvent
    data class ToggleMessageSelection(val message: MessageEntity) : MessagesUiEvent
}
sealed interface MessagesUiEffect {
    data object NavigateBack : MessagesUiEffect
    data object NavigateToMessageDetail : MessagesUiEffect
    data class NavigateToMessageDetailWithArgs(
        val messageId: Long,
        val skipAutoClose: Boolean = true
    ) : MessagesUiEffect
    data object ShowMarkAllReadDialog : MessagesUiEffect
}
