package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.TccButtonsState
import ifac.td.taxi.ui.screen.components.TccCustomButton
import ifac.td.taxi.ui.screen.components.TccButtonStyle
import ifac.td.taxi.ui.screen.components.TccButtonState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 578-4: import androidx.compose.runtime.Immutable
@Immutable
data class TccButtonStyle(
    val background: Color,
    val content: Color,
    val disabledBackground: Color,
    val disabledContent: Color,
    val border: Color? = null
) {
    companion object {
        fun accept() = TccButtonStyle(
            background = Color(0xFF2E7D32),
            content = Color.White,
            disabledBackground = Color(0xFF9E9E9E),
            disabledContent = Color(0xFFE0E0E0)
        )
        fun cancel() = TccButtonStyle(
            background = Color(0xFFD32F2F),
            content = Color.White,
            disabledBackground = Color(0xFF9E9E9E),
            disabledContent = Color(0xFFE0E0E0)
        )
    }
}
@Composable
fun TccCustomButton(
    text: String,
    modifier: Modifier = Modifier,
    style: TccButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    val bg = if (enabled) style.background else style.disabledBackground
    val fg = if (enabled) style.content else style.disabledContent
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg,
            disabledContainerColor = bg,
            disabledContentColor = fg
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = fg
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text)
    }
}
fun Dispatch.toTccButtonsState(isLandscape: Boolean): TccButtonsState {
    fun attr(title: String?) = AttributeSpinnerState(
        visible = !title.isNullOrEmpty(),
        title = title.orEmpty(),
        value = 1
    )
    return TccButtonsState(
        accept = TccButtonState(visible = true, enabled = true, loading = false),
        cancel = TccButtonState(visible = true, enabled = true, loading = false),
        attributes1 = attr(attribute1Title),
        attributes2 = attr(attribute2Title),
        attributes3 = attr(attribute3Title),
        attributes4 = attr(attribute4Title),
    )
}
A helper:
@Composable
fun VisibilityWrapper(
    visible: Boolean,
    goneWhenHidden: Boolean,
    content: @Composable () -> Unit
) {
    if (visible) {
        content()
    } else if (!goneWhenHidden) {
        Box(modifier = Modifier.alpha(0f)) {
            content()
        }
    }
}
