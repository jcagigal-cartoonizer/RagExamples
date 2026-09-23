package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PointsOfInterestButtons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 491-4: import androidx.compose.foundation.BorderStroke
@Composable
fun CustomComposeButton(
    text: String,
    style: ComposeButtonStyle,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = Color(style.containerColor)
    val content = Color(style.contentColor)
    val disabledBg = Color(style.disabledContainerColor)
    val disabledContent = Color(style.disabledContentColor)
    if (style.borderColor != null) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (enabled) bg else disabledBg,
                contentColor = if (enabled) content else disabledContent,
                disabledContentColor = disabledContent,
                disabledContainerColor = disabledBg
            ),
            border = BorderStroke(
                style.strokeWidthDp.dp,
                Color(style.borderColor)
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(text)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = bg,
                contentColor = content,
                disabledContainerColor = disabledBg,
                disabledContentColor = disabledContent
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(text)
        }
    }
}
@Composable
fun PointsOfInterestButtons(
    state: ifac.td.taxi.ui.screen.state.PointsOfInterestButtonsState,
    onCancel: () -> Unit,
    onSearch: () -> Unit
) {
    if (state.cancelVisible || state.searchVisible) {
        androidx.compose.foundation.layout.Row {
            if (state.cancelVisible) {
                CustomComposeButton(
                    text = state.cancelText,
                    style = state.cancelStyle,
                    enabled = state.cancelEnabled,
                    onClick = onCancel
                )
            }
            if (state.searchVisible) {
                CustomComposeButton(
                    text = state.searchText,
                    style = state.searchStyle,
                    enabled = state.searchEnabled,
                    onClick = onSearch
                )
            }
        }
    }
}
