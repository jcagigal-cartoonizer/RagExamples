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
// # Block 381-6: import android.net.Uri
@Composable
fun CropImageScreen(
    vModel: CropImageComposeViewModel,
    sharedViewModel: MainActivityViewModel,
    serviceId: String?,
    onRequestCameraPermission: () -> Unit,
    onOpenImageSelect: () -> Unit,
    onOpenCropImage: (Uri?) -> Unit
) {
    val uiState by vModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(serviceId) {
        vModel.setServiceId(serviceId)
    }
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vModel.uiEffect.collectLatest { effect ->
                when (effect) {
                    CropImageUiEffect.RequestCameraPermission -> onRequestCameraPermission()
                    CropImageUiEffect.OpenImageSelector -> onOpenImageSelect()
                    CropImageUiEffect.OpenCropper -> onOpenCropImage(uiState.imageUri)
                    is CropImageUiEffect.ShowToast -> sharedViewModel.showToast(effect.messageResId)
                    CropImageUiEffect.NavigateBack -> sharedViewModel.navigateBack()
                    is CropImageUiEffect.OpenScannerQr -> {
                        sharedViewModel.navigateToScannerQR(effect.dispatchNumber)
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        vModel.requestCameraPermission()
    }
    val buttonsState = CropImageButtonsState.from(hasImage = uiState.imageUri != null)
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CropPreview(uri = uiState.imageUri)
            if (uiState.showConfirmSendDialog) {
                CropImageScreenCropImageCustomDialog(
                    titleResId = R.string.app_name,
                    messageResId = R.string.dialog_send_image_description,
                    onConfirm = { vModel.onEvent(CropImageUiEvent.ConfirmSend) },
                    onDismiss = { vModel.onEvent(CropImageUiEvent.DismissDialog) }
                )
            }
            CropImageButtons(
                state = buttonsState,
                onAccept = { vModel.onEvent(CropImageUiEvent.ClickAccept) },
                onCrop = { vModel.onEvent(CropImageUiEvent.ClickCrop) },
                onSelectImage = { vModel.onEvent(CropImageUiEvent.ClickSelectImage) }
            )
        }
    }
}
@Composable
fun CropPreview(uri: Uri?) {
    if (uri == null) {
        Text(
            text = stringResource(R.string.dialog_voucher_required),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }
    Text(
        text = uri.toString(),
        modifier = Modifier.fillMaxWidth()
    )
}
@Composable
fun CropImageButtons(
    state: CropImageButtonsState,
    onAccept: () -> Unit,
    onCrop: () -> Unit,
    onSelectImage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.acceptVisible) {
            androidx.compose.material3.Button(
                onClick = onAccept,
                enabled = state.acceptEnabled,
                shape = CropButtonStyle.shape,
                colors = CropButtonStyle.colors(state.acceptContainerColor, state.acceptContentColor),
                border = CropButtonStyle.border(state.acceptEnabled, state.acceptContainerColor),
                contentPadding = CropButtonStyle.contentPadding,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.accept))
            }
        }
        if (state.cropVisible) {
            androidx.compose.material3.Button(
                onClick = onCrop,
                enabled = state.cropEnabled,
                shape = CropButtonStyle.shape,
                colors = CropButtonStyle.colors(state.cropContainerColor, state.cropContentColor),
                border = CropButtonStyle.border(state.cropEnabled, state.cropContainerColor),
                contentPadding = CropButtonStyle.contentPadding,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.crop))
            }
        }
        if (state.selectImageVisible) {
            androidx.compose.material3.Button(
                onClick = onSelectImage,
                enabled = state.selectImageEnabled,
                shape = CropButtonStyle.shape,
                colors = CropButtonStyle.colors(state.selectContainerColor, state.selectContentColor),
                border = CropButtonStyle.border(state.selectImageEnabled, state.selectContainerColor),
                contentPadding = CropButtonStyle.contentPadding,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.select_image))
            }
        }
    }
}
I can’t guarantee exact visual parity without seeing:
But the code above gives you:
In your original fragment, `btnAccept` does this:
In Compose, that is already handled by:
CropImageUiEvent.ClickAccept
which makes the UI layer dumb and the ViewModel responsible for the decision, which is the right MVI-style replacement.
1. a **full fragment host wrapper** using `ComposeView` so this can be dropped into your current navigation graph with no route changes, or  
2. a version that uses **Material3 `AlertDialog`** only, or  
3. a version that more closely mimics your **custom XML button component** once you share that XML/Kotlin file.
