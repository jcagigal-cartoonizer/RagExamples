package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.OfflineInvoiceButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 214-2: import androidx.compose.runtime.Composable
@Stable
data class OfflineInvoiceButtonsState(
    val acceptEnabled: Boolean,
    val cancelEnabled: Boolean = true,
    val acceptVisible: Boolean = true,
    val cancelVisible: Boolean = true,
    val acceptBackgroundColor: Color,
    val acceptContentColor: Color,
    val cancelBackgroundColor: Color,
    val cancelContentColor: Color
) {
    companion object {
        @Composable
        fun fromFormState(
            isValidToSubmit: Boolean,
            isBusy: Boolean = false
        ): OfflineInvoiceButtonsState {
            return OfflineInvoiceButtonsState(
                acceptEnabled = isValidToSubmit && !isBusy,
                cancelEnabled = !isBusy,
                acceptVisible = true,
                cancelVisible = true,
                acceptBackgroundColor = if (isValidToSubmit && !isBusy) ButtonColors.AcceptEnabledBg else ButtonColors.AcceptDisabledBg,
                acceptContentColor = if (isValidToSubmit && !isBusy) ButtonColors.AcceptEnabledText else ButtonColors.AcceptDisabledText,
                cancelBackgroundColor = ButtonColors.CancelBg,
                cancelContentColor = ButtonColors.CancelText
            )
        }
    }
}
object ButtonColors {
    val AcceptEnabledBg = Color(0xFF2E7D32)
    val AcceptDisabledBg = Color(0xFFBDBDBD)
    val AcceptEnabledText = Color.White
    val AcceptDisabledText = Color(0xFF616161)
    val CancelBg = Color(0xFFE0E0E0)
    val CancelText = Color(0xFF212121)
}
