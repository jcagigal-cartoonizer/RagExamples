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
data class MessageDetailUiState(
    val message: MessageEntity? = null,
    val hasPredefinedMessages: Boolean = false,
    val isAnswerVisible: Boolean = true,
    val isPrintVisible: Boolean = true,
    val isPredefinedVisible: Boolean = false,
    val isNewMessageVisible: Boolean = false,
    val answerEnabled: Boolean = true,
    val acceptTimerState: ButtonTimerState? = null,
    val dialogState: MessageDetailDialogState? = null,
)
data class ButtonTimerState(
    val maxSeconds: Long,
    val remainingSeconds: Long,
    val isActive: Boolean,
    val startTime: Long,
)
data class MessageDetailButtonsState(
    val answer: MessageDetailButtonStyle = MessageDetailButtonStyle.raisedEnabled(),
    val print: MessageDetailButtonStyle = MessageDetailButtonStyle.raisedEnabled(),
    val delete: MessageDetailButtonStyle = MessageDetailButtonStyle.raisedEnabled(),
    val accept: MessageDetailButtonStyle = MessageDetailButtonStyle.raisedEnabled(),
    val predefined: MessageDetailButtonStyle = MessageDetailButtonStyle.raisedDisabled(),
    val newMessage: MessageDetailButtonStyle = MessageDetailButtonStyle.raisedEnabled(),
    val answerVisible: Boolean = true,
    val printVisible: Boolean = true,
    val deleteVisible: Boolean = true,
    val acceptVisible: Boolean = true,
    val predefinedVisible: Boolean = false,
    val newMessageVisible: Boolean = false,
)
data class MessageDetailButtonStyle(
    val enabled: Boolean,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
    val borderColor: androidx.compose.ui.graphics.Color? = null,
)
object MessageDetailButtonColors {
    val enabledContainer = androidx.compose.ui.graphics.Color(0xFF1E88E5)
    val disabledContainer = androidx.compose.ui.graphics.Color(0xFFBDBDBD)
    val enabledContent = androidx.compose.ui.graphics.Color.White
    val disabledContent = androidx.compose.ui.graphics.Color.White
}
data class MessageDetailDialogState(
    val title: String,
    val message: String? = null,
    val description: String? = null,
    val hint: String? = null,
    val messageOptions: List<String>? = null,
    val buttons: List<MessageDetailComposeFragmentDialogButtonType>,
)
enum class MessageDetailComposeFragmentDialogButtonType {
    CANCEL, ACCEPT, SEND
}
sealed interface MessageDetailUiEffect {
    data object NavigateBack : MessageDetailUiEffect
    data object OpenPredefinedMessages : MessageDetailUiEffect
    data class ShowDeleteConfirmDialog(val dialog: MessageDetailDialogState) : MessageDetailUiEffect
    data class ShowWriteCustomMessageDialog(val dialog: MessageDetailDialogState) : MessageDetailUiEffect
    data class SendMessage(val response: String, val dispatchNumber: String?) : MessageDetailUiEffect
    data class PrintMessage(val messageId: Int) : MessageDetailUiEffect
    data class DeleteMessage(val messageId: Int) : MessageDetailUiEffect
    data object StopAutoClose : MessageDetailUiEffect
}
