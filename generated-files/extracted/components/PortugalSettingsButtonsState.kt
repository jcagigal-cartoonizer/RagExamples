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
// # Block 316-4: import androidx.compose.runtime.Immutable
@Immutable
data class PortugalSettingsButtonsState(
    val acceptEnabled: Boolean,
    val acceptVisible: Boolean = true,
    val cancelVisible: Boolean = true,
    val changePinVisible: Boolean = true,
    val acceptContainerColor: Color,
    val acceptContentColor: Color,
    val cancelContainerColor: Color,
    val cancelContentColor: Color,
    val changePinContainerColor: Color,
    val changePinContentColor: Color,
) {
    companion object {
        fun fromUiState(state: PortugalSettingsUiState): PortugalSettingsButtonsState {
            // This mirrors typical XML behavior:
            // - Accept always visible
            // - Cancel always visible
            // - Change PIN visible
            // - When reset hash is active, Accept remains enabled but may be blocked by validation
            return PortugalSettingsButtonsState(
                acceptEnabled = true,
                acceptVisible = true,
                cancelVisible = true,
                changePinVisible = true,
                acceptContainerColor = PortugalButtonColors.AcceptContainer,
                acceptContentColor = PortugalButtonColors.AcceptContent,
                cancelContainerColor = PortugalButtonColors.CancelContainer,
                cancelContentColor = PortugalButtonColors.CancelContent,
                changePinContainerColor = PortugalButtonColors.ChangePinContainer,
                changePinContentColor = PortugalButtonColors.ChangePinContent,
            )
        }
    }
}
object PortugalButtonColors {
    val AcceptContainer = Color(0xFF2E7D32)
    val AcceptContent = Color.White
    val CancelContainer = Color(0xFF757575)
    val CancelContent = Color.White
    val ChangePinContainer = Color(0xFF1565C0)
    val ChangePinContent = Color.White
}
