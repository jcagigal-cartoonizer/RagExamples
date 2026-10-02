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
// # Block 9-1: import android.Manifest
@Composable
fun PermissionsRoute(
    navController: NavController,
    viewModel: PermissionsComposeViewModel,
    safePermissionType: String? = null, // equivalent to navArgs()
    onRequestPermission: (String) -> Unit,
    onLaunchOverlayPermission: () -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // Lifecycle-aware effect collector
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collectLatest { effect ->
                when (effect) {
                    is PermissionsUiEffect.RequestPermission ->
                        onRequestPermission(effect.permission)
                    PermissionsUiEffect.RequestManageWriteSettings ->
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    PermissionsUiEffect.RequestIgnoreBatteryOptimization ->
                        context.startActivity(
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        )
                    PermissionsUiEffect.LaunchOverlayPermission ->
                        onLaunchOverlayPermission()
                    PermissionsUiEffect.ShowToastNotifDefault ->
                        onShowToast(R.string.toast_notif_activadas_por_defecto)
                    PermissionsUiEffect.ShowToastOldBackgroundLocation ->
                        onShowToast(R.string.toast_android_antiguo_ubicacion_seg_plano)
                    PermissionsUiEffect.NavigateToChangePassword ->
                        navController.navigate(R.id.action_permissionsFragment_to_changePasswordRedSysFragment3)
                    is PermissionsUiEffect.Navigate ->
                        navController.navigate(effect.resId)
                    is PermissionsUiEffect.ShowToast ->
                        onShowToast(effect.messageRes)
                    is PermissionsUiEffect.OpenRedSysDialog -> Unit // handled by Compose dialog state
                }
            }
        }
    }
    PermissionsScreen(
        uiState = uiState,
        safePermissionType = safePermissionType,
        onEvent = viewModel::onEvent
    )
}
