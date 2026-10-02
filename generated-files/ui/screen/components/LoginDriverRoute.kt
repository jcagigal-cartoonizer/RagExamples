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
