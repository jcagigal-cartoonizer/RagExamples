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
// # Block 168-3: import androidx.compose.runtime.Immutable
@Immutable
data class ClosedPartialButtonsState(
    val cancel: ClosedPartialCustomDialogButtonVisualState = ClosedPartialCustomDialogButtonVisualState.primary(),
    val print: ClosedPartialCustomDialogButtonVisualState = ClosedPartialCustomDialogButtonVisualState.primary(),
    val totalizers: ClosedPartialCustomDialogButtonVisualState = ClosedPartialCustomDialogButtonVisualState.hidden(),
) {
    companion object {
        fun from(
            isTaximeterConnected: Boolean,
            hasTotalizers: Boolean,
            currentStatus: String?
        ): ClosedPartialButtonsState {
            val shouldShowTotalizers =
                currentStatus != ifConstants.STATE_DISCONNECTED && isTaximeterConnected
            val totalizersState = if (shouldShowTotalizers) {
                if (isTaximeterConnected && hasTotalizers) {
                    ClosedPartialCustomDialogButtonVisualState.enabled()
                } else {
                    ClosedPartialCustomDialogButtonVisualState.disabled()
                }
            } else {
                ClosedPartialCustomDialogButtonVisualState.hidden()
            }
            return ClosedPartialButtonsState(
                cancel = ClosedPartialCustomDialogButtonVisualState.primary(),
                print = ClosedPartialCustomDialogButtonVisualState.primary(),
                totalizers = totalizersState
            )
        }
    }
}
@Immutable
data class ClosedPartialCustomDialogButtonVisualState(
    val visible: Boolean,
    val enabled: Boolean,
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val alpha: Float = 1f,
) {
    companion object {
        fun primary() = ClosedPartialCustomDialogButtonVisualState(
            visible = true,
            enabled = true,
            backgroundColor = Color(0xFF1976D2),
            contentColor = Color.White,
            borderColor = Color(0xFF1976D2)
        )
        fun enabled() = ClosedPartialCustomDialogButtonVisualState(
            visible = true,
            enabled = true,
            backgroundColor = Color(0xFF2E7D32),
            contentColor = Color.White,
            borderColor = Color(0xFF2E7D32)
        )
        fun disabled() = ClosedPartialCustomDialogButtonVisualState(
            visible = true,
            enabled = false,
            backgroundColor = Color(0xFFE0E0E0),
            contentColor = Color(0xFF9E9E9E),
            borderColor = Color(0xFFBDBDBD),
            alpha = 1f
        )
        fun hidden() = ClosedPartialCustomDialogButtonVisualState(
            visible = false,
            enabled = false,
            backgroundColor = Color.Transparent,
            contentColor = Color.Transparent,
            borderColor = Color.Transparent,
            alpha = 0f
        )
    }
}
