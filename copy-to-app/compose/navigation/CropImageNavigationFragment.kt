package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.CropImageScreen
import ifac.td.taxi.compose.viewModel.CropImageComposeViewModel
import ifac.td.taxi.viewModel.CropImageViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 5-1: import android.net.Uri
class CropImageNavigationFragment : Fragment() {
    private val vModel: CropImageComposeViewModel by viewModels()
    private val sharedViewModel: MainActivityViewModel by activityViewModels()
    private val args: CropImageNavigationFragmentArgs by navArgs()
    private val serviceId by lazy { args.serviceId }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel.setServiceId(serviceId)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val uiState = vModel.uiState.collectAsStateWithLifecycle().value
                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        vModel.uiEffect.collectLatest { effect ->
                            when (effect) {
                                CropImageUiEffect.RequestCameraPermission -> {
                                    sharedViewModel.requestPermission(
                                        ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.PERMISSION_CAMERA
                                    )
                                }
                                CropImageUiEffect.OpenImageSelector -> {
                                    sharedViewModel.openImageSelect()
                                }
                                CropImageUiEffect.OpenCropper -> {
                                    sharedViewModel.openCropImage(uiState.imageUri)
                                }
                                is CropImageUiEffect.ShowToast -> {
                                    sharedViewModel.showToast(effect.messageResId)
                                }
                                CropImageUiEffect.NavigateBack -> {
                                    sharedViewModel.navigateBack()
                                }
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
                LaunchedEffect(sharedViewModel.cropImageFlow) {
                    sharedViewModel.cropImageFlow.collectLatest { uriContent ->
                        if (uriContent != null) {
                            vModel.saveFileFromUri(uriContent)
                        }
                    }
                }
                CropImageScreen(
                    vModel = vModel,
                    sharedViewModel = sharedViewModel,
                    serviceId = serviceId,
                    onRequestCameraPermission = {
                        sharedViewModel.requestPermission(
                            ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.PERMISSION_CAMERA
                        )
                    },
                    onOpenImageSelect = {
                        sharedViewModel.openImageSelect()
                    },
                    onOpenCropImage = { uri: Uri? ->
                        sharedViewModel.openCropImage(uri)
                    }
                )
            }
        }
    }
}
Replace the old destination:
<fragment
    android:id="@+id/cropImageViewFragment"
    android:name="ifac.td.taxi.ui.screen.CropImageViewFragment"
    android:label="CropImageViewFragment"
    tools:layout="@layout/fragment_crop_image_view">
    <argument
        android:name="serviceId"
        app:argType="string"
        app:nullable="true" />
</fragment>
with:
<fragment
    android:id="@+id/cropImageViewFragment"
    android:name="ifac.td.taxi.ui.screen.CropImageNavigationFragment"
    android:label="CropImageNavigationFragment"
    tools:layout="@layout/fragment_crop_image_view">
    <argument
        android:name="serviceId"
        app:argType="string"
        app:nullable="true" />
</fragment>
Your composable `CropImageScreen(...)` already contains the UI-effect collection and also requests camera permission with:
LaunchedEffect(Unit) {
    vModel.requestCameraPermission()
}
That means if you use the fragment above exactly as-is, you may be triggering effect handling twice if you also collect `uiEffect` in the fragment.
Pick one place to handle side effects:
For a fragment wrapper, **Option B is cleaner**, but since you asked to base the answer on the provided composable, I kept it compatible.
// # Block 148-2: import android.os.Bundle
class CropImageNavigationFragment : Fragment() {
    private val vModel: CropImageComposeViewModel by viewModels()
    private val sharedViewModel: MainActivityViewModel by activityViewModels()
    private val args: CropImageNavigationFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                CropImageScreen(
                    vModel = vModel,
                    sharedViewModel = sharedViewModel,
                    serviceId = args.serviceId,
                    onRequestCameraPermission = {
                        sharedViewModel.requestPermission(
                            ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.PERMISSION_CAMERA
                        )
                    },
                    onOpenImageSelect = {
                        sharedViewModel.openImageSelect()
                    },
                    onOpenCropImage = { uri ->
                        sharedViewModel.openCropImage(uri)
                    }
                )
            }
        }
    }
}
