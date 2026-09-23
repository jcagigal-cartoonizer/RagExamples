package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.GPSTestButtonsState
import ifac.td.taxi.ui.screen.components.GPSTestUiEffect
import ifac.td.taxi.compose.viewmodel.GPSTestComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 272-5: import android.app.Activity
@Composable
fun GPSTestRoute(
    viewModel: GPSTestComposeViewModel,
    onNavigateBack: () -> Unit,
    onLaunchIntent: (Intent) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        viewModel.checkExternalGPS()
        viewModel.initViewModel()
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.removeHandlerCallback()
        }
    }
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is GPSTestUiEffect.NavigateBack -> onNavigateBack()
                    is GPSTestUiEffect.LaunchIntent -> onLaunchIntent(effect.intent)
                    is GPSTestUiEffect.HideDialog -> Unit
                    is GPSTestUiEffect.ShowDialog -> Unit
                }
            }
        }
    }
    GPSTestScreen(
        uiState = uiState,
        buttonsState = GPSTestButtonsState(),
        onAcceptClick = viewModel::onAcceptClicked,
        onGpsClick = viewModel::onGpsClicked
    )
}
