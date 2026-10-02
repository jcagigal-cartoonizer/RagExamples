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
// # Block 199-3: import androidx.activity.compose.BackHandler
@Composable
fun RequirementsScreen(
    viewModel: RequirementsComposeViewModel,
    onNavigateBack: () -> Unit = {},
    onShowToast: (String) -> Unit = {},
    onShowHeader: (Boolean) -> Unit = {},
    onShowBottomBar: (Boolean) -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState(initial = RequirementsUiState())
    LaunchedEffect(Unit) {
        onShowHeader(true)
        onShowBottomBar(true)
    }
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        is RequirementsUiEffect.ShowToast -> onShowToast(effect.message)
                        RequirementsUiEffect.NavigateBack -> onNavigateBack()
                        is RequirementsUiEffect.OpenDialog -> Unit
                        RequirementsUiEffect.CloseDialog -> Unit
                    }
                }
            }
        }
    }
    BackHandler {
        onNavigateBack()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        RequirementsSection(
                            title = "Driver Requirements",
                            noDataVisible = uiState.showDriverNoData,
                            noDataText = "No driver requirements",
                            items = uiState.driverRequirements,
                            buttonState = uiState.buttonsState.driverPrimary,
                            onButtonClick = { viewModel.onEvent(RequirementsUiEvent.OnDriverPrimaryClicked) }
                        )
                    }
                    item {
                        RequirementsSection(
                            title = "Vehicle Requirements",
                            noDataVisible = uiState.showVehicleNoData,
                            noDataText = "No vehicle requirements",
                            items = uiState.vehicleRequirements,
                            buttonState = uiState.buttonsState.vehiclePrimary,
                            onButtonClick = { viewModel.onEvent(RequirementsUiEvent.OnVehiclePrimaryClicked) }
                        )
                    }
                }
            }
        }
        if (uiState.dialogState is RequirementsDialogState.Info) {
            val dialog = uiState.dialogState as RequirementsDialogState.Info
            RequirementsButtonStyleHelperRequirementsCustomDialog(
                title = dialog.title,
                message = dialog.message,
                confirmText = dialog.confirmText,
                dismissText = dialog.dismissText,
                onConfirm = { viewModel.onEvent(RequirementsUiEvent.OnDialogConfirmClicked) },
                onDismiss = { viewModel.onEvent(RequirementsUiEvent.OnDialogDismissClicked) }
            )
        }
    }
}
@Composable
fun RequirementsSection(
    title: String,
    noDataVisible: Boolean,
    noDataText: String,
    items: List<String>,
    buttonState: RequirementsButtonStyleHelperButtonVisualState,
    onButtonClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (noDataVisible) {
            Text(text = noDataText, style = MaterialTheme.typography.bodyMedium)
        }
        if (items.isNotEmpty()) {
            Column {
                items.forEachIndexed { index, item ->
                    Text(text = item, style = MaterialTheme.typography.bodyMedium)
                    if (index != items.lastIndex) Divider()
                }
            }
        }
        if (buttonState.visible) {
            RequirementsButton(
                text = "Open",
                state = buttonState,
                onClick = onButtonClick
            )
        }
    }
}
