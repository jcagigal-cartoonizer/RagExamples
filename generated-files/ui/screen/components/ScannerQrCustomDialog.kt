package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ScannerQrCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 294-5: import androidx.compose.foundation.background
@Composable
fun ScannerQrCustomDialog(
    state: ScannerQrCustomDialogState,
    onDismiss: () -> Unit,
    onButtonClicked: (ScannerQrDialogButtonType) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = state.model.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF111111)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = state.model.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF444444)
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.model.buttons.forEach { button ->
                        Button(
                            onClick = { onButtonClicked(button) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = when (button) {
                                    ScannerQrDialogButtonType.FRONT_CAMERA -> "Front camera"
                                    ScannerQrDialogButtonType.BACK_CAMERA -> "Back camera"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
