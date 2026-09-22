package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.graphics.Color
data class LegalTextButtonsState(
    val acceptVisible: Boolean = true,
    val acceptEnabled: Boolean = true,
    val acceptBackgroundColor: Color = Color(0xFF1E88E5),
    val acceptContentColor: Color = Color.White,
    val acceptDisabledBackgroundColor: Color = Color(0xFFBDBDBD),
    val acceptDisabledContentColor: Color = Color(0xFFE0E0E0),
    val acceptCornerRadiusDp: Int = 12,
    val acceptMinHeightDp: Int = 48,
    val acceptPaddingVerticalDp: Int = 14,
    val acceptPaddingHorizontalDp: Int = 20,
)
