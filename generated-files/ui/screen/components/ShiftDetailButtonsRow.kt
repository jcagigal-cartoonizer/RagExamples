package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ShiftDetailButtonsRow
import ifac.td.taxi.ui.screen.components.ShiftDetailCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 313-5: import androidx.compose.foundation.background
@Composable
fun ShiftDetailButtonsRow(
    buttons: ShiftDetailButtonsState,
    onExport: () -> Unit,
    onEmail: () -> Unit,
    onPrint: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ShiftActionButton(buttons.export, onClick = onExport, modifier = Modifier.weight(1f))
        ShiftActionButton(buttons.email, onClick = onEmail, modifier = Modifier.weight(1f))
        ShiftActionButton(buttons.print, onClick = onPrint, modifier = Modifier.weight(1f))
    }
}
@Composable
fun ShiftSortRow(
    buttons: ShiftDetailButtonsState,
    onSortById: () -> Unit,
    onSortByAmount: () -> Unit,
    onSortByInitHour: () -> Unit,
    onSortByDistance: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SortHeaderButton(buttons.idSort, onSortById, Modifier.weight(1f))
        SortHeaderButton(buttons.amountSort, onSortByAmount, Modifier.weight(1f))
        SortHeaderButton(buttons.initHourSort, onSortByInitHour, Modifier.weight(1f))
        SortHeaderButton(buttons.distanceSort, onSortByDistance, Modifier.weight(1f))
    }
}
@Composable
fun ShiftActionButton(
    state: ButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Color(state.backgroundColor)
    val content = Color(state.contentColor)
    val border = state.borderColor?.let { Color(it) }
    Surface(
        modifier = modifier
            .height(48.dp)
            .then(
                if (border != null) Modifier.border(1.dp, border, RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable(enabled = state.enabled, onClick = onClick),
        color = bg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = state.label, color = content)
        }
    }
}
@Composable
fun SortHeaderButton(
    state: SortButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = shape,
        color = Color(0xFFEFEFEF)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = state.label, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(4.dp))
            when (state.ascending) {
                true -> Icon(Icons.Default.ArrowDropUp, contentDescription = null)
                false -> Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                null -> Box(modifier = Modifier.size(24.dp))
            }
        }
    }
}
@Composable
fun ShiftDetailCustomDialog(
    state: ShiftDetailCustomDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = state.title) },
        text = { Text(text = state.message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = state.positiveButtonText)
            }
        },
        dismissButton = {
            state.negativeButtonText?.let {
                TextButton(onClick = onDismiss) {
                    Text(text = it)
                }
            }
        }
    )
}
A simple placeholder for your `TripsInShiftAdapter` row replacement.
