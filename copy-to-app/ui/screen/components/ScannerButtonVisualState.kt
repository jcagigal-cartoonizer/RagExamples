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
// # Block 51-2: import androidx.compose.ui.graphics.Color
enum class ScannerScannerQrButtonButtonVisualState {
    Enabled,
    Loading,
    Disabled
}
data class ScannerQrButtonsState(
    val cancel: ScannerButtonSpec = ScannerButtonSpec.cancel(),
    val scanner: ScannerButtonSpec = ScannerButtonSpec.scanner()
)
data class ScannerButtonSpec(
    val text: String,
    val enabled: Boolean,
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color? = null,
    val loading: Boolean = false,
    val visible: Boolean = true
) {
    companion object {
        fun cancel() = ScannerButtonSpec(
            text = "Cancel",
            enabled = true,
            backgroundColor = Color(0xFFE0E0E0),
            contentColor = Color(0xFF1F1F1F),
            borderColor = null,
            loading = false,
            visible = true
        )
        fun scanner() = ScannerButtonSpec(
            text = "Scanner",
            enabled = true,
            backgroundColor = Color(0xFF1976D2),
            contentColor = Color.White,
            borderColor = null,
            loading = false,
            visible = true
        )
        fun scannerLoading() = ScannerButtonSpec(
            text = "Scanner",
            enabled = false,
            backgroundColor = Color(0xFF90CAF9),
            contentColor = Color.White,
            borderColor = null,
            loading = true,
            visible = true
        )
    }
}
fun ScannerQrButtonsState.withScannerLoading(): ScannerQrButtonsState =
    copy(scanner = ScannerButtonSpec.scannerLoading())
fun ScannerQrButtonsState.withScannerEnabled(): ScannerQrButtonsState =
    copy(scanner = ScannerButtonSpec.scanner())
