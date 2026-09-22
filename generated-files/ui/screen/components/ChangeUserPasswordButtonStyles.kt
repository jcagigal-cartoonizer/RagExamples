package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.graphics.Color
object ChangeUserPasswordButtonStyles {
    val Primary = ButtonAppearance(
        backgroundColor = Color(0xFF1E88E5).value.toLong(),
        contentColor = Color.White.value.toLong(),
        cornerRadiusDp = 10,
        minHeightDp = 48,
        textStyle = ButtonTextStyle.Emphasis
    )
    val Secondary = ButtonAppearance(
        backgroundColor = Color.Transparent.value.toLong(),
        contentColor = Color(0xFF1E88E5).value.toLong(),
        strokeColor = Color(0xFF1E88E5).value.toLong(),
        strokeWidthDp = 1,
        cornerRadiusDp = 10,
        minHeightDp = 48,
        textStyle = ButtonTextStyle.Default
    )
}
