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
// # Block 7-1: import android.Manifest
@Composable
fun SettingsRoute(
    navController: NavController,
    viewModel: SettingsComposeViewModel,
    showHeader: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        showHeader(true)
        viewModel.onScreenShown()
    }
    LaunchedEffect(viewModel.uiEffect) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    SettingsUiEffect.NavigateToUserLogin -> {
                        navController.navigate(R.id.action_settingsFragment_to_userLoginFragment)
                    }
                    SettingsUiEffect.NavigateToDeviceSettings -> {
                        context.startActivity(
                            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                    SettingsUiEffect.NavigateToDiscoveryChannel -> {
                        navController.navigate(R.id.action_settingsFragment_to_discoveryChannelFragment)
                    }
                    SettingsUiEffect.NavigateToAbout -> {
                        navController.navigate(R.id.action_settingsFragment_to_aboutFragment)
                    }
                    SettingsUiEffect.NavigateToGPS -> {
                        navController.navigate(R.id.action_settingsFragment_to_GPSConfigFragment)
                    }
                    SettingsUiEffect.NavigateToLights -> {
                        navController.navigate(R.id.action_settingsFragment_to_lightsTestFragment)
                    }
                    SettingsUiEffect.NavigateToRequirements -> {
                        navController.navigate(R.id.action_settingsFragment_to_toolsFragment)
                    }
                    SettingsUiEffect.NavigateToUserConfiguration -> {
                        navController.navigate(R.id.action_settingsFragment_to_userConfigurationFragment)
                    }
                    SettingsUiEffect.NavigateToWebView(valUrl = effect.url) -> {
                        navController.navigate(
                            R.id.action_settingsFragment_to_webViewFragment
                        )
                    }
                    SettingsUiEffect.RequestBluetoothPermission -> {
                        // call your permission launcher from UI
                    }
                    SettingsUiEffect.ShowToastIncorrectPin -> {
                        // show toast/snackbar in UI layer
                    }
                }
            }
        }
    }
    SettingsScreen(
        state = uiState,
        onEvent = viewModel::onEvent,
    )
}
