package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.foundation.background
// import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
@Composable
fun ContactCentralDialogHost(
    dialogState: ContactCentralDialogState?,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
) {
    if (dialogState == null) return
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Text(
                    text = dialogState.title,
                    style = MaterialTheme.typography.titleMedium
                )
                dialogState.message?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = dialogState.cancelText)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onAccept) {
                        Text(text = dialogState.acceptText)
                    }
                }
            }
        }
    }
}
// // # Block 496-6: import androidx.compose.foundation.background
// // import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
// import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
@Composable
fun ContactCentralButton(
    style: ContactCentralButtonStyle,
    onClick: () -> Unit,
) {
    if (!style.visible) return
    val bgColor = when (style.background) {
        ContactCentralButtonBackground.DEFAULT -> MaterialTheme.colorScheme.primary
        ContactCentralButtonBackground.RED -> Color(0xFFD32F2F)
        ContactCentralButtonBackground.GREEN -> Color(0xFF2E7D32)
    }
    val contentColor = Color.White
    val alpha = if (style.enabled) 1f else 0.45f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor.copy(alpha = alpha))
            .then(
                if (style.enabled && !style.loading) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
    ) {
        if (style.loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(22.dp)
                    .align(androidx.compose.ui.Alignment.Center),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = style.text,
                color = contentColor,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
The fragment logic is preserved like this:
To preserve navigation exactly like the fragment, use the composable inside a Fragment-hosted ComposeView, or directly in a Compose NavHost route, but keep the navigation destinations the same:
1. a **full Fragment wrapper** using `ComposeView` while preserving your existing `BaseFragment`
2. a **version that uses your existing `MainActivityViewModel` as the shared state source**
3. a **more exact Material/shape replica** of your `CustomButton` and `CustomDialog` XML style.
