package ifac.td.taxi.ui.screen
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
import ifac.td.taxi.ui.screen.WelcomeScreen
// # Block 126-2: import androidx.compose.foundation.layout.*
@Composable
fun WelcomeScreen(
    uiState: WelcomeUiState,
    onAction: (WelcomeUiAction) -> Unit,
) {
    val buttons = uiState.buttons
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WelcomeButton(
            text = buttons.start.text,
            enabled = buttons.start.enabled,
            loading = buttons.start.loading,
            color = buttons.start.color,
            visible = buttons.start.visible,
            onClick = { onAction(WelcomeUiAction.StartPressed) }
        )
        WelcomeButton(
            text = buttons.settings.text,
            enabled = buttons.settings.enabled,
            loading = buttons.settings.loading,
            color = buttons.settings.color,
            visible = buttons.settings.visible,
            onClick = { onAction(WelcomeUiAction.SettingsPressed) }
        )
        WelcomeButton(
            text = buttons.shifts.text,
            enabled = buttons.shifts.enabled,
            loading = buttons.shifts.loading,
            color = buttons.shifts.color,
            visible = buttons.shifts.visible,
            onClick = { onAction(WelcomeUiAction.ShiftsPressed) }
        )
        WelcomeButton(
            text = buttons.statistics.text,
            enabled = buttons.statistics.enabled,
            loading = buttons.statistics.loading,
            color = buttons.statistics.color,
            visible = buttons.statistics.visible,
            onClick = { onAction(WelcomeUiAction.StatisticsPressed) }
        )
        WelcomeButton(
            text = buttons.configuration.text,
            enabled = buttons.configuration.enabled,
            loading = buttons.configuration.loading,
            color = buttons.configuration.color,
            visible = buttons.configuration.visible,
            onClick = { onAction(WelcomeUiAction.ConfigurationPressed) }
        )
        WelcomeButton(
            text = buttons.permissions.text,
            enabled = buttons.permissions.enabled,
            loading = buttons.permissions.loading,
            color = buttons.permissions.color,
            visible = buttons.permissions.visible,
            onClick = { onAction(WelcomeUiAction.PermissionsPressed) }
        )
        WelcomeButton(
            text = buttons.partials.text,
            enabled = buttons.partials.enabled,
            loading = buttons.partials.loading,
            color = buttons.partials.color,
            visible = buttons.partials.visible,
            onClick = { onAction(WelcomeUiAction.PartialsPressed) }
        )
        WelcomeButton(
            text = buttons.exit.text,
            enabled = buttons.exit.enabled,
            loading = buttons.exit.loading,
            color = buttons.exit.color,
            visible = buttons.exit.visible,
            onClick = { onAction(WelcomeUiAction.ExitPressed) }
        )
    }
}
