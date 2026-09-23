package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementButtonState = RequestStandReinforcementButtonState
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementButtonsState
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementButtonState = reinforcement
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementButtonState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 276-4: import androidx.compose.runtime.Immutable
@Immutable
data class RequestStandReinforcementButtonsState(
    val cancel: ComposeButtonStyle = ComposeButtonStyle.neutral(),
    val addFavourites: RequestStandReinforcementButtonState = RequestStandReinforcementButtonState(
        text = "Add favourites",
        visible = true,
        style = ComposeButtonStyle.primary()
    ),
    val removeFavourites: RequestStandReinforcementButtonState = RequestStandReinforcementButtonState(
        text = "Remove favourites",
        visible = false,
        style = ComposeButtonStyle.danger()
    ),
    val reinforcement0: RequestStandReinforcementButtonState = reinforcement("0", 0),
    val reinforcement1: RequestStandReinforcementButtonState = reinforcement("1", 1),
    val reinforcement2: RequestStandReinforcementButtonState = reinforcement("2", 2),
    val reinforcement3: RequestStandReinforcementButtonState = reinforcement("3", 3),
    val reinforcement4: RequestStandReinforcementButtonState = reinforcement("4", 4),
    val reinforcement5: RequestStandReinforcementButtonState = reinforcement("5", 5),
    val reinforcement10: RequestStandReinforcementButtonState = reinforcement("10", 10),
    val reinforcement15: RequestStandReinforcementButtonState = reinforcement("15", 15),
    val reinforcement20: RequestStandReinforcementButtonState = reinforcement("20", 20),
    val reinforcement25: RequestStandReinforcementButtonState = reinforcement("25", 25),
) {
    companion object {
        fun from(isInFavourites: Boolean): RequestStandReinforcementButtonsState {
            return RequestStandReinforcementButtonsState(
                addFavourites = RequestStandReinforcementButtonState(
                    text = "Add favourites",
                    visible = !isInFavourites,
                    style = ComposeButtonStyle.primary()
                ),
                removeFavourites = RequestStandReinforcementButtonState(
                    text = "Remove favourites",
                    visible = isInFavourites,
                    style = ComposeButtonStyle.danger()
                )
            )
        }
        fun reinforcement(text: String, value: Int): RequestStandReinforcementButtonState {
            return RequestStandReinforcementButtonState(
                text = text,
                value = value,
                visible = true,
                style = ComposeButtonStyle.reinforcement()
            )
        }
    }
}
@Immutable
data class RequestStandReinforcementButtonState(
    val text: String,
    val value: Int = 0,
    val visible: Boolean = true,
    val style: ComposeButtonStyle
)
@Immutable
data class ComposeButtonStyle(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color? = null,
    val enabled: Boolean = true,
    val cornerRadiusDp: Int = 12,
    val minHeightDp: Int = 48,
) {
    companion object {
        fun primary() = ComposeButtonStyle(
            containerColor = Color(0xFF1976D2),
            contentColor = Color.White
        )
        fun danger() = ComposeButtonStyle(
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White
        )
        fun neutral() = ComposeButtonStyle(
            containerColor = Color(0xFFE0E0E0),
            contentColor = Color(0xFF111111),
            borderColor = Color(0xFFBDBDBD)
        )
        fun reinforcement() = ComposeButtonStyle(
            containerColor = Color(0xFF455A64),
            contentColor = Color.White
        )
    }
}
@Composable
fun CustomButton(
    text: String,
    style: ComposeButtonStyle,
    visible: Boolean = true,
    onClick: () -> Unit
) {
    if (!visible) return
    val shape = RoundedCornerShape(style.cornerRadiusDp.dp)
    Button(
        onClick = onClick,
        enabled = style.enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.containerColor,
            contentColor = style.contentColor
        ),
        border = style.borderColor?.let { BorderStroke(1.dp, it) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = style.minHeightDp.dp)
    ) {
        Text(text = text)
    }
}
