package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.TripHistoryUiEffect
import ifac.td.taxi.compose.viewmodel.TripHistoryComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 6-1: import androidx.compose.runtime.Composable
@Composable
fun TripHistoryRoute(
    navController: NavController,
    viewModel: TripHistoryComposeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState by viewModel.buttonsState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                TripHistoryUiEffect.NavigateBackToReceiptHistory -> {
                    navController.navigate(
                        "receipt_history/-1"
                    )
                }
                is TripHistoryUiEffect.NavigateToReceiptHistory -> {
                    navController.navigate("receipt_history/${effect.tripId}")
                }
                is TripHistoryUiEffect.ShowNoTicketDialog -> {
                    viewModel.showNoTicketDialog(effect.trip)
                }
                TripHistoryUiEffect.HideDialog -> {
                    viewModel.hideDialog()
                }
            }
        }
    }
    TripHistoryScreen(
        uiState = uiState,
        buttonsState = buttonsState,
        onBackClick = { viewModel.onBackClicked() },
        onAllClick = { viewModel.onAllClicked() },
        onDeleteClick = { viewModel.onDeleteClicked() },
        onTripClick = { viewModel.onTripClicked(it) },
        onDialogDismiss = { viewModel.onDialogDismiss() },
        onDialogAccept = { viewModel.onDialogAccept() },
        onLoadMore = { viewModel.loadNextPage() },
    )
}
> If you are using safe-args Navigation, replace the string navigation with your generated directions. I kept it string-based here to stay Compose-only and preserve the destination intent.
