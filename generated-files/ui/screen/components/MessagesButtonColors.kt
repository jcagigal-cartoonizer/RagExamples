package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 337-5: import androidx.compose.foundation.shape.RoundedCornerShape
@Composable
fun messagesButtonColors(isPrimary: Boolean, isDanger: Boolean = false): ButtonColors {
    return when {
        isDanger -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
        isPrimary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        else -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    }
}
val messagesButtonShape = RoundedCornerShape(12.dp)
