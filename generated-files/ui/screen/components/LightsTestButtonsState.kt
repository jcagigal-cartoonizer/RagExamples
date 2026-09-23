package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.LightsTestCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 370-6: import androidx.compose.foundation.background
@Composable
fun LightsTestCustomDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(20.dp)
                    .widthIn(min = 280.dp)
            ) {
                Text(text = title)
                Spacer(Modifier.height(12.dp))
                Text(text = message)
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text(dismissText)
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(onClick = onConfirm) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
To match XML more closely, you should plug in the actual drawable resources and any exact spacing/sizing from:
In Compose, the important styling knobs are:
1. a **full XML-to-Compose layout parity version**, or  
2. a **Navigation Component + Hilt/Koin-friendly integration**, or  
3. a **more exact custom button composable** mirroring your old `CustomButton` API.
