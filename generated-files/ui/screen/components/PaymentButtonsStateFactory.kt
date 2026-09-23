package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 244-4: import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CASH_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CASH_NO_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CREDIT_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CREDIT_CARD_COMPULSORY
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CREDIT_NO_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_TCC
import ifac.td.taxi.domain.utils.StaticConfiguration
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
