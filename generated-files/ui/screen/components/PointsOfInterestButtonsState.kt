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
// # Block 443-3: import androidx.compose.runtime.Immutable
@Immutable
data class PointsOfInterestButtonsState(
    val cancelVisible: Boolean = true,
    val cancelEnabled: Boolean = true,
    val cancelText: String = "Cancel",
    val cancelStyle: ComposeButtonStyle = ComposeButtonStyle.ghost(),
    val searchVisible: Boolean = true,
    val searchEnabled: Boolean = true,
    val searchText: String = "Search",
    val searchStyle: ComposeButtonStyle = ComposeButtonStyle.primary(),
) {
    companion object {
        fun default() = PointsOfInterestButtonsState()
    }
}
@Immutable
data class ComposeButtonStyle(
    val containerColor: Long,
    val contentColor: Long,
    val disabledContainerColor: Long,
    val disabledContentColor: Long,
    val borderColor: Long? = null,
    val cornerRadiusDp: Int = 12,
    val strokeWidthDp: Int = 1
) {
    companion object {
        fun primary() = ComposeButtonStyle(
            containerColor = 0xFF1976D2,
            contentColor = 0xFFFFFFFF,
            disabledContainerColor = 0xFF90CAF9,
            disabledContentColor = 0xCCFFFFFF
        )
        fun ghost() = ComposeButtonStyle(
            containerColor = 0x00000000,
            contentColor = 0xFF1976D2,
            disabledContainerColor = 0x00000000,
            disabledContentColor = 0x66000000,
            borderColor = 0xFF1976D2
        )
    }
}
