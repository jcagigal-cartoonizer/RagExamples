package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.StatisticsUiEffect
import ifac.td.taxi.compose.viewmodel.StatisticsComposeViewModel
import ifac.td.taxi.ui.screen.components.StatisticsScreen
import ifac.td.taxi.ui.screen.components.StatisticsUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 12-1: import androidx.compose.foundation.layout.Column
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsComposeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState by viewModel.buttonsState.collectAsStateWithLifecycle()
    var dialogState by rememberSaveable { mutableStateOf<StatisticsDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is StatisticsUiEffect.OpenDialog -> {
                    dialogState = effect.dialogState
                }
                StatisticsUiEffect.CloseDialog -> {
                    dialogState = null
                }
                StatisticsUiEffect.NavigateBack -> {
                    navController.popBackStack()
                }
                StatisticsUiEffect.NavigateToHome -> {
                    navController.popBackStack()
                }
            }
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(padding)) {
                StatisticsButtons(
                    state = buttonsState,
                    onEvent = viewModel::onEvent
                )
                StatisticsTabsContent(
                    uiState = uiState,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
    dialogState?.let { dialog ->
        StatisticsCustomDialog(
            state = dialog,
            onDismiss = {
                dialogState = null
                viewModel.onEvent(StatisticsUiEvent.DialogDismissed)
            },
            onConfirm = {
                dialogState = null
                viewModel.onEvent(StatisticsUiEvent.DialogConfirmed)
            }
        )
    }
}
