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
// # Block 169-2: import androidx.compose.foundation.layout.*
@Composable
fun PortugalSettingsRoute(
    viewModel: PortugalSettingsComposeViewModel,
    onNavigateBack: () -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var dialogState by remember { mutableStateOf<PortugalDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.onEvent(PortugalSettingsUiEvent.ScreenShown)
    }
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                PortugalSettingsUiEffect.NavigateBack -> onNavigateBack()
                PortugalSettingsUiEffect.ShowPinIncorrectToast -> onShowToast(R.string.pin_incorrecto)
                PortugalSettingsUiEffect.OpenCurrentPinDialog -> {
                    dialogState = PortugalDialogState.CurrentPin
                }
                PortugalSettingsUiEffect.OpenNewPinDialog -> {
                    dialogState = PortugalDialogState.NewPin
                }
            }
        }
    }
    PortugalSettingsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        dialogState = dialogState,
        onDismissDialog = { dialogState = null }
    )
    when (dialogState) {
        PortugalDialogState.CurrentPin -> {
            PortugalSettingsCustomDialogCustomDialog(
                title = stringResource(R.string.pin_actual),
                description = stringResource(R.string.pin_actual_hint),
                editTextTypePin = true,
                editTextMaxLength = 4,
                onCancel = {
                    dialogState = null
                },
                onAccept = { pin ->
                    dialogState = null
                    viewModel.onEvent(PortugalSettingsUiEvent.PinEntered(pin))
                }
            )
        }
        PortugalDialogState.NewPin -> {
            PortugalSettingsCustomDialogCustomDialog(
                title = stringResource(R.string.change_pin),
                description = stringResource(R.string.nuevo_pin),
                editTextTypePin = true,
                editTextMaxLength = 4,
                onCancel = {
                    dialogState = null
                },
                onAccept = { pin ->
                    dialogState = null
                    viewModel.onEvent(PortugalSettingsUiEvent.NewPinEntered(pin))
                }
            )
        }
        null -> Unit
    }
}
enum class PortugalDialogState {
    CurrentPin, NewPin
}
