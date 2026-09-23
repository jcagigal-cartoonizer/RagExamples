package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ZoningButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 95-3: import androidx.annotation.DrawableRes
data class ZoningButtonsState(
    val locateOnHired: ComposeActionButtonState = ComposeActionButtonState.Hidden,
    val soonInZone: ComposeActionButtonState = ComposeActionButtonState.Hidden,
    val pending: ComposeActionButtonState = ComposeActionButtonState.Disabled(
        textRes = R.string.pending,
        iconRes = R.drawable.ic_pending,
        color = ComposeButtonColor.Blue
    ),
    val trips: ComposeActionButtonState = ComposeActionButtonState.Enabled(
        textRes = R.string.trips,
        iconRes = R.drawable.ic_trips,
        color = ComposeButtonColor.Blue
    ),
    val cars: ComposeActionButtonState = ComposeActionButtonState.Enabled(
        textRes = R.string.cars,
        iconRes = R.drawable.ic_cars,
        color = ComposeButtonColor.Blue
    ),
) {
    fun hasVisibleActions(): Boolean =
        locateOnHired.isVisible() || soonInZone.isVisible() || pending.isVisible() || trips.isVisible() || cars.isVisible()
}
sealed class ComposeActionButtonState {
    abstract val textRes: Int
    abstract val iconRes: Int
    abstract val color: ComposeButtonColor
    data class Hidden(
        override val textRes: Int = 0,
        override val iconRes: Int = 0,
        override val color: ComposeButtonColor = ComposeButtonColor.Blue
    ) : ComposeActionButtonState()
    data class Disabled(
        override val textRes: Int,
        override val iconRes: Int,
        override val color: ComposeButtonColor,
    ) : ComposeActionButtonState()
    data class Enabled(
        override val textRes: Int,
        override val iconRes: Int,
        override val color: ComposeButtonColor,
    ) : ComposeActionButtonState()
    data class Loading(
        override val textRes: Int,
        override val iconRes: Int,
        override val color: ComposeButtonColor,
    ) : ComposeActionButtonState()
}
fun ComposeActionButtonState.isVisible(): Boolean = this !is ComposeActionButtonState.Hidden
enum class ComposeButtonColor {
    Blue,
    Red,
    Orange,
    Green,
    Gray,
}
These helpers approximate your current `CustomButton` behavior more closely than a plain Material button.
