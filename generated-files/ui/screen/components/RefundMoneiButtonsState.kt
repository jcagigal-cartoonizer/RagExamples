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
// # Block 196-3: import androidx.compose.foundation.background
data class RefundMoneiButtonsState(
    val showCancel: Boolean = true,
    val showRefund: Boolean = true,
    val cancelEnabled: Boolean = true,
    val refundEnabled: Boolean = true,
    val cancelText: String = "Cancel",
    val refundText: String = "Refund",
)
object RefundMoneiButtonStyle {
    val shape = RoundedCornerShape(10.dp)
    val primary = Color(0xFF1E88E5)
    val secondary = Color(0xFFE53935)
    val textOnPrimary = Color.White
    val textOnSecondary = Color.White
    val disabled = Color(0xFFBDBDBD)
    val contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
}
@Composable
fun RefundMoneiButton(
    text: String,
    enabled: Boolean,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = if (enabled) textColor else Color.White,
        fontSize = 16.sp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(if (enabled) background else RefundMoneiButtonStyle.disabled, RefundMoneiButtonStyle.shape)
            .border(1.dp, background, RefundMoneiButtonStyle.shape)
            .clickable(enabled = enabled, onClick = onClick)
            .then(Modifier)
            .paddingCompat()
    )
}
fun Modifier.paddingCompat(): Modifier = this
> If your XML custom button had a stronger “button” look, you can swap the `Text` with a `Box`/`Surface`; I kept it minimal and Compose-native.
