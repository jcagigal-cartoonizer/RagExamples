package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
object ComposeCustomButtonDefaults {
    @Composable
    fun styleFor(
        background: ButtonBackgroundColor,
        enabled: Boolean,
        loading: Boolean = false
    ): ButtonVisualStyle {
        return when {
            !enabled || background == ButtonBackgroundColor.DISABLED -> ButtonVisualStyle(
                background = ButtonBackgroundColor.DISABLED,
                textColor = 0xFFFFFFFF,
                iconTint = 0xFFFFFFFF
            )
            loading -> ButtonVisualStyle(
                background = background,
                textColor = 0xFFFFFFFF,
                iconTint = 0xFFFFFFFF
            )
            else -> when (background) {
                ButtonBackgroundColor.GREEN -> ButtonVisualStyle(
                    background = ButtonBackgroundColor.GREEN,
                    textColor = 0xFFFFFFFF,
                    iconTint = 0xFFFFFFFF
                )
                ButtonBackgroundColor.RED -> ButtonVisualStyle(
                    background = ButtonBackgroundColor.RED,
                    textColor = 0xFFFFFFFF,
                    iconTint = 0xFFFFFFFF
                )
                ButtonBackgroundColor.ORANGE -> ButtonVisualStyle(
                    background = ButtonBackgroundColor.ORANGE,
                    textColor = 0xFFFFFFFF,
                    iconTint = 0xFFFFFFFF
                )
                ButtonBackgroundColor.GRAY -> ButtonVisualStyle(
                    background = ButtonBackgroundColor.GRAY,
                    textColor = 0xFFFFFFFF,
                    iconTint = 0xFFFFFFFF
                )
                ButtonBackgroundColor.DISABLED -> ButtonVisualStyle(
                    background = ButtonBackgroundColor.DISABLED,
                    textColor = 0xFFFFFFFF,
                    iconTint = 0xFFFFFFFF
                )
            }
        }
    }
    fun backgroundColorOf(background: ButtonBackgroundColor): Color = when (background) {
        ButtonBackgroundColor.GREEN -> Color(0xFF2E7D32)
        ButtonBackgroundColor.RED -> Color(0xFFC62828)
        ButtonBackgroundColor.ORANGE -> Color(0xFFEF6C00)
        ButtonBackgroundColor.GRAY -> Color(0xFF546E7A)
        ButtonBackgroundColor.DISABLED -> Color(0xFF9E9E9E)
    }
}
