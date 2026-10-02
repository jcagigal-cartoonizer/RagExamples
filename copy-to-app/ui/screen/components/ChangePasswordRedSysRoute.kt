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
// # Block 296-5: import androidx.compose.foundation.layout.Column
@Composable
fun ChangePasswordRedSysRoute(
    viewModel: ChangePasswordRedSysComposeViewModel,
    navigateBack: () -> Unit,
    showHeader: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var dialogState by remember { mutableStateOf<ChangePasswordRedSysDialogState?>(null) }
    LaunchedEffect(Unit) {
        showHeader(false)
    }
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collectLatest { effect ->
                when (effect) {
                    is ChangePasswordRedSysUiEffect.NavigateBack -> navigateBack()
                    is ChangePasswordRedSysUiEffect.ShowDialog -> dialogState = effect.dialog
                }
            }
        }
    }
    ChangePasswordRedSysScreen(
        uiState = uiState,
        dialogState = dialogState,
        onDismissDialog = { dialogState = null },
        onDialogAccepted = {
            dialogState = null
            viewModel.onDialogAccepted()
        },
        onCancel = {
            viewModel.onCancelClick()
        },
        onAccept = {
            viewModel.onAcceptClick()
        },
        onUserChanged = viewModel::onUserChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onNewPasswordChanged = viewModel::onNewPasswordChanged,
        onRepeatNewPasswordChanged = viewModel::onRepeatNewPasswordChanged
    )
}
@Composable
fun ChangePasswordRedSysScreen(
    uiState: ifac.td.taxi.viewmodel.ChangePasswordRedSysUiState,
    dialogState: ChangePasswordRedSysDialogState?,
    onDismissDialog: () -> Unit,
    onDialogAccepted: () -> Unit,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    onUserChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onRepeatNewPasswordChanged: (String) -> Unit,
) {
    val buttonsState = ChangePasswordRedSysButtonsStateProvider.fromUiState(uiState.isLoading)
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Change password")
        Spacer(modifier = Modifier.height(16.dp))
        // Replace these with your actual text fields.
        // These are here to show state flow from ViewModel to composable.
        // You can wire them to OutlinedTextField etc.
        Spacer(modifier = Modifier.height(24.dp))
        ChangePasswordRedSysActionButton(
            state = buttonsState.accept,
            onClick = onAccept
        )
        Spacer(modifier = Modifier.height(12.dp))
        ChangePasswordRedSysActionButton(
            state = buttonsState.cancel,
            onClick = onCancel
        )
        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
    dialogState?.let { dialog ->
        ChangePasswordRedSysRouteChangePasswordRedSysCustomDialog(
            model = ChangePasswordRedSysRouteCustomDialogModel(
                title = dialog.title,
                description = dialog.description,
                buttons = listOf(ChangePasswordRedSysRouteDialogButtonType.ACCEPT)
            ),
            onDismissRequest = onDismissDialog,
            onResponse = { response ->
                when (response.buttonPressed) {
                    ChangePasswordRedSysRouteDialogButtonType.ACCEPT -> onDialogAccepted()
                    ChangePasswordRedSysRouteDialogButtonType.CANCEL -> onDismissDialog()
                }
            }
        )
    }
}
@Composable
fun ChangePasswordRedSysDestination(
    viewModel: ChangePasswordRedSysComposeViewModel,
    onNavigateBack: () -> Unit
) {
    ChangePasswordRedSysRoute(
        viewModel = viewModel,
        navigateBack = onNavigateBack,
        showHeader = { visible ->
            // call your scaffold/header state here
        }
    )
}
navigateBack = { iMainActivity.navigateBack() }
sealed interface ChangePasswordRedSysRouteButtonVisualState {
    data object Enabled : ChangePasswordRedSysRouteButtonVisualState
    data object Disabled : ChangePasswordRedSysRouteButtonVisualState
    data object Loading : ChangePasswordRedSysRouteButtonVisualState
}
Then map that to colors, padding, border, corner radius, and elevation in a single composable.
1. a full Compose screen including the text fields and validation,
2. a Hilt/Koin ViewModel factory version,
3. or a more exact `ChangePasswordRedSysRouteCustomDialog` and `CustomButton` recreation based on your XML styles if you paste them.
