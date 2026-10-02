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
// # Block 473-3: import androidx.compose.foundation.BorderStroke
@Composable
fun AddAmountButtons(
    state: AddAmountButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.acceptButton.visible) {
            ComposeCustomButton(
                state = state.acceptButton,
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (state.cancelButton.visible) {
            ComposeCustomButton(
                state = state.cancelButton,
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@Composable
fun ComposeCustomButton(
    state: AddAmountComposeButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = rememberButtonColors(state.type)
    val shape = MaterialTheme.shapes.medium
    Button(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.height(52.dp),
        shape = shape,
        colors = colors,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        border = rememberButtonBorder(state.type)
    ) {
        Text(state.text)
    }
}
@Composable
fun rememberButtonColors(type: ButtonType): ButtonColors {
    return when (type) {
        ButtonType.ACCEPT -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        ButtonType.CANCEL -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        else -> ButtonDefaults.buttonColors()
    }
}
@Composable
fun rememberButtonBorder(type: ButtonType): BorderStroke? {
    return when (type) {
        ButtonType.CANCEL -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else -> null
    }
}
