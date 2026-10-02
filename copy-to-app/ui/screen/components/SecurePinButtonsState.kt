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
// # Block 105-2: import androidx.compose.runtime.Immutable
@Immutable
data class SecurePinButtonsState(
    val accept: SecurePinButtonState = SecurePinButtonState(),
    val cancel: SecurePinButtonState = SecurePinButtonState()
) {
    companion object {
        fun default(
            hasSecurePin: Boolean? = null,
            pin: String = "",
            pinRepeat: String = ""
        ): SecurePinButtonsState {
            val isReady = when {
                pin.isEmpty() && pinRepeat.isEmpty() -> true
                else -> false
            }
            return SecurePinButtonsState(
                accept = SecurePinButtonState(
                    visible = true,
                    enabled = hasSecurePin != null,
                    textRes = androidx.compose.runtime.staticCompositionLocalOf { 0 },
                ),
                cancel = SecurePinButtonState(
                    visible = true,
                    enabled = true,
                    textRes = androidx.compose.runtime.staticCompositionLocalOf { 0 },
                )
            )
        }
    }
}
@Immutable
data class SecurePinButtonState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val backgroundColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val borderColor: Color = Color.Unspecified,
)
// # Block 150-3: import androidx.annotation.StringRes
@Immutable
data class SecurePinButtonsState(
    val accept: SecurePinButtonState = SecurePinButtonState(
        textRes = R.string.accept,
        visible = true,
        enabled = true,
        style = SecurePinButtonStyle.Primary
    ),
    val cancel: SecurePinButtonState = SecurePinButtonState(
        textRes = R.string.cancel,
        visible = true,
        enabled = true,
        style = SecurePinButtonStyle.Secondary
    )
) {
    companion object {
        fun from(hasSecurePin: Boolean? = null): SecurePinButtonsState {
            return SecurePinButtonsState(
                accept = SecurePinButtonState(
                    textRes = R.string.accept,
                    visible = true,
                    enabled = hasSecurePin != null,
                    style = SecurePinButtonStyle.Primary
                ),
                cancel = SecurePinButtonState(
                    textRes = R.string.cancel,
                    visible = true,
                    enabled = true,
                    style = SecurePinButtonStyle.Secondary
                )
            )
        }
    }
}
@Immutable
data class SecurePinButtonState(
    @StringRes val textRes: Int,
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val style: SecurePinButtonStyle = SecurePinButtonStyle.Primary,
)
enum class SecurePinButtonStyle {
    Primary,
    Secondary,
    Danger,
    Disabled
}
