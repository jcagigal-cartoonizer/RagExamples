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
// # Block 600-5: import androidx.compose.foundation.BorderStroke
@Composable
fun BluetoothDiscoveryButtons(
    state: BluetoothDiscoveryButtonsState,
    onDiscover: () -> Unit,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        StyledBluetoothButton(
            text = "Discover",
            state = state.discoverState,
            modifier = Modifier.fillMaxWidth(),
            onClick = onDiscover
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StyledBluetoothButton(
                text = "Cancel",
                state = state.cancelState,
                modifier = Modifier.weight(1f),
                onClick = onCancel
            )
            Spacer(modifier = Modifier.width(12.dp))
            StyledBluetoothButton(
                text = "Accept",
                state = state.acceptState,
                modifier = Modifier.weight(1f),
                onClick = onAccept
            )
        }
    }
}
@Composable
fun StyledBluetoothButton(
    text: String,
    state: BluetoothDiscoveryComposeButtonState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val enabled = state == BluetoothDiscoveryComposeButtonState.ENABLE
    val colors = when (state) {
        BluetoothDiscoveryComposeButtonState.ENABLE -> ButtonDefaults.buttonColors()
        BluetoothDiscoveryComposeButtonState.DISABLE -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BluetoothDiscoveryComposeButtonState.LOADING -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.height(48.dp)
    ) {
        if (state == BluetoothDiscoveryComposeButtonState.LOADING) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}
