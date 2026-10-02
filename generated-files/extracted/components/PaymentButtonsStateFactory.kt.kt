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
// # Block 244-4: import androidx.compose.ui.graphics.Color
object PaymentButtonsStateFactory {
    fun build(
        clientType: Int?,
        subscriberFailPin: Boolean,
        isConcertedOrMax: Boolean,
        hasMoneiAccount: Boolean,
        showAppPayment: Boolean,
        waitingAppPaymentResponse: Boolean,
        showBackToDispatch: Boolean,
        showCourtesyLight: Boolean,
        showUvLight: Boolean,
        showLocution: Boolean,
        showAddAmount: Boolean,
    ): PaymentButtonsState {
        val cardEnabled = when (clientType) {
            SUBSCRIBER_CASH_NO_CARD,
            SUBSCRIBER_CASH_CARD -> false
            SUBSCRIBER_CREDIT_NO_CARD,
            SUBSCRIBER_CREDIT_CARD,
            SUBSCRIBER_CREDIT_CARD_COMPULSORY,
            SUBSCRIBER_TCC -> subscriberFailPin
            else -> true
        }
        val cashEnabled = when (clientType) {
            SUBSCRIBER_CASH_NO_CARD,
            SUBSCRIBER_CASH_CARD -> true
            SUBSCRIBER_CREDIT_NO_CARD,
            SUBSCRIBER_CREDIT_CARD,
            SUBSCRIBER_CREDIT_CARD_COMPULSORY,
            SUBSCRIBER_TCC -> subscriberFailPin
            else -> true
        }
        return PaymentButtonsState(
            card = PaymentButtonUiState(
                visible = true,
                enabled = cardEnabled,
                style = if (cardEnabled) PaymentButtonStyle.ENABLE else PaymentButtonStyle.DISABLE,
                text = "Card"
            ),
            cash = PaymentButtonUiState(
                visible = true,
                enabled = cashEnabled,
                style = if (cashEnabled) PaymentButtonStyle.ENABLE else PaymentButtonStyle.DISABLE,
                text = "Cash"
            ),
            subscriber = PaymentButtonUiState(
                visible = true,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "Subscriber"
            ),
            bizum = PaymentButtonUiState(
                visible = hasMoneiAccount,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "Bizum"
            ),
            appPayment = PaymentButtonUiState(
                visible = showAppPayment,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "App payment"
            ),
            othersPayment = PaymentButtonUiState(
                visible = showAppPayment,
                enabled = !isConcertedOrMax,
                style = if (isConcertedOrMax) PaymentButtonStyle.DISABLE else PaymentButtonStyle.ENABLE,
                text = "Others"
            ),
            cancel = PaymentButtonUiState(
                visible = false,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "Cancel"
            ),
            addAmount = PaymentButtonUiState(
                visible = showAddAmount,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "+"
            ),
            backToDispatch = PaymentButtonUiState(
                visible = showBackToDispatch,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "Back"
            ),
            coutesyLight = PaymentButtonUiState(
                visible = showCourtesyLight,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "Courtesy"
            ),
            uvLight = PaymentButtonUiState(
                visible = showUvLight,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "UV"
            ),
            locution = PaymentButtonUiState(
                visible = showLocution,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "TTS"
            )
        )
    }
}
