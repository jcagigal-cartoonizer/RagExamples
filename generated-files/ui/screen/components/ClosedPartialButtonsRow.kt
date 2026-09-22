package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch
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
