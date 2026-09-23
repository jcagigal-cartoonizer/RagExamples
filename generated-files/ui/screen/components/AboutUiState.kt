package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 276-4: import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Immutable
@Immutable
data class AboutUiState(
    val appInfo: String,
    val privacyPolicyText: String,
    val bluetoothInfoText: String,
    val showBluetoothInfo: Boolean,
    val buttonsState: AboutButtonsState,
)
sealed interface AboutUiEvent {
    data object ClickPrivacy : AboutUiEvent
    data object ClickAccept : AboutUiEvent
    data object LogoTapped : AboutUiEvent
    data object DismissWarningDialog : AboutUiEvent
}
sealed interface AboutUiEffect {
    data object NavigateBack : AboutUiEffect
    data object ShowWarningDialog : AboutUiEffect
}
