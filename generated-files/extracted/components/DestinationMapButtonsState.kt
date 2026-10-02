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
// # Block 289-3: import androidx.compose.ui.graphics.Color
data class DestinationMapButtonsState(
    val showPrimaryButton: Boolean = true,
    val showSecondaryButton: Boolean = true,
    val showTertiaryButton: Boolean = false,
    val primaryEnabled: Boolean = true,
    val secondaryEnabled: Boolean = true,
    val tertiaryEnabled: Boolean = true,
    val primaryText: String = "Open",
    val secondaryText: String = "Dialog",
    val tertiaryText: String = "Back",
    val primaryContainerColor: Color = Color(0xFF1E88E5),
    val primaryContentColor: Color = Color.White,
    val secondaryContainerColor: Color = Color(0xFFFFFFFF),
    val secondaryContentColor: Color = Color(0xFF1E88E5),
    val tertiaryContainerColor: Color = Color(0xFFF5F5F5),
    val tertiaryContentColor: Color = Color(0xFF222222),
    val isRouteVisible: Boolean = false
)
