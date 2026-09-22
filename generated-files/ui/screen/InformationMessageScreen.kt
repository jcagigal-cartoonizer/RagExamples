package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.ui.screen.state.InformationMessageDialog
import ifac.td.taxi.ui.screen.compose.state.InformationMessageButtonsState
import ifac.td.taxi.compose.viewmodel.InformationMessageComposeViewModel
@Composable
fun InformationMessageScreen(
    viewModel: InformationMessageComposeViewModel,
    onBack: () -> Unit,
    onNavigateBackAfterSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                InformationMessageUiEffect.NavigateBack -> onBack()
                InformationMessageUiEffect.NavigateBackAfterSend -> onNavigateBackAfterSend()
                is InformationMessageUiEffect.ShowToast -> {
                }
            }
        }
    }
    val buttonsState = remember(uiState) {
        InformationMessageButtonsState.from(uiState)
    }
    BackHandler(enabled = true) {
        viewModel.onCancelPressed()
    }
    LaunchedEffect(Unit) {
        viewModel.initVM()
    }
    uiState.selectedInformationMessage?.let { selected ->
        if (uiState.dialogVisible) {
            InformationMessageDialog(
                title = uiState.dialogTitle,
                description = selected.second,
                buttonsState = buttonsState.dialogButtons,
                onDismiss = viewModel::onDialogDismiss,
                onButtonClick = { type ->
                    viewModel.onDialogButtonPressed(type, selected.first)
                }
            )
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
        },
        bottomBar = {
            InformationMessageBottomBar(
                buttonsState = buttonsState,
                onCancel = viewModel::onCancelPressed
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(uiState.informationMessages) { index, message ->
                InformationMessageRow(
                    index = index,
                    message = message,
                    onClick = { viewModel.onMessageSelected(index, message) }
                )
            }
        }
    }
}
@Composable
fun InformationMessageRow(
    index: Int,
    message: String,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
with a Compose-friendly state/effect model.
