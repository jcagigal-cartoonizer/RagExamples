package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.InfoDispatchUiEffect
import ifac.td.taxi.compose.viewmodel.InfoDispatchComposeViewModel
import ifac.td.taxi.ui.screen.components.InfoDispatchUiEvent
import ifac.td.taxi.ui.screen.components.InfoDispatchScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 352-5: import androidx.compose.foundation.layout.*
@Composable
fun InfoDispatchScreen(
    navController: NavController,
    viewModel: ifac.td.taxi.viewmodel.InfoDispatchComposeViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<InfoDispatchDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                InfoDispatchUiEffect.NavigateToDirections -> {
                    navController.navigate(/* Directions route */ "directions")
                }
                InfoDispatchUiEffect.NavigateToHome -> {
                    navController.navigate(HomeDirections.goToOnTripFragment().actionId)
                }
                is InfoDispatchUiEffect.NavigateToMeetingSign -> {
                    navController.navigate(/* meeting sign route */ "meeting_sign/${effect.textColor}/${effect.backgroundColor}")
                }
                InfoDispatchUiEffect.RequestPhonePermission -> {
                    // launch permission flow from host if desired
                }
                is InfoDispatchUiEffect.ShowToast -> {
                    // host-side toast/snackbar handling
                }
                is InfoDispatchUiEffect.OpenExternalPhoneCall -> {
                    // use host / activity to start phone intent
                }
                is InfoDispatchUiEffect.OpenDialog -> dialogState = effect.dialog
                InfoDispatchUiEffect.CloseDialog -> dialogState = null
            }
        }
    }
    Scaffold(
        topBar = { /* preserve header behavior */ },
        bottomBar = { /* preserve bottom bar behavior */ }
    ) { padding ->
        InfoDispatchContent(
            modifier = Modifier.padding(padding),
            state = state,
            onEvent = viewModel::onEvent
        )
    }
    dialogState?.let { dialog ->
        InfoDispatchCustomDialog(
            state = dialog,
            onDismiss = { dialogState = null },
            onButtonClick = { button ->
                viewModel.onEvent(InfoDispatchUiEvent.OnDialogButtonClicked(button))
                dialogState = null
            }
        )
    }
}
