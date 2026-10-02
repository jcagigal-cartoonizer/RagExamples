package ifac.td.taxi.compose.navigation
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
