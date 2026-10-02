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
// # Block 94-2: import android.content.Intent
sealed interface PaymentUiEvent {
    data object OnCreate : PaymentUiEvent
    data object OnResume : PaymentUiEvent
    data object OnCardClicked : PaymentUiEvent
    data object OnCashClicked : PaymentUiEvent
    data object OnSubscriberClicked : PaymentUiEvent
    data object OnAddAmountClicked : PaymentUiEvent
    data object OnBizumClicked : PaymentUiEvent
    data object OnCancelAppPaymentClicked : PaymentUiEvent
    data object OnAppPaymentClicked : PaymentUiEvent
    data object OnOthersPaymentClicked : PaymentUiEvent
    data object OnBackToDispatchClicked : PaymentUiEvent
    data object OnLocutionClicked : PaymentUiEvent
    data object OnCourtesyLightClicked : PaymentUiEvent
    data object OnUvLightClicked : PaymentUiEvent
    data class OnDialogButtonClicked(val button: PaymentDialogButton, val text: String? = null) : PaymentUiEvent
    data class OnDialogOptionSelected(val optionId: Int) : PaymentUiEvent
    data class OnPaymentResult(val success: Boolean, val data: Intent?) : PaymentUiEvent
}
sealed interface PaymentUiEffect {
    data class NavigateTo(val routeId: Int) : PaymentUiEffect
    data class NavigateToDirections(val directions: Any) : PaymentUiEffect
    data class OpenDialog(val dialog: PaymentDialogState) : PaymentUiEffect
    data class ShowToast(val messageRes: Int) : PaymentUiEffect
    data class StartActivityForResult(val intent: Intent) : PaymentUiEffect
    data class OpenExternalPayment(val amount: Int) : PaymentUiEffect
    data class RequestAppPaymentAuth(val amount: Int) : PaymentUiEffect
    data object StopCountdown : PaymentUiEffect
    data object ResetBottomBarText : PaymentUiEffect
    data object BackToDispatched : PaymentUiEffect
    data class ShowRedSysPopup(val state: RedSysPaymentState) : PaymentUiEffect
}
