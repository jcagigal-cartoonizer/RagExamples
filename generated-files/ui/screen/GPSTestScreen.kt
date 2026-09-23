package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 327-6: import android.graphics.Color
import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
@Composable
fun GPSTestScreen(
    uiState: GPSTestUiState,
    buttonsState: GPSTestButtonsState,
    onAcceptClick: () -> Unit,
    onGpsClick: () -> Unit,
) {
    val keyColor = remember(uiState.keyActive) {
        if (uiState.keyActive == true) Color.GREEN else Color.RED
    }
    val alarmColor = remember(uiState.alarmActive) {
        if (uiState.alarmActive == true) Color.GREEN else Color.RED
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "GPS Type: ${uiState.gpsType}")
        Spacer(Modifier.height(8.dp))
        Text(text = "UTC: ${uiState.utcTime}")
        Text(text = "Satellites: ${uiState.satellites}")
        if (uiState.showPower) Text(text = "Power: ${uiState.power}")
        Text(text = "HDOP: ${uiState.hdop}")
        Text(text = "Lat: ${uiState.latitude}")
        Text(text = "Lon: ${uiState.longitude}")
        if (uiState.showSpeedAndAngle) {
            Text(text = "Speed: ${uiState.speed}")
            Text(text = "Angle: ${uiState.heading}")
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "\uF00C", // example font-awesome glyph placeholder
            color = androidx.compose.ui.graphics.Color(keyColor),
            fontFamily = FontFamily.Default
        )
        Text(
            text = "\uF0E7",
            color = androidx.compose.ui.graphics.Color(alarmColor),
            fontFamily = FontFamily.Default
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (buttonsState.accept.visible) {
                Button(
                    onClick = onAcceptClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonsState.accept.containerColor,
                        contentColor = buttonsState.accept.contentColor
                    )
                ) { Text(buttonsState.accept.text) }
            }
            if (buttonsState.gps.visible) {
                Button(
                    onClick = onGpsClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonsState.gps.containerColor,
                        contentColor = buttonsState.gps.contentColor
                    )
                ) { Text(buttonsState.gps.text) }
            }
        }
        if (uiState.dialogState !is DialogState.Hidden) {
            GPSTestCustomDialog(
                state = uiState.dialogState,
                onDismiss = { /* handled by effect in host if needed */ }
            )
        }
    }
}
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
@Composable
fun GPSTestCustomDialog(
    state: DialogState.Message,
    onDismiss: () -> Unit,
    onPositiveClick: () -> Unit = onDismiss,
    onNegativeClick: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(text = state.title) },
        text = { Text(text = state.message) },
        confirmButton = {
            TextButton(onClick = onPositiveClick) {
                Text(state.positiveText)
            }
        },
        dismissButton = if (state.negativeText != null && onNegativeClick != null) {
            {
                TextButton(onClick = onNegativeClick) {
                    Text(state.negativeText)
                }
            }
        } else null
    )
}
