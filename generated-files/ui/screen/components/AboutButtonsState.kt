package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 302-5: import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
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
