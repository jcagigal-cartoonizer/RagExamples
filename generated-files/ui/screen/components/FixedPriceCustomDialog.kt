package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.FixedPriceCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 436-3: import androidx.compose.foundation.background
@Composable
fun FixedPriceCustomDialog(
    title: String,
    message: String,
    positiveText: String,
    negativeText: String? = null,
    onPositive: () -> Unit,
    onNegative: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {}
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF212121)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242)
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (negativeText != null && onNegative != null) {
                    Button(onClick = onNegative) {
                        Text(negativeText)
                    }
                    Spacer(Modifier.width(12.dp))
                }
                Button(onClick = onPositive) {
                    Text(positiveText)
                }
            }
        }
    }
}
