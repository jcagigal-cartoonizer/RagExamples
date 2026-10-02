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
// # Block 213-4: import androidx.compose.foundation.layout.*
@Composable
fun ChooseOptionScreen(
    viewModel: ChooseOptionComposeViewModel,
    onNavigateBack: () -> Unit,
    onCloseScreenAndGoBack: () -> Unit,
    onSetFragmentActive: (Boolean) -> Unit,
    title: String = "Choose Option",
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                ChooseOptionUiEffect.NavigateBack -> {
                    onSetFragmentActive(false)
                    onNavigateBack()
                }
                ChooseOptionUiEffect.HideDialog -> Unit
                is ChooseOptionUiEffect.ShowDialog -> Unit
                ChooseOptionUiEffect.CloseScreenAndGoBack -> {
                    onSetFragmentActive(false)
                    onCloseScreenAndGoBack()
                }
            }
        }
    }
    BackHandler {
        viewModel.onEvent(ChooseOptionUiEvent.BackPressed)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.options) { option ->
                    ChooseOptionOptionRow(
                        label = option.label,
                        onClick = {
                            viewModel.onEvent(
                                ChooseOptionUiEvent.OptionClicked(
                                    id = option.id,
                                    label = option.label
                                )
                            )
                        }
                    )
                }
            }
        }
        if (uiState.isDialogVisible) {
            uiState.dialogState?.let { dialogState ->
                ChooseOptionCustomDialog(
                    state = dialogState,
                    onDismissRequest = { viewModel.onEvent(ChooseOptionUiEvent.DialogCancelClicked) },
                    onCancel = { viewModel.onEvent(ChooseOptionUiEvent.DialogCancelClicked) },
                    onAccept = { viewModel.onEvent(ChooseOptionUiEvent.DialogAcceptClicked) }
                )
            }
        }
    }
}
@Composable
fun ChooseOptionOptionRow(
    label: String,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(text = label)
    }
}
