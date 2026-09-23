package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.LoginDriverComposeViewModel
import ifac.td.taxi.ui.screen.components.LoginDriverUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 277-3: import android.widget.Toast
@Composable
fun LoginDriverRoute(
    connectionMode: Int,
    startTurnMode: Int,
    onNavigateBack: () -> Unit,
    onNavigateToChangePin: () -> Unit,
    onLoginWithoutCentralFinished: () -> Unit,
    viewModel: LoginDriverComposeViewModel = viewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(connectionMode, startTurnMode) {
        viewModel.onArgsReceived(connectionMode, startTurnMode)
    }
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    LoginDriverUiEffect.NavigateBack -> onNavigateBack()
                    LoginDriverUiEffect.NavigateToChangePin -> onNavigateToChangePin()
                    LoginDriverUiEffect.LoginWithoutCentralFinished -> onLoginWithoutCentralFinished()
                    LoginDriverUiEffect.ShowNetworkErrorToast ->
                        Toast.makeText(context, context.getString(ifac.td.taxi.R.string.network_error), Toast.LENGTH_SHORT).show()
                    LoginDriverUiEffect.ShowIncorrectLoginNoCredentialsToast ->
                        Toast.makeText(context, context.getString(ifac.td.taxi.R.string.incorrect_login_no_credentials), Toast.LENGTH_LONG).show()
                    LoginDriverUiEffect.ShowErrorLoginToast ->
                        Toast.makeText(context, context.getString(ifac.td.taxi.R.string.error_login), Toast.LENGTH_SHORT).show()
                    is LoginDriverUiEffect.ShowIncorrectCredentials -> {
                        // handled in composable dialog state below
                    }
                    is LoginDriverUiEffect.SetLastSessionDriver -> Unit
                    is LoginDriverUiEffect.SetButtonLoading -> Unit
                    is LoginDriverUiEffect.ResetButtons -> Unit
                }
            }
        }
    }
    LoginDriverScreen(
        uiState = uiState,
        onDriverChange = viewModel::onDriverChanged,
        onPasswordChange = viewModel::onPasswordChanged,
        onCancel = viewModel::onCancelClicked,
        onLoginCentral = { viewModel.loginDriver(uiState.driverId, uiState.password, false) },
        onLoginRefuerzo = { viewModel.loginDriver(uiState.driverId, uiState.password, true) },
        onLoginWithoutCentral = viewModel::loginWithoutCentral,
        onChangePin = viewModel::clickChangePin,
        onDismissDialog = { /* local dialog state */ }
    )
}
Includes field/container logic, visibility, button styles, and dialog handling.
