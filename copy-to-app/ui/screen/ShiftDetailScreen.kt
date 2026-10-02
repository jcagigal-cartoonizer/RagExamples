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
// # Block 227-4: import android.content.Intent
@Composable
fun ShiftDetailScreen(
    navController: NavController,
    viewModel: ShiftDetailComposeViewModel,
    shiftId: Long,
    onShowToast: (Int) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<ShiftDetailComposeFragmentCustomDialogState?>(null) }
    LaunchedEffect(shiftId) {
        viewModel.onScreenStarted(shiftId)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ShiftDetailUiEffect.OpenIntent -> {
                    try {
                        if (effect.intent.getStringExtra("EXTRA_INTENT_PURPOSE") == "PURPOSE_SEND_EMAIL") {
                            navController.context.startActivity(
                                Intent.createChooser(effect.intent, navController.context.getString(R.string.send_email_title))
                            )
                        } else {
                            navController.context.startActivity(effect.intent)
                        }
                    } catch (_: Exception) {
                        onShowToast(R.string.start_activity_error_toast)
                    }
                }
                is ShiftDetailUiEffect.ShowDialog -> dialogState = effect.dialog
                is ShiftDetailUiEffect.ShowToast -> onShowToast(effect.messageRes)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ShiftDetailButtonsRow(
                buttons = state.buttons,
                onExport = viewModel::onExportClicked,
                onEmail = viewModel::onEmailClicked,
                onPrint = viewModel::onPrintClicked
            )
            Spacer(modifier = Modifier.height(12.dp))
            ShiftSortRow(
                buttons = state.buttons,
                onSortById = { viewModel.onSortClicked(ShiftOrderOptions.ID) },
                onSortByAmount = { viewModel.onSortClicked(ShiftOrderOptions.AMOUNT) },
                onSortByInitHour = { viewModel.onSortClicked(ShiftOrderOptions.START_DATE) },
                onSortByDistance = { viewModel.onSortClicked(ShiftOrderOptions.DISTANCE) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.trips) { trip ->
                    TripRow(trip = trip)
                }
            }
        }
        dialogState?.let { dialog ->
            ShiftDetailComposeFragmentShiftDetailCustomDialog(
                state = dialog,
                onDismiss = { dialogState = null },
                onConfirm = { dialogState = null }
            )
        }
    }
}
