package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.SettingsComposeViewModel
import ifac.td.taxi.ui.screen.components.SettingsUiEvent
import ifac.td.taxi.ui.screen.components.SettingsScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 7-1: import android.Manifest
@Composable
fun SettingsRoute(
    navController: NavController,
    viewModel: SettingsComposeViewModel,
    showHeader: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        showHeader(true)
        viewModel.onScreenShown()
    }
    LaunchedEffect(viewModel.uiEffect) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    SettingsUiEffect.NavigateToUserLogin -> {
                        navController.navigate(R.id.action_settingsFragment_to_userLoginFragment)
                    }
                    SettingsUiEffect.NavigateToDeviceSettings -> {
                        context.startActivity(
                            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                    SettingsUiEffect.NavigateToDiscoveryChannel -> {
                        navController.navigate(R.id.action_settingsFragment_to_discoveryChannelFragment)
                    }
                    SettingsUiEffect.NavigateToAbout -> {
                        navController.navigate(R.id.action_settingsFragment_to_aboutFragment)
                    }
                    SettingsUiEffect.NavigateToGPS -> {
                        navController.navigate(R.id.action_settingsFragment_to_GPSConfigFragment)
                    }
                    SettingsUiEffect.NavigateToLights -> {
                        navController.navigate(R.id.action_settingsFragment_to_lightsTestFragment)
                    }
                    SettingsUiEffect.NavigateToRequirements -> {
                        navController.navigate(R.id.action_settingsFragment_to_toolsFragment)
                    }
                    SettingsUiEffect.NavigateToUserConfiguration -> {
                        navController.navigate(R.id.action_settingsFragment_to_userConfigurationFragment)
                    }
                    SettingsUiEffect.NavigateToWebView(valUrl = effect.url) -> {
                        navController.navigate(
                            R.id.action_settingsFragment_to_webViewFragment
                        )
                    }
                    SettingsUiEffect.RequestBluetoothPermission -> {
                        // call your permission launcher from UI
                    }
                    SettingsUiEffect.ShowToastIncorrectPin -> {
                        // show toast/snackbar in UI layer
                    }
                }
            }
        }
    }
    SettingsScreen(
        state = uiState,
        onEvent = viewModel::onEvent,
    )
}
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    BackHandler(enabled = false)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsButton(
            text = stringResource(R.string.configuration),
            style = state.buttons.configuration.style,
            visible = state.buttons.configuration.visible,
            onClick = { onEvent(SettingsUiEvent.ConfigurationClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.device_settings),
            style = state.buttons.deviceSettings.style,
            visible = state.buttons.deviceSettings.visible,
            onClick = { onEvent(SettingsUiEvent.DeviceSettingsClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.gps),
            style = state.buttons.gps.style,
            visible = state.buttons.gps.visible,
            onClick = { onEvent(SettingsUiEvent.GpsClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.about),
            style = state.buttons.about.style,
            visible = state.buttons.about.visible,
            onClick = { onEvent(SettingsUiEvent.AboutClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.light),
            style = state.buttons.light.style,
            visible = state.buttons.light.visible,
            onClick = { onEvent(SettingsUiEvent.LightClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.requirements),
            style = state.buttons.requirements.style,
            visible = state.buttons.requirements.visible,
            onClick = { onEvent(SettingsUiEvent.RequirementsClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.preferencias),
            style = state.buttons.preferencias.style,
            visible = state.buttons.preferencias.visible,
            onClick = { onEvent(SettingsUiEvent.PreferenciasClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.bluetooth),
            style = state.buttons.bluetooth.style,
            visible = state.buttons.bluetooth.visible,
            onClick = { onEvent(SettingsUiEvent.BluetoothClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.webview),
            style = state.buttons.webView.style,
            visible = state.buttons.webView.visible,
            onClick = { onEvent(SettingsUiEvent.WebViewClicked) }
        )
    }
    if (state.dialog != null) {
        SettingsCustomDialog(
            dialog = state.dialog,
            onDismiss = { onEvent(SettingsUiEvent.DialogDismissed) },
            onEvent = onEvent
        )
    }
}
The UI state contains a full `SettingsButtonsState`, dialog state, and permission-related flags.
