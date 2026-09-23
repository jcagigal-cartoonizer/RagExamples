package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.ShiftDetailUiEffect
import ifac.td.taxi.ui.screen.components.ShiftDetailScreen
import ifac.td.taxi.compose.viewmodel.ShiftDetailComposeViewModel
import ifac.td.taxi.ui.screen.components.ShiftDetailCustomDialogState?
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 227-4: import android.content.Intent
@Composable
fun ShiftDetailScreen(
    navController: NavController,
    viewModel: ShiftDetailComposeViewModel,
    shiftId: Long,
    onShowToast: (Int) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<ShiftDetailCustomDialogState?>(null) }
    LaunchedEffect(shiftId) {
        viewModel.onScreenStarted(shiftId)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ShiftDetailUiEffect.OpenIntent -> {
                    try {
                        if (effect.intent.getStringExtra("EXTRA_INTENT_PURPOSE") == "PURPOSE_SEND_EMAIL") {
                            navController.context.startActivity(
                                Intent.createChooser(effect.intent, navController.context.getString(R.string.send_email_title))
                            )
                        } else {
                            navController.context.startActivity(effect.intent)
                        }
                    } catch (_: Exception) {
                        onShowToast(R.string.start_activity_error_toast)
                    }
                }
                is ShiftDetailUiEffect.ShowDialog -> dialogState = effect.dialog
                is ShiftDetailUiEffect.ShowToast -> onShowToast(effect.messageRes)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ShiftDetailButtonsRow(
                buttons = state.buttons,
                onExport = viewModel::onExportClicked,
                onEmail = viewModel::onEmailClicked,
                onPrint = viewModel::onPrintClicked
            )
            Spacer(modifier = Modifier.height(12.dp))
            ShiftSortRow(
                buttons = state.buttons,
                onSortById = { viewModel.onSortClicked(ShiftOrderOptions.ID) },
                onSortByAmount = { viewModel.onSortClicked(ShiftOrderOptions.AMOUNT) },
                onSortByInitHour = { viewModel.onSortClicked(ShiftOrderOptions.START_DATE) },
                onSortByDistance = { viewModel.onSortClicked(ShiftOrderOptions.DISTANCE) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.trips) { trip ->
                    TripRow(trip = trip)
                }
            }
        }
        dialogState?.let { dialog ->
            ShiftDetailCustomDialog(
                state = dialog,
                onDismiss = { dialogState = null },
                onConfirm = { dialogState = null }
            )
        }
    }
}
These are intended to match your custom button component behavior more closely.
