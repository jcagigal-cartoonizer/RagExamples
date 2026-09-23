package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.StatisticsButtonState
import ifac.td.taxi.ui.screen.components.StatisticsTab
import ifac.td.taxi.ui.screen.components.StatisticsButtonsState
import ifac.td.taxi.ui.screen.components.StatisticsButtonState = StatisticsButtonState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 278-5: import androidx.compose.ui.graphics.Color
data class StatisticsButtonsState(
    val billing: StatisticsButtonState = StatisticsButtonState(),
    val time: StatisticsButtonState = StatisticsButtonState(),
    val back: StatisticsButtonState = StatisticsButtonState(),
) {
    companion object {
        fun forTab(tab: StatisticsTab): StatisticsButtonsState {
            return when (tab) {
                StatisticsTab.Week -> StatisticsButtonsState(
                    billing = StatisticsButtonState(
                        visible = true,
                        enabled = true,
                        selected = true,
                        backgroundColor = Color(0xFF1E88E5),
                        contentColor = Color.White,
                        borderColor = Color(0xFF1E88E5),
                    ),
                    time = StatisticsButtonState(
                        visible = true,
                        enabled = true,
                        selected = false,
                        backgroundColor = Color.Transparent,
                        contentColor = Color(0xFF1E88E5),
                        borderColor = Color(0xFF1E88E5),
                    )
                )
                StatisticsTab.Month -> StatisticsButtonsState(
                    billing = StatisticsButtonState(
                        visible = true,
                        enabled = true,
                        selected = false,
                        backgroundColor = Color.Transparent,
                        contentColor = Color(0xFF1E88E5),
                        borderColor = Color(0xFF1E88E5),
                    ),
                    time = StatisticsButtonState(
                        visible = true,
                        enabled = true,
                        selected = true,
                        backgroundColor = Color(0xFF1E88E5),
                        contentColor = Color.White,
                        borderColor = Color(0xFF1E88E5),
                    )
                )
                StatisticsTab.Year -> StatisticsButtonsState(
                    billing = StatisticsButtonState(
                        visible = false,
                        enabled = false
                    ),
                    time = StatisticsButtonState(
                        visible = false,
                        enabled = false
                    )
                )
            }
        }
    }
}
data class StatisticsButtonState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val backgroundColor: Color = Color.Transparent,
    val contentColor: Color = Color.Black,
    val borderColor: Color = Color.Gray,
)
> If you need exact XML matching, you can plug in your project’s actual colors/dimens instead of these defaults.
