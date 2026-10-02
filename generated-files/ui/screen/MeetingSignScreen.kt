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
// # Block 316-6: import androidx.compose.foundation.background
@Composable
fun MeetingSignScreen(
    navController: NavController,
    viewModel: MeetingSignComposeViewModel,
    textColor: Int,
    backgroundColor: Int,
    fromDispatch: Boolean,
    isInSettings: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToInfoDispatch: () -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(textColor, backgroundColor, fromDispatch, isInSettings) {
        viewModel.setEnvironment(
            fromDispatch = fromDispatch,
            isInSettings = isInSettings,
            textColor = textColor,
            backgroundColor = backgroundColor
        )
    }
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    MeetingSignUiEffect.NavigateBack -> onNavigateBack()
                    MeetingSignUiEffect.NavigateToInfoDispatch -> onNavigateToInfoDispatch()
                    is MeetingSignUiEffect.ShowToast -> onShowToast(effect.messageRes)
                    is MeetingSignUiEffect.OpenEditDialog -> {
                        // handled by dialog state below
                    }
                }
            }
        }
    }
    val bg = if (uiState.backgroundColor != 0) Color(uiState.backgroundColor) else Color.Transparent
    val txt = if (uiState.textColor != 0) Color(uiState.textColor) else Color.Unspecified
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MeetingSignText(
                message = uiState.message,
                color = txt,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MeetingSignFabButton(
                    state = uiState.buttonsState.edit,
                    onClick = { viewModel.onEditClicked() }
                )
                MeetingSignFabButton(
                    state = uiState.buttonsState.dispatch,
                    onClick = { viewModel.onDispatchClicked() }
                )
                MeetingSignFabButton(
                    state = uiState.buttonsState.options,
                    onClick = { viewModel.onOptionsClicked() }
                )
            }
        }
    }
    if (uiState.message.isBlank()) {
        LaunchedEffect(uiState.message) {
            viewModel.onEditClicked()
        }
    }
    if (uiState.dialogState != null) {
        val d = uiState.dialogState!!
        MeetingSignScreenMeetingSignCustomDialog(
            title = d.title,
            hint = d.hint,
            editText = d.editText,
            isCancellable = d.isCancellable,
            onDismiss = { viewModel.onDialogDismiss() },
            onCancel = { viewModel.onDialogDismiss() },
            onAccept = { input ->
                viewModel.onDialogConfirm(input)
                viewModel.onDialogDismiss()
            }
        )
    }
}
@Composable
fun MeetingSignText(
    message: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        style = MaterialTheme.typography.displayLarge,
        color = color,
        modifier = modifier
            .wrapContentSize()
            .padding(16.dp)
    )
}
That gives you one-off effect handling without leaking collectors.
For dialog handling:
In Compose, `Modifier.alpha` and `Modifier.offset` can mimic this visually, but Compose does not use `GONE` the same way. The `visible` flag in `MeetingSignFabButtonState` is the equivalent control. If you need animation parity, you can wrap buttons in `AnimatedVisibility`, `animateFloatAsState`, and `animateDpAsState`.
I can refine the Compose `MeetingSignScreenCustomDialog` into a more exact `Dialog + Surface + Column` implementation that mirrors padding, shape, and typography more closely.
1. a full `@Composable` version with `AnimatedVisibility` for the FAB menu,
2. a `MeetingSignRoute()` example with Koin injection,
3. a more exact Material3 dialog styled to match your XML theme.
