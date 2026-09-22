package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
@Immutable
data class LoginUserRedSysButtonsState(
    val cancelVisible: Boolean = true,
    val acceptVisible: Boolean = true,
    val acceptEnabled: Boolean = true,
    val acceptLoading: Boolean = false,
    val acceptBackground: Color = Color(0xFF2E7D32), // green
    val acceptContentColor: Color = Color.White,
    val cancelBackground: Color = Color(0xFFE0E0E0),
    val cancelContentColor: Color = Color.Black
) {
    companion object {
        fun enabledGreen() = LoginUserRedSysButtonsState(
            acceptEnabled = true,
            acceptLoading = false,
            acceptBackground = Color(0xFF2E7D32),
            acceptContentColor = Color.White
        )
        fun loading() = LoginUserRedSysButtonsState(
            acceptEnabled = false,
            acceptLoading = true,
            acceptBackground = Color(0xFF2E7D32),
            acceptContentColor = Color.White
        )
        fun disabled() = LoginUserRedSysButtonsState(
            acceptEnabled = false,
            acceptLoading = false,
            acceptBackground = Color(0xFFA5D6A7),
            acceptContentColor = Color.White
        )
    }
}
object LoginUserRedSysButtonStyles {
    val Green = Color(0xFF2E7D32)
    val GreenDisabled = Color(0xFFA5D6A7)
    val Gray = Color(0xFFE0E0E0)
    val Black = Color(0xFF212121)
    val White = Color.White
}
