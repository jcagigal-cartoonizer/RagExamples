package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.OpenPartialViewModel
import kotlinx.coroutines.flow.collectLatest
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
// // # Block 260-4: import androidx.compose.foundation.layout.*
