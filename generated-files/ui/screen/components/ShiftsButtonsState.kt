package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ShiftsButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 251-3: import androidx.compose.ui.graphics.Color
data class ShiftsButtonsState(
    val isOpen: Boolean = false,
    val isSelectionMode: Boolean = false,
    val mainRotation: Float = 0f,
    val exportAlpha: Float = 0f,
    val trashAlpha: Float = 0f,
    val exportTranslationY: Float = 100f,
    val trashTranslationY: Float = 100f,
    val mainIconTint: Color = Color.Unspecified,
    val exportVisible: Boolean = false,
    val trashVisible: Boolean = false,
    val sortIdArrow: ArrowState = ArrowState.Hidden,
    val sortAmountArrow: ArrowState = ArrowState.Hidden
) {
    companion object {
        fun closed() = ShiftsButtonsState(
            isOpen = false,
            isSelectionMode = false,
            mainRotation = 0f,
            exportAlpha = 0f,
            trashAlpha = 0f,
            exportTranslationY = 100f,
            trashTranslationY = 100f,
            exportVisible = false,
            trashVisible = false
        )
        fun opened() = ShiftsButtonsState(
            isOpen = true,
            isSelectionMode = true,
            mainRotation = 180f,
            exportAlpha = 1f,
            trashAlpha = 1f,
            exportTranslationY = 0f,
            trashTranslationY = 0f,
            exportVisible = true,
            trashVisible = true
        )
    }
}
