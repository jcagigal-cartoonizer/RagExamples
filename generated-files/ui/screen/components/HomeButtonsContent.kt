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
import ifac.td.taxi.ui.screen.HomeScreen
// # Block 471-4: import androidx.annotation.StringRes
@Composable
fun HomeButtonsContent(
    buttons: HomeButtonsState,
    onLocationClick: () -> Unit,
    onZoningClick: () -> Unit,
    onPendingClick: () -> Unit,
    onLocateStandClick: () -> Unit,
    onCentralClick: () -> Unit,
    onReceiptsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onRoofLightClick: () -> Unit,
    onDashboardClick: () -> Unit,
    onFixedPriceClick: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HomeButton(buttons.location, R.string.btn_location_on, onLocationClick)
        HomeButton(buttons.zoning, R.string.btn_location_on, onZoningClick)
        HomeButton(buttons.pending, R.string.btn_location_on, onPendingClick)
        HomeButton(buttons.locateStand, R.string.btn_locate_stop, onLocateStandClick)
        HomeButton(buttons.central, R.string.btn_location_on, onCentralClick)
        HomeButton(buttons.receipts, R.string.btn_location_on, onReceiptsClick)
        HomeButton(buttons.messages, R.string.btn_location_on, onMessagesClick)
        HomeButton(buttons.roofLight, R.string.btn_location_on, onRoofLightClick)
        if (buttons.dashboard.visible) HomeButton(buttons.dashboard, R.string.btn_location_on, onDashboardClick)
        if (buttons.fixedPrice.visible) HomeButton(buttons.fixedPrice, R.string.btn_location_on, onFixedPriceClick)
    }
}
@Composable
fun HomeButton(
    state: HomeComposeButtonState,
    @StringRes fallbackText: Int,
    onClick: () -> Unit,
) {
    if (!state.visible) return
    val bg = when (state.background) {
        ButtonColor.Blue -> Color(0xFF1565C0)
        ButtonColor.Red -> Color(0xFFD32F2F)
        ButtonColor.Green -> Color(0xFF2E7D32)
        ButtonColor.Orange -> Color(0xFFF57C00)
        ButtonColor.Gray -> Color(0xFF757575)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .clickable(enabled = state.enabled && !state.loading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (state.textRes != null) androidx.compose.ui.res.stringResource(state.textRes)
            else androidx.compose.ui.res.stringResource(fallbackText),
            color = state.textColor
        )
    }
}
fun HomeComposeButtonState.toEnabledIf(condition: Boolean, background: ButtonColor = this.background) =
    copy(enabled = condition, background = background)
fun HomeComposeButtonState.toVisible(visible: Boolean) = copy(visible = visible)
fun HomeComposeButtonState.toLoading() = copy(enabled = true, loading = true)
