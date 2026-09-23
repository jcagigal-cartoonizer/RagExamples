package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 197-3: import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color
data class LegalTextButtonsState(
    val acceptVisible: Boolean = true,
    val acceptEnabled: Boolean = true,
    // Exact styling placeholders: adapt these to match your XML/custom button colors
    val acceptBackgroundColor: Color = Color(0xFF1E88E5),
    val acceptContentColor: Color = Color.White,
    val acceptDisabledBackgroundColor: Color = Color(0xFFBDBDBD),
    val acceptDisabledContentColor: Color = Color(0xFFE0E0E0),
    val acceptCornerRadiusDp: Int = 12,
    val acceptMinHeightDp: Int = 48,
    val acceptPaddingVerticalDp: Int = 14,
    val acceptPaddingHorizontalDp: Int = 20,
)
