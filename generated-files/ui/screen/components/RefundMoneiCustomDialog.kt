package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.RefundMoneiCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 257-4: import androidx.compose.foundation.background
@Composable
fun RefundMoneiCustomDialog(
    title: String,
    message: String,
    confirmText: String = "OK",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = Color.Black)
            Spacer(Modifier.height(12.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF444444))
            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dismissText,
                    color = Color(0xFF666666),
                    modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp)
                )
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                ) {
                    Text(confirmText, color = Color.White)
                }
            }
        }
    }
}
