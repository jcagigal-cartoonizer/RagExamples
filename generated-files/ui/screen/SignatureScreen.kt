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
// # Block 244-3: import android.widget.Toast
@Composable
fun SignatureScreen(
    navController: NavController,
    viewModel: SignatureComposeViewModel,
    sharedViewModel: MainActivityViewModel,
    serviceId: String?,
    onShowHeader: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var signatureBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var showDialog by remember { mutableStateOf<SignatureDialogState?>(null) }
    LaunchedEffect(Unit) {
        onShowHeader(true)
        viewModel.setServiceId(serviceId)
    }
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is SignatureUiEffect.NavigateBack -> navController.popBackStack()
                is SignatureUiEffect.NavigateTo -> navController.navigate(effect.direction)
                is SignatureUiEffect.ShowToast -> onShowToast(effect.messageResId)
                is SignatureUiEffect.ShowDialog -> showDialog = effect.dialog
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.signatureResponse.collectLatest { valid ->
            val trip = sharedViewModel.tripFlow.value
            val dispatch = sharedViewModel.dispatchFlow.value
            if (valid) {
                if (serviceId == dispatch?.longDispatchNumber) {
                    if (dispatch != null && trip != null) {
                        viewModel.dispatchSubscriberNextStep(dispatch, trip)
                    }
                } else {
                    viewModel.sendSubscriberAuth(serviceId, trip?.id)
                }
            } else {
                StaticConfiguration.subscriberFailPin = true
            }
            onShowToast(
                if (valid) R.string.dialog_signature_valid_desc else R.string.dialog_signature_invalid_desc
            )
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SignaturePadContainer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onSignatureChanged = { hasSigned ->
                viewModel.onEvent(SignatureUiEvent.OnSignatureChanged(hasSigned))
            },
            onBitmapChanged = { bitmap ->
                signatureBitmap = bitmap
            },
            onCleared = {
                viewModel.onEvent(SignatureUiEvent.OnClearClicked)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        SignatureButtons(
            state = uiState.signatureButtonsState,
            onAccept = {
                if (uiState.hasSigned) {
                    viewModel.submitSignature(
                        signatureBitmap = signatureBitmap,
                        fromDispatch = (sharedViewModel.tripFlow.value?.fromDispatch == true &&
                            sharedViewModel.dispatchFlow.value?.requireSignature == true)
                    )
                } else {
                    onShowToast(R.string.toast_sign_first)
                }
            },
            onClear = {
                signatureBitmap = null
                viewModel.onEvent(SignatureUiEvent.OnClearClicked)
            }
        )
    }
    if (showDialog != null) {
        SignatureButtonDefaultsSignatureCustomDialog(
            state = showDialog!!,
            onDismiss = { showDialog = null },
            onConfirm = { showDialog = null }
        )
    }
}
