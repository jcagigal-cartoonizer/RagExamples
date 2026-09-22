package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
@Composable
fun LoginDriverButton(
    text: String,
    state: ButtonStyleState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (!state.visible) return
    val bg = when (state.background) {
        LoginButtonBackground.GREEN -> Color(0xFF2E7D32)
        LoginButtonBackground.BLUE -> Color(0xFF1565C0)
        LoginButtonBackground.ORANGE -> Color(0xFFEF6C00)
        LoginButtonBackground.GRAY -> Color(0xFF616161)
    }
    Box(
        modifier = modifier
            .height(52.dp)
            .background(bg, RoundedCornerShape(10.dp))
            .clickable(enabled = state.enabled && !state.isLoading) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = state.textColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = state.textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
