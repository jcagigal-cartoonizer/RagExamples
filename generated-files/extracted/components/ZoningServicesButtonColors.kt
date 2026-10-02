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
// # Block 389-4: import androidx.compose.foundation.BorderStroke
private val EnabledBg = Color(0xFF1E88E5)
private val DisabledBg = Color(0xFFE0E0E0)
private val EnabledText = Color.White
private val DisabledText = Color(0xFF9E9E9E)
private val Stroke = Color(0xFFBDBDBD)
@Composable
fun zoningServicesButtonColors(enabled: Boolean): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = if (enabled) EnabledBg else DisabledBg,
        contentColor = if (enabled) EnabledText else DisabledText,
        disabledContainerColor = DisabledBg,
        disabledContentColor = DisabledText
    )
}
@Composable
fun zoningServicesOutlinedColors(enabled: Boolean): ButtonColors {
    return ButtonDefaults.outlinedButtonColors(
        contentColor = if (enabled) EnabledBg else DisabledText,
        disabledContentColor = DisabledText
    )
}
fun zoningServicesBorder(enabled: Boolean): BorderStroke {
    return BorderStroke(1.dp, if (enabled) Stroke else DisabledBg)
}
fun zoningServicesShape() = RoundedCornerShape(8.dp)
fun zoningServicesPadding() = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
