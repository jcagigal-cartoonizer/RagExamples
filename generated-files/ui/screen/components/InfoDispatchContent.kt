package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ifac.td.taxi.HomeDirections
import kotlinx.coroutines.flow.collectLatest
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
// // # Block 420-6: import androidx.compose.foundation.layout.*
// // import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
// import androidx.compose.runtime.Composable
@Composable
fun InfoDispatchContent(
    modifier: Modifier = Modifier,
    state: InfoDispatchUiState,
    onEvent: (InfoDispatchUiEvent) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        InfoDispatchButtonsRow(
            buttons = state.buttons,
            onEvent = onEvent
        )
        if (state.tabs.isNotEmpty()) {
            InfoDispatchTabs(
                tabs = state.tabs,
                selectedIndex = state.selectedTabIndex,
                onTabSelected = { onEvent(InfoDispatchUiEvent.OnTabSelected(it)) }
            )
        }
        state.flightCode?.let {
            if (it.isNotBlank()) {
                Text(text = it)
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Dispatch fields here
        }
    }
}
