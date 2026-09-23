package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.DeviceSettingsCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 479-5: import androidx.compose.foundation.layout.Column
@Composable
fun DeviceSettingsSliderCard(
    title: String,
    value: Int,
    max: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = "$title: $value")
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..max.toFloat(),
            enabled = enabled,
            onValueChangeFinished = onValueChangeFinished
        )
    }
}
@Composable
fun DeviceSettingsCustomDialog(
    state: DeviceSettingsDialogState,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = { Text(state.title) },
        text = { Text(state.message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(state.confirmText)
            }
        },
        dismissButton = {
            if (state.dismissText != null) {
                Button(onClick = { onDismiss?.invoke() }) {
                    Text(state.dismissText)
                }
            }
        }
    )
}
