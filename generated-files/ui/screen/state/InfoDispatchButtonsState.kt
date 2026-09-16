package ifac.td.taxi.ui.screen.state
import ifac.td.taxi.ui.screen.state.InfoDispatchButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // ## `InfoDispatchButtonsState.kt`


import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class InfoDispatchButtonsState(
    val notifications: ButtonUiState = ButtonUiState(
        visible = true,
        enabled = false,
        style = ButtonStyleUi.Disabled,
        background = ButtonBackgroundUi.Gray,
        text = "NOTIFICATIONS"
    ),
    val navigate: ButtonUiState = ButtonUiState(
        visible = true,
        enabled = true,
        style = ButtonStyleUi.Enabled,
        background = ButtonBackgroundUi.Blue,
        text = "NAVIGATE"
    ),
    val voiceCall: ButtonUiState = ButtonUiState(
        visible = true,
        enabled = true,
        style = ButtonStyleUi.Enabled,
        background = ButtonBackgroundUi.Green,
        text = "CALL"
    ),
    val print: ButtonUiState = ButtonUiState(
        visible = true,
        enabled = true,
        style = ButtonStyleUi.Enabled,
        background = ButtonBackgroundUi.Blue,
        text = "PRINT"
    ),
    val noClient: ButtonUiState = ButtonUiState(
        visible = true,
        enabled = true,
        style = ButtonStyleUi.Enabled,
        background = ButtonBackgroundUi.Red,
        text = "NO CLIENT"
    ),
    val returnDispatch: ButtonUiState = ButtonUiState(
        visible = true,
        enabled = false,
        style = ButtonStyleUi.Disabled,
        background = ButtonBackgroundUi.Red,
        text = "RETURN"
    ),
)

@Immutable
data class ButtonUiState(
    val visible: Boolean,
    val enabled: Boolean,
    val style: ButtonStyleUi,
    val background: ButtonBackgroundUi,
    val text: String,
)

enum class ButtonStyleUi { Enabled, Disabled, Loading }

enum class ButtonBackgroundUi { Green, Red, Orange, Blue, Gray }


// # 5) Compose button styling helpers

These mimic the old custom button behavior as closely as possible using Compose.

// ## `InfoDispatchButtonStyles.kt`


import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

data class ComposeButtonStyle(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val shapeRadius: Int = 12
)

fun ButtonBackgroundUi.toColor(): Color = when (this) {
    ButtonBackgroundUi.Green -> Color(0xFF2E7D32)
    ButtonBackgroundUi.Red -> Color(0xFFC62828)
    ButtonBackgroundUi.Orange -> Color(0xFFEF6C00)
    ButtonBackgroundUi.Blue -> Color(0xFF1565C0)
    ButtonBackgroundUi.Gray -> Color(0xFF616161)
}

fun infoDispatchButtonStyle(background: ButtonBackgroundUi): ComposeButtonStyle {
    val color = background.toColor()
    return ComposeButtonStyle(
        containerColor = color,
        contentColor = Color.White,
        disabledContainerColor = color.copy(alpha = 0.45f),
        disabledContentColor = Color.White.copy(alpha = 0.75f),
        shapeRadius = 12
    )
}


// # 6) Compose buttons row

