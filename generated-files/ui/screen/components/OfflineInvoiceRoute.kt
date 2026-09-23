package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 370-5: import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ifac.td.taxi.compose.viewmodel.OfflineInvoiceComposeViewModel
import ifac.td.taxi.ui.screen.state.OfflineInvoiceUiEffect
import ifac.td.taxi.ui.screen.state.OfflineInvoiceUiEvent
import kotlinx.coroutines.flow.collectLatest
@Composable
fun OfflineInvoiceRoute(
    navController: NavController,
    tripId: Long,
    viewModel: OfflineInvoiceComposeViewModel,
    onPrintTicket: (receipt: String, brokenDownTaxTicket: String) -> Unit,
    onShowToast: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(tripId) {
        viewModel.onEvent(OfflineInvoiceUiEvent.TripIdChanged(tripId))
        viewModel.onEvent(OfflineInvoiceUiEvent.LoadPreferences)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is OfflineInvoiceUiEffect.PrintInvoice -> {
                    onShowToast(R.string.btn_printing_invoice)
                    onPrintTicket(effect.receipt, effect.brokenDownTaxTicket)
                }
                is OfflineInvoiceUiEffect.ShowToast -> onShowToast(effect.messageRes)
                OfflineInvoiceUiEffect.NavigateBack -> navController.navigateUp()
                is OfflineInvoiceUiEffect.OpenCopyDialog -> Unit
            }
        }
    }
    val buttonsState = remember(uiState) {
        val isValidToSubmit = uiState.driverDirection.isNotBlank() &&
                uiState.driverPostalCode.isNotBlank() &&
                uiState.driverCity.isNotBlank() &&
                uiState.clientNameAndSurname.isNotBlank() &&
                uiState.clientNIF.isNotBlank() &&
                uiState.clientDirection.isNotBlank() &&
                uiState.clientPostalCode.isNotBlank() &&
                uiState.clientCity.isNotBlank()
        OfflineInvoiceButtonsState.fromFormState(
            isValidToSubmit = isValidToSubmit,
            isBusy = uiState.isInvoiceRequestInProgress
        )
    }
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OfflineInvoiceContent(
            uiState = uiState,
            buttonsState = buttonsState,
            onEvent = viewModel::onEvent
        )
        if (uiState.isCopyDialogVisible) {
            ComposeOfflineInvoiceCustomDialog(
                model = ComposeCustomDialogModel(
                    title = stringResource(R.string.btn_invoice),
                    description = stringResource(R.string.ask_invoice_copy),
                    isCancellable = false
                ),
                onAction = { button ->
                    when (button) {
                        OfflineInvoiceDialogButtonType.ACCEPT -> viewModel.onEvent(OfflineInvoiceUiEvent.DialogCopyAcceptClicked)
                        OfflineInvoiceDialogButtonType.CANCEL -> viewModel.onEvent(OfflineInvoiceUiEvent.DialogCopyCancelClicked)
                    }
                }
            )
        }
    }
}
@Composable
fun OfflineInvoiceContent(
    uiState: ifac.td.taxi.ui.screen.state.OfflineInvoiceUiState,
    buttonsState: OfflineInvoiceButtonsState,
    onEvent: (OfflineInvoiceUiEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = uiState.driverDirection,
            onValueChange = { onEvent(OfflineInvoiceUiEvent.DriverDirectionChanged(it)) },
            label = { Text("Driver direction") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.driverPostalCode,
            onValueChange = { onEvent(OfflineInvoiceUiEvent.DriverPostalCodeChanged(it)) },
            label = { Text("Driver postal code") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.driverCity,
            onValueChange = { onEvent(OfflineInvoiceUiEvent.DriverCityChanged(it)) },
            label = { Text("Driver city") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.clientNameAndSurname,
            onValueChange = { onEvent(OfflineInvoiceUiEvent.ClientNameChanged(it)) },
            label = { Text("Client name and surname") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.clientNIF,
            onValueChange = { onEvent(OfflineInvoiceUiEvent.ClientNifChanged(it)) },
            label = { Text("Client NIF") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.clientDirection,
            onValueChange = { onEvent(OfflineInvoiceUiEvent.ClientDirectionChanged(it)) },
            label = { Text("Client direction") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.clientPostalCode,
            onValueChange = { onEvent(OfflineInvoiceUiEvent.ClientPostalCodeChanged(it)) },
            label = { Text("Client postal code") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.clientCity,
            onValueChange = { onEvent(OfflineInvoiceUiEvent.ClientCityChanged(it)) },
            label = { Text("Client city") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            if (buttonsState.cancelVisible) {
                OfflineInvoiceActionButton(
                    text = "Cancel",
                    enabled = buttonsState.cancelEnabled,
                    backgroundColor = buttonsState.cancelBackgroundColor,
                    contentColor = buttonsState.cancelContentColor,
                    onClick = { onEvent(OfflineInvoiceUiEvent.CancelClicked) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (buttonsState.acceptVisible) {
                OfflineInvoiceActionButton(
                    text = "Accept",
                    enabled = buttonsState.acceptEnabled,
                    backgroundColor = buttonsState.acceptBackgroundColor,
                    contentColor = buttonsState.acceptContentColor,
                    onClick = { onEvent(OfflineInvoiceUiEvent.AcceptClicked) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
1. a `Compose` dialog that visually matches your XML `custom_dialog.xml` more closely,
2. a `TextField` wrapper that mimics your custom fields and `FieldType.DNI` validation,
3. a `MainActivityViewModel` Compose bridge for `printTicket`, `showToast`, and `navigateBack`,
4. a `Scaffold`-based full screen with header support.
