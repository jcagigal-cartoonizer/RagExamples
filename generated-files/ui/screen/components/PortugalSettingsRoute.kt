package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.PortugalSettingsComposeViewModel
import ifac.td.taxi.ui.screen.components.PortugalSettingsUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 169-2: import androidx.compose.foundation.layout.*
@Composable
fun PortugalSettingsRoute(
    viewModel: PortugalSettingsComposeViewModel,
    onNavigateBack: () -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var dialogState by remember { mutableStateOf<PortugalDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.onEvent(PortugalSettingsUiEvent.ScreenShown)
    }
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                PortugalSettingsUiEffect.NavigateBack -> onNavigateBack()
                PortugalSettingsUiEffect.ShowPinIncorrectToast -> onShowToast(R.string.pin_incorrecto)
                PortugalSettingsUiEffect.OpenCurrentPinDialog -> {
                    dialogState = PortugalDialogState.CurrentPin
                }
                PortugalSettingsUiEffect.OpenNewPinDialog -> {
                    dialogState = PortugalDialogState.NewPin
                }
            }
        }
    }
    PortugalSettingsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        dialogState = dialogState,
        onDismissDialog = { dialogState = null }
    )
    when (dialogState) {
        PortugalDialogState.CurrentPin -> {
            PortugalSettingsCustomDialog(
                title = stringResource(R.string.pin_actual),
                description = stringResource(R.string.pin_actual_hint),
                editTextTypePin = true,
                editTextMaxLength = 4,
                onCancel = {
                    dialogState = null
                },
                onAccept = { pin ->
                    dialogState = null
                    viewModel.onEvent(PortugalSettingsUiEvent.PinEntered(pin))
                }
            )
        }
        PortugalDialogState.NewPin -> {
            PortugalSettingsCustomDialog(
                title = stringResource(R.string.change_pin),
                description = stringResource(R.string.nuevo_pin),
                editTextTypePin = true,
                editTextMaxLength = 4,
                onCancel = {
                    dialogState = null
                },
                onAccept = { pin ->
                    dialogState = null
                    viewModel.onEvent(PortugalSettingsUiEvent.NewPinEntered(pin))
                }
            )
        }
        null -> Unit
    }
}
enum class PortugalDialogState {
    CurrentPin, NewPin
}
