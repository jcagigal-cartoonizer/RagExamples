package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.SignatureUiEvent
import ifac.td.taxi.ui.screen.components.SignatureUiEffect
import ifac.td.taxi.ui.screen.components.SignatureScreen
import ifac.td.taxi.compose.viewmodel.SignatureComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
        SignatureCustomDialog(
            state = showDialog!!,
            onDismiss = { showDialog = null },
            onConfirm = { showDialog = null }
        )
    }
}
Because the original view uses `com.github.gcacace.signaturepad.views.SignaturePad`, the simplest Compose bridge is `AndroidView`.
