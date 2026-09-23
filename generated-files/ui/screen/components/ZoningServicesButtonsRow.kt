package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ZoningServicesButtonsRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 432-5: import androidx.compose.foundation.layout.*
@Composable
fun ZoningServicesButtonsRow(
    state: ZoningServicesButtonsState,
    onShowAllClick: () -> Unit,
    onShowRecentClick: () -> Unit,
    onCancelClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        if (state.showAllVisible) {
            Button(
                onClick = onShowAllClick,
                enabled = state.showAllEnabled,
                colors = zoningServicesButtonColors(state.showAllEnabled),
                shape = zoningServicesShape(),
                contentPadding = zoningServicesPadding(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show all")
            }
        }
        if (state.showRecentVisible) {
            Button(
                onClick = onShowRecentClick,
                enabled = state.showRecentEnabled,
                colors = zoningServicesButtonColors(state.showRecentEnabled),
                shape = zoningServicesShape(),
                contentPadding = zoningServicesPadding(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show recent")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.cancelVisible) {
                OutlinedButton(
                    onClick = onCancelClick,
                    colors = zoningServicesOutlinedColors(true),
                    border = zoningServicesBorder(true),
                    shape = zoningServicesShape(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
            if (state.closeVisible) {
                Button(
                    onClick = onCloseClick,
                    colors = zoningServicesButtonColors(true),
                    shape = zoningServicesShape(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
