package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 261-3: import androidx.compose.foundation.background
@Composable
fun OfflineInvoiceActionButton(
    text: String,
    enabled: Boolean,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .alpha(if (enabled) 1f else 0.65f)
            .clickable(enabled = enabled) { onClick() }
            .defaultMinSize(minHeight = 48.dp),
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
