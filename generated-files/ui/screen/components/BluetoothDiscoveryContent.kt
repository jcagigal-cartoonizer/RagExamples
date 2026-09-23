package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 540-4: import androidx.compose.foundation.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
@Composable
fun BluetoothDiscoveryContent(
    state: BluetoothDiscoveryUiState,
    onAction: (BluetoothDiscoveryUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .animateContentSize()
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        BluetoothHeader(
            title = state.title,
            imageRes = state.taximeterImageRes
        )
        Spacer(modifier = Modifier.height(16.dp))
        ExternalGpsSection(
            visible = state.showExternalGpsCheckbox,
            checked = state.externalGpsEnabled,
            onCheckedChange = { onAction(BluetoothDiscoveryUiEvent.ExternalGpsChanged(it)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        BluetoothDiscoveryButtons(
            state = state.buttons,
            onDiscover = { onAction(BluetoothDiscoveryUiEvent.DiscoverClicked) },
            onAccept = { onAction(BluetoothDiscoveryUiEvent.AcceptClicked) },
            onCancel = { onAction(BluetoothDiscoveryUiEvent.CancelClicked) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        DevicesList(
            devices = state.devices,
            selectedDevice = state.selectedDevice,
            savedDevice = state.savedDevice,
            onSelect = { onAction(BluetoothDiscoveryUiEvent.DeviceSelected(it)) },
            onDeselect = { onAction(BluetoothDiscoveryUiEvent.DeviceDeselected(it)) }
        )
    }
}
@Composable
fun BluetoothHeader(title: String, imageRes: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(140.dp)
        )
    }
}
@Composable
fun ExternalGpsSection(
    visible: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    if (visible) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(text = stringResource(id = R.string.strGPSExterno))
        }
    }
}
@Composable
fun DevicesList(
    devices: List<BluetoothInfo>,
    selectedDevice: BluetoothInfo?,
    savedDevice: BluetoothInfo?,
    onSelect: (BluetoothInfo) -> Unit,
    onDeselect: (BluetoothInfo) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(devices, key = { it.macAddress }) { device ->
            val isSelected = selectedDevice?.macAddress == device.macAddress
            val isSaved = savedDevice?.macAddress == device.macAddress
            BluetoothDeviceRow(
                device = device,
                isSelected = isSelected,
                isSaved = isSaved,
                onClick = {
                    if (isSelected) onDeselect(device) else onSelect(device)
                }
            )
        }
    }
}
@Composable
fun BluetoothDeviceRow(
    device: BluetoothInfo,
    isSelected: Boolean,
    isSaved: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        isSelected -> Color(0xFFE3F2FD)
        isSaved -> Color(0xFFF1F8E9)
        else -> Color.White
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(background, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = device.name ?: "Bluetooth")
            Text(text = device.macAddress ?: "", style = MaterialTheme.typography.bodySmall)
        }
    }
}
