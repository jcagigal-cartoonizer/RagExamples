package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
// import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
@Composable
fun OfflineInvoiceActionButton(
    text: String,
    enabled: Boolean,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .alpha(if (enabled) 1f else 0.65f)
            .clickable(enabled = enabled) { onClick() }
            .defaultMinSize(minHeight = 48.dp),
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
// // # Block 307-4: import androidx.compose.foundation.background
// // import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
// import androidx.compose.runtime.Composable
enum class OfflineInvoiceDialogButtonType { ACCEPT, CANCEL }
data class ComposeCustomDialogModel(
    val title: String,
    val description: String,
    val isCancellable: Boolean = false,
    val buttons: List<OfflineInvoiceDialogButtonType> = listOf(OfflineInvoiceDialogButtonType.CANCEL, OfflineInvoiceDialogButtonType.ACCEPT)
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeOfflineInvoiceCustomDialog(
    model: ComposeCustomDialogModel,
    onAction: (OfflineInvoiceDialogButtonType) -> Unit,
    onDismissRequest: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = { if (model.isCancellable) onDismissRequest?.invoke() },
        title = { Text(text = model.title) },
        text = { Text(text = model.description) },
        confirmButton = {
            if (model.buttons.contains(OfflineInvoiceDialogButtonType.ACCEPT)) {
                Button(onClick = { onAction(OfflineInvoiceDialogButtonType.ACCEPT) }) {
                    Text("Accept")
                }
            }
        },
        dismissButton = {
            if (model.buttons.contains(OfflineInvoiceDialogButtonType.CANCEL)) {
                Button(
                    onClick = { onAction(OfflineInvoiceDialogButtonType.CANCEL) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Cancel", color = Color.Black)
                }
            }
        }
    )
}
