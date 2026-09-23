package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ReceiptHistoryButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 207-3: import androidx.compose.ui.graphics.Color
data class ReceiptHistoryButtonsState(
    val print: ButtonUiState = ButtonUiState.Disabled(text = "Print"),
    val previous: ButtonUiState = ButtonUiState.Enabled(text = "Previous"),
    val partials: ButtonUiState = ButtonUiState.Enabled(text = "Partials"),
    val card: ButtonUiState = ButtonUiState.Empty(text = "Card"),
    val bill: ButtonUiState = ButtonUiState.Empty(text = "Factura"),
    val foto: ButtonUiState = ButtonUiState.Disabled(text = "Foto")
) {
    companion object {
        fun from(
            trip: Trip?,
            isReceiptEmpty: Boolean,
            canGenerateInvoice: Boolean,
            isVoucherEnabled: Boolean,
            isSubscriberAndFromDispatch: Boolean,
            shouldPrint: Boolean,
            isCardLoading: Boolean = false
        ): ReceiptHistoryButtonsState {
            val printState = when {
                isReceiptEmpty || trip == null -> ButtonUiState.Disabled("Print")
                isCardLoading -> ButtonUiState.Loading("Print")
                shouldPrint -> ButtonUiState.Enabled("Print")
                else -> ButtonUiState.Disabled("Print")
            }
            val billState = when {
                trip == null || isReceiptEmpty -> ButtonUiState.Empty("Factura")
                canGenerateInvoice -> ButtonUiState.Invoice("Factura")
                else -> ButtonUiState.Empty("Factura")
            }
            val fotoState = when {
                trip == null || isReceiptEmpty -> ButtonUiState.Disabled("Foto")
                isVoucherEnabled && isSubscriberAndFromDispatch -> ButtonUiState.Enabled("Foto")
                else -> ButtonUiState.Disabled("Foto")
            }
            val cardState = when {
                trip == null || isReceiptEmpty -> ButtonUiState.Empty("Card")
                else -> ButtonUiState.Enabled("Card")
            }
            return ReceiptHistoryButtonsState(
                print = printState,
                previous = ButtonUiState.Enabled("Previous"),
                partials = ButtonUiState.Enabled("Partials"),
                card = cardState,
                bill = billState,
                foto = fotoState
            )
        }
    }
}
sealed class ButtonUiState(
    val text: String,
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val background: Color,
    val content: Color,
    val border: Color
) {
    class Enabled(text: String) : ButtonUiState(
        text = text,
        enabled = true,
        background = Color(0xFF1976D2),
        content = Color.White,
        border = Color(0xFF1976D2)
    )
    class Disabled(text: String) : ButtonUiState(
        text = text,
        enabled = false,
        background = Color(0xFFE0E0E0),
        content = Color(0xFF9E9E9E),
        border = Color(0xFFBDBDBD)
    )
    class Loading(text: String) : ButtonUiState(
        text = text,
        enabled = false,
        loading = true,
        background = Color(0xFF1565C0),
        content = Color.White,
        border = Color(0xFF1565C0)
    )
    class Empty(text: String) : ButtonUiState(
        text = text,
        visible = false,
        enabled = false,
        background = Color.Transparent,
        content = Color.Transparent,
        border = Color.Transparent
    )
    class Invoice(text: String) : ButtonUiState(
        text = text,
        enabled = true,
        background = Color(0xFF2E7D32),
        content = Color.White,
        border = Color(0xFF2E7D32)
    )
}
