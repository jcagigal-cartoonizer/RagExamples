package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
