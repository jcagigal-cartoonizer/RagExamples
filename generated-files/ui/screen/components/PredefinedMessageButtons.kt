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
// # Block 383-5: import androidx.compose.foundation.BorderStroke
@Composable
fun PredefinedMessageButtons(
    state: PredefinedMessageButtonsState,
    onNewMessageClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.newMessage.visible) {
            ComposeStyledButton(
                text = "New message",
                state = state.newMessage,
                modifier = Modifier.weight(1f),
                onClick = onNewMessageClick
            )
        }
        if (state.cancel.visible) {
            ComposeStyledButton(
                text = "Cancel",
                state = state.cancel,
                modifier = Modifier.weight(1f),
                onClick = onCancelClick
            )
        }
    }
}
@Composable
fun ComposeStyledButton(
    text: String,
    state: PredefinedMessageComposeDialogButtonType{ButtonVisualState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = buttonColorsFor(state.type, state.enabled)
    val shape = RoundedCornerShape(14.dp)
    when (state.type) {
        PredefinedMessageButtonType.PRIMARY -> {
            Button(
                onClick = onClick,
                enabled = state.enabled,
                modifier = modifier,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.container,
                    contentColor = colors.content,
                    disabledContainerColor = colors.disabledContainer,
                    disabledContentColor = colors.disabledContent
                )
            ) {
                Text(text = text)
            }
        }
        PredefinedMessageButtonType.SECONDARY,
        PredefinedMessageButtonType.GHOST -> {
            OutlinedButton(
                onClick = onClick,
                enabled = state.enabled,
                modifier = modifier,
                shape = shape,
                border = BorderStroke(1.dp, colors.container),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.container,
                    disabledContentColor = colors.disabledContent
                )
            ) {
                Text(text = text)
            }
        }
        PredefinedMessageButtonType.DANGER -> {
            Button(
                onClick = onClick,
                enabled = state.enabled,
                modifier = modifier,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.container,
                    contentColor = colors.content,
                    disabledContainerColor = colors.disabledContainer,
                    disabledContentColor = colors.disabledContent
                )
            ) {
                Text(text = text)
            }
        }
    }
}
data class StyledColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color
)
fun buttonColorsFor(type: PredefinedMessageButtonType, enabled: Boolean): StyledColors {
    return when (type) {
        PredefinedMessageButtonType.PRIMARY -> StyledColors(
            container = Color(0xFF1565C0),
            content = Color.White,
            disabledContainer = Color(0xFF90A4AE),
            disabledContent = Color(0xFFEEEEEE)
        )
        PredefinedMessageButtonType.SECONDARY -> StyledColors(
            container = Color(0xFF455A64),
            content = Color.White,
            disabledContainer = Color(0xFFB0BEC5),
            disabledContent = Color(0xFFEEEEEE)
        )
        PredefinedMessageButtonType.DANGER -> StyledColors(
            container = Color(0xFFD32F2F),
            content = Color.White,
            disabledContainer = Color(0xFFEF9A9A),
            disabledContent = Color(0xFFFAFAFA)
        )
        PredefinedMessageButtonType.GHOST -> StyledColors(
            container = Color(0xFF1565C0),
            content = Color(0xFF1565C0),
            disabledContainer = Color.Transparent,
            disabledContent = Color(0xFF90A4AE)
        )
    }
}
