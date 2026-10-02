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
// # Block 5-1: import androidx.compose.foundation.layout.*
@Composable
fun LegalTextRoute(
    navController: NavController,
    viewModel: LegalTextComposeViewModel,
    showHeader: (Boolean) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Preserve fragment behavior: hide header on screen entry
    LaunchedEffect(Unit) {
        showHeader(false)
        viewModel.loadLegalText()
    }
    // Collect effects lifecycle-aware
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.effects.collectLatest { effect ->
                    when (effect) {
                        LegalTextUiEffect.NavigateToWelcome -> {
                            navController.navigate(R.id.action_legalTextFragment_to_welcomeFragment)
                        }
                        LegalTextUiEffect.ShowAcceptDialog -> {
                            viewModel.setDialogVisible(true)
                        }
                        LegalTextUiEffect.HideAcceptDialog -> {
                            viewModel.setDialogVisible(false)
                        }
                    }
                }
            }
        }
    }
    LegalTextScreen(
        uiState = uiState,
        onAcceptClick = viewModel::onAcceptClick,
        onDismissDialog = viewModel::dismissDialog,
        onConfirmDialog = viewModel::confirmDialog,
    )
}
@Composable
fun LegalTextScreen(
    uiState: ifac.td.taxi.viewmodel.LegalTextUiState,
    onAcceptClick: () -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDialog: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = uiState.legalText ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            )
            Spacer(modifier = Modifier.height(16.dp))
            LegalTextButtons(
                state = uiState.buttonsState,
                onAccept = onAcceptClick
            )
        }
        if (uiState.isDialogVisible) {
            LegalTextCustomFilledButtonColorsLegalTextCustomDialog(
                title = uiState.dialog.title,
                message = uiState.dialog.message,
                confirmText = uiState.dialog.confirmText,
                dismissText = uiState.dialog.dismissText,
                onConfirm = onConfirmDialog,
                onDismiss = onDismissDialog
            )
        }
    }
}
