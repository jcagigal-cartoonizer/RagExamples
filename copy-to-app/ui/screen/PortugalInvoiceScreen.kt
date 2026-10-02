package ifac.td.taxi.ui.screen
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
