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
// # Block 57-2: import androidx.compose.runtime.Immutable
enum class BluetoothDiscoveryComposeButtonState {
    ENABLE,
    DISABLE,
    LOADING
}
@Immutable
data class BluetoothDiscoveryButtonsState(
    val discoverState: BluetoothDiscoveryComposeButtonState = BluetoothDiscoveryComposeButtonState.ENABLE,
    val acceptState: BluetoothDiscoveryComposeButtonState = BluetoothDiscoveryComposeButtonState.ENABLE,
    val cancelState: BluetoothDiscoveryComposeButtonState = BluetoothDiscoveryComposeButtonState.ENABLE,
    val externalGpsVisible: Boolean = false,
    val externalGpsChecked: Boolean = false,
) {
    val discoverEnabled: Boolean get() = discoverState == BluetoothDiscoveryComposeButtonState.ENABLE
    val acceptEnabled: Boolean get() = acceptState == BluetoothDiscoveryComposeButtonState.ENABLE
    val cancelEnabled: Boolean get() = cancelState == BluetoothDiscoveryComposeButtonState.ENABLE
    val isDiscoverLoading: Boolean get() = discoverState == BluetoothDiscoveryComposeButtonState.LOADING
    companion object {
        fun from(
            shiftDisconnected: Boolean,
            discovering: Boolean,
            externalGpsVisible: Boolean,
            externalGpsChecked: Boolean
        ): BluetoothDiscoveryButtonsState {
            val baseState = if (shiftDisconnected) BluetoothDiscoveryComposeButtonState.ENABLE else BluetoothDiscoveryComposeButtonState.DISABLE
            return BluetoothDiscoveryButtonsState(
                discoverState = if (discovering) BluetoothDiscoveryComposeButtonState.LOADING else baseState,
                acceptState = baseState,
                cancelState = BluetoothDiscoveryComposeButtonState.ENABLE,
                externalGpsVisible = externalGpsVisible,
                externalGpsChecked = externalGpsChecked
            )
        }
    }
}
