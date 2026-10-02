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
// # Block 254-6: import androidx.compose.foundation.background
@Composable
fun SubscriberPaymentButtons(
    buttonsState: SubscriberPaymentButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    onQrClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (buttonsState.showQrButton) {
            ComposeCustomButton(
                text = "Scan QR",
                state = SubscriberPaymentComposeButtonState(
                    visible = true,
                    enabled = true,
                    backgroundColor = Color(0xFF2196F3)
                ),
                onClick = onQrClick
            )
        }
        ComposeCustomButton(
            text = if (buttonsState.accept.loading) "Loading..." else "Accept",
            state = buttonsState.accept,
            onClick = onAccept
        )
        ComposeCustomButton(
            text = "Cancel",
            state = buttonsState.cancel.copy(backgroundColor = Color(0xFFE53935)),
            onClick = onCancel
        )
    }
}
@Composable
fun ComposeCustomButton(
    text: String,
    state: SubscriberPaymentComposeButtonState,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .alpha(if (state.enabled) 1f else 0.6f)
            .background(state.backgroundColor, shape)
            .then(
                if (state.borderColor != null) {
                    Modifier.border(1.dp, state.borderColor, shape)
                } else Modifier
            )
            .clickable(enabled = state.enabled && !state.loading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (state.loading) {
            CircularProgressIndicator(
                color = state.textColor,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = text,
                color = state.textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
