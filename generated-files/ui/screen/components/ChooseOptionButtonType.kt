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
// # Block 50-2: import androidx.compose.runtime.Immutable
enum class ChooseOptionButtonType {
    CANCEL,
    ACCEPT
}
@Immutable
data class ChooseOptionButtonStyle(
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color? = null,
    val visible: Boolean = true,
)
@Immutable
data class ChooseOptionButtonsState(
    val cancel: ChooseOptionButtonStyle = ChooseOptionButtonStyle(
        backgroundColor = Color(0xFFE0E0E0),
        contentColor = Color(0xFF1C1C1C),
        visible = true
    ),
    val accept: ChooseOptionButtonStyle = ChooseOptionButtonStyle(
        backgroundColor = Color(0xFF2E7D32),
        contentColor = Color.White,
        visible = true
    ),
) {
    companion object {
        /**
         * Mirrors the fragment dialog:
         * buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
         */
        fun confirmCancelAccept(): ChooseOptionButtonsState = ChooseOptionButtonsState(
            cancel = ChooseOptionButtonStyle(
                backgroundColor = Color(0xFFE0E0E0),
                contentColor = Color(0xFF1C1C1C),
                visible = true
            ),
            accept = ChooseOptionButtonStyle(
                backgroundColor = Color(0xFF2E7D32),
                contentColor = Color.White,
                visible = true
            )
        )
    }
}
But exposes state/effects cleanly for Compose.
