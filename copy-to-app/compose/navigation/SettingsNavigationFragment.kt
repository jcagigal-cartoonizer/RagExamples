package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.SettingsScreen
import ifac.td.taxi.compose.viewModel.SettingsComposeViewModel
import ifac.td.taxi.viewModel.SettingsViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
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
