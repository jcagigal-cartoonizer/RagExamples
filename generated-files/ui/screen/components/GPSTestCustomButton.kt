package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
object GPSTestButtonStyle {
    fun primaryContainer() = ComposeColor(0xFF2E7D32)
    fun secondaryContainer() = ComposeColor(0xFF1565C0)
    fun disabledContainer() = ComposeColor(0xFFBDBDBD)
    fun primaryContent() = ComposeColor.White
    fun mutedText() = ComposeColor(0xFF757575)
}
