package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import android.net.Uri
sealed interface CropImageUiEvent {
    data class ServiceIdChanged(val serviceId: String?) : CropImageUiEvent
    data class ImageSelected(val uri: Uri) : CropImageUiEvent
    data object AcceptClicked : CropImageUiEvent
    data object CropClicked : CropImageUiEvent
    data object SelectImageClicked : CropImageUiEvent
    data object DialogDismissed : CropImageUiEvent
    data object DialogConfirmed : CropImageUiEvent
}
sealed interface CropImageUiEffect {
    data class ShowToast(val messageResId: Int) : CropImageUiEffect
    data class RequestCropImage(val uri: Uri?) : CropImageUiEffect
    data object RequestImageSelect : CropImageUiEffect
    data object NavigateBack : CropImageUiEffect
    data class OpenCropImageCustomDialog(
        val title: String,
        val description: String,
        val confirmText: String = "OK",
        val dismissText: String? = null
    ) : CropImageUiEffect
}
