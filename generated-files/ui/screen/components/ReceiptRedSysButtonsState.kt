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
// # Block 48-2: import androidx.compose.runtime.Immutable
@Immutable
data class ReceiptRedSysButtonsState(
    val showRefund: Boolean = false,
    val showPrint: Boolean = false,
    val refundEnabled: Boolean = false,
    val printEnabled: Boolean = false,
    val refundButtonColors: ButtonColorsState = ButtonColorsState(
        container = Color(0xFFE53935),
        content = Color.White,
        disabledContainer = Color(0xFFFFCDD2),
        disabledContent = Color(0xFF8D6E63)
    ),
    val printButtonColors: ButtonColorsState = ButtonColorsState(
        container = Color(0xFF1E88E5),
        content = Color.White,
        disabledContainer = Color(0xFFBBDEFB),
        disabledContent = Color(0xFF607D8B)
    )
) {
    companion object {
        fun forOperation(operation: RedSysOperation): ReceiptRedSysButtonsState {
            val canRefund = operation.operationType == AUTHORIZATION && operation.result != DENIED
            val canPrint = (operation.operationType == AUTHORIZATION && operation.result != DENIED) ||
                (operation.operationType == REFUND && operation.refundResponse != null)
            return ReceiptRedSysButtonsState(
                showRefund = canRefund,
                showPrint = canPrint,
                refundEnabled = canRefund,
                printEnabled = canPrint
            )
        }
    }
}
@Immutable
data class ButtonColorsState(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color
)
