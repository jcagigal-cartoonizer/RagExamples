package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PointsOfInterestUiEffect
import ifac.td.taxi.compose.viewmodel.PointsOfInterestComposeViewModel
import ifac.td.taxi.ui.screen.components.PointsOfInterestScreen
import ifac.td.taxi.ui.screen.components.PointsOfInterestUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 269-2: import android.content.Context
@Composable
fun PointsOfInterestRoute(
    viewModel: PointsOfInterestComposeViewModel,
    onNavigateBack: () -> Unit,
    onLaunchIntent: (android.content.Intent?) -> Unit,
    onSaveHiredZone: (com.interfacom.sdk.taximeter.licensing.models.zoning.Zone?) -> Unit,
    showHeader: (Boolean) -> Unit = {},
) {
    LaunchedEffect(Unit) { showHeader(true) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var dialogState by remember { mutableStateOf<PointsOfInterestDialogState?>(null) }
    var selectedPoiForDialog by remember { mutableStateOf<com.interfacom.sdk.taximeter.bravocomm.rest.poi.response.Poi?>(null) }
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                PointsOfInterestUiEffect.NavigateBack -> onNavigateBack()
                is PointsOfInterestUiEffect.OpenNavigator -> onLaunchIntent(effect.intent)
                is PointsOfInterestUiEffect.SaveHiredZoneAndNavigateBack -> {
                    onSaveHiredZone(effect.zone)
                    onNavigateBack()
                    onNavigateBack()
                }
                is PointsOfInterestUiEffect.ShowPoiDialog -> {
                    dialogState = effect.dialog
                }
                is PointsOfInterestUiEffect.ShowLocateConfirmDialog -> {
                    dialogState = effect.dialog
                }
                PointsOfInterestUiEffect.HideKeyboard -> {
                    hideKeyboard(context)
                }
            }
        }
    }
    PointsOfInterestScreen(
        state = state,
        dialogState = dialogState,
        onEvent = { event ->
            when (event) {
                is PointsOfInterestUiEvent.PoiClicked -> {
                    selectedPoiForDialog = event.poi
                    viewModel.onEvent(event)
                }
                is PointsOfInterestUiEvent.DialogButtonClicked -> {
                    when (event.action) {
                        PoiDialogAction.UBICAR_DESTINO ->
                            dialogState = PointsOfInterestDialogState(
                                title = selectedPoiForDialog?.zone?.nombreZone.orEmpty(),
                                description = "Do you want to locate destination?",
                                buttons = listOf(ButtonTypeUi.CANCEL, ButtonTypeUi.ACCEPT)
                            )
                        PoiDialogAction.NAVEGAR -> viewModel.openNavigatorApp(
                            selectedPoiForDialog?.lat,
                            selectedPoiForDialog?.lon,
                            selectedPoiForDialog?.street
                        )
                        PoiDialogAction.UBICAR_DESTINO_NAVEGAR ->
                            dialogState = PointsOfInterestDialogState(
                                title = selectedPoiForDialog?.zone?.nombreZone.orEmpty(),
                                description = "Do you want to locate destination?",
                                buttons = listOf(ButtonTypeUi.CANCEL, ButtonTypeUi.ACCEPT)
                            )
                    }
                }
                else -> viewModel.onEvent(event)
            }
        },
        onDialogAction = { action ->
            when (action) {
                ButtonTypeUi.ACCEPT -> {
                    val poi = selectedPoiForDialog
                    val navigateAfter = false
                    if (poi != null) {
                        if (dialogState?.buttons?.contains(ButtonTypeUi.ACCEPT) == true) {
                            viewModel.confirmLocateAndMaybeNavigate(navigateAfter)
                        }
                    }
                    dialogState = null
                }
                ButtonTypeUi.CANCEL -> dialogState = null
                else -> Unit
            }
        }
    )
}
@Composable
fun PointsOfInterestScreen(
    state: PointsOfInterestUiState,
    dialogState: PointsOfInterestDialogState?,
    onEvent: (PointsOfInterestUiEvent) -> Unit,
    onDialogAction: (ButtonTypeUi) -> Unit,
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("Points of Interest") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.searchText,
                onValueChange = { onEvent(PointsOfInterestUiEvent.SearchTextChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search POI") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { onEvent(PointsOfInterestUiEvent.SearchClicked) }
                )
            )
            Spacer(Modifier.height(12.dp))
            PointsOfInterestButtons(
                state = state.buttons,
                onCancel = { onEvent(PointsOfInterestUiEvent.CancelClicked) },
                onSearch = { onEvent(PointsOfInterestUiEvent.SearchClicked) }
            )
            Spacer(Modifier.height(16.dp))
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.pois) { poi ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onEvent(PointsOfInterestUiEvent.PoiClicked(poi)) }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(text = poi.poi)
                                Text(text = poi.zone?.nombreZone ?: "Out of zone")
                            }
                        }
                    }
                }
            }
        }
        dialogState?.let {
            PointsOfInterestCustomDialog(
                state = it,
                onDismissRequest = { onEvent(PointsOfInterestUiEvent.DialogDismissed) },
                onButtonClick = onDialogAction
            )
        }
    }
}
