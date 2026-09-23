package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 328-3: import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.OnlineInvoiceViewModel
import kotlinx.coroutines.flow.collectLatest
@Composable
fun OnlineInvoiceScreen(
    tripId: Long,
    navController: NavController,
    viewModel: OnlineInvoiceViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    val buttonsState = remember(uiState.isLoading, uiState.areRequiredFieldsEmpty) {
        OnlineInvoiceButtonsState.from(
            isLoading = uiState.isLoading,
            canGenerateInvoice = !uiState.areRequiredFieldsEmpty
        )
    }
    LaunchedEffect(tripId) {
        viewModel.onTripIdReceived(tripId)
    }
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            viewModel.effects.collectLatest { effect ->
                when (effect) {
                    OnlineInvoiceUiEffect.NavigateBack -> navController.popBackStack()
                    is OnlineInvoiceUiEffect.ShowToast -> {
                        // Hook this up from your Fragment/Activity if needed
                    }
                    is OnlineInvoiceUiEffect.OpenDialog -> {
                        viewModelStateDialogBridge(viewModel, effect.dialogState)
                    }
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Online Invoice",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = uiState.fiscalId,
                onValueChange = viewModel::onFiscalIdChanged,
                label = { Text("NIF") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )
            if (uiState.areFiscalFieldsVisible) {
                OutlinedTextField(value = uiState.companyName, onValueChange = viewModel::onCompanyNameChanged, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading)
                OutlinedTextField(value = uiState.streetName, onValueChange = viewModel::onStreetNameChanged, label = { Text("Street Name") }, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading)
                OutlinedTextField(value = uiState.number, onValueChange = viewModel::onNumberChanged, label = { Text("Number") }, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading)
                OutlinedTextField(value = uiState.city, onValueChange = viewModel::onCityChanged, label = { Text("City") }, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading)
                OutlinedTextField(value = uiState.postalCode, onValueChange = viewModel::onPostalCodeChanged, label = { Text("Postal Code") }, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading)
                OutlinedTextField(value = uiState.province, onValueChange = viewModel::onProvinceChanged, label = { Text("Province") }, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading)
                OutlinedTextField(value = uiState.email, onValueChange = viewModel::onEmailChanged, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading)
                OutlinedTextField(value = uiState.country, onValueChange = viewModel::onCountryChanged, label = { Text("Country") }, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OnlineInvoiceStyledButton(
                    modifier = Modifier.weight(1f),
                    text = "Accept",
                    state = buttonsState.accept,
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onAcceptClicked()
                    }
                )
                OnlineInvoiceStyledButton(
                    modifier = Modifier.weight(1f),
                    text = "Cancel",
                    state = buttonsState.cancel,
                    onClick = viewModel::onCancelClicked
                )
            }
        }
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.dialogState?.let { dialog ->
            OnlineInvoiceCustomDialog(
                dialogState = dialog,
                onDismiss = viewModel::dismissDialog,
                onAccept = {
                    viewModel.confirmDialog()
                    navController.popBackStack()
                }
            )
        }
    }
}
fun viewModelStateDialogBridge(
    viewModel: OnlineInvoiceComposeViewModel,
    dialogState: OnlineInvoiceDialogState
) {
    // If you prefer, route effect -> state here.
    // For pure state-driven dialogs, set dialog state from the VM directly.
}
