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
// # Block 6-1: import ifac.td.taxi.domain.model.Trip
data class ScannerQrUiState(
    val serviceId: String? = null,
    val isLoading: Boolean = false,
    val dialogState: ScannerQrButtonCustomDialogState? = null,
    val buttonsState: ScannerQrButtonsState = ScannerQrButtonsState()
)
data class ScannerQrButtonCustomDialogState(
    val model: ScannerQrButtonCustomDialogModel,
    val visible: Boolean = true
)
data class ScannerQrButtonCustomDialogModel(
    val title: String,
    val description: String,
    val buttons: List<ScannerQrButtonDialogButtonType>
)
enum class ScannerQrButtonDialogButtonType {
    FRONT_CAMERA,
    BACK_CAMERA
}
sealed interface ScannerQrUiEvent {
    data class ServiceIdLoaded(val serviceId: String?) : ScannerQrUiEvent
    data class ScannerQrResult(val rawQr: String) : ScannerQrUiEvent
    data object CancelClicked : ScannerQrUiEvent
    data object ScannerClicked : ScannerQrUiEvent
    data object DialogDismissed : ScannerQrUiEvent
    data class DialogButtonClicked(val button: ScannerQrButtonDialogButtonType) : ScannerQrUiEvent
}
sealed interface ScannerQrUiEffect {
    data class ShowCameraDialog(val model: ScannerQrButtonCustomDialogModel) : ScannerQrUiEffect
    data class OpenScanner(val cameraPosition: Int) : ScannerQrUiEffect
    data object NavigateBack : ScannerQrUiEffect
    data object NavigateHome : ScannerQrUiEffect
    data object EnableScannerButton : ScannerQrUiEffect
}
