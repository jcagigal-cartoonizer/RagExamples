package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
object PaymentButtonStyles {
    val EnabledContainer = Color(0xFF1E88E5)
    val EnabledContent = Color.White
    val DisabledContainer = Color(0xFFE0E0E0)
    val DisabledContent = Color(0xFF9E9E9E)
    val LoadingContainer = Color(0xFF90CAF9)
    val LoadingContent = Color.White
}
@Composable
fun paymentButtonColors(style: PaymentButtonStyle): ButtonColors {
    return when (style) {
        PaymentButtonStyle.ENABLE -> ButtonDefaults.buttonColors(
            containerColor = PaymentButtonStyles.EnabledContainer,
            contentColor = PaymentButtonStyles.EnabledContent,
            disabledContainerColor = PaymentButtonStyles.DisabledContainer,
            disabledContentColor = PaymentButtonStyles.DisabledContent
        )
        PaymentButtonStyle.DISABLE -> ButtonDefaults.buttonColors(
            containerColor = PaymentButtonStyles.DisabledContainer,
            contentColor = PaymentButtonStyles.DisabledContent
        )
        PaymentButtonStyle.LOADING -> ButtonDefaults.buttonColors(
            containerColor = PaymentButtonStyles.LoadingContainer,
            contentColor = PaymentButtonStyles.LoadingContent
        )
    }
}
