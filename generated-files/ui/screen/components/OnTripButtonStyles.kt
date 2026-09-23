package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 86-2: import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color
object OnTripButtonStyles {
    val red = Color(0xFFD32F2F)
    val green = Color(0xFF2E7D32)
    val blue = Color(0xFF1976D2)
    val orange = Color(0xFFF57C00)
    val gray = Color(0xFF9E9E9E)
    val white = Color.White
    val disabledTint = Color(0xFFBDBDBD)
    fun backgroundColor(background: ButtonBackground): Color = when (background) {
        ButtonBackground.Red -> red
        ButtonBackground.Green -> green
        ButtonBackground.Blue -> blue
        ButtonBackground.Orange -> orange
        ButtonBackground.Gray -> gray
        ButtonBackground.Default -> green
    }
    fun textColor(style: ButtonStyle): Color = when (style) {
        ButtonStyle.Enabled -> white
        ButtonStyle.Loading -> white
        ButtonStyle.Disabled -> disabledTint
    }
    fun alpha(style: ButtonStyle): Float = when (style) {
        ButtonStyle.Enabled -> 1f
        ButtonStyle.Loading -> 0.75f
        ButtonStyle.Disabled -> 0.38f
    }
}
