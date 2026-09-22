package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 45-2: import androidx.annotation.DrawableRes
// import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
@Immutable
data class MeetingSignButtonsState(
    val options: MeetingSignFabButtonState = MeetingSignFabButtonState(
        iconRes = R.drawable.more, // replace with your actual options icon
        visible = true,
        enabled = true,
        alpha = 1f,
        translationY = 0f,
        rotation = 0f,
    ),
    val edit: MeetingSignFabButtonState = MeetingSignFabButtonState(
        iconRes = R.drawable.edit, // replace with your actual edit icon
        visible = false,
        enabled = true,
        alpha = 0f,
        translationY = 100f,
        rotation = 0f,
    ),
    val dispatch: MeetingSignFabButtonState = MeetingSignFabButtonState(
        iconRes = R.drawable.back,
        visible = false,
        enabled = true,
        alpha = 0f,
        translationY = 100f,
        rotation = 0f,
    ),
)
@Immutable
data class MeetingSignFabButtonState(
    @DrawableRes val iconRes: Int,
    val visible: Boolean,
    val enabled: Boolean,
    val alpha: Float,
    val translationY: Float,
    val rotation: Float,
)
