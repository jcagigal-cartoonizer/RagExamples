package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 221-3: import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.Color
import ifac.td.taxi.ui.screen.compose.InformationMessageUiState
import ifac.td.taxi.ui.screen.compose.InformationMessageInformationMessageDialogButtonType
data class InformationMessageButtonsState(
    val cancelVisible: Boolean,
    val cancelEnabled: Boolean,
    val cancelContainerColor: Color,
    val cancelContentColor: Color,
    val dialogButtons: DialogButtonsState,
) {
    data class DialogButtonsState(
        val cancelVisible: Boolean = true,
        val acceptVisible: Boolean = true,
        val cancelContainerColor: Color,
        val cancelContentColor: Color,
        val acceptContainerColor: Color,
        val acceptContentColor: Color,
    )
    companion object {
        fun from(uiState: InformationMessageUiState): InformationMessageButtonsState {
            // Match XML behavior:
            // - main screen has cancel visible and enabled
            // - dialog has cancel/accept visible
            return InformationMessageButtonsState(
                cancelVisible = true,
                cancelEnabled = true,
                cancelContainerColor = Color(0xFF757575), // neutral gray, adjust to XML
                cancelContentColor = Color.White,
                dialogButtons = DialogButtonsState(
                    cancelVisible = true,
                    acceptVisible = true,
                    cancelContainerColor = Color(0xFF757575),
                    cancelContentColor = Color.White,
                    acceptContainerColor = Color(0xFF2E7D32), // green accept, adjust if needed
                    acceptContentColor = Color.White,
                )
            )
        }
    }
}
> If you know the exact XML colors, replace the `Color(...)` values with those exact hex colors. This structure is designed to mirror the XML state behavior, but the color values must be copied from your `colors.xml` / drawable tints for perfect parity.
