package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 447-4: import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color
data class AddAmountButtonsState(
    val acceptEnabled: Boolean = true,
    val cancelEnabled: Boolean = true,
    val acceptVisible: Boolean = true,
    val cancelVisible: Boolean = true,
    val acceptContainerColor: Color = Color(0xFF1E88E5),
    val acceptContentColor: Color = Color.White,
    val acceptDisabledContainerColor: Color = Color(0xFFBDBDBD),
    val acceptDisabledContentColor: Color = Color(0xFFFFFFFF),
    val cancelContainerColor: Color = Color(0xFFE53935),
    val cancelContentColor: Color = Color.White,
    val cancelDisabledContainerColor: Color = Color(0xFFBDBDBD),
    val cancelDisabledContentColor: Color = Color(0xFFFFFFFF),
    val serviceEnabled: Boolean = true,
    val extraEnabled: Boolean = false,
    val tollsEnabled: Boolean = true,
    val tipsEnabled: Boolean = true,
    val extraVisible: Boolean = true,
    val tollsVisible: Boolean = true,
    val tipsVisible: Boolean = true,
)
