package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 696-4: import androidx.compose.animation.animateColorAsState
@Composable
fun PermissionSwitchRow(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    visible: Boolean,
    highlighted: Boolean,
    onCheckedChange: () -> Unit,
) {
    if (!visible) return
    val containerColor by animateColorAsState(
        targetValue = when {
            highlighted -> Color(0xFFFFF3CD)
            checked -> Color(0xFFE8F5E9)
            else -> Color(0xFFFFFFFF)
        },
        label = "permissionRowColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            highlighted -> Color(0xFFFFB300)
            checked -> Color(0xFF2E7D32)
            else -> Color(0xFFE0E0E0)
        },
        label = "permissionRowBorder"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors()
        )
    }
}
