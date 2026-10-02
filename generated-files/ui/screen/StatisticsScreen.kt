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
// # Block 12-1: import androidx.compose.foundation.layout.Column
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsComposeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState by viewModel.buttonsState.collectAsStateWithLifecycle()
    var dialogState by rememberSaveable { mutableStateOf<StatisticsDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is StatisticsUiEffect.OpenDialog -> {
                    dialogState = effect.dialogState
                }
                StatisticsUiEffect.CloseDialog -> {
                    dialogState = null
                }
                StatisticsUiEffect.NavigateBack -> {
                    navController.popBackStack()
                }
                StatisticsUiEffect.NavigateToHome -> {
                    navController.popBackStack()
                }
            }
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(padding)) {
                StatisticsButtons(
                    state = buttonsState,
                    onEvent = viewModel::onEvent
                )
                StatisticsTabsContent(
                    uiState = uiState,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
    dialogState?.let { dialog ->
        StatisticsPageStatisticsCustomDialog(
            state = dialog,
            onDismiss = {
                dialogState = null
                viewModel.onEvent(StatisticsUiEvent.DialogDismissed)
            },
            onConfirm = {
                dialogState = null
                viewModel.onEvent(StatisticsUiEvent.DialogConfirmed)
            }
        )
    }
}
