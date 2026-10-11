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
import ifac.td.taxi.ui.screen.WelcomeScreen
// # Block 1-1: import androidx.annotation.StringRes
@Immutable
data class WelcomeUiState(
    val buttons: WelcomeButtonsState = WelcomeButtonsState(),
    val showPrivacyDialog: Boolean = false,
    val showExitDialog: Boolean = false,
    val showSecurePinDialog: Boolean = false,
    val securePinDestination: WelcomeViewModel.SecurePinDestination? = null,
    val securePinErrorMessageRes: Int? = null,
)
@Immutable
data class WelcomeButtonsState(
    val inicio: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.BLUE,
    ),
    val ajustes: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.BLUE,
    ),
    val turnos: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = false,
        loading = false,
        background = WelcomeButtonBackground.ORANGE,
    ),
    val estadisticas: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.BLUE,
    ),
    val configuracion: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.BLUE,
    ),
    val permisos: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.ORANGE,
    ),
    val parciales: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = false,
        loading = false,
        background = WelcomeButtonBackground.ORANGE,
    ),
    val salir: WelcomeButtonState = WelcomeButtonState(
        visible = true,
        enabled = true,
        loading = false,
        background = WelcomeButtonBackground.BLUE,
    ),
)
@Immutable
data class WelcomeButtonState(
    val visible: Boolean,
    val enabled: Boolean,
    val loading: Boolean,
    val background: WelcomeButtonBackground,
)
enum class WelcomeButtonBackground {
    BLUE,
    ORANGE
}
sealed interface WelcomeUiEvent {
    data object OnResume : WelcomeUiEvent
    data object OnStart : WelcomeUiEvent
    data object OnInicioClicked : WelcomeUiEvent
    data object OnAjustesClicked : WelcomeUiEvent
    data object OnTurnosClicked : WelcomeUiEvent
    data object OnEstadisticasClicked : WelcomeUiEvent
    data object OnConfiguracionClicked : WelcomeUiEvent
    data object OnPermisosClicked : WelcomeUiEvent
    data object OnParcialesClicked : WelcomeUiEvent
    data object OnSalirClicked : WelcomeUiEvent
    data object OnPrivacyAccepted : WelcomeUiEvent
    data object OnPrivacyDeclined : WelcomeUiEvent
    data class OnSecurePinConfirmed(val pin: String) : WelcomeUiEvent
    data object OnSecurePinDismissed : WelcomeUiEvent
    data object OnExitConfirmed : WelcomeUiEvent
}
sealed interface WelcomeUiEffect {
    data class Navigate(val directions: Any) : WelcomeUiEffect
    data class ShowToast(@StringRes val messageRes: Int) : WelcomeUiEffect
    data class OpenUrl(val url: String) : WelcomeUiEffect
    data object RequestPermissions : WelcomeUiEffect
    data object ConnectTaximeter : WelcomeUiEffect
    data object ExitApp : WelcomeUiEffect
    data class ShowWelcomeSharedFlowBridgeWelcomeCustomDialog(val dialog: WelcomeDialogModel) : WelcomeUiEffect
}
@Immutable
data class WelcomeDialogModel(
    val titleRes: Int,
    val descriptionRes: Int? = null,
    val hintRes: Int? = null,
    val hrefTextRes: Int? = null,
    val hrefUrl: String? = null,
    val checkBoxTextRes: Int? = null,
    val buttons: List<WelcomeDialogButton> = emptyList(),
    val pinInput: Boolean = false,
)
enum class WelcomeDialogButton {
    CANCEL,
    ACCEPT
}
