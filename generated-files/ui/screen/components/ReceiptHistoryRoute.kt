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
// # Block 9-1: import androidx.compose.foundation.layout.*
@Composable
fun ReceiptHistoryRoute(
    ticketId: Long,
    navController: NavController,
    viewModel: ReceiptHistoryComposeViewModel,
    onShowHeader: (Boolean) -> Unit = {},
    onShareImage: (android.net.Uri) -> Unit = {},
    onToast: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState by viewModel.buttonsState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    // Equivalent of onResume()
    LifecycleStartEffect(ticketId) {
        viewModel.onScreenStarted(ticketId)
        onShowHeader(true)
        onStopOrDispose { }
    }
    // SharedFlow effects -> one pipeline
    CollectReceiptHistoryEffects(
        effects = viewModel.uiEffect,
        navController = navController,
        onShareImage = onShareImage,
        onToast = onToast,
        onOpenDialog = { viewModel.openDialog(it) }
    )
    ReceiptHistoryScreen(
        uiState = uiState,
        buttonsState = buttonsState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent
    )
}
@Composable
fun ReceiptHistoryScreen(
    uiState: ReceiptHistoryUiState,
    buttonsState: ReceiptHistoryButtonsState,
    dialogState: ReceiptHistoryDialogState,
    onEvent: (ReceiptHistoryUiEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ReceiptViewer(
                trip = uiState.trip,
                isVisible = uiState.trip != null,
                isEmpty = uiState.isReceiptEmpty,
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.trip == null || uiState.isReceiptEmpty) {
                Text(
                    text = "No hay viajes",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ReceiptHistoryButtons(
                state = buttonsState,
                onPrint = { onEvent(ReceiptHistoryUiEvent.PrintClicked) },
                onPrevious = { onEvent(ReceiptHistoryUiEvent.PreviousClicked) },
                onPartials = { onEvent(ReceiptHistoryUiEvent.PartialsClicked) },
                onCard = { onEvent(ReceiptHistoryUiEvent.CardClicked) },
                onBill = { onEvent(ReceiptHistoryUiEvent.BillClicked) },
                onFoto = { onEvent(ReceiptHistoryUiEvent.PhotoClicked) }
            )
        }
        if (dialogState.visible) {
            ReceiptHistoryButtonsCustomDialogCompose(
                state = dialogState,
                onDismiss = { onEvent(ReceiptHistoryUiEvent.DialogDismissed) },
                onButtonClick = { btn -> onEvent(ReceiptHistoryUiEvent.DialogButtonClicked(btn)) }
            )
        }
    }
}
@Composable
fun ReceiptViewer(
    trip: Trip?,
    isVisible: Boolean,
    isEmpty: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    Surface(modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(text = "Recibo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(text = "Trip: ${trip?.id ?: "-"}")
            Text(text = "Importe: ${trip?.totalAmount ?: 0.0}")
            Text(text = "Empty: $isEmpty")
        }
    }
}
@Composable
fun CollectReceiptHistoryEffects(
    effects: SharedFlow<ReceiptHistoryUiEffect>,
    navController: NavController,
    onShareImage: (android.net.Uri) -> Unit,
    onToast: (Int) -> Unit,
    onOpenDialog: (ReceiptHistoryDialogState) -> Unit
) {
    LaunchedEffect(Unit) {
        effects.collect { effect ->
            when (effect) {
                ReceiptHistoryUiEffect.NavigateToTripHistory -> {
                    navController.navigate(R.id.action_receiptHistoryFragment_to_tripHistoryFragment)
                }
                ReceiptHistoryUiEffect.NavigateToHome -> {
                    navController.navigate(R.id.action_receiptHistoryFragment_to_homeFragment)
                }
                ReceiptHistoryUiEffect.NavigateToOnTrip -> {
                    navController.navigate(R.id.action_receiptHistoryFragment_to_onTripFragment)
                }
                is ReceiptHistoryUiEffect.NavigateToOnlineInvoice -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToOnlineInvoiceFragment(
                            effect.tripId
                        )
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToOfflineInvoice -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToOfflineInvoiceFragment(
                            effect.tripId
                        )
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToRefundMonei -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToRefundMoneiFragment(
                            effect.tripId
                        )
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToReceiptRedSys -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToReceiptRedSysFragment(
                            effect.operations
                        )
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToCropImage -> {
                    navController.navigate(
                        HomeDirections.goToCropImageViewFragment(effect.dispatchNumber)
                    )
                }
                is ReceiptHistoryUiEffect.ShareTicketImage -> onShareImage(effect.uri)
                is ReceiptHistoryUiEffect.ToastRes -> onToast(effect.resId)
                is ReceiptHistoryUiEffect.ShowDialog -> onOpenDialog(effect.dialogState)
            }
        }
    }
}
