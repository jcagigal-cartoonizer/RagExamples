package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 53-1: import androidx.annotation.DrawableRes
// import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
@Immutable
data class LightsTestButtonsState(
    val uvButton: ButtonState,
    val courtesyButton: ButtonState,
    val beeperState: BeeperState,
) {
    companion object {
        fun from(
            isCourtesyLightOn: Boolean,
            isUvLightAvailable: Boolean,
            isUvLightOn: Boolean,
        ): LightsTestButtonsState {
            val uvEnabled = isUvLightAvailable && !isCourtesyLightOn
            val courtesyEnabled = !isUvLightOn
            return LightsTestButtonsState(
                uvButton = ButtonState(
                    enabled = uvEnabled,
                    visible = true,
                    backgroundRes = if (isUvLightOn) R.drawable.luz_ultravioleta_blue else R.drawable.luz_ultravioleta,
                    iconRes = if (isUvLightOn) R.drawable.luz_ultravioleta_blue else R.drawable.luz_ultravioleta,
                    contentDescription = "UV light"
                ),
                courtesyButton = ButtonState(
                    enabled = courtesyEnabled,
                    visible = true,
                    backgroundRes = if (isCourtesyLightOn) R.drawable.lumact_yellow else R.drawable.lumact,
                    iconRes = if (isCourtesyLightOn) R.drawable.lumact_yellow else R.drawable.lumact,
                    contentDescription = "Courtesy light"
                ),
                beeperState = BeeperState(
                    enabled = true,
                    max = 100,
                    progress = 50
                )
            )
        }
    }
}
@Immutable
data class ButtonState(
    val enabled: Boolean,
    val visible: Boolean,
    @DrawableRes val backgroundRes: Int,
    @DrawableRes val iconRes: Int,
    val contentDescription: String
)
@Immutable
data class BeeperState(
    val enabled: Boolean,
    val max: Int,
    val progress: Int
)
