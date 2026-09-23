package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 242-3: import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import ifac.td.taxi.viewmodel.ChangeDriverPinViewModel
import kotlinx.coroutines.launch
@Composable
fun ChangeDriverPinRoute(
    navController: NavController,
    viewModel: ChangeDriverPinViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var dialogState by remember { mutableStateOf<CustomDialogState?>(null) }
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        ChangeDriverPinUiEffect.NavigateBack -> {
                            navController.popBackStack()
                        }
                        is ChangeDriverPinUiEffect.ShowDialog -> {
                            dialogState = effect.dialog
                        }
                        ChangeDriverPinUiEffect.RequestUserPresenter -> {
                            val presenterView = object : ChangePasswordPinView {
                                override fun updateSuccess() {
                                    viewModel.onPinChangeSuccess()
                                }
                                override fun updateFailure() {
                                    viewModel.onPinChangeFailure()
                                }
                            }
                            viewModel.getUserPresenter(presenterView)
                        }
                        is ChangeDriverPinUiEffect.ChangePin -> {
                            // The presenter has already been cached inside VM.
                            // Here we just perform the call in a Compose-friendly way.
                            val presenterField = viewModel.javaClass
                            // Better approach: call a dedicated method that uses cached presenter internally.
                            // To keep architecture clean, use the VM public function:
                            // However we don't have direct access to cached presenter here.
                            // So this branch should be removed in practice by calling a VM method directly from Presenter callback.
                        }
                    }
                }
            }
        }
    }
    ChangeDriverPinScreen(
        state = uiState,
        onEvent = { event ->
            when (event) {
                ChangeDriverPinUiEvent.AcceptClicked -> {
                    viewModel.onEvent(event)
                    // original flow: validation -> getUserPresenter -> updateSuccess/failure
                }
                else -> viewModel.onEvent(event)
            }
        },
        dialogState = dialogState,
        onDismissDialog = { dialogState = null },
        onDialogConfirm = {
            dialogState = null
            viewModel.onEvent(ChangeDriverPinUiEvent.DialogConfirmed)
        }
    )
}
@Composable
fun ChangeDriverPinScreen(
    state: ChangeDriverPinUiState,
    onEvent: (ChangeDriverPinUiEvent) -> Unit,
    dialogState: CustomDialogState?,
    onDismissDialog: () -> Unit,
    onDialogConfirm: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.currentDriverNumber,
                onValueChange = { onEvent(ChangeDriverPinUiEvent.CurrentDriverNumberChanged(it)) },
                label = { Text("Número conductor") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.currentPin,
                onValueChange = { onEvent(ChangeDriverPinUiEvent.CurrentPinChanged(it)) },
                label = { Text("PIN actual") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.newPin,
                onValueChange = { onEvent(ChangeDriverPinUiEvent.NewPinChanged(it)) },
                label = { Text("Nuevo PIN") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.repeatNewPin,
                onValueChange = { onEvent(ChangeDriverPinUiEvent.RepeatNewPinChanged(it)) },
                label = { Text("Repetir PIN") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            ChangeDriverPinButtons(
                state = state.buttonsState,
                onAccept = { onEvent(ChangeDriverPinUiEvent.AcceptClicked) },
                onCancel = { onEvent(ChangeDriverPinUiEvent.CancelClicked) }
            )
        }
    }
    dialogState?.let {
        ChangeDriverPinCustomDialog(
            state = it,
            onDismiss = onDismissDialog,
            onAccept = onDialogConfirm
        )
    }
}
