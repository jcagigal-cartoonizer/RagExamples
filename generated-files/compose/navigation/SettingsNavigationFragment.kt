package ifac.td.taxi.compose.navigation
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
// # Block 4-1: import android.os.Bundle
class SettingsNavigationFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        SettingsScreen(
                            state = uiState,
                            onEvent = { event ->
                                handleEvent(event)
                            }
                        )
                    }
                }
            }
        }
    }
    fun handleEvent(event: SettingsUiEvent) {
        when (event) {
            SettingsUiEvent.ConfigurationClicked -> {
                // If you have a navigation action for configuration, use it here.
                // Example:
                // findNavController().navigate(R.id.action_settingsNavigationFragment_to_userConfigurationFragment)
                viewModel.clickConfiguration()
            }
            SettingsUiEvent.DeviceSettingsClicked -> {
                viewModel.clickDeviceSettings()
                // If the VM does not navigate directly, do it here:
                // findNavController().navigate(R.id.action_settingsNavigationFragment_to_deviceSettingsFragment)
            }
            SettingsUiEvent.GpsClicked -> {
                viewModel.clickGPS()
                // Example:
                // findNavController().navigate(R.id.action_settingsNavigationFragment_to_GPSConfigFragment)
            }
            SettingsUiEvent.AboutClicked -> {
                viewModel.clickAbout()
                // Example:
                // findNavController().navigate(R.id.action_settingsNavigationFragment_to_aboutFragment)
            }
            SettingsUiEvent.LightClicked -> {
                viewModel.clickLights()
                // Example:
                // findNavController().navigate(R.id.action_settingsNavigationFragment_to_lightsTestFragment)
            }
            SettingsUiEvent.RequirementsClicked -> {
                viewModel.clickRequirements()
                // Example:
                // findNavController().navigate(R.id.action_settingsNavigationFragment_to_requirementsFragment)
            }
            SettingsUiEvent.PreferenciasClicked -> {
                // Old fragment used a password dialog before navigating.
                // Keep that logic in the ViewModel or handle it here.
                viewModel.onPreferenciasClicked()
            }
            SettingsUiEvent.BluetoothClicked -> {
                viewModel.onBluetoothClicked()
            }
            SettingsUiEvent.WebViewClicked -> {
                viewModel.onWebViewClicked()
            }
            SettingsUiEvent.DialogDismissed -> {
                viewModel.onDialogDismissed()
            }
            is SettingsUiEvent.DialogConfirmed -> {
                viewModel.onDialogConfirmed(event.value)
            }
        }
    }
}
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
val uiState by viewModel.uiState.collectAsState()
Replace the old fragment destination:
<fragment
    android:id="@+id/settingsFragment"
    android:name="ifac.td.taxi.ui.screen.SettingsFragment"
    android:label="SettingsFragment"
    tools:layout="@layout/fragment_settings">
with:
<fragment
    android:id="@+id/settingsFragment"
    android:name="ifac.td.taxi.ui.screen.SettingsNavigationFragment"
    android:label="SettingsNavigationFragment"
    tools:layout="@layout/fragment_settings" />
private val viewModel: SettingsViewModel by viewModels()
Your old `SettingsFragment` contained direct navigation actions like:
fun handleEvent(event: SettingsUiEvent) {
    when (event) {
        SettingsUiEvent.ConfigurationClicked ->
            findNavController().navigate(R.id.action_settingsFragment_to_userConfigurationFragment)
        SettingsUiEvent.DeviceSettingsClicked ->
            findNavController().navigate(R.id.action_settingsFragment_to_deviceSettingsFragment)
        SettingsUiEvent.GpsClicked ->
            findNavController().navigate(R.id.action_settingsFragment_to_GPSConfigFragment)
        SettingsUiEvent.AboutClicked ->
            findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
        SettingsUiEvent.LightClicked ->
            findNavController().navigate(R.id.action_settingsFragment_to_lightsTestFragment)
        SettingsUiEvent.RequirementsClicked ->
            findNavController().navigate(R.id.action_settingsFragment_to_requirementsFragment)
        SettingsUiEvent.PreferenciasClicked ->
            findNavController().navigate(R.id.action_settingsFragment_to_userConfigurationFragment)
        SettingsUiEvent.BluetoothClicked ->
            viewModel.onBluetoothClicked()
        SettingsUiEvent.WebViewClicked ->
            viewModel.onWebViewClicked()
        SettingsUiEvent.DialogDismissed ->
            viewModel.onDialogDismissed()
        is SettingsUiEvent.DialogConfirmed ->
            viewModel.onDialogConfirmed(event.value)
    }
}
