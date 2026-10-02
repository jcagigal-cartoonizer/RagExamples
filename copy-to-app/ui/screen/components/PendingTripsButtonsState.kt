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
// # Block 329-3: import androidx.compose.ui.graphics.Color
data class PendingTripsButtonsState(
    val confirmVisible: Boolean = true,
    val cancelVisible: Boolean = true,
    val confirmEnabled: Boolean = true,
    val cancelEnabled: Boolean = true,
    val confirmText: String = "Accept",
    val cancelText: String = "Cancel",
    val confirmContainerColor: Color = Color(0xFF2E7D32),
    val confirmContentColor: Color = Color.White,
    val cancelContainerColor: Color = Color(0xFFE0E0E0),
    val cancelContentColor: Color = Color(0xFF1F1F1F),
    val confirmDisabledContainerColor: Color = Color(0xFFBDBDBD),
    val confirmDisabledContentColor: Color = Color(0xFF757575),
    val cancelDisabledContainerColor: Color = Color(0xFFF5F5F5),
    val cancelDisabledContentColor: Color = Color(0xFF9E9E9E),
    val dialogButtonsState: PendingTripsDialogButtonsState = PendingTripsDialogButtonsState.default()
) {
    companion object {
        fun default() = PendingTripsButtonsState()
    }
}
data class PendingTripsDialogButtonsState(
    val cancelVisible: Boolean = true,
    val acceptVisible: Boolean = true,
    val cancelEnabled: Boolean = true,
    val acceptEnabled: Boolean = true,
    val cancelContainerColor: Color = Color(0xFFE0E0E0),
    val cancelContentColor: Color = Color(0xFF1F1F1F),
    val acceptContainerColor: Color = Color(0xFF2E7D32),
    val acceptContentColor: Color = Color.White
) {
    companion object {
        fun default() = PendingTripsDialogButtonsState()
    }
}
enum class PendingTripsScreenButtonAction {
    CANCEL,
    ACCEPT
}
enum class PendingTripsDialogButton {
    CANCEL,
    ACCEPT
}
