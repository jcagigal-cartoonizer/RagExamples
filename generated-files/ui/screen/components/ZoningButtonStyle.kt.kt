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
// # Block 159-4: import androidx.annotation.DrawableRes
object ZoningButtonStyle {
    val Blue = Color(0xFF1E88E5)
    val Red = Color(0xFFE53935)
    val Orange = Color(0xFFFF9800)
    val Green = Color(0xFF43A047)
    val Gray = Color(0xFF9E9E9E)
    val White = Color.White
}
fun ComposeButtonColor.toColor(): Color = when (this) {
    ComposeButtonColor.Blue -> ZoningButtonStyle.Blue
    ComposeButtonColor.Red -> ZoningButtonStyle.Red
    ComposeButtonColor.Orange -> ZoningButtonStyle.Orange
    ComposeButtonColor.Green -> ZoningButtonStyle.Green
    ComposeButtonColor.Gray -> ZoningButtonStyle.Gray
}
@Composable
fun ZoningActionButton(
    state: ComposeActionButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state is ComposeActionButtonState.Hidden) return
    val enabled = state is ComposeActionButtonState.Enabled
    val loading = state is ComposeActionButtonState.Loading
    val background = state.color.toColor()
    Box(
        modifier = modifier
            .height(64.dp)
            .background(background, RoundedCornerShape(12.dp))
            .alpha(if (enabled) 1f else 0.62f),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = ZoningButtonStyle.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            } else {
                if (state.iconRes != 0) {
                    Icon(
                        painter = painterResource(state.iconRes),
                        contentDescription = null,
                        tint = ZoningButtonStyle.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
            if (state.textRes != 0) {
                Text(
                    text = androidx.compose.ui.res.stringResource(state.textRes),
                    color = ZoningButtonStyle.White
                )
            }
        }
    }
}
