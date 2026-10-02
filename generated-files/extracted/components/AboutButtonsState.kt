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
// # Block 408-6: import androidx.compose.runtime.Immutable
@Immutable
data class AboutButtonsState(
    val accept: ButtonUiState = ButtonUiState(
        text = "Accept",
        backgroundColor = 0xFF2196F3,
        contentColor = 0xFFFFFFFF,
        visible = true,
        enabled = true
    ),
    val logo: ButtonUiState = ButtonUiState(
        text = "Logo",
        backgroundColor = 0x00000000,
        contentColor = 0xFF757575,
        visible = true,
        enabled = true
    ),
    val privacy: ButtonUiState = ButtonUiState(
        text = "Privacy Policy",
        backgroundColor = 0x00000000,
        contentColor = 0xFF1E88E5,
        visible = true,
        enabled = true
    ),
    val bluetoothInfoVisible: Boolean = false
)
enum class AboutButtonStyle {
    FILLED,
    OUTLINED,
    TEXT
}
@Composable
fun AboutStyledButton(
    text: String,
    style: AboutButtonStyle,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (style) {
        AboutButtonStyle.FILLED -> Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.fillMaxWidth().height(48.dp)
        ) { Text(text) }
        AboutButtonStyle.OUTLINED -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.fillMaxWidth().height(48.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) { Text(text) }
        AboutButtonStyle.TEXT -> TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.fillMaxWidth().height(48.dp)
        ) { Text(text, color = Color(0xFF1E88E5)) }
    }
}
To preserve the original fragment behavior:
@Composable
fun AboutRouteHost(
    viewModel: AboutComposeViewModel,
    onBack: () -> Unit,
    showToast: (String) -> Unit
) {
    AboutRoute(
        viewModel = viewModel,
        onNavigateBack = onBack,
        onShowToast = showToast
    )
}
1. a **fully compilable version with imports organized per file**, or  
2. a **Material3-based implementation that more closely matches your XML spacing/colors**.
