package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.DispatchReceivedUiEffect
import ifac.td.taxi.compose.viewmodel.DispatchReceivedComposeViewModel
import ifac.td.taxi.ui.screen.components.DispatchReceivedScreen
import ifac.td.taxi.ui.screen.components.DispatchReceivedUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 4-1: import androidx.compose.foundation.layout.*
@Composable
fun DispatchReceivedRoute(
    viewModel: DispatchReceivedComposeViewModel,
    idDispatch: Long,
    onNavigateToInfoDispatch: (Long) -> Unit,
    onCloseDialog: (String) -> Unit,
    onOpenDialog: (DispatchReceivedDialogModel) -> Unit,
    onRequestPhonePermission: () -> Unit,
    onShowToast: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(idDispatch) {
        viewModel.onEvent(DispatchReceivedUiEvent.LoadDispatch(idDispatch))
        viewModel.onEvent(DispatchReceivedUiEvent.CheckCancelDialogPermission)
        viewModel.onEvent(DispatchReceivedUiEvent.CloseExternalDialog(DispatchReceivedCustomDialogTags.LOCATE_ON_STAND_DIALOG))
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is DispatchReceivedUiEffect.NavigateToInfoDispatch ->
                    onNavigateToInfoDispatch(effect.tripId)
                is DispatchReceivedUiEffect.OpenPermissionDialog ->
                    onOpenDialog(effect.dialog)
                DispatchReceivedUiEffect.RequestPhonePermission ->
                    onRequestPhonePermission()
                is DispatchReceivedUiEffect.ShowRejectConfirmDialog ->
                    onOpenDialog(
                        DispatchReceivedDialogModel(
                            title = stringResource(R.string.dialog_warning_title),
                            description = "¿Quieres rechazar este despacho?",
                            buttons = listOf(DispatchReceivedDialogButtonSpec.CancelAccept)
                        )
                    )
                is DispatchReceivedUiEffect.CloseDialog ->
                    onCloseDialog(effect.tag)
                is DispatchReceivedUiEffect.ShowToast ->
                    onShowToast(effect.message)
            }
        }
    }
    DispatchReceivedScreen(
        uiState = uiState,
        buttonsState = viewModel.buttonsState(uiState),
        onAccept = { viewModel.onEvent(DispatchReceivedUiEvent.AcceptClicked) },
        onCancel = { viewModel.onEvent(DispatchReceivedUiEvent.CancelClicked) },
        onCallPhone = { viewModel.onEvent(DispatchReceivedUiEvent.PhoneClicked) },
        onRequestRejectConfirmation = { viewModel.onEvent(DispatchReceivedUiEvent.RequestCancelConfirmation) },
    )
}
@Composable
fun DispatchReceivedScreen(
    uiState: DispatchReceivedUiState,
    buttonsState: DispatchReceivedButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    onCallPhone: () -> Unit,
    onRequestRejectConfirmation: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = uiState.hour ?: "")
                Text(text = uiState.pickUpZone ?: "")
                Text(text = uiState.simpleAddress ?: "")
                Text(text = uiState.city ?: "")
                Text(text = uiState.passengerName ?: "")
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.showCallButton) {
                    Button(
                        onClick = onCallPhone,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Llamar")
                    }
                }
                DispatchReceivedButtons(
                    state = buttonsState,
                    onAccept = onAccept,
                    onCancel = onCancel,
                    onRequestRejectConfirmation = onRequestRejectConfirmation
                )
            }
        }
    }
}
