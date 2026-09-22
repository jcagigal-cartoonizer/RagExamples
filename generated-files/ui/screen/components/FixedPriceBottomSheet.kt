package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ifac.td.taxi.ui.screen.components.*
import ifac.td.taxi.viewmodel.FixedPriceComposeViewModel
import ifac.td.taxi.viewmodel.FixedPriceUiEffect
import ifac.td.taxi.viewmodel.FixedPriceUiEvent
import ifac.td.taxi.viewmodel.FixedPriceDialogState
import kotlinx.coroutines.flow.collectLatest
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
                }
                is FixedPriceUiEffect.UpdateMapPickup -> {
                }
                is FixedPriceUiEffect.UpdateMapDropOff -> {
                }
                is FixedPriceUiEffect.OpenDialog -> dialogState = effect.dialog
                FixedPriceUiEffect.CloseDialog -> dialogState = null
                FixedPriceUiEffect.HideKeyboard -> {
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
import androidx.compose.runtime.Composable
import ifac.td.taxi.viewmodel.FixedPriceUiState
@Composable
fun FixedPriceBottomSheet(
    buttonsState: FixedPriceButtonsState,
    uiState: FixedPriceUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        tonalElevation = 8.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            if (buttonsState.closeVisible) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onClose) { Text("Close") }
                }
            }
            Text(
                text = uiState.selectedDropOff?.getPrintableStreetTextShort().orEmpty(),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            PriceRow("Taxi", buttonsState.taxi)
            Spacer(Modifier.height(8.dp))
            PriceRow("Van", buttonsState.van)
            Spacer(Modifier.height(8.dp))
            PriceRow("Business", buttonsState.business)
            Spacer(Modifier.height(12.dp))
            if (buttonsState.taxi.error || buttonsState.van.error || buttonsState.business.error) {
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                    Text("Retry")
                }
            }
        }
    }
}
@Composable
fun PriceRow(label: String, state: PriceButtonState) {
    val text = when {
        state.showProgress -> "Loading..."
        state.showError -> "Error"
        state.showText -> state.text
        else -> ""
    }
    OutlinedButton(
        onClick = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        colors = if (state.error) FixedPriceButtonStyle.errorColors() else FixedPriceButtonStyle.neutralColors(),
        contentPadding = FixedPriceButtonStyle.contentPadding
    ) {
        Text("$label: $text")
    }
}
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import ifac.td.taxi.repository.connections.rest.bravoRest.SuggestModel
@Composable
fun SuggestionList(
    items: List<SuggestModel>,
    onClick: (SuggestModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .padding(horizontal = 16.dp)
    ) {
        items(items) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(item) }
                    .padding(vertical = 12.dp)
            ) {
                Text(item.getPrintableStreetTextShort())
            }
        }
    }
}
override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View {
    return ComposeView(requireContext()).apply {
        setContent {
            val navController = findNavController()
            FixedPriceMapScreen(
                navController = navController,
                viewModel = vModelCompose
            )
        }
    }
}
Because the map is from a non-Compose SDK, wrap it in `AndroidView`:
@Composable
fun FixedPriceMapView(
    modifier: Modifier = Modifier,
    onMapReady: () -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            com.nexusgeographics.cercalia.maps.CercaliaMapView(context).apply {
            }
        },
        update = { /* update map state */ }
    )
}
Then in your screen:
FixedPriceMapView(
    modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
)
Here’s the direct Compose equivalent of the original behaviors:
To match the XML behavior exactly, wire these from your resources:
For closer parity, update `FixedPriceButtonStyle` with your exact resource values.
1. a **complete `@Composable` screen with an `AndroidView` Cercalia map integration**,  
2. a **`FixedPriceComposeFragment` wrapper**, and  
3. a **more faithful re-creation of the original bottom sheet + custom button XML styling** using your actual resource names.
