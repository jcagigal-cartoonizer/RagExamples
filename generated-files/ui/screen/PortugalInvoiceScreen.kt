package ifac.td.taxi.ui.screen
import ifac.td.taxi.compose.viewmodel.PortugalInvoiceComposeViewModel
import ifac.td.taxi.ui.screen.components.PortugalInvoiceUiEffect
import ifac.td.taxi.ui.screen.components.PortugalInvoiceScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 226-3: import androidx.compose.foundation.layout.*
@Composable
fun PortugalInvoiceScreen(
    viewModel: PortugalInvoiceComposeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReceiptHistory: (Long) -> Unit,
    showHeader: Boolean = false, // preserve original behavior, handled by host if needed
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState = remember(uiState) {
        PortugalInvoiceButtonsState(
            cancel = ButtonUiState(
                text = "Cancelar",
                enabled = true,
                visible = true,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            accept = ButtonUiState(
                text = "Aceitar",
                enabled = !uiState.isAcceptLoading,
                loading = uiState.isAcceptLoading,
                visible = true,
                backgroundColor = if (uiState.isAcceptLoading)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        )
    }
    var showDialog by remember { mutableStateOf(uiState.showExternalCustomerDialog) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is PortugalInvoiceUiEffect.NavigateBack -> onNavigateBack()
                is PortugalInvoiceUiEffect.NavigateToReceiptHistory -> onNavigateToReceiptHistory(effect.tripId)
                PortugalInvoiceUiEffect.ShowExternalCustomerDialog -> showDialog = true
                PortugalInvoiceUiEffect.HideExternalCustomerDialog -> showDialog = false
                PortugalInvoiceUiEffect.EnableAcceptButton -> Unit
            }
        }
    }
    if (showDialog) {
        PortugalExternalCustomerDialog(
            onCancel = {
                showDialog = false
                viewModel.onExternalCustomerDialogResult(false)
            },
            onAccept = {
                showDialog = false
                viewModel.onExternalCustomerDialogResult(true)
            }
        )
    }
    PortugalInvoiceContent(
        uiState = uiState,
        buttonsState = buttonsState,
        onCancel = viewModel::onCancelClick,
        onAccept = viewModel::onAcceptClicked,
        onCountrySelected = viewModel::onCountrySelected,
        onNifChanged = viewModel::onNifChanged,
        onNameChanged = viewModel::onNameChanged,
        onLocalidadeChanged = viewModel::onLocalidadeChanged,
    )
}
@Composable
fun PortugalInvoiceContent(
    uiState: PortugalInvoiceUiState,
    buttonsState: PortugalInvoiceButtonsState,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    onCountrySelected: (Int) -> Unit,
    onNifChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onLocalidadeChanged: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Replace with your actual fields
        OutlinedTextField(
            value = uiState.nif,
            onValueChange = onNifChanged,
            label = { Text("NIF") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChanged,
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.localidade,
            onValueChange = onLocalidadeChanged,
            label = { Text("Localidade") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        CountryDropdown(
            countries = uiState.countryOptions,
            selectedIndex = uiState.selectedCountryIndex,
            onSelected = onCountrySelected
        )
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ComposeCustomButton(
                state = buttonsState.cancel,
                onClick = onCancel
            )
            ComposeCustomButton(
                state = buttonsState.accept,
                onClick = onAccept
            )
        }
    }
}
