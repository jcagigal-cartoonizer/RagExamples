package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
data class CustomButtonStyle(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color = containerColor.copy(alpha = 0.4f),
    val disabledContentColor: Color = contentColor.copy(alpha = 0.4f),
    val borderColor: Color? = null,
)
@Composable
fun CustomFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: CustomButtonStyle,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = style.containerColor,
            contentColor = style.contentColor,
            disabledContainerColor = style.disabledContainerColor,
            disabledContentColor = style.disabledContentColor
        )
    ) {
        content()
    }
}
@Composable
fun CustomOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: CustomButtonStyle,
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = style.contentColor,
            disabledContentColor = style.disabledContentColor
        ),
        border = BorderStroke(1.dp, style.borderColor ?: style.containerColor)
    ) {
        content()
    }
}
@Composable
fun InformationMessageRoute(
    viewModel: InformationMessageComposeViewModel,
    navigateBack: () -> Unit,
    autoNavigateBack: () -> Unit
) {
    InformationMessageScreen(
        viewModel = viewModel,
        onBack = navigateBack,
        onNavigateBackAfterSend = autoNavigateBack
    )
}
To get exact parity with your old custom views:
1. Replace the placeholder colors in `InformationMessageButtonsState`
2. Replace the `RoundedCornerShape(12.dp/16.dp)` values with the same corner radius as your XML
3. If your `custom_dialog.xml` has:
   then add those into `InformationMessageDialog`
