package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SplashScreenButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 286-3: import androidx.compose.ui.graphics.Color
data class SplashScreenButtonsState(
    val isVisible: Boolean = false, // original splash had no buttons shown
    val primaryButtonVisible: Boolean = false,
    val secondaryButtonVisible: Boolean = false,
    val tertiaryButtonVisible: Boolean = false,
    val primaryEnabled: Boolean = true,
    val secondaryEnabled: Boolean = true,
    val tertiaryEnabled: Boolean = true,
    val primaryButtonStyle: SplashButtonStyle = SplashButtonStyle.Primary,
    val secondaryButtonStyle: SplashButtonStyle = SplashButtonStyle.Secondary,
    val tertiaryButtonStyle: SplashButtonStyle = SplashButtonStyle.Tertiary
)
enum class SplashButtonStyle {
    Primary, Secondary, Tertiary
}
data class SplashButtonColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
    val border: Color
)
fun SplashButtonStyle.colors(): SplashButtonColors = when (this) {
    SplashButtonStyle.Primary -> SplashButtonColors(
        container = Color(0xFF1E88E5),
        content = Color.White,
        disabledContainer = Color(0xFF90CAF9),
        disabledContent = Color(0x80FFFFFF),
        border = Color.Transparent
    )
    SplashButtonStyle.Secondary -> SplashButtonColors(
        container = Color.Transparent,
        content = Color(0xFF1E88E5),
        disabledContainer = Color.Transparent,
        disabledContent = Color(0x801E88E5),
        border = Color(0xFF1E88E5)
    )
    SplashButtonStyle.Tertiary -> SplashButtonColors(
        container = Color.Transparent,
        content = Color(0xFF757575),
        disabledContainer = Color.Transparent,
        disabledContent = Color(0x80757575),
        border = Color(0xFF757575)
    )
}
But it satisfies the state-holder requirement and can be extended to match XML-driven button layouts.
