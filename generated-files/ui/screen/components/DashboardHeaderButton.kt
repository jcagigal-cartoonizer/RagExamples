package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 389-4: import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.HeaderButtonState
@Composable
fun DashboardHeaderButton(
    state: HeaderButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.visible) return
    Button(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = state.backgroundColor,
            contentColor = state.textColor
        ),
        border = BorderStroke(1.dp, Color.Transparent)
    ) {
        Text(text = state.text, color = state.textColor)
    }
}
