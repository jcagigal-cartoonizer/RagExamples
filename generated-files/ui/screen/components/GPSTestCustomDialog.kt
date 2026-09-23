package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun GPSTestCustomDialog(
    state: DialogState.Message,
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit = onDismiss,
    onNegativeClick: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(text = state.title) },
        text = { Text(text = state.message) },
        confirmButton = {
            TextButton(onClick = onPositiveClick) {
                Text(state.positiveText)
            }
        },
        dismissButton = if (state.negativeText != null && onNegativeClick != null) {
            {
                TextButton(onClick = onNegativeClick) {
                    Text(state.negativeText)
                }
            }
        } else null
    )
}
// # Block 447-8: import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
@Composable
fun GPSTestCustomButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    borderColor: androidx.compose.ui.graphics.Color? = null,
    modifier: Modifier = Modifier
) {
    val colors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = GPSTestButtonStyle.disabledContainer(),
        disabledContentColor = GPSTestButtonStyle.mutedText()
    )
    if (borderColor != null) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            border = BorderStroke(1.dp, borderColor),
            modifier = modifier.defaultMinSize(minHeight = 48.dp)
        ) {
            Text(text)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            modifier = modifier.defaultMinSize(minHeight = 48.dp)
        ) {
            Text(text)
        }
    }
}
@Composable
fun GPSTestHost(
    viewModel: GPSTestComposeViewModel,
    navigateBack: () -> Unit,
    launchIntent: (android.content.Intent) -> Unit
) {
    GPSTestRoute(
        viewModel = viewModel,
        onNavigateBack = navigateBack,
        onLaunchIntent = launchIntent
    )
}
1. a **fully working fragment wrapper** that hosts this Compose screen inside your existing app,
2. a **more exact recreation of the XML layout** in Compose,
3. or a **Koin-based Compose ViewModel wiring example** for this screen.
