package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.LightsTestComposeViewModel
import ifac.td.taxi.ui.screen.components.LightsTestUiEvent
import ifac.td.taxi.ui.screen.components.LightsTestUiEffect
import ifac.td.taxi.ui.screen.components.LightsTestScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 234-4: import android.app.Activity
@Composable
fun LightsTestScreen(
    viewModel: LightsTestComposeViewModel,
    navController: NavController,
    showHeader: (Boolean) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        showHeader(true)
        viewModel.onEvent(LightsTestUiEvent.ScreenStarted)
    }
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collectLatest { effect ->
                when (effect) {
                    LightsTestUiEffect.NavigateBack -> navController.popBackStack()
                    is LightsTestUiEffect.ShowToast -> {
                        android.widget.Toast.makeText(context, effect.message, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    BackHandler {
        viewModel.onEvent(LightsTestUiEvent.BackClicked)
    }
    LightsTestContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
    uiState.dialog?.let { dialog ->
        when (dialog) {
            LightsTestDialogState.ConfirmUvLight -> {
                LightsTestCustomDialog(
                    title = "UV Light",
                    message = "Turn on UV light?",
                    confirmText = "Yes",
                    dismissText = "No",
                    onConfirm = { viewModel.onEvent(LightsTestUiEvent.DialogConfirmed) },
                    onDismiss = { viewModel.onEvent(LightsTestUiEvent.DialogDismissed) }
                )
            }
        }
    }
}
