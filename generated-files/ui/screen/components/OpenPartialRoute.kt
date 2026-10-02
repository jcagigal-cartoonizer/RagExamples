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
// # Block 211-3: import androidx.compose.foundation.layout.*
@Composable
fun OpenPartialRoute(
    navController: NavController,
    viewModel: OpenPartialComposeViewModel,
    canClose: Boolean,
    showToast: (Int) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(canClose) {
        viewModel.init(canClose)
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                OpenPartialUiEffect.NavigateBack -> navController.popBackStack()
                OpenPartialUiEffect.NavigateToTotalizers ->
                    navController.navigate(R.id.action_openPartialFragment_to_totalizersFragment)
                OpenPartialUiEffect.ShowToastNoPartialsToPrint -> showToast(R.string.no_partials_to_print)
                OpenPartialUiEffect.RequestClosePartialsConfirmation -> viewModel.showCloseDialog()
                OpenPartialUiEffect.ClosePartialsAndNavigate ->
                    navController.navigate(R.id.action_openPartialsFragment_to_closedPartialFragment)
            }
        }
    }
    OpenPartialScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}
