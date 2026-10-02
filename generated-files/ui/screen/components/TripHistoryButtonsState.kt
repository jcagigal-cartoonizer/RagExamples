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
// # Block 263-3: import androidx.compose.runtime.Immutable
@Immutable
data class TripHistoryButtonsState(
    val backVisible: Boolean = true,
    val allVisible: Boolean = true,
    val deleteVisible: Boolean = true,
    val backEnabled: Boolean = true,
    val allEnabled: Boolean = true,
    val deleteEnabled: Boolean = true,
    val backColors: ButtonColorTokens = ButtonColorTokens.primary(),
    val allColors: ButtonColorTokens = ButtonColorTokens.secondary(),
    val deleteColors: ButtonColorTokens = ButtonColorTokens.danger(),
) {
    companion object {
        fun from(
            hasTrips: Boolean,
            hasSelection: Boolean
        ): TripHistoryButtonsState {
            return TripHistoryButtonsState(
                backVisible = true,
                allVisible = hasTrips,
                deleteVisible = hasTrips,
                backEnabled = true,
                allEnabled = hasTrips,
                deleteEnabled = hasSelection,
                backColors = ButtonColorTokens.primary(),
                allColors = ButtonColorTokens.secondary(enabled = hasTrips),
                deleteColors = ButtonColorTokens.danger(enabled = hasSelection),
            )
        }
    }
}
@Immutable
data class ButtonColorTokens(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
) {
    companion object {
        fun primary(enabled: Boolean = true) = ButtonColorTokens(
            container = Color(0xFF1976D2),
            content = Color.White,
            disabledContainer = Color(0xFFB0BEC5),
            disabledContent = Color(0xFFFAFAFA),
        )
        fun secondary(enabled: Boolean = true) = ButtonColorTokens(
            container = Color(0xFF455A64),
            content = Color.White,
            disabledContainer = Color(0xFFB0BEC5),
            disabledContent = Color(0xFFFAFAFA),
        )
        fun danger(enabled: Boolean = true) = ButtonColorTokens(
            container = Color(0xFFD32F2F),
            content = Color.White,
            disabledContainer = Color(0xFFB0BEC5),
            disabledContent = Color(0xFFFAFAFA),
        )
    }
}
> If you have exact XML colors, replace the hardcoded colors above with your app’s color values. This structure is what you asked for: a full state holder mirroring visibility/color behavior.
