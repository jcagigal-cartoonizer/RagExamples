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
// # Block 5-1: import androidx.annotation.DrawableRes
@Immutable
data class InfoDispatchUiState(
    val dispatch: InfoDispatchModelUi? = null,
    val extraData: DispatchExtraDataUi? = null,
    val bravoConfig: BravoConfigurationUi? = null,
    val meetingSignColors: Pair<Int, Int> = 0 to 0,
    val noClientEnabled: Boolean? = null,
    val bridgeCallState: Int = 0,
    val customerCallAvailable: Boolean = true,
    val customerCallButtonAvailable: Boolean = true,
    val canMakeCalls: Boolean = true,
    val taximeterConnected: Boolean = false,
    val shiftIsManual: Boolean = false,
    val currentStatus: Int? = null,
    val showReturnDispatchButton: Boolean = false,
    val isCurrentBluetoothITop: Boolean = false,
    val hasShowedConcertedPriceDialog: Boolean = false,
    val timerText: String = "",
    val buttons: InfoDispatchButtonsState = InfoDispatchButtonsState()
)
@Immutable
data class InfoDispatchButtonsState(
    val notification: InfoDispatchButtonUiState = InfoDispatchButtonUiState.Hidden,
    val voiceCall: InfoDispatchButtonUiState = InfoDispatchButtonUiState.Hidden,
    val print: InfoDispatchButtonUiState = InfoDispatchButtonUiState.Hidden,
    val noClient: InfoDispatchButtonUiState = InfoDispatchButtonUiState.Hidden,
    val returnTrip: InfoDispatchButtonUiState = InfoDispatchButtonUiState.Hidden,
    val navigate: InfoDispatchButtonUiState = InfoDispatchButtonUiState.Hidden,
)
sealed interface InfoDispatchUiEffect {
    data object NavigateToDirections : InfoDispatchUiEffect
    data object NavigateToHome : InfoDispatchUiEffect
    data class NavigateToMeetingSign(val textColor: Int, val backgroundColor: Int) : InfoDispatchUiEffect
    data object RequestPhonePermission : InfoDispatchUiEffect
    data class ShowToast(@StringRes val messageRes: Int) : InfoDispatchUiEffect
    data class OpenDialog(val dialog: InfoDispatchDialogState) : InfoDispatchUiEffect
    data object CloseDialog : InfoDispatchUiEffect
    data class OpenExternalPhoneCall(val phoneNumber: String) : InfoDispatchUiEffect
}
@Immutable
data class InfoDispatchDialogState(
    val title: String,
    val description: String? = null,
    @DrawableRes val iconRes: Int? = null,
    val buttons: List<InfoDispatchDialogButton> = listOf(InfoDispatchDialogButton.Cancel, InfoDispatchDialogButton.Accept),
    val dismissOnOutsideTap: Boolean = true,
    val type: InfoDispatchDialogType = InfoDispatchDialogType.Default,
    val payload: String? = null
)
sealed interface InfoDispatchDialogButton {
    data object Cancel : InfoDispatchDialogButton
    data object Accept : InfoDispatchDialogButton
    data object AtDoor : InfoDispatchDialogButton
    data object RiderInCab : InfoDispatchDialogButton
}
enum class InfoDispatchDialogType {
    Default, Warning, Confirm, Notification
}
sealed class InfoDispatchButtonUiState {
    data object Hidden : InfoDispatchButtonUiState()
    data class Visible(
        val enabled: Boolean,
        val text: String,
        val kind: ButtonKind,
        val background: ButtonBackground,
        val textColor: androidx.compose.ui.graphics.Color,
        val borderColor: androidx.compose.ui.graphics.Color? = null,
        val loading: Boolean = false
    ) : InfoDispatchButtonUiState()
}
enum class ButtonKind { Notification, VoiceCall, Print, NoClient, Return, Navigate }
enum class ButtonBackground { Green, Red, Orange, Disabled }
