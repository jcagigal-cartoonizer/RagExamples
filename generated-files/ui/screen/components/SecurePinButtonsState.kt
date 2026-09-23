package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SecurePinButtonsState
import ifac.td.taxi.ui.screen.components.SecurePinButtonState = SecurePinButtonState
import ifac.td.taxi.ui.screen.components.SecurePinButtonState
import ifac.td.taxi.ui.screen.components.SecurePin: Boolean? = null
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 106-2: import androidx.compose.runtime.Immutable
@Immutable
data class SecurePinButtonsState(
    val accept: SecurePinButtonState = SecurePinButtonState(),
    val cancel: SecurePinButtonState = SecurePinButtonState()
) {
    companion object {
        fun default(
            hasSecurePin: Boolean? = null,
            pin: String = "",
            pinRepeat: String = ""
        ): SecurePinButtonsState {
            val isReady = when {
                pin.isEmpty() && pinRepeat.isEmpty() -> true
                else -> false
            }
            return SecurePinButtonsState(
                accept = SecurePinButtonState(
                    visible = true,
                    enabled = hasSecurePin != null,
                    textRes = androidx.compose.runtime.staticCompositionLocalOf { 0 },
                ),
                cancel = SecurePinButtonState(
                    visible = true,
                    enabled = true,
                    textRes = androidx.compose.runtime.staticCompositionLocalOf { 0 },
                )
            )
        }
    }
}
@Immutable
data class SecurePinButtonState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val backgroundColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val borderColor: Color = Color.Unspecified,
)
The above is the state holder. For a more useful version, use this exact button-state API:
// # Block 152-3: import androidx.annotation.StringRes
@Immutable
data class SecurePinButtonsState(
    val accept: SecurePinButtonState = SecurePinButtonState(
        textRes = R.string.accept,
        visible = true,
        enabled = true,
        style = SecurePinButtonStyle.Primary
    ),
    val cancel: SecurePinButtonState = SecurePinButtonState(
        textRes = R.string.cancel,
        visible = true,
        enabled = true,
        style = SecurePinButtonStyle.Secondary
    )
) {
    companion object {
        fun from(hasSecurePin: Boolean? = null): SecurePinButtonsState {
            return SecurePinButtonsState(
                accept = SecurePinButtonState(
                    textRes = R.string.accept,
                    visible = true,
                    enabled = hasSecurePin != null,
                    style = SecurePinButtonStyle.Primary
                ),
                cancel = SecurePinButtonState(
                    textRes = R.string.cancel,
                    visible = true,
                    enabled = true,
                    style = SecurePinButtonStyle.Secondary
                )
            )
        }
    }
}
@Immutable
data class SecurePinButtonState(
    @StringRes val textRes: Int,
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val style: SecurePinButtonStyle = SecurePinButtonStyle.Primary,
)
enum class SecurePinButtonStyle {
    Primary,
    Secondary,
    Danger,
    Disabled
}
