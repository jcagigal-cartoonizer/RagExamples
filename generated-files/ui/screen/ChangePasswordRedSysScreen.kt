package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
enum class ComposeButtonStyle {
    ENABLE,
    DISABLE,
    LOADING
}
data class CustomButtonColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color
)
object ChangePasswordRedSysButtonStyles {
    val acceptColors = CustomButtonColors(
        container = Color(0xFF1E88E5),
        content = Color.White,
        disabledContainer = Color(0xFF90CAF9),
        disabledContent = Color.White.copy(alpha = 0.7f)
    )
    val cancelColors = CustomButtonColors(
        container = Color(0xFFE0E0E0),
        content = Color(0xFF212121),
        disabledContainer = Color(0xFFF5F5F5),
        disabledContent = Color(0xFF9E9E9E)
    )
}
@Composable
fun StyledCustomButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    colors: CustomButtonColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (enabled) colors.container else colors.disabledContainer
    val content = if (enabled) colors.content else colors.disabledContent
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = colors.disabledContainer,
            disabledContentColor = colors.disabledContent
        ),
        modifier = modifier.height(48.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = content
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Loading")
        } else {
            Text(text = text)
        }
    }
}
