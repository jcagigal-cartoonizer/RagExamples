package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.WelcomeUiAction
import ifac.td.taxi.compose.viewmodel.WelcomeComposeViewModel
import ifac.td.taxi.ui.screen.components.WelcomeUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 6-1: import android.content.ActivityNotFoundException
@Composable
fun WelcomeRoute(
    navController: NavController,
    viewModel: WelcomeComposeViewModel,
    showToast: (Int) -> Unit = {},
    showHeader: (Boolean) -> Unit = {},
    exitApp: () -> Unit = {},
    onBackPressed: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<WelcomeDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WelcomeUiEffect.Navigate -> {
                    navController.navigate(effect.directions)
                }
                WelcomeUiEffect.NavigateBack -> onBackPressed()
                WelcomeUiEffect.ExitApp -> exitApp()
                is WelcomeUiEffect.Toast -> showToast(effect.messageRes)
                WelcomeUiEffect.RequestPermissions -> {
                    // Hook this to your permissions launcher in the Activity/host
                }
                WelcomeUiEffect.OpenBatteryOptimizationSettings -> {
                    openBatteryOptimizationSettings(context)
                }
                WelcomeUiEffect.OpenPrivacyPolicy -> {
                    openUri(context, effect.url)
                }
                is WelcomeUiEffect.ShowSecurePinDialog -> {
                    dialogState = WelcomeDialogState.SecurePin(effect.destination)
                }
                WelcomeUiEffect.ShowExitDialog -> {
                    dialogState = WelcomeDialogState.Exit
                }
                WelcomeUiEffect.ShowBatteryOptimizationDialog -> {
                    dialogState = WelcomeDialogState.BatteryOptimization
                }
                WelcomeUiEffect.ClearDialog -> {
                    dialogState = null
                }
            }
        }
    }
    LifecycleStartEffect(Unit) {
        showHeader(true)
        viewModel.onScreenStarted()
        onStopOrDispose { }
    }
    WelcomeScreen(
        uiState = uiState,
        onAction = viewModel::onUiAction,
    )
    dialogState?.let { state ->
        WelcomeDialog(
            state = state,
            onDismiss = { dialogState = null },
            onAccept = { input ->
                when (state) {
                    is WelcomeDialogState.Exit -> viewModel.onUiAction(
                        ifac.td.taxi.viewmodel.WelcomeUiAction.ConfirmExit
                    )
                    is WelcomeDialogState.BatteryOptimization -> viewModel.onUiAction(
                        ifac.td.taxi.viewmodel.WelcomeUiAction.ConfirmBatteryOptimization
                    )
                    is WelcomeDialogState.SecurePin -> viewModel.onUiAction(
                        ifac.td.taxi.viewmodel.WelcomeUiAction.SubmitSecurePin(
                            pin = input.orEmpty(),
                            destination = state.destination
                        )
                    )
                }
                dialogState = null
            }
        )
    }
}
fun openUri(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // handle in host/toast
    }
}
fun openBatteryOptimizationSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
