package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ClosedPartialUiEffect
import ifac.td.taxi.compose.viewmodel.ClosedPartialComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 177-3: import androidx.compose.foundation.layout.*
@Composable
fun ClosedPartialRoute(
    viewModel: ClosedPartialComposeViewModel,
    justClosed: Boolean,
    navController: NavController,
    sharedViewModel: MainActivityViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.onStart()
    }
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffects.collect { effect ->
                    when (effect) {
                        ClosedPartialUiEffect.NavigateBack -> navController.popBackStack()
                        is ClosedPartialUiEffect.NavigateToTotalizers ->
                            navController.navigate(effect.destinationId)
                        ClosedPartialUiEffect.PrintPartial -> Unit
                        ClosedPartialUiEffect.CloseDialog -> Unit
                    }
                }
            }
        }
    }
    ClosedPartialScreen(
        uiState = uiState,
        onCancel = { viewModel.onCancelClicked(justClosed) },
        onPrint = { viewModel.onPrintClicked() },
        onTotalizers = { viewModel.onTotalizersClicked(R.id.action_closedPartialFragment_to_totalizersFragment) },
        onDialogDismiss = { viewModel.onDialogDismiss() }
    )
}
