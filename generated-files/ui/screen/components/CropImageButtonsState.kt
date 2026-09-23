package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.CropImageButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 68-4: import androidx.compose.runtime.Immutable
@Immutable
data class CropImageButtonsState(
    val acceptVisible: Boolean = true,
    val cropVisible: Boolean = true,
    val selectImageVisible: Boolean = true,
    val acceptEnabled: Boolean = true,
    val cropEnabled: Boolean = true,
    val selectImageEnabled: Boolean = true,
    val acceptContainerColor: Color,
    val acceptContentColor: Color,
    val cropContainerColor: Color,
    val cropContentColor: Color,
    val selectContainerColor: Color,
    val selectContentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color
) {
    companion object {
        fun default(): CropImageButtonsState {
            return CropImageButtonsState(
                acceptVisible = true,
                cropVisible = true,
                selectImageVisible = true,
                acceptEnabled = true,
                cropEnabled = true,
                selectImageEnabled = true,
                acceptContainerColor = Color(0xFF2E7D32),   // green-ish
                acceptContentColor = Color.White,
                cropContainerColor = Color(0xFF1565C0),     // blue-ish
                cropContentColor = Color.White,
                selectContainerColor = Color(0xFF455A64),   // gray-blue
                selectContentColor = Color.White,
                disabledContainerColor = Color(0xFFE0E0E0),
                disabledContentColor = Color(0xFF9E9E9E)
            )
        }
    }
}
