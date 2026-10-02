package ifac.td.taxi.ui.screen.components
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// # Block 370-5: import androidx.compose.foundation.layout.*
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
            ComposeOfflineInvoiceRouteOfflineInvoiceCustomDialog(
                model = ComposeOfflineInvoiceRouteCustomDialogModel(
                    title = stringResource(R.string.btn_invoice),
                    description = stringResource(R.string.ask_invoice_copy),
                    isCancellable = false
                ),
                onAction = { button ->
                    when (button) {
                        OfflineInvoiceRouteDialogButtonType.ACCEPT -> viewModel.onEvent(OfflineInvoiceUiEvent.DialogCopyAcceptClicked)
                        OfflineInvoiceRouteDialogButtonType.CANCEL -> viewModel.onEvent(OfflineInvoiceUiEvent.DialogCopyCancelClicked)
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
Your fragment navigation can be preserved by hosting the Compose screen inside a fragment:
class OfflineInvoiceComposeFragment : Fragment() {
    private val args: OfflineInvoiceFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            val navController = findNavController()
            OfflineInvoiceRoute(
                navController = navController,
                tripId = args.tripId,
                viewModel = /* obtain your VM here */,
                onPrintTicket = { receipt, brokenDownTaxTicket ->
                    // sharedViewModel.printTicket(...)
                },
                onShowToast = { resId ->
                    Toast.makeText(requireContext(), getString(resId), Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
1. a `Compose` dialog that visually matches your XML `custom_dialog.xml` more closely,
2. a `TextField` wrapper that mimics your custom fields and `FieldType.DNI` validation,
3. a `MainActivityViewModel` Compose bridge for `printTicket`, `showToast`, and `navigateBack`,
4. a `Scaffold`-based full screen with header support.
