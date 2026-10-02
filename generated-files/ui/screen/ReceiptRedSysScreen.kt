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
// # Block 310-5: import androidx.compose.foundation.layout.*
@Composable
fun ReceiptRedSysScreen(
    navController: NavController,
    viewModel: ReceiptRedSysComposeViewModel,
    operations: List<RedSysOperation>
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(operations) {
        // If you pass navArgs list here, keep original fragment behavior
        viewModel.onEvent(ReceiptRedSysUiEvent.ScreenOpened)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ReceiptRedSysUiEffect.ShowDialog -> {
                    // handled by state in this version
                }
                is ReceiptRedSysUiEffect.ShowMessage -> {
                    // You can show Snackbar if desired
                }
                is ReceiptRedSysUiEffect.PrintServiceTicket -> {
                    viewModel.printServiceTicket(effect.operation)
                }
                is ReceiptRedSysUiEffect.PrintRefundTicket -> {
                    viewModel.printRefundTicket(effect.operation)
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("RedSys Receipts") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.operations.isEmpty()) {
                Text(
                    text = "No operations available",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.operations) { operation ->
                        ReceiptRedSysItem(
                            operation = operation,
                            onClick = { viewModel.onEvent(ReceiptRedSysUiEvent.OperationClicked(operation)) }
                        )
                    }
                }
            }
            if (uiState.dialogState != null) {
                ReceiptRedSysDialog(
                    state = uiState.dialogState!!,
                    onDismiss = { viewModel.onEvent(ReceiptRedSysUiEvent.DialogDismissed) },
                    onButtonClick = { button ->
                        viewModel.onEvent(ReceiptRedSysUiEvent.DialogButtonClicked(button))
                    }
                )
            }
        }
    }
}
