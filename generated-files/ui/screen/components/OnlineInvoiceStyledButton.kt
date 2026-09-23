package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 467-4: import androidx.compose.foundation.background
@Composable
fun OnlineInvoiceStyledButton(
    modifier: Modifier = Modifier,
    text: String,
    state: OnlineInvoiceButtonUi,
    onClick: () -> Unit
) {
    val containerColor = when {
        state.isLoading -> state.loadingContainerColor
        state.enabled -> state.containerColor
        else -> state.disabledContainerColor
    }
    val contentColor = when {
        state.isLoading -> state.loadingContentColor
        state.enabled -> state.contentColor
        else -> state.disabledContentColor
    }
    Box(
        modifier = modifier
            .height(48.dp)
            .background(containerColor, RoundedCornerShape(12.dp))
            .clickable(enabled = state.enabled && !state.isLoading) { onClick() }
            .alpha(if (state.enabled || state.isLoading) 1f else 0.7f),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
