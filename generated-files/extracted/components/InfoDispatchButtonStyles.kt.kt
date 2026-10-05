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
import ifac.td.taxi.ui.screen.InfoDispatchScreen
// # Block 87-2: import androidx.compose.foundation.BorderStroke
object InfoDispatchButtonStyles {
    val Green = Color(0xFF2E7D32)
    val Red = Color(0xFFC62828)
    val Orange = Color(0xFFEF6C00)
    val Disabled = Color(0xFF9E9E9E)
    val White = Color.White
    val DarkText = Color(0xFF1B1B1B)
    fun backgroundFor(background: ButtonBackground): Color = when (background) {
        ButtonBackground.Green -> Green
        ButtonBackground.Red -> Red
        ButtonBackground.Orange -> Orange
        ButtonBackground.Disabled -> Disabled
    }
    fun textColorFor(background: ButtonBackground): Color = when (background) {
        ButtonBackground.Disabled -> Color.White.copy(alpha = 0.8f)
        else -> White
    }
}
@Composable
fun InfoDispatchActionButton(
    modifier: Modifier = Modifier,
    state: InfoDispatchButtonUiState.Visible,
    onClick: () -> Unit
) {
    val bg = InfoDispatchButtonStyles.backgroundFor(state.background)
    Button(
        onClick = onClick,
        enabled = state.enabled && !state.loading,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            disabledContainerColor = bg.copy(alpha = 0.45f),
            contentColor = state.textColor
        ),
        shape = RoundedCornerShape(12.dp),
        border = state.borderColor?.let { BorderStroke(1.dp, it) }
    ) {
        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = state.textColor
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(state.text)
    }
}
