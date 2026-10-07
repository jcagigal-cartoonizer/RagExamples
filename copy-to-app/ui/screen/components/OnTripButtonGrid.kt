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
import ifac.td.taxi.ui.screen.OnTripScreen
// # Block 584-5: import androidx.compose.foundation.background
@Composable
fun OnTripButtonGrid(
    buttons: OnTripButtonsState,
    locationAllowedByCentral: Boolean,
    hiredZoneExists: Boolean,
    shiftStatusCurrentStatus: Int?,
    dispatch: InfoDispatchModel?,
    roofLight: Boolean?,
    onFixedPrice: () -> Unit,
    onNotifications: () -> Unit,
    onZoning: () -> Unit,
    onNavigate: () -> Unit,
    onDispatchInfo: () -> Unit,
    onReceipts: () -> Unit,
    onMessages: () -> Unit,
    onCentral: () -> Unit,
    onClient: () -> Unit,
    onRoofLight: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OnTripActionButton(buttons.fixedPrice, "Fixed Price", onFixedPrice)
        OnTripActionButton(buttons.notifications, "Notifications", onNotifications)
        OnTripActionButton(buttons.zoning, "Zoning", onZoning)
        OnTripActionButton(buttons.navigate, "Navigate", onNavigate)
        OnTripActionButton(buttons.dispatchInfo, "Dispatch Info", onDispatchInfo)
        OnTripActionButton(buttons.receipts, "Receipts", onReceipts)
        OnTripActionButton(buttons.messages, "Messages", onMessages)
        OnTripActionButton(buttons.central, "Central", onCentral)
        OnTripActionButton(buttons.client, "Client", onClient)
        OnTripActionButton(buttons.roofLight, "Roof Light", onRoofLight)
    }
}
@Composable
fun OnTripActionButton(
    state: OnTripButtonState,
    fallbackLabel: String,
    onClick: () -> Unit
) {
    if (!state.visible) return
    val bg = OnTripButtonStyles.backgroundColor(state.background)
    val enabled = state.style == ButtonStyle.Enabled
    val alpha = OnTripButtonStyles.alpha(state.style)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .alpha(alpha),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            disabledContainerColor = bg,
            contentColor = OnTripButtonStyles.white,
            disabledContentColor = OnTripButtonStyles.disabledTint
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = if (state.text.isNotBlank()) state.text else fallbackLabel)
    }
}
