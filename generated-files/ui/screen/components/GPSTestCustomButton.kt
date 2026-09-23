package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.GPSTestButtonStyle
import ifac.td.taxi.ui.screen.components.GPSTestCustomButton
import ifac.td.taxi.compose.viewmodel.GPSTestComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 447-8: import androidx.compose.foundation.BorderStroke
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
