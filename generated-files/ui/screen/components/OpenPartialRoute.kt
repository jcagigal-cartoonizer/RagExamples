package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.OpenPartialComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 211-3: import androidx.compose.foundation.layout.*
@Composable
fun OpenPartialRoute(
    navController: NavController,
    viewModel: OpenPartialComposeViewModel,
    canClose: Boolean,
    showToast: (Int) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(canClose) {
        viewModel.init(canClose)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                OpenPartialUiEffect.NavigateBack -> navController.popBackStack()
                OpenPartialUiEffect.NavigateToTotalizers ->
                    navController.navigate(R.id.action_openPartialFragment_to_totalizersFragment)
                OpenPartialUiEffect.ShowToastNoPartialsToPrint -> showToast(R.string.no_partials_to_print)
                OpenPartialUiEffect.RequestClosePartialsConfirmation -> viewModel.showCloseDialog()
                OpenPartialUiEffect.ClosePartialsAndNavigate ->
                    navController.navigate(R.id.action_openPartialsFragment_to_closedPartialFragment)
            }
        }
    }
    OpenPartialScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}
