package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
@Composable
fun CropImageScreen(
    viewModel: CropImageComposeViewModel,
    navController: NavHostController,
    onRequestHeaderVisibility: (Boolean) -> Unit,
    onRequestCameraPermission: () -> Unit,
    onOpenCropImage: (Uri?) -> Unit,
    onOpenImageSelect: () -> Unit,
    onShowToast: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        onRequestHeaderVisibility(true)
        onRequestCameraPermission()
    }
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        is CropImageUiEffect.ShowToast -> onShowToast(effect.messageResId)
                        is CropImageUiEffect.RequestCropImage -> onOpenCropImage(effect.uri)
                        CropImageUiEffect.RequestImageSelect -> onOpenImageSelect()
                        CropImageUiEffect.NavigateBack -> navController.popBackStack()
                        is CropImageUiEffect.OpenCustomDialog -> {
                        }
                    }
                }
            }
        }
    }
    val dialogState = state.dialogState
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ImagePreview(
                uri = state.imageUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
            CropImageButtons(
                state = state.buttonsState,
                onAccept = { viewModel.onEvent(CropImageUiEvent.AcceptClicked) },
                onCrop = { viewModel.onEvent(CropImageUiEvent.CropClicked) },
                onSelectImage = { viewModel.onEvent(CropImageUiEvent.SelectImageClicked) }
            )
        }
        if (state.isUploading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        when (val dialog = dialogState) {
            CustomDialogState.Hidden -> Unit
            is CustomDialogState.Visible -> {
                CropImageCustomDialog(
                    title = dialog.title,
                    description = dialog.description,
                    confirmText = dialog.confirmText,
                    dismissText = dialog.dismissText,
                    onConfirm = { viewModel.onEvent(CropImageUiEvent.DialogConfirmed) },
                    onDismiss = { viewModel.onEvent(CropImageUiEvent.DialogDismissed) }
                )
            }
        }
    }
}
@Composable
fun ImagePreview(
    uri: Uri?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF2F2F2)
    ) {
        if (uri == null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "No image selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    android.widget.ImageView(context).apply {
                        scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                        setImageURI(uri)
                    }
                },
                update = { it.setImageURI(uri) }
            )
        }
    }
}
