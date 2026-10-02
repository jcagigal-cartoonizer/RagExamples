package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.UserPreferencesScreen
import ifac.td.taxi.compose.viewModel.UserPreferencesComposeViewModel
import ifac.td.taxi.viewModel.UserPreferencesViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
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
