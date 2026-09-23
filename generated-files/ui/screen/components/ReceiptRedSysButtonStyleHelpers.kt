package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 486-8: import androidx.compose.foundation.BorderStroke
object ReceiptRedSysButtonStyleHelpers {
    @Composable
    fun refundColors() = ButtonDefaults.buttonColors(
        containerColor = androidx.compose.ui.graphics.Color(0xFFE53935),
        contentColor = androidx.compose.ui.graphics.Color.White,
        disabledContainerColor = androidx.compose.ui.graphics.Color(0xFFFFCDD2),
        disabledContentColor = androidx.compose.ui.graphics.Color(0xFF8D6E63)
    )
    @Composable
    fun printColors() = ButtonDefaults.buttonColors(
        containerColor = androidx.compose.ui.graphics.Color(0xFF1E88E5),
        contentColor = androidx.compose.ui.graphics.Color.White,
        disabledContainerColor = androidx.compose.ui.graphics.Color(0xFFBBDEFB),
        disabledContentColor = androidx.compose.ui.graphics.Color(0xFF607D8B)
    )
    @Composable
    fun acceptColors() = ButtonDefaults.buttonColors(
        containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
        contentColor = androidx.compose.ui.graphics.Color.White
    )
    fun padding() = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    fun border() = BorderStroke(1.dp, androidx.compose.ui.graphics.Color.Transparent)
}
Custom action button:
