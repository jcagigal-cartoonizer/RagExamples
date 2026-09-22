package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.rememberUpdatedState
import androidx.navigation.NavController
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
@Composable
fun BluetoothDiscoveryScreen(
    viewModel: BluetoothDiscoveryComposeViewModel,
    navController: NavController,
    onShowHeader: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
    onConnectTaximeter: () -> Unit,
    onRequestPermission: (String) -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnShowToast by rememberUpdatedState(onShowToast)
    val currentOnConnectTaximeter by rememberUpdatedState(onConnectTaximeter)
    val currentOnRequestPermission by rememberUpdatedState(onRequestPermission)
    var dialogState by remember { mutableStateOf<BluetoothDiscoveryDialogState?>(null) }
    val btPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onBluetoothPermissionResult(granted)
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onLocationPermissionResult(granted)
    }
    LaunchedEffect(Unit) {
        onShowHeader(false)
        viewModel.onScreenStarted()
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BluetoothDiscoveryUiEffect.ShowDialog -> dialogState = effect.dialogState
                is BluetoothDiscoveryUiEffect.HideDialog -> dialogState = null
                is BluetoothDiscoveryUiEffect.RequestBluetoothPermission -> {
                    btPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                }
                is BluetoothDiscoveryUiEffect.RequestLocationPermission -> {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                is BluetoothDiscoveryUiEffect.OpenBluetoothSettings -> {
                    context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
                is BluetoothDiscoveryUiEffect.OpenLocationSettings -> {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                is BluetoothDiscoveryUiEffect.NavigateBack -> {
                    navController.popBackStack()
                }
                is BluetoothDiscoveryUiEffect.ConnectTaximeterAndBack -> {
                    currentOnConnectTaximeter()
                    navController.popBackStack()
                }
                is BluetoothDiscoveryUiEffect.ShowToast -> {
                    currentOnShowToast(effect.messageRes)
                }
                is BluetoothDiscoveryUiEffect.OpenPermissionSettings -> {
                    currentOnRequestPermission(effect.permission)
                }
            }
        }
    }
    if (dialogState != null) {
        BluetoothDiscoveryCustomDialog(
            state = dialogState!!,
            onDismiss = { viewModel.onDialogAction(BluetoothDiscoveryDialogAction.Dismiss) },
            onAction = { action -> viewModel.onDialogAction(action) }
        )
    }
    BluetoothDiscoveryContent(
        state = uiState,
        onAction = viewModel::onUiEvent
    )
}
