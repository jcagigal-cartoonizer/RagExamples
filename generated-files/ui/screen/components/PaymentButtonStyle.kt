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
// # Block 13-1: import android.content.res.Configuration
enum class PaymentButtonStyle {
    ENABLE,
    DISABLE,
    LOADING
}
data class PaymentButtonUiState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val style: PaymentButtonStyle = PaymentButtonStyle.ENABLE,
    val text: String,
    @DrawableRes val iconRes: Int? = null
)
data class PaymentButtonsState(
    val card: PaymentButtonUiState = PaymentButtonUiState(text = "Card"),
    val cash: PaymentButtonUiState = PaymentButtonUiState(text = "Cash"),
    val subscriber: PaymentButtonUiState = PaymentButtonUiState(text = "Subscriber"),
    val bizum: PaymentButtonUiState = PaymentButtonUiState(text = "Bizum"),
    val appPayment: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Pay in App"),
    val othersPayment: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Others"),
    val cancel: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Cancel"),
    val addAmount: PaymentButtonUiState = PaymentButtonUiState(text = "Add amount"),
    val backToDispatch: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Back"),
    val coutesyLight: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Light"),
    val uvLight: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "UV"),
    val locution: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Locution"),
) {
    companion object {
        fun initial() = PaymentButtonsState()
    }
}
data class PaymentDialogState(
    val title: String,
    val description: String,
    val buttons: List<PaymentDialogButton>,
    val isCancellable: Boolean = true,
    val centerText: Boolean = false,
    val showCheckBox: Boolean = false,
    val checkBoxText: String? = null,
    val listOptions: List<PaymentDialogOption> = emptyList(),
    val iconRes: Int? = null,
    val editTextHint: String? = null,
    val editTextMaxLength: Int? = null,
    val tag: String? = null
)
data class PaymentDialogOption(
    val id: Int,
    val title: String
)
enum class PaymentDialogButton {
    CANCEL,
    ACCEPT,
    RETRY,
    OTHERS
}
data class PaymentScreenUiState(
    val amountText: String = "",
    val buttons: PaymentButtonsState = PaymentButtonsState.initial(),
    val showBottomMenu: Boolean = true,
    val showAppPaymentLabel: Boolean = false,
    val showExtraButtons: Boolean = true,
    val showDialog: PaymentDialogState? = null,
    val redSysState: RedSysPaymentState = RedSysPaymentState.IDLE,
    val timerText: String? = null,
    val showCourtesyLight: Boolean = false,
    val showUvLight: Boolean = false,
    val showLocution: Boolean = false,
    val waitingAppPaymentResponse: Boolean = true,
    val isLandscape: Boolean = false
)
