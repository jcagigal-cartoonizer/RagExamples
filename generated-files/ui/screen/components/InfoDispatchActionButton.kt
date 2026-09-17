package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # 8) Compose custom button


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DispatchActionButton(
    state: ComposeButtonState,
    onClick: () -> Unit
) {
    if (!state.visible) return

    val bg = ComposeCustomButtonDefaults.backgroundColorOf(state.style.background)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(bg)
            .then(
                if (state.enabled && !state.loading) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (state.loading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = state.label,
                color = androidx.compose.ui.graphics.Color(state.style.textColor),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


