package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.CropImageButtonsState
import ifac.td.taxi.ui.screen.components.CropImageUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 9-1: import android.net.Uri
data class CropImageUiState(
    val serviceId: String? = null,
    val imageUri: Uri? = null,
    val isUploading: Boolean = false,
    val dialogState: CropImageCustomDialogState = CropImageCustomDialogState.Hidden,
    val buttonsState: CropImageButtonsState = CropImageButtonsState.default()
)
sealed interface CropImageCustomDialogState {
    data object Hidden : CropImageCustomDialogState
    data class Visible(
        val title: String,
        val description: String,
        val confirmText: String = "OK",
        val dismissText: String? = null
    ) : CropImageCustomDialogState
}
