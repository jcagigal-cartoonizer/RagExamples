package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 503-7: import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
@Composable
fun DashboardEffectCollector(
    viewModel: DashboardComposeViewModel,
    onNavigateBack: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                DashboardUiEffect.NavigateBack -> onNavigateBack()
                is DashboardUiEffect.ShowDialog -> {
                    // handled by DashboardScreen local dialog state if you wire it through state/effects
                }
                is DashboardUiEffect.ShowToast -> {
                    // hook toast/snackbar here if needed
                }
            }
        }
    }
}
Old fragment behavior -> Compose equivalent:
A few things depend on your project specifics:
1. **Exact `AvailableColumnsEnum` parsing**
2. **Exact custom button visuals**
3. **Exact dialog XML layout**
4. **Pending trips list**
