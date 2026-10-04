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
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.SplashScreenScreen
// # Block 286-3: import androidx.compose.ui.graphics.Color
data class SplashScreenButtonsState(
    val isVisible: Boolean = false, // original splash had no buttons shown
    val primaryButtonVisible: Boolean = false,
    val secondaryButtonVisible: Boolean = false,
    val tertiaryButtonVisible: Boolean = false,
    val primaryEnabled: Boolean = true,
    val secondaryEnabled: Boolean = true,
    val tertiaryEnabled: Boolean = true,
    val primaryButtonStyle: SplashButtonStyle = SplashButtonStyle.Primary,
    val secondaryButtonStyle: SplashButtonStyle = SplashButtonStyle.Secondary,
    val tertiaryButtonStyle: SplashButtonStyle = SplashButtonStyle.Tertiary
)
enum class SplashButtonStyle {
    Primary, Secondary, Tertiary
}
data class SplashButtonColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
    val border: Color
)
fun SplashButtonStyle.colors(): SplashButtonColors = when (this) {
    SplashButtonStyle.Primary -> SplashButtonColors(
        container = Color(0xFF1E88E5),
        content = Color.White,
        disabledContainer = Color(0xFF90CAF9),
        disabledContent = Color(0x80FFFFFF),
        border = Color.Transparent
    )
    SplashButtonStyle.Secondary -> SplashButtonColors(
        container = Color.Transparent,
        content = Color(0xFF1E88E5),
        disabledContainer = Color.Transparent,
        disabledContent = Color(0x801E88E5),
        border = Color(0xFF1E88E5)
    )
    SplashButtonStyle.Tertiary -> SplashButtonColors(
        container = Color.Transparent,
        content = Color(0xFF757575),
        disabledContainer = Color.Transparent,
        disabledContent = Color(0x80757575),
        border = Color(0xFF757575)
    )
}
But it satisfies the state-holder requirement and can be extended to match XML-driven button layouts.
