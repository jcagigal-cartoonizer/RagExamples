package ifac.td.taxi.ui.screen
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.layout.*
import ifac.td.taxi.viewmodel.model.MessageUIEnum
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import ifac.td.taxi.viewmodel.HomeViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.grid.*
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
// // ## 7) Screen UI matching the XML grid

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onZoningClick: () -> Unit,
    onPendingClick: () -> Unit,
    onLocateStandClick: () -> Unit,
    onLocationClick: () -> Unit,
    onReceiptsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onCentralClick: () -> Unit,
    onDashboardClick: () -> Unit,
    onFixedPriceClick: () -> Unit,
    onRoofLightClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(1.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            userScrollEnabled = false
        ) {
            item { HomeTile(uiState.buttons.zoning, onZoningClick, "Zoning") }
            item { HomeTile(uiState.buttons.pending, onPendingClick, "Pending") }
            item { HomeTile(uiState.buttons.locateStand, onLocateStandClick, "Locate Stand") }
            item { HomeTile(uiState.buttons.location, onLocationClick, "Location") }
            item { HomeTile(uiState.buttons.dashboard, onDashboardClick, "Dashboard") }
            item { HomeTile(uiState.buttons.fixedPrice, onFixedPriceClick, "Fixed price") }
            item { HomeTile(uiState.buttons.receipts, onReceiptsClick, "Receipts") }
            item { HomeTile(uiState.buttons.messages, onMessagesClick, "Messages") }
            item { HomeTile(uiState.buttons.central, onCentralClick, "Central") }
            item { HomeTile(uiState.buttons.roofLight, onRoofLightClick, "Roof light") }
        }
    }
}


