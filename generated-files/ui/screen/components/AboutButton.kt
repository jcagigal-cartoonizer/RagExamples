package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
// // import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Immutable
data class AboutButtonsState(
    val accept: AboutButtonState
) {
    companion object {
        fun default(): AboutButtonsState {
            return AboutButtonsState(
                accept = AboutButtonState.primary(
                    label = "Accept",
                    visible = true,
                    enabled = true
                )
            )
        }
    }
}
@Immutable
data class AboutButtonState(
    val label: String,
    val visible: Boolean,
    val enabled: Boolean,
    val colors: AboutButtonColors,
    val shapeRadiusDp: Int = 12,
    val borderWidthDp: Int = 0,
) {
    companion object {
        fun primary(
            label: String,
            visible: Boolean = true,
            enabled: Boolean = true
        ): AboutButtonState {
            return AboutButtonState(
                label = label,
                visible = visible,
                enabled = enabled,
                colors = AboutButtonColors.primary()
            )
        }
    }
}
@Immutable
data class AboutButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val borderColor: Color? = null
) {
    companion object {
        fun primary(): AboutButtonColors = AboutButtonColors(
            containerColor = Color(0xFF1E88E5),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF9E9E9E),
            disabledContentColor = Color(0xFFE0E0E0),
            borderColor = null
        )
        fun outline(): AboutButtonColors = AboutButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFF1E88E5),
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color(0xFFB0BEC5),
            borderColor = Color(0xFF1E88E5)
        )
    }
}
// // # Block 378-6: import androidx.compose.foundation.BorderStroke
// // import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
@Composable
fun AboutButton(
    text: String,
    enabled: Boolean,
    visible: Boolean,
    colors: AboutButtonColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    val shape = RoundedCornerShape(12.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.containerColor,
            contentColor = colors.contentColor,
            disabledContainerColor = colors.disabledContainerColor,
            disabledContentColor = colors.disabledContentColor
        ),
        border = colors.borderColor?.let { BorderStroke(1.dp, it) },
        modifier = modifier.height(48.dp)
    ) {
        Text(text = text)
    }
}
// // # Block 419-7: import androidx.compose.runtime.Immutable
// // import androidx.compose.runtime.Immutable
@Immutable
data class AboutCustomDialogState(
    val title: String,
    val message: String,
    val confirmText: String,
    val dismissText: String? = null,
    val showDismiss: Boolean = false
)
// // # Block 434-8: import androidx.compose.foundation.background
// import androidx.compose.foundation.background
// import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
@Composable
fun AboutAboutCustomDialog(
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
