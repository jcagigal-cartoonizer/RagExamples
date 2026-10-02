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
// # Block 11-1: import android.net.Uri
data class CropImageUiState(
    val imageUri: Uri? = null,
    val serviceId: String? = null,
    val isUploading: Boolean = false,
    val showConfirmSendDialog: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessageResId: Int? = null
)
sealed interface CropImageUiEvent {
    data class ImageSelected(val uri: Uri?) : CropImageUiEvent
    data object ClickCrop : CropImageUiEvent
    data object ClickSelectImage : CropImageUiEvent
    data object ClickAccept : CropImageUiEvent
    data object DismissDialog : CropImageUiEvent
    data object ConfirmSend : CropImageUiEvent
    data object PermissionGranted : CropImageUiEvent
}
sealed interface CropImageUiEffect {
    data object RequestCameraPermission : CropImageUiEffect
    data object OpenImageSelector : CropImageUiEffect
    data object OpenCropper : CropImageUiEffect
    data class ShowToast(val messageResId: Int) : CropImageUiEffect
    data object NavigateBack : CropImageUiEffect
    data class OpenScannerQr(val dispatchNumber: Long) : CropImageUiEffect
}
