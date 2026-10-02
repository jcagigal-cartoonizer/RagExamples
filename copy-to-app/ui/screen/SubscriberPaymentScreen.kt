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
// # Block 4-1: import android.text.InputFilter
@Composable
fun SubscriberPaymentScreen(
    navController: NavController,
    viewModel: SubscriberPaymentComposeViewModel,
    onOpenScanner: (cameraPosition: Int, onResult: (String) -> Unit) -> Unit,
    onShowToast: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var dialogState by remember { mutableStateOf<SubscriberPaymentDialogState?>(null) }
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        is SubscriberPaymentUiEffect.ShowToast -> onShowToast(effect.messageRes)
                        is SubscriberPaymentUiEffect.ShowErrorDialog -> {
                            dialogState = SubscriberPaymentDialogState(
                                title = context.getString(R.string.dialog_error_title),
                                description = effect.description,
                                buttons = listOf(SubscriberPaymentDialogButton.Accept)
                            )
                        }
                        is SubscriberPaymentUiEffect.ShowOperationAuthorizedDialog -> {
                            dialogState = SubscriberPaymentDialogState(
                                title = context.getString(R.string.operation_authorized),
                                description = null,
                                buttons = listOf(SubscriberPaymentDialogButton.Accept),
                                cancellable = false
                            )
                        }
                        is SubscriberPaymentUiEffect.NavigateBack -> navController.popBackStack()
                        is SubscriberPaymentUiEffect.NavigateHome -> {
                            navController.navigate("home") {
                                popUpTo(0)
                            }
                        }
                        is SubscriberPaymentUiEffect.NavigateToSignature -> {
                            navController.navigate("signature/${effect.dispatchId}")
                        }
                        is SubscriberPaymentUiEffect.NavigateToVoucher -> {
                            navController.navigate("voucher/${effect.dispatchId}")
                        }
                        is SubscriberPaymentUiEffect.NavigateToQr -> {
                            navController.navigate("scannerQr/${effect.dispatchId}")
                        }
                    }
                }
            }
        }
    }
    SubscriberPaymentDialog(
        state = dialogState,
        onDismiss = { dialogState = null },
        onButtonClick = {
            dialogState = null
        }
    )
    SubscriberPaymentContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onOpenScanner = onOpenScanner,
        modifier = modifier
    )
}
