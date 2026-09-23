package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.ZoningServicesComposeViewModel
import ifac.td.taxi.ui.screen.components.ZoningServicesCustomDialog
import ifac.td.taxi.ui.screen.components.ZoningServicesUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 504-6: import androidx.compose.foundation.layout.*
@Composable
fun ZoningServicesCustomDialog(
    state: ZoningServicesCustomDialogState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = state.title)
        },
        text = {
            Text(text = state.message)
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = state.confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = state.dismissText)
            }
        }
    )
}
Example Fragment host:
class ZoningServicesComposeFragment : Fragment() {
    private val viewModel: ZoningServicesComposeViewModel by viewModel()
    private val args: ZoningServicesFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // If using ComposeView
    }
}
But in Compose you can keep your existing nav actions via the lambdas:
ZoningServicesScreen(
    navController = navController,
    viewModel = viewModel,
    onNavigateBack = { navController.popBackStack() },
    onNavigateToHome = {
        navController.navigate(ZoningServicesFragmentDirections.actionZoningServicesFragmentToHomeFragment())
    },
    onNavigateToOnTrip = {
        navController.navigate(HomeDirections.goToOnTripFragment())
    }
)
Your old fragment had:
That is now represented in `ZoningServicesButtonsState`.
The old:
is now consolidated into `ZoningServicesUiState`.
@Composable
fun ZoningServicesRoute(
    navController: NavController,
    viewModel: ZoningServicesComposeViewModel,
    idMacroZone: Int,
    idZone: Int,
) {
    LaunchedEffect(idMacroZone, idZone) {
        viewModel.onEvent(ZoningServicesUiEvent.Init(idMacroZone, idZone))
    }
    ZoningServicesScreen(
        navController = navController,
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToHome = {
            navController.navigate(
                ifac.td.taxi.ui.screen.ZoningServicesFragmentDirections
                    .actionZoningServicesFragmentToHomeFragment()
            )
        },
        onNavigateToOnTrip = {
            navController.navigate(ifac.td.taxi.HomeDirections.goToOnTripFragment())
        }
    )
}
To match your XML even more precisely, the next step would be:
