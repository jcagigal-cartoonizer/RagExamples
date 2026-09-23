package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PreReservationTripsButtons
import ifac.td.taxi.ui.screen.components.PreReservationTripsButtonVisibility
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 441-6: import androidx.compose.foundation.BorderStroke
@Composable
fun PreReservationTripsButtons(
    state: PreReservationTripsButtonsState,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        if (state.assign.visibility == PreReservationTripsButtonVisibility.Visible) {
            CustomComposeButton(
                text = "Assign",
                kind = state.assign.kind,
                enabled = state.assign.enabled
            ) {}
        }
        if (state.cancel.visibility == PreReservationTripsButtonVisibility.Visible) {
            CustomComposeButton(
                text = "Cancel",
                kind = state.cancel.kind,
                enabled = state.cancel.enabled
            ) {}
        }
    }
}
@Composable
fun CustomComposeButton(
    text: String,
    kind: PreReservationTripsButtonKind,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when (kind) {
        PreReservationTripsButtonKind.Primary -> MaterialTheme.colorScheme.primary
        PreReservationTripsButtonKind.Secondary -> MaterialTheme.colorScheme.secondary
        PreReservationTripsButtonKind.Disabled -> MaterialTheme.colorScheme.surfaceVariant
        PreReservationTripsButtonKind.Warning -> MaterialTheme.colorScheme.error
    }
    val contentColor = when (kind) {
        PreReservationTripsButtonKind.Disabled -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color.White
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
    ) {
        Text(text = text)
    }
}
@Composable
fun DialogActionButton(
    text: String,
    destructive: Boolean,
    secondary: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = when {
        destructive -> MaterialTheme.colorScheme.error
        secondary -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.primary
    }
    val contentColor = when {
        secondary -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color.White
    }
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = text)
    }
}
