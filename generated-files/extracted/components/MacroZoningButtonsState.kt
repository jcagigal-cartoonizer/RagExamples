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
// # Block 477-4: import androidx.compose.ui.graphics.Color
data class MacroZoningButtonsState(
    val onStopVisible: Boolean = false,
    val onZoneVisible: Boolean = false,
    val hiredVisible: Boolean = false,
    val tripsVisible: Boolean = false,
    val hiredLabel: String = "Hired",
    val tripsLabel: String = "Trips",
    val placeholderText: String = "Sort by",
    val activeOrder: OrderOptions = OrderOptions.NONE,
    val pendingButton: MacroZoningActionButtonState = MacroZoningActionButtonState.Pending(),
    val preReservationButton: MacroZoningActionButtonState = MacroZoningActionButtonState.PreReservation()
)
sealed class MacroZoningActionButtonState(
    open val enabled: Boolean,
    open val visible: Boolean,
    open val backgroundColor: Color,
    open val contentColor: Color,
    open val disabledBackgroundColor: Color,
    open val disabledContentColor: Color
) {
    data class Pending(
        override val enabled: Boolean = false,
        override val visible: Boolean = true,
        override val backgroundColor: Color = Color(0xFF1976D2), // blue
        override val contentColor: Color = Color.White,
        override val disabledBackgroundColor: Color = Color(0xFFB0BEC5),
        override val disabledContentColor: Color = Color.White
    ) : MacroZoningActionButtonState(
        enabled, visible, backgroundColor, contentColor, disabledBackgroundColor, disabledContentColor
    )
    data class PreReservation(
        override val enabled: Boolean = false,
        override val visible: Boolean = false,
        override val backgroundColor: Color = Color(0xFF1976D2),
        override val contentColor: Color = Color.White,
        override val disabledBackgroundColor: Color = Color(0xFFB0BEC5),
        override val disabledContentColor: Color = Color.White
    ) : MacroZoningActionButtonState(
        enabled, visible, backgroundColor, contentColor, disabledBackgroundColor, disabledContentColor
    )
}
