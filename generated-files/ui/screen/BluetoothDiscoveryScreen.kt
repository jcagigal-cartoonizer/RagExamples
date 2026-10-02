package ifac.td.taxi.ui.screen
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
// # Block 477-4: import android.Manifest
@Composable
fun BluetoothDiscoveryScreen(
    navBack: () -> Unit,
    onRequestBluetoothPermission: (String) -> Unit,
    onRequestLocationPermission: (String) -> Unit,
    onStartActivity: (Intent) -> Unit,
    onConnectTaximeter: () -> Unit,
    viewModel: BluetoothDiscoveryComposeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<BluetoothDiscoveryDialogState?>(null) }
    val buttonState = remember(uiState) {
        BluetoothDiscoveryButtonsState.from(
            shiftDisconnected = uiState.shiftDisconnected,
            discovering = uiState.isDiscovering,
            externalGpsVisible = uiState.showExternalGpsCheckbox,
            externalGpsChecked = uiState.externalGpsChecked
        )
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is BluetoothDiscoveryUiEffect.ShowDialog -> dialogState = effect.dialog
                is BluetoothDiscoveryUiEffect.ShowToast -> { /* host toast/snackbar */ }
                is BluetoothDiscoveryUiEffect.RequestBluetoothPermission ->
                    onRequestBluetoothPermission(effect.permission)
                is BluetoothDiscoveryUiEffect.RequestLocationPermission ->
                    onRequestLocationPermission(effect.permission)
                is BluetoothDiscoveryUiEffect.OpenBluetoothSettings ->
                    onStartActivity(Intent(effect.intentAction))
                is BluetoothDiscoveryUiEffect.OpenLocationSettings ->
                    onStartActivity(Intent(effect.intentAction))
                BluetoothDiscoveryUiEffect.NavigateBack -> navBack()
                BluetoothDiscoveryUiEffect.ConnectTaximeter -> onConnectTaximeter()
            }
        }
    }
    BackHandler {
        viewModel.onCancelClicked()
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = uiState.titleText,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        Image(
            painter = painterResource(id = uiState.titleImageRes.takeIf { it != 0 } ?: R.drawable.background_dialog_transparent),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        if (buttonState.externalGpsVisible) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = buttonState.externalGpsChecked,
                    onCheckedChange = { viewModel.onExternalGpsCheckedChanged(it) }
                )
                Text(text = "GPS externo")
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(uiState.discoveredDevices, key = { it.macAddress }) { device ->
                BluetoothDeviceRow(
                    device = device,
                    selected = uiState.selectedDevice?.macAddress == device.macAddress,
                    onSelect = { viewModel.onDeviceSelected(device) }
                )
            }
        }
        BluetoothDiscoveryButtons(
            state = buttonState,
            onDiscover = { viewModel.onDiscoverClicked() },
            onAccept = { viewModel.onAcceptClicked(uiState.selectedDevice ?: uiState.savedDevice) },
            onCancel = { viewModel.onCancelClicked() },
        )
    }
    dialogState?.let { state ->
        BluetoothDiscoveryBluetoothDeviceRowBluetoothDiscoveryCustomDialog(
            state = state,
            onDismiss = { dialogState = null },
            onAccept = {
                dialogState = null
                viewModel.onDialogAccepted(state.title)
            },
            onCancel = {
                dialogState = null
            }
        )
    }
}
