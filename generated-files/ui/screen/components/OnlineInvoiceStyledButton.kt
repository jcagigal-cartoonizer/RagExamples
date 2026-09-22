package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 467-4: import androidx.compose.foundation.background
// // import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
// import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
@Composable
fun OnlineInvoiceStyledButton(
    modifier: Modifier = Modifier,
    text: String,
    state: OnlineInvoiceButtonUi,
    onClick: () -> Unit
) {
    val containerColor = when {
        state.isLoading -> state.loadingContainerColor
        state.enabled -> state.containerColor
        else -> state.disabledContainerColor
    }
    val contentColor = when {
        state.isLoading -> state.loadingContentColor
        state.enabled -> state.contentColor
        else -> state.disabledContentColor
    }
    Box(
        modifier = modifier
            .height(48.dp)
            .background(containerColor, RoundedCornerShape(12.dp))
            .clickable(enabled = state.enabled && !state.isLoading) { onClick() }
            .alpha(if (state.enabled || state.isLoading) 1f else 0.7f),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
// // # Block 526-5: import androidx.compose.foundation.background
// // import androidx.compose.foundation.background
// import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
@Composable
fun OnlineInvoiceOnlineInvoiceCustomDialog(
    dialogState: OnlineInvoiceDialogState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = dialogState.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = dialogState.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dialogState.buttons.any { it is OnlineInvoiceDialogButton.Accept }) {
                        TextButton(onClick = onAccept) {
                            Text(text = stringResource(id = R.string.accept))
                        }
                    }
                }
            }
        }
    }
}
A few details from the XML version are not visible in your snippet, so the Compose version above matches the behavior rather than the exact XML pixels. To make it visually identical, you’d typically port:
I can produce a pixel-closer Compose version with exact colors, sizes, and typography.
