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
// # Block 328-5: import androidx.compose.foundation.layout.Arrangement
@Composable
fun RefundMoneiRoute(
    tripId: Long,
    viewModel: RefundMoneiComposeViewModel,
    onNavigateBack: () -> Unit,
    onShowSnackbar: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(tripId) {
        viewModel.start(tripId)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                RefundMoneiUiEffect.NavigateBack -> onNavigateBack()
                is RefundMoneiUiEffect.ShowSnackbar -> onShowSnackbar(effect.message)
                RefundMoneiUiEffect.StartPolling -> Unit
                RefundMoneiUiEffect.StopPolling -> Unit
            }
        }
    }
    RefundMoneiScreen(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}
@Composable
fun RefundMoneiScreen(
    state: RefundMoneiUiState,
    onEvent: (RefundMoneiUiEvent) -> Unit
) {
    val buttonsState = remember(state) {
        RefundMoneiButtonsState(
            showCancel = true,
            showRefund = state.status != PaymentMoneiViewModel.StatusPayments.REFUNDED,
            cancelEnabled = true,
            refundEnabled = state.canRefund && state.status != PaymentMoneiViewModel.StatusPayments.REFUNDED,
            cancelText = "Cancel",
            refundText = "Refund",
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Amount")
        Spacer(Modifier.height(8.dp))
        Text(text = state.amountText.toCurrency())
        Spacer(Modifier.height(12.dp))
        Text(text = state.status.name)
        if (state.isLoading) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            if (buttonsState.showCancel) {
                RefundMoneiButton(
                    text = buttonsState.cancelText,
                    enabled = buttonsState.cancelEnabled,
                    background = RefundMoneiButtonStyle.secondary,
                    textColor = RefundMoneiButtonStyle.textOnSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(RefundMoneiUiEvent.OnCancelClicked) }
                )
            }
            if (buttonsState.showRefund) {
                RefundMoneiButton(
                    text = buttonsState.refundText,
                    enabled = buttonsState.refundEnabled,
                    background = RefundMoneiButtonStyle.primary,
                    textColor = RefundMoneiButtonStyle.textOnPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(RefundMoneiUiEvent.OnRefundClicked) }
                )
            }
        }
        if (state.showConfirmDialog) {
            RefundMoneiComposeFragmentRefundMoneiCustomDialog(
                title = "Refund",
                message = "Do you want to refund this payment?",
                confirmText = "Refund",
                dismissText = "Cancel",
                onConfirm = { onEvent(RefundMoneiUiEvent.OnConfirmRefund) },
                onDismiss = { onEvent(RefundMoneiUiEvent.OnDismissDialog) }
            )
        }
        if (state.showErrorDialog) {
            RefundMoneiComposeFragmentRefundMoneiCustomDialog(
                title = "Error",
                message = state.errorMessage ?: "Something went wrong",
                confirmText = "OK",
                dismissText = "Close",
                onConfirm = { onEvent(RefundMoneiUiEvent.OnDismissDialog) },
                onDismiss = { onEvent(RefundMoneiUiEvent.OnDismissDialog) }
            )
        }
    }
}
To preserve your existing Navigation component flow, create a fragment that hosts the Compose screen and reads `navArgs()` exactly like before.
