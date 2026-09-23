package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.WelcomeButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 650-5: import androidx.annotation.StringRes
@Composable
fun WelcomeButton(
    @StringRes text: Int,
    enabled: Boolean,
    loading: Boolean,
    color: WelcomeButtonColor,
    visible: Boolean,
    onClick: () -> Unit,
) {
    if (!visible) return
    val container = when (color) {
        WelcomeButtonColor.BLUE -> Color(0xFF1976D2)
        WelcomeButtonColor.ORANGE -> Color(0xFFFF9800)
        WelcomeButtonColor.GRAY -> Color(0xFFBDBDBD)
    }
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            disabledContainerColor = container.copy(alpha = 0.45f),
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp),
                color = Color.White
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text = stringResource(text))
    }
}
