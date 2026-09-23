package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ReceiptRedSysButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 48-2: import androidx.compose.runtime.Immutable
@Immutable
data class ReceiptRedSysButtonsState(
    val showRefund: Boolean = false,
    val showPrint: Boolean = false,
    val refundEnabled: Boolean = false,
    val printEnabled: Boolean = false,
    val refundButtonColors: ButtonColorsState = ButtonColorsState(
        container = Color(0xFFE53935),
        content = Color.White,
        disabledContainer = Color(0xFFFFCDD2),
        disabledContent = Color(0xFF8D6E63)
    ),
    val printButtonColors: ButtonColorsState = ButtonColorsState(
        container = Color(0xFF1E88E5),
        content = Color.White,
        disabledContainer = Color(0xFFBBDEFB),
        disabledContent = Color(0xFF607D8B)
    )
) {
    companion object {
        fun forOperation(operation: RedSysOperation): ReceiptRedSysButtonsState {
            val canRefund = operation.operationType == AUTHORIZATION && operation.result != DENIED
            val canPrint = (operation.operationType == AUTHORIZATION && operation.result != DENIED) ||
                (operation.operationType == REFUND && operation.refundResponse != null)
            return ReceiptRedSysButtonsState(
                showRefund = canRefund,
                showPrint = canPrint,
                refundEnabled = canRefund,
                printEnabled = canPrint
            )
        }
    }
}
@Immutable
data class ButtonColorsState(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color
)
