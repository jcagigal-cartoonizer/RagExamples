package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
@Composable
fun MeetingSignFabButton(
    state: MeetingSignFabButtonState,
    onClick: () -> Unit,
) {
    if (!state.visible) return
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .alpha(state.alpha),
        containerColor = Color.White,
        contentColor = Color.Black,
        elevation = FloatingActionButtonDefaults.elevation()
    ) {
        Icon(
            painter = painterResource(id = state.iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
