package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 11-1: import android.media.ToneGenerator
// import android.media.ToneGenerator
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.compose.rememberNavController
import androidx.core.net.toUri
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    effects: SharedFlow<HomeUiEffect>,
    onNavigate: (HomeNavigation) -> Unit,
    onShowToast: (Int) -> Unit,
    onBeep: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var dialogState by remember { mutableStateOf<HomeDialogState?>(null) }
    LaunchedEffect(effects, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            effects.collectLatest { effect ->
                when (effect) {
                    is HomeUiEffect.Navigate -> onNavigate(effect.destination)
                    is HomeUiEffect.ShowToast -> onShowToast(effect.messageRes)
                    is HomeUiEffect.Beep -> onBeep(effect.tone)
                    is HomeUiEffect.ShowDialog -> dialogState = effect.dialog
                    HomeUiEffect.HideDialog -> dialogState = null
                }
            }
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeButton(
                state = state.buttons.zoning,
                onClick = { onEvent(HomeUiEvent.ZoningClicked) }
            )
            HomeButton(
                state = state.buttons.pending,
                onClick = { onEvent(HomeUiEvent.PendingClicked) }
            )
            HomeButton(
                state = state.buttons.location,
                onClick = { onEvent(HomeUiEvent.LocationClicked) }
            )
            HomeButton(
                state = state.buttons.receipts,
                onClick = { onEvent(HomeUiEvent.ReceiptsClicked) }
            )
            HomeButton(
                state = state.buttons.messages,
                onClick = { onEvent(HomeUiEvent.MessagesClicked) }
            )
            HomeButton(
                state = state.buttons.central,
                onClick = { onEvent(HomeUiEvent.CentralClicked) }
            )
            HomeButton(
                state = state.buttons.dashboard,
                onClick = { onEvent(HomeUiEvent.DashboardClicked) }
            )
            HomeButton(
                state = state.buttons.fixedPrice,
                onClick = { onEvent(HomeUiEvent.FixedPriceClicked) }
            )
            HomeButton(
                state = state.buttons.roofLight,
                onClick = { onEvent(HomeUiEvent.RoofLightClicked) }
            )
            HomeButton(
                state = state.buttons.locateStand,
                onClick = { onEvent(HomeUiEvent.LocateStandClicked) }
            )
        }
        dialogState?.let { dialog ->
            HomeHomeCustomDialog(
                dialog = dialog,
                onDismiss = {
                    onEvent(HomeUiEvent.DialogDismissed)
                },
                onConfirm = {
                    onEvent(HomeUiEvent.DialogConfirmed)
                },
                onCancel = {
                    onEvent(HomeUiEvent.DialogCancelled)
                }
            )
        }
    }
}
