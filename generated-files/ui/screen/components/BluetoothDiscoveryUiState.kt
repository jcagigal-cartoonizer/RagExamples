package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 124-2: import androidx.annotation.DrawableRes
// import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
data class BluetoothDiscoveryUiState(
    val isDiscovering: Boolean = false,
    val devices: List<BluetoothInfo> = emptyList(),
    val selectedDevice: BluetoothInfo? = null,
    val savedDevice: BluetoothInfo? = null,
    val isLoaded: Boolean = false,
    val externalGpsEnabled: Boolean = false,
    val showExternalGpsCheckbox: Boolean = false,
    val isBluetoothEnabled: Boolean = true,
    val isLocationEnabled: Boolean = true,
    val title: String = "Bluetooth",
    @DrawableRes val taximeterImageRes: Int = R.drawable.background_dialog_transparent,
    val buttons: BluetoothDiscoveryButtonsState = BluetoothDiscoveryButtonsState(),
    val pendingRemoveDevice: BluetoothInfo? = null,
    val pendingDeselectDevice: BluetoothInfo? = null,
)
sealed interface BluetoothDiscoveryUiEvent {
    data object ScreenStarted : BluetoothDiscoveryUiEvent
    data object DiscoverClicked : BluetoothDiscoveryUiEvent
    data object AcceptClicked : BluetoothDiscoveryUiEvent
    data object CancelClicked : BluetoothDiscoveryUiEvent
    data class DeviceSelected(val device: BluetoothInfo) : BluetoothDiscoveryUiEvent
    data class DeviceDeselected(val device: BluetoothInfo) : BluetoothDiscoveryUiEvent
    data class ExternalGpsChanged(val checked: Boolean) : BluetoothDiscoveryUiEvent
    data class BluetoothPermissionResult(val granted: Boolean) : BluetoothDiscoveryUiEvent
    data class LocationPermissionResult(val granted: Boolean) : BluetoothDiscoveryUiEvent
    data object BluetoothDialogAccept : BluetoothDiscoveryUiEvent
    data object LocationDialogAccept : BluetoothDiscoveryUiEvent
    data object PermissionDialogCancel : BluetoothDiscoveryUiEvent
}
sealed interface BluetoothDiscoveryUiEffect {
    data class ShowDialog(val dialogState: BluetoothDiscoveryDialogState) : BluetoothDiscoveryUiEffect
    data object HideDialog : BluetoothDiscoveryUiEffect
    data object RequestBluetoothPermission : BluetoothDiscoveryUiEffect
    data object RequestLocationPermission : BluetoothDiscoveryUiEffect
    data object OpenBluetoothSettings : BluetoothDiscoveryUiEffect
    data object OpenLocationSettings : BluetoothDiscoveryUiEffect
    data object NavigateBack : BluetoothDiscoveryUiEffect
    data object ConnectTaximeterAndBack : BluetoothDiscoveryUiEffect
    data class ShowToast(@StringRes val messageRes: Int) : BluetoothDiscoveryUiEffect
    data class OpenPermissionSettings(val permission: String) : BluetoothDiscoveryUiEffect
}
enum class BluetoothDiscoveryDialogType {
    BluetoothPermission,
    LocationPermission,
    EnableBluetooth,
    EnableLocation,
    ReplaceSavedDevice,
    UnlinkSavedDevice,
    SaveResult,
    RemoveResult
}
data class BluetoothDiscoveryDialogState(
    val type: BluetoothDiscoveryDialogType,
    val title: String,
    val description: String,
    val isCancelable: Boolean = true,
    val buttons: List<BluetoothDiscoveryDialogButtonSpec> = listOf(
        BluetoothDiscoveryDialogButtonSpec.Cancel,
        BluetoothDiscoveryDialogButtonSpec.Accept
    )
)
sealed interface BluetoothDiscoveryDialogAction {
    data object Accept : BluetoothDiscoveryDialogAction
    data object Cancel : BluetoothDiscoveryDialogAction
    data object Dismiss : BluetoothDiscoveryDialogAction
}
sealed class BluetoothDiscoveryDialogButtonSpec(
    val label: String,
    val type: BluetoothDiscoveryDialogButtonType
) {
    data object Cancel : BluetoothDiscoveryDialogButtonSpec("Cancel", BluetoothDiscoveryDialogButtonType.Cancel)
    data object Accept : BluetoothDiscoveryDialogButtonSpec("Accept", BluetoothDiscoveryDialogButtonType.Accept)
}
enum class BluetoothDiscoveryDialogButtonType { Cancel, Accept }
data class BluetoothDiscoveryButtonsState(
    val discover: BluetoothDiscoveryButtonVisualState = BluetoothDiscoveryButtonVisualState.PrimaryEnabled,
    val accept: BluetoothDiscoveryButtonVisualState = BluetoothDiscoveryButtonVisualState.PrimaryEnabled,
    val cancel: BluetoothDiscoveryButtonVisualState = BluetoothDiscoveryButtonVisualState.SecondaryEnabled,
)
sealed interface BluetoothDiscoveryButtonVisualState {
    data object PrimaryEnabled : BluetoothDiscoveryButtonVisualState
    data object PrimaryLoading : BluetoothDiscoveryButtonVisualState
    data object PrimaryDisabled : BluetoothDiscoveryButtonVisualState
    data object SecondaryEnabled : BluetoothDiscoveryButtonVisualState
    data object SecondaryDisabled : BluetoothDiscoveryButtonVisualState
}
data class BluetoothDiscoveryListItemState(
    val device: BluetoothInfo,
    val isSelected: Boolean,
    val isSaved: Boolean
)
