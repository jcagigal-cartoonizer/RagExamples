package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PredefinedMessageButtonsState
import ifac.td.taxi.ui.screen.components.PredefinedMessageButtonVisualState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 339-4: import androidx.compose.runtime.Immutable
@Immutable
data class PredefinedMessageButtonsState(
    val newMessage: PredefinedMessageButtonVisualState,
    val cancel: PredefinedMessageButtonVisualState,
) {
    companion object {
        fun default(hasMessages: Boolean): PredefinedMessageButtonsState {
            return PredefinedMessageButtonsState(
                newMessage = PredefinedMessageButtonVisualState(
                    visible = true,
                    enabled = true,
                    type = PredefinedMessageButtonType.PRIMARY
                ),
                cancel = PredefinedMessageButtonVisualState(
                    visible = true,
                    enabled = true,
                    type = PredefinedMessageButtonType.SECONDARY
                )
            )
        }
    }
}
@Immutable
data class PredefinedMessageButtonVisualState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val type: PredefinedMessageButtonType = PredefinedMessageButtonType.PRIMARY,
)
enum class PredefinedMessageButtonType {
    PRIMARY,
    SECONDARY,
    DANGER,
    GHOST
}
These helpers are meant to imitate the old custom button component:
