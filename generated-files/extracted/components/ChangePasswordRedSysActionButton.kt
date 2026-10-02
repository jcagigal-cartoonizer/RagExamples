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
// # Block 169-3: import androidx.compose.foundation.background
private val ButtonShape = RoundedCornerShape(8.dp)
@Composable
fun ChangePasswordRedSysActionButton(
    state: ActionButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.visible) return
    val colors = when (state.buttonStyle) {
        CustomComposeButtonStyle.ENABLED -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E88E5),
            contentColor = Color.White
        )
        CustomComposeButtonStyle.DISABLED -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE0E0E0),
            contentColor = Color(0xFF9E9E9E)
        )
        CustomComposeButtonStyle.LOADING -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E88E5),
            contentColor = Color.White
        )
    }
    Button(
        onClick = onClick,
        enabled = state.enabled && state.buttonStyle != CustomComposeButtonStyle.LOADING,
        colors = colors,
        shape = ButtonShape,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
    ) {
        if (state.buttonStyle == CustomComposeButtonStyle.LOADING) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(text = state.text)
        }
    }
}
