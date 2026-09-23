package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.FixedPriceButtonStyle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 615-5: import androidx.compose.foundation.layout.*
@Composable
fun FixedPriceBottomSheet(
    buttonsState: FixedPriceButtonsState,
    uiState: FixedPriceUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        tonalElevation = 8.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            if (buttonsState.closeVisible) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onClose) { Text("Close") }
                }
            }
            Text(
                text = uiState.selectedDropOff?.getPrintableStreetTextShort().orEmpty(),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            PriceRow("Taxi", buttonsState.taxi)
            Spacer(Modifier.height(8.dp))
            PriceRow("Van", buttonsState.van)
            Spacer(Modifier.height(8.dp))
            PriceRow("Business", buttonsState.business)
            Spacer(Modifier.height(12.dp))
            if (buttonsState.taxi.error || buttonsState.van.error || buttonsState.business.error) {
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                    Text("Retry")
                }
            }
        }
    }
}
@Composable
fun PriceRow(label: String, state: PriceButtonState) {
    val text = when {
        state.showProgress -> "Loading..."
        state.showError -> "Error"
        state.showText -> state.text
        else -> ""
    }
    OutlinedButton(
        onClick = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        colors = if (state.error) FixedPriceButtonStyle.errorColors() else FixedPriceButtonStyle.neutralColors(),
        contentPadding = FixedPriceButtonStyle.contentPadding
    ) {
        Text("$label: $text")
    }
}
