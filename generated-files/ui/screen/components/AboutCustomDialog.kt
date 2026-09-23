package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 434-8: import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun AboutCustomDialog(
    dialogState: CustomDialogState,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .widthIn(min = 280.dp, max = 340.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = dialogState.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = dialogState.message,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dialogState.showDismiss && dialogState.dismissText != null) {
                        TextButton(onClick = { onDismiss?.invoke() }) {
                            Text(dialogState.dismissText)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(onClick = onConfirm) {
                        Text(dialogState.confirmText)
                    }
                }
            }
        }
    }
}
ui/screen/about/
  AboutRoute.kt
  AboutScreen.kt
  AboutViewModel.kt
  AboutUiModels.kt
  AboutButtonsState.kt
  AboutButton.kt
  CustomDialog.kt
  CustomDialogState.kt
1. a **fully self-contained single-file Compose implementation**, or  
2. a **Koin module** for the Compose `AboutViewModel`, or  
3. a **Navigation-Compose version** of this screen.
