package ifac.td.taxi.ui.screen
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
// # Block 11-1: import androidx.compose.foundation.layout.*
@Composable
fun ChangeDriverPinScreen(
    viewModel: ChangeDriverPinComposeViewModel,
    onNavigateBack: () -> Unit,
    onShowToast: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<ChangeDriverPinCustomDialogStateCustomDialogState?>(null) }
    // Collect one-shot effects in a lifecycle-safe way
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                ChangeDriverPinUiEffect.NavigateBack -> onNavigateBack()
                is ChangeDriverPinUiEffect.ShowDialog -> {
                    dialogState = effect.dialogState
                }
                is ChangeDriverPinUiEffect.ShowToast -> {
                    onShowToast(effect.message)
                }
            }
        }
    }
    if (dialogState != null) {
        ChangeDriverPinCustomDialogStateCustomDialog(
            state = dialogState!!,
            onDismiss = { dialogState = null },
            onButtonClick = { button ->
                // Forward dialog action to ViewModel
                viewModel.onDialogButtonClicked(button)
                dialogState = null
            }
        )
    }
    ChangeDriverPinContent(
        state = state,
        buttonsState = state.buttonsState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}
