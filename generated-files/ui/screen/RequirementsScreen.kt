package ifac.td.taxi.ui.screen
import ifac.td.taxi.compose.viewmodel.RequirementsComposeViewModel
import ifac.td.taxi.ui.screen.components.RequirementsUiEvent
import ifac.td.taxi.ui.screen.components.RequirementsUiEffect
import ifac.td.taxi.ui.screen.components.RequirementsScreen
import ifac.td.taxi.ui.screen.components.RequirementsUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 199-3: import androidx.activity.compose.BackHandler
@Composable
fun RequirementsScreen(
    viewModel: RequirementsComposeViewModel,
    onNavigateBack: () -> Unit = {},
    onShowToast: (String) -> Unit = {},
    onShowHeader: (Boolean) -> Unit = {},
    onShowBottomBar: (Boolean) -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState(initial = RequirementsUiState())
    LaunchedEffect(Unit) {
        onShowHeader(true)
        onShowBottomBar(true)
    }
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        is RequirementsUiEffect.ShowToast -> onShowToast(effect.message)
                        RequirementsUiEffect.NavigateBack -> onNavigateBack()
                        is RequirementsUiEffect.OpenDialog -> Unit
                        RequirementsUiEffect.CloseDialog -> Unit
                    }
                }
            }
        }
    }
    BackHandler {
        onNavigateBack()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        RequirementsSection(
                            title = "Driver Requirements",
                            noDataVisible = uiState.showDriverNoData,
                            noDataText = "No driver requirements",
                            items = uiState.driverRequirements,
                            buttonState = uiState.buttonsState.driverPrimary,
                            onButtonClick = { viewModel.onEvent(RequirementsUiEvent.OnDriverPrimaryClicked) }
                        )
                    }
                    item {
                        RequirementsSection(
                            title = "Vehicle Requirements",
                            noDataVisible = uiState.showVehicleNoData,
                            noDataText = "No vehicle requirements",
                            items = uiState.vehicleRequirements,
                            buttonState = uiState.buttonsState.vehiclePrimary,
                            onButtonClick = { viewModel.onEvent(RequirementsUiEvent.OnVehiclePrimaryClicked) }
                        )
                    }
                }
            }
        }
        if (uiState.dialogState is RequirementsDialogState.Info) {
            val dialog = uiState.dialogState as RequirementsDialogState.Info
            RequirementsCustomDialog(
                title = dialog.title,
                message = dialog.message,
                confirmText = dialog.confirmText,
                dismissText = dialog.dismissText,
                onConfirm = { viewModel.onEvent(RequirementsUiEvent.OnDialogConfirmClicked) },
                onDismiss = { viewModel.onEvent(RequirementsUiEvent.OnDialogDismissClicked) }
            )
        }
    }
}
@Composable
fun RequirementsSection(
    title: String,
    noDataVisible: Boolean,
    noDataText: String,
    items: List<String>,
    buttonState: RequirementsButtonVisualState,
    onButtonClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (noDataVisible) {
            Text(text = noDataText, style = MaterialTheme.typography.bodyMedium)
        }
        if (items.isNotEmpty()) {
            Column {
                items.forEachIndexed { index, item ->
                    Text(text = item, style = MaterialTheme.typography.bodyMedium)
                    if (index != items.lastIndex) Divider()
                }
            }
        }
        if (buttonState.visible) {
            RequirementsButton(
                text = "Open",
                state = buttonState,
                onClick = onButtonClick
            )
        }
    }
}
