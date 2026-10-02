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
// # Block 39-2: import androidx.compose.runtime.Immutable
@Immutable
data class LoginUserRedSysButtonsState(
    val cancelVisible: Boolean = true,
    val acceptVisible: Boolean = true,
    val acceptEnabled: Boolean = true,
    val acceptLoading: Boolean = false,
    val acceptBackground: Color = Color(0xFF2E7D32), // green
    val acceptContentColor: Color = Color.White,
    val cancelBackground: Color = Color(0xFFE0E0E0),
    val cancelContentColor: Color = Color.Black
) {
    companion object {
        fun enabledGreen() = LoginUserRedSysButtonsState(
            acceptEnabled = true,
            acceptLoading = false,
            acceptBackground = Color(0xFF2E7D32),
            acceptContentColor = Color.White
        )
        fun loading() = LoginUserRedSysButtonsState(
            acceptEnabled = false,
            acceptLoading = true,
            acceptBackground = Color(0xFF2E7D32),
            acceptContentColor = Color.White
        )
        fun disabled() = LoginUserRedSysButtonsState(
            acceptEnabled = false,
            acceptLoading = false,
            acceptBackground = Color(0xFFA5D6A7),
            acceptContentColor = Color.White
        )
    }
}
