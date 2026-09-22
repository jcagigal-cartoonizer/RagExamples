package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun MessagesButtons(
    state: MessagesButtonsState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    onMarkRead: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.showBack) {
            Button(
                onClick = onBack,
                enabled = state.backEnabled,
                colors = messagesButtonColors(isPrimary = false),
                shape = messagesButtonShape
            ) { Text("Back") }
        }
        if (state.showSelect) {
            Button(
                onClick = onSelect,
                enabled = state.selectEnabled,
                colors = messagesButtonColors(isPrimary = true),
                shape = messagesButtonShape
            ) { Text("Select") }
        }
        if (state.showDelete) {
            Button(
                onClick = onDelete,
                enabled = state.deleteEnabled,
                colors = messagesButtonColors(isPrimary = false, isDanger = true),
                shape = messagesButtonShape
            ) { Text("Delete") }
        }
        if (state.showMarkRead) {
            Button(
                onClick = onMarkRead,
                enabled = state.markReadEnabled,
                colors = messagesButtonColors(isPrimary = false),
                shape = messagesButtonShape
            ) { Text("Mark Read") }
        }
    }
}
