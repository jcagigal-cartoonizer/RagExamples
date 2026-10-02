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
import  androidx.compose.ui.window.Dialog
// # Block 683-6: import androidx.compose.foundation.background
@Composable
fun BluetoothDiscoveryBluetoothDeviceRowBluetoothDiscoveryCustomDialog(
    state: BluetoothDiscoveryDialogState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
                .widthIn(min = 280.dp, max = 360.dp)
        ) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                if (state.buttons.contains(BluetoothDialogButton.Cancel)) {
                    OutlinedButton(
                        onClick = {
                            onCancel()
                            if (state.cancellable) onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                if (state.buttons.contains(BluetoothDialogButton.Accept)) {
                    Button(
                        onClick = {
                            onAccept()
                            if (state.cancellable) onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Accept") }
                }
            }
        }
    }
}
@Composable
fun BluetoothDeviceRow(
    device: BluetoothInfo,
    selected: Boolean,
    onSelect: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = device.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = device.macAddress, style = MaterialTheme.typography.bodyMedium)
            if (selected) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Selected", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
In the Fragment version, navigation and permission requests were tightly coupled to `iMainActivity`.  
In Compose, keep that behavior by hosting the screen in your existing fragment/activity and forwarding actions:
So the Compose layer stays UI-only, while the host preserves your existing navigation architecture.
Your old `bluetoothLocalUseCase.getNearbyDevices(_bluetoothDiscoveryFlow)` pushed data into a `StateFlow<BluetoothInfo?>`.  
In the Compose version, you can keep that behavior by either:
1. exposing the same flow from the use case and collecting it in the screen, or  
2. adapting `BluetoothDiscoveryComposeViewModel` so `getDevices()` starts discovery and the UI collects a flow from the VM exactly as before.
