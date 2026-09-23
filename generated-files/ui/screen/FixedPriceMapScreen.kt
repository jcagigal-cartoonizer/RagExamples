package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.FixedPriceUiEvent
import ifac.td.taxi.compose.viewmodel.FixedPriceComposeViewModel
import ifac.td.taxi.ui.screen.components.FixedPriceButtonsState
import ifac.td.taxi.ui.screen.components.FixedPriceUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 509-4: import androidx.compose.foundation.layout.*
@Composable
fun FixedPriceMapScreen(
    navController: NavController,
    viewModel: FixedPriceComposeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState = remember(uiState) { FixedPriceButtonsState.fromUiState(uiState) }
    var dialogState by remember { mutableStateOf<FixedPriceDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is FixedPriceUiEffect.ShowToast -> {
                    // Handle in host if needed
                }
                is FixedPriceUiEffect.UpdateMapPickup -> {
                    // Hook map update here
                }
                is FixedPriceUiEffect.UpdateMapDropOff -> {
                    // Hook map update here
                }
                is FixedPriceUiEffect.OpenDialog -> dialogState = effect.dialog
                FixedPriceUiEffect.CloseDialog -> dialogState = null
                FixedPriceUiEffect.HideKeyboard -> {
                    // host should hide keyboard
                }
                FixedPriceUiEffect.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TextField(
                value = uiState.query,
                onValueChange = { viewModel.onEvent(FixedPriceUiEvent.QueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search drop off") }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.isPoiSearch,
                    onClick = { viewModel.onEvent(FixedPriceUiEvent.PoiSearchChanged(!uiState.isPoiSearch)) },
                    label = { Text("POI") }
                )
            }
            if (buttonsState.suggestionsVisible) {
                SuggestionList(
                    items = uiState.suggestions,
                    onClick = { viewModel.onEvent(FixedPriceUiEvent.SuggestionClicked(it)) }
                )
            }
            Spacer(Modifier.weight(1f))
            FixedPriceBottomSheet(
                buttonsState = buttonsState,
                uiState = uiState,
                onClose = { viewModel.onEvent(FixedPriceUiEvent.CloseClicked) },
                onRetry = { viewModel.onEvent(FixedPriceUiEvent.RetryPrices) }
            )
        }
        dialogState?.let { dialog ->
            FixedPriceCustomDialog(
                title = dialog.title,
                message = dialog.message,
                positiveText = dialog.positiveText,
                negativeText = dialog.negativeText,
                onPositive = {
                    dialogState = null
                },
                onNegative = {
                    dialogState = null
                },
                onDismiss = {
                    dialogState = null
                }
            )
        }
    }
}
