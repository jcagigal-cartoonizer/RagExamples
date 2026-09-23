package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 129-2: import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
@Immutable
data class ChooseOptionButtonsState(
    val primaryVisible: Boolean,
    val secondaryVisible: Boolean,
    val tertiaryVisible: Boolean,
    val primaryEnabled: Boolean = true,
    val secondaryEnabled: Boolean = true,
    val tertiaryEnabled: Boolean = true,
    val primaryStyle: ChooseOptionButtonStyle,
    val secondaryStyle: ChooseOptionButtonStyle,
    val tertiaryStyle: ChooseOptionButtonStyle,
) {
    companion object {
        fun default() = fromCount(0)
        fun fromCount(count: Int): ChooseOptionButtonsState {
            return when (count) {
                0 -> ChooseOptionButtonsState(
                    primaryVisible = false,
                    secondaryVisible = false,
                    tertiaryVisible = false,
                    primaryStyle = ChooseOptionButtonStyle.primary(),
                    secondaryStyle = ChooseOptionButtonStyle.secondary(),
                    tertiaryStyle = ChooseOptionButtonStyle.tertiary(),
                )
                1 -> ChooseOptionButtonsState(
                    primaryVisible = true,
                    secondaryVisible = false,
                    tertiaryVisible = false,
                    primaryStyle = ChooseOptionButtonStyle.primary(),
                    secondaryStyle = ChooseOptionButtonStyle.secondary(hidden = true),
                    tertiaryStyle = ChooseOptionButtonStyle.tertiary(hidden = true),
                )
                2 -> ChooseOptionButtonsState(
                    primaryVisible = true,
                    secondaryVisible = true,
                    tertiaryVisible = false,
                    primaryStyle = ChooseOptionButtonStyle.primary(),
                    secondaryStyle = ChooseOptionButtonStyle.secondary(),
                    tertiaryStyle = ChooseOptionButtonStyle.tertiary(hidden = true),
                )
                else -> ChooseOptionButtonsState(
                    primaryVisible = true,
                    secondaryVisible = true,
                    tertiaryVisible = true,
                    primaryStyle = ChooseOptionButtonStyle.primary(),
                    secondaryStyle = ChooseOptionButtonStyle.secondary(),
                    tertiaryStyle = ChooseOptionButtonStyle.tertiary(),
                )
            }
        }
    }
}
@Immutable
data class ChooseOptionButtonStyle(
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color?,
    val visible: Boolean,
) {
    companion object {
        fun primary(hidden: Boolean = false) = ChooseOptionButtonStyle(
            backgroundColor = Color(0xFF1E88E5),
            contentColor = Color.White,
            borderColor = null,
            visible = !hidden
        )
        fun secondary(hidden: Boolean = false) = ChooseOptionButtonStyle(
            backgroundColor = Color(0xFFFFFFFF),
            contentColor = Color(0xFF1E88E5),
            borderColor = Color(0xFF1E88E5),
            visible = !hidden
        )
        fun tertiary(hidden: Boolean = false) = ChooseOptionButtonStyle(
            backgroundColor = Color(0xFFF2F2F2),
            contentColor = Color(0xFF333333),
            borderColor = Color(0xFFBDBDBD),
            visible = !hidden
        )
    }
}
