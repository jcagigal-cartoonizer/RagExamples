package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.BluetoothDiscoveryButtons
import ifac.td.taxi.ui.screen.components.BluetoothDiscoveryButtonVisualState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 685-5: import androidx.compose.foundation.layout.*
@Composable
fun BluetoothDiscoveryButtons(
    state: BluetoothDiscoveryButtonsState,
    onDiscover: () -> Unit,
    onAccept: () -> Unit,
    onCancel: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StyledButton(
            textRes = R.string.cancel,
            visualState = state.cancel,
            modifier = Modifier.weight(1f),
            onClick = onCancel
        )
        StyledButton(
            textRes = R.string.discover,
            visualState = state.discover,
            modifier = Modifier.weight(1f),
            onClick = onDiscover
        )
    }
    Spacer(Modifier.height(8.dp))
    StyledButton(
        textRes = R.string.accept,
        visualState = state.accept,
        modifier = Modifier.fillMaxWidth(),
        onClick = onAccept
    )
}
@Composable
fun StyledButton(
    textRes: Int,
    visualState: BluetoothDiscoveryButtonVisualState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val enabled = visualState !is BluetoothDiscoveryButtonVisualState.PrimaryDisabled &&
        visualState !is BluetoothDiscoveryButtonVisualState.SecondaryDisabled &&
        visualState !is BluetoothDiscoveryButtonVisualState.PrimaryLoading
    val colors = when (visualState) {
        BluetoothDiscoveryButtonVisualState.PrimaryEnabled,
        BluetoothDiscoveryButtonVisualState.PrimaryLoading -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        BluetoothDiscoveryButtonVisualState.PrimaryDisabled -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BluetoothDiscoveryButtonVisualState.SecondaryEnabled -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
        BluetoothDiscoveryButtonVisualState.SecondaryDisabled -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        colors = colors
    ) {
        if (visualState is BluetoothDiscoveryButtonVisualState.PrimaryLoading) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = androidx.compose.ui.res.stringResource(id = textRes))
    }
}
