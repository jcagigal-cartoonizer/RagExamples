package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
@Immutable
data class LoginUserButtonsState(
    val changeUser: ComposeButtonState = ComposeButtonState(
        text = "Cambiar usuario",
        visible = false,
        style = ComposeButtonStyle.DisabledGreen
    ),
    val cancel: ComposeButtonState = ComposeButtonState(
        text = "Cancelar",
        visible = true,
        style = ComposeButtonStyle.Outline
    ),
    val accept: ComposeButtonState = ComposeButtonState(
        text = "Aceptar",
        visible = true,
        style = ComposeButtonStyle.EnabledGreen
    ),
)
@Immutable
data class ComposeButtonState(
    val text: String,
    val visible: Boolean = true,
    val style: ComposeButtonStyle = ComposeButtonStyle.EnabledGreen
) {
    fun loading() = copy(style = ComposeButtonStyle.LoadingGreen)
    fun enabledGreen() = copy(style = ComposeButtonStyle.EnabledGreen)
    fun disabledGreen() = copy(style = ComposeButtonStyle.DisabledGreen)
}
sealed interface ComposeButtonStyle {
    data object EnabledGreen : ComposeButtonStyle
    data object DisabledGreen : ComposeButtonStyle
    data object LoadingGreen : ComposeButtonStyle
    data object Outline : ComposeButtonStyle
}
fun ComposeButtonStyle.backgroundColor(): Color = when (this) {
    ComposeButtonStyle.EnabledGreen -> Color(0xFF2E7D32)
    ComposeButtonStyle.DisabledGreen -> Color(0xFF9E9E9E)
    ComposeButtonStyle.LoadingGreen -> Color(0xFF1B5E20)
    ComposeButtonStyle.Outline -> Color.Transparent
}
fun ComposeButtonStyle.contentColor(): Color = when (this) {
    ComposeButtonStyle.Outline -> Color(0xFF2E7D32)
    else -> Color.White
}
fun ComposeButtonStyle.borderColor(): Color = when (this) {
    ComposeButtonStyle.Outline -> Color(0xFF2E7D32)
    else -> Color.Transparent
}
