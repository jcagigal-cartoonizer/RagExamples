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
// # Block 5-1: import android.app.Activity
class UserPreferencesNavigationFragment : Fragment() {
    private val viewModel: UserPreferencesComposeViewModel by viewModels()
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
                MaterialTheme {
                    UserPreferencesNavigationContent(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
@Composable
fun UserPreferencesNavigationContent(
    viewModel: UserPreferencesComposeViewModel
) {
    val navController = androidx.navigation.compose.rememberNavController() // not used for XML navigation
    val fragmentNavController = androidx.navigation.fragment.findNavController(
        androidx.compose.ui.platform.LocalView.current.findViewTreeLifecycleOwner()!!.lifecycleOwner as Fragment
    )
}
// # Block 68-2: import android.app.Activity
class UserPreferencesNavigationFragment : Fragment() {
    private val viewModel: UserPreferencesComposeViewModel by viewModels()
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
                MaterialTheme {
                    val navController = findNavController()
                    UserPreferencesScreenHost(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
@Composable
fun UserPreferencesScreenHost(
    navController: androidx.navigation.NavController,
    viewModel: UserPreferencesComposeViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember {
        mutableStateOf<UserPreferencesButtonStylesCustomDialog.UserPreferencesButtonStylesCustomDialogModel?>(null)
    }
    var ringtoneRequestType by remember { mutableIntStateOf(-1) }
    val ringtoneLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.onRingtoneOrNotificationPicked(result.data, ringtoneRequestType)
            }
        }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UserPreferencesUiEffect.NavigateBack -> {
                    navController.popBackStack()
                }
                is UserPreferencesUiEffect.NavigateToDeepLink -> {
                    navController.navigate(
                        NavDeepLinkRequest.Builder
                            .fromUri(effect.uri.toUri())
                            .build()
                    )
                }
                is UserPreferencesUiEffect.NavigateToSecurePin -> {
                    navController.navigate(
                        NavDeepLinkRequest.Builder
                            .fromUri("android-app://ifac.td.taxi/changeDriverPinFragment/".toUri())
                            .build()
                    )
                }
                is UserPreferencesUiEffect.NavigateToPortugalSettings -> {
                    navController.navigate(
                        NavDeepLinkRequest.Builder
                            .fromUri("android-app://ifac.td.taxi/portugalSettingsFragment/".toUri())
                            .build()
                    )
                }
                is UserPreferencesUiEffect.ShowToast -> {
                    // Handle in host if needed
                }
                is UserPreferencesUiEffect.OpenDialog -> {
                    dialogState = effect.dialog
                }
                is UserPreferencesUiEffect.CloseDialog -> {
                    dialogState = null
                }
                is UserPreferencesUiEffect.RequestPhonePermission -> {
                    // Request from host if needed
                }
                is UserPreferencesUiEffect.RequestBluetoothPermission -> {
                    // Request from host if needed
                }
                is UserPreferencesUiEffect.RequestOverlayPermission -> {
                    // Request from host if needed
                }
                is UserPreferencesUiEffect.OpenRingtonePicker -> {
                    ringtoneRequestType = effect.type
                    ringtoneLauncher.launch(effect.intent)
                }
                is UserPreferencesUiEffect.KeepScreenOn -> {
                    // Optional host action
                }
            }
        }
    }
    if (dialogState != null) {
        UserPreferencesButtonStylesUserPreferencesCustomDialog(
            dialog = dialogState!!,
            onDismiss = {
                viewModel.onDialogAction(
                    UserPreferencesUiEffect.DialogAction.Cancel
                )
                dialogState = null
            },
            onAction = { action ->
                viewModel.onDialogAction(action)
                if (action is UserPreferencesUiEffect.DialogAction.Accept) {
                    dialogState = null
                }
            }
        )
    }
    UserPreferencesScreen(
        navController = navController,
        viewModel = viewModel
    )
}
