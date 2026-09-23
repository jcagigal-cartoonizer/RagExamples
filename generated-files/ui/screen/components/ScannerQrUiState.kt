package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ScannerQrDialogButtonType
import ifac.td.taxi.ui.screen.components.ScannerQrButtonsState = ScannerQrButtonsState
import ifac.td.taxi.ui.screen.components.ScannerQrCustomDialogModel
import ifac.td.taxi.ui.screen.components.ScannerQrCustomDialogState
import ifac.td.taxi.ui.screen.components.ScannerQrUiState
import ifac.td.taxi.ui.screen.components.ScannerQrResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 6-1: import ifac.td.taxi.domain.model.Trip
data class ScannerQrUiState(
    val serviceId: String? = null,
    val isLoading: Boolean = false,
    val dialogState: ScannerQrCustomDialogState? = null,
    val buttonsState: ScannerQrButtonsState = ScannerQrButtonsState()
)
data class ScannerQrCustomDialogState(
    val model: ScannerQrCustomDialogModel,
    val visible: Boolean = true
)
data class ScannerQrCustomDialogModel(
    val title: String,
    val description: String,
    val buttons: List<ScannerQrDialogButtonType>
)
enum class ScannerQrDialogButtonType {
    FRONT_CAMERA,
    BACK_CAMERA
}
sealed interface ScannerQrUiEvent {
    data class ServiceIdLoaded(val serviceId: String?) : ScannerQrUiEvent
    data class ScannerQrResult(val rawQr: String) : ScannerQrUiEvent
    data object CancelClicked : ScannerQrUiEvent
    data object ScannerClicked : ScannerQrUiEvent
    data object DialogDismissed : ScannerQrUiEvent
    data class DialogButtonClicked(val button: ScannerQrDialogButtonType) : ScannerQrUiEvent
}
sealed interface ScannerQrUiEffect {
    data class ShowCameraDialog(val model: ScannerQrCustomDialogModel) : ScannerQrUiEffect
    data class OpenScanner(val cameraPosition: Int) : ScannerQrUiEffect
    data object NavigateBack : ScannerQrUiEffect
    data object NavigateHome : ScannerQrUiEffect
    data object EnableScannerButton : ScannerQrUiEffect
}
