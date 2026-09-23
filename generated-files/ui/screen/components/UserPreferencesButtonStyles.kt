package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.UserPreferencesButtonStyles
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 773-5: import androidx.compose.foundation.shape.RoundedCornerShape
object UserPreferencesButtonStyles {
    @Composable
    fun primary(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF2D6CDF),
        contentColor = Color.White,
        disabledContainerColor = Color(0xFFB0B0B0),
        disabledContentColor = Color(0xFFFFFFFF)
    )
    @Composable
    fun cancel(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF757575),
        contentColor = Color.White,
        disabledContainerColor = Color(0xFFB0B0B0),
        disabledContentColor = Color.White
    )
    @Composable
    fun warning(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFFE67E22),
        contentColor = Color.White
    )
    @Composable
    fun success(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF1E8E3E),
        contentColor = Color.White
    )
    val shape = RoundedCornerShape(12.dp)
}
Button(
    onClick = ...,
    shape = UserPreferencesButtonStyles.shape,
    colors = UserPreferencesButtonStyles.primary()
) { ... }
I preserved the behavior using:
Examples:
Your request said:
> Use dialog state, lifecycle collection of state/events for dialog handling
That’s why the screen uses:
So the dialog is driven from `UiEffect.OpenDialog` and dismissed via state reset.
1. **Spinners/dropdowns**
2. **Permissions**
3. **Ringtone picker**
4. **Button visibility**
