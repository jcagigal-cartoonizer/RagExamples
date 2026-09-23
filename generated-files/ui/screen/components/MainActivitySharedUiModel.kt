package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 673-5: import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.model.Trip
data class MainActivitySharedUiModel(
    val trip: Trip? = null
)
In the fragment version you used:
In Compose, keep that behavior by passing lambdas from your navigation host or activity:
PaymentMoneiScreen(
    viewModel = paymentViewModel,
    sharedViewModel = sharedUiModel,
    onNavigateBack = { navController.popBackStack() },
    onNavigateHome = { navController.navigate("home") },
    onPrintTicket = { ticket -> sharedViewModel.printTicket(ticket) },
    showHeader = { visible -> mainActivity.showHeader(visible) }
)
