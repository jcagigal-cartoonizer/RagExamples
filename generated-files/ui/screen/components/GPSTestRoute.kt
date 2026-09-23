package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 272-5: import android.app.Activity
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.GPSTestViewModel
import kotlinx.coroutines.launch
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
