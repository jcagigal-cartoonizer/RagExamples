package ifac.td.taxi.ui.screen
import ifac.td.taxi.compose.viewmodel.BluetoothDiscoveryComposeViewModel
import ifac.td.taxi.ui.screen.components.BluetoothDiscoveryScreen
import ifac.td.taxi.ui.screen.components.BluetoothDiscoveryUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 5-1: import android.Manifest
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
