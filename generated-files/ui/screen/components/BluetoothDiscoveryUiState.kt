package ifac.td.taxi.ui.screen.components
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// # Block 11-1: import ifac.td.taxi.framework.sdk.model.BluetoothInfo
data class BluetoothDiscoveryUiState(
    val savedDevice: BluetoothInfo? = null,
    val discoveredDevices: List<BluetoothInfo> = emptyList(),
    val selectedDevice: BluetoothInfo? = null,
    val isDiscovering: Boolean = false,
    val isBluetoothEnabled: Boolean = true,
    val isLocationEnabled: Boolean = true,
    val hasBluetoothPermission: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val showExternalGpsCheckbox: Boolean = false,
    val externalGpsChecked: Boolean = false,
    val shiftDisconnected: Boolean = true,
    val titleText: String = "Bluetooth",
    val titleImageRes: Int = android.R.color.transparent,
    val isSavedDeviceLoaded: Boolean = false,
)
sealed interface BluetoothDiscoveryUiEffect {
    data class ShowDialog(val dialog: BluetoothDiscoveryDialogState) : BluetoothDiscoveryUiEffect
    data class ShowToast(val messageRes: Int) : BluetoothDiscoveryUiEffect
    data class RequestBluetoothPermission(val permission: String) : BluetoothDiscoveryUiEffect
    data class RequestLocationPermission(val permission: String) : BluetoothDiscoveryUiEffect
    data class OpenBluetoothSettings(val intentAction: String) : BluetoothDiscoveryUiEffect
    data class OpenLocationSettings(val intentAction: String) : BluetoothDiscoveryUiEffect
    data object NavigateBack : BluetoothDiscoveryUiEffect
    data object ConnectTaximeter : BluetoothDiscoveryUiEffect
}
data class BluetoothDiscoveryDialogState(
    val title: String,
    val description: String,
    val buttons: List<BluetoothDialogButton> = listOf(
        BluetoothDialogButton.Cancel,
        BluetoothDialogButton.Accept
    ),
    val cancellable: Boolean = true,
)
enum class BluetoothDialogButton {
    Cancel,
    Accept
}
