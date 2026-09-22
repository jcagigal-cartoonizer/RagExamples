package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color as ComposeColor
@Immutable
data class GPSTestUiState(
    val gpsType: String = "",
    val utcTime: String = "",
    val satellites: String = "",
    val power: String = "",
    val hdop: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val speed: String = "",
    val heading: String = "",
    val showSpeedAndAngle: Boolean = false,
    val showPower: Boolean = false,
    val showExternalGpsBlocks: Boolean = false,
    val keyActive: Boolean? = null,
    val alarmActive: Boolean? = null,
    val useExternalGps: Boolean? = null,
    val bluetoothName: String? = null,
    val isReplacingGps: Boolean = false,
    val isDebug: Boolean = false,
    val dialogState: DialogState = DialogState.Hidden
)
sealed interface GPSTestUiEffect {
    data class LaunchIntent(val intent: Intent) : GPSTestUiEffect
    data object NavigateBack : GPSTestUiEffect
    data class ShowDialog(val dialogState: DialogState) : GPSTestUiEffect
    data object HideDialog : GPSTestUiEffect
}
sealed interface DialogState {
    data object Hidden : DialogState
    data class Message(
        val title: String,
        val message: String,
        val positiveText: String = "OK",
        val negativeText: String? = null
    ) : DialogState
}
// // # Block 62-2: import androidx.compose.runtime.Immutable
// import androidx.compose.runtime.Immutable
@Immutable
data class GPSTestButtonsState(
    val accept: ButtonState = ButtonState(
        text = "Accept",
        enabled = true,
        visible = true,
        containerColor = ComposeColor(0xFF2E7D32),
        contentColor = ComposeColor.White
    ),
    val gps: ButtonState = ButtonState(
        text = "GPS",
        enabled = true,
        visible = true,
        containerColor = ComposeColor(0xFF1565C0),
        contentColor = ComposeColor.White
    )
) {
    @Immutable
    data class ButtonState(
        val text: String,
        val enabled: Boolean,
        val visible: Boolean,
        val containerColor: ComposeColor,
        val contentColor: ComposeColor,
        val borderColor: ComposeColor? = null
    )
}
