package ifac.td.taxi.ui.screen.state
import ifac.td.taxi.ui.screen.state.InfoDispatchButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # 2) Full `InfoDispatchButtonsState` with button coloring/visibility matching fragment behavior

// This is the Compose replacement for the fragment’s button logic.


import androidx.compose.runtime.Immutable

@Immutable
data class InfoDispatchButtonsState(
    val notifications: ComposeButtonState = ComposeButtonState.disabled(),
    val navigate: ComposeButtonState = ComposeButtonState.enabled(),
    val voiceCall: ComposeButtonState = ComposeButtonState.disabled(),
    val print: ComposeButtonState = ComposeButtonState.enabled(),
    val noClient: ComposeButtonState = ComposeButtonState.disabled(),
    val returnButton: ComposeButtonState = ComposeButtonState.disabled()
)

@Immutable
data class ComposeButtonState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val label: String = "",
    val style: ButtonVisualStyle = ButtonVisualStyle()
) {
    companion object {
        fun enabled(label: String = "") = ComposeButtonState(enabled = true, loading = false, label = label)
        fun disabled(label: String = "") = ComposeButtonState(enabled = false, loading = false, label = label)
        fun loading(label: String = "") = ComposeButtonState(enabled = false, loading = true, label = label)
        fun hidden() = ComposeButtonState(visible = false, enabled = false, loading = false)
    }
}

@Immutable
data class ButtonVisualStyle(
    val background: ButtonBackgroundColor = ButtonBackgroundColor.GRAY,
    val textColor: Long = 0xFFFFFFFF,
    val borderColor: Long = 0x00000000,
    val iconTint: Long = 0xFFFFFFFF
)

enum class ButtonBackgroundColor {
    GREEN, RED, ORANGE, GRAY, DISABLED
}


