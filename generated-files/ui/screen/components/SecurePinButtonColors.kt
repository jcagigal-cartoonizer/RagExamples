package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SecurePinButtonColors
import ifac.td.taxi.ui.screen.components.SecurePinButtonStyle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 490-8: import androidx.compose.material3.ButtonDefaults
data class SecurePinButtonColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
    val border: Color = Color.Unspecified
)
@Composable
fun securePinButtonColors(style: SecurePinButtonStyle): SecurePinButtonColors {
    return when (style) {
        SecurePinButtonStyle.Primary -> SecurePinButtonColors(
            container = BrandPrimary,
            content = Color.White,
            disabledContainer = BrandPrimary.copy(alpha = 0.4f),
            disabledContent = Color.White.copy(alpha = 0.7f)
        )
        SecurePinButtonStyle.Secondary -> SecurePinButtonColors(
            container = Color.Transparent,
            content = BrandPrimary,
            disabledContainer = Color.Transparent,
            disabledContent = BrandPrimary.copy(alpha = 0.4f),
            border = BrandPrimary
        )
        SecurePinButtonStyle.Danger -> SecurePinButtonColors(
            container = ErrorRed,
            content = Color.White,
            disabledContainer = ErrorRed.copy(alpha = 0.4f),
            disabledContent = Color.White.copy(alpha = 0.7f)
        )
        SecurePinButtonStyle.Disabled -> SecurePinButtonColors(
            container = Color.LightGray,
            content = Color.DarkGray,
            disabledContainer = Color.LightGray,
            disabledContent = Color.DarkGray
        )
    }
}
Example composable using those helpers:
