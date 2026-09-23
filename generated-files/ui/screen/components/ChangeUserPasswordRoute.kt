package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.ChangeUserPasswordComposeViewModel
import ifac.td.taxi.ui.screen.components.ChangeUserPasswordUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 220-5: import androidx.compose.foundation.layout.*
@Composable
fun ChangeUserPasswordRoute(
    viewModel: ChangeUserPasswordComposeViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffects.collect { effect ->
                    when (effect) {
                        is ChangeUserPasswordUiEffect.NavigateBack -> onNavigateBack()
                        is ChangeUserPasswordUiEffect.ShowDialog -> {
                            // Dialog is state-driven; handled below by state
                        }
                        ChangeUserPasswordUiEffect.OpenUserPresenter -> Unit
                    }
                }
            }
        }
    }
    val dialogState = uiState.dialogState
    ChangeUserPasswordScreen(
        state = uiState,
        onCurrentPasswordChanged = viewModel::onCurrentPasswordChanged,
        onNewPasswordChanged = viewModel::onNewPasswordChanged,
        onRepeatPasswordChanged = viewModel::onRepeatPasswordChanged,
        onAcceptClick = viewModel::onAcceptClicked,
        onCancelClick = viewModel::onCancelClicked,
        onDialogAccepted = viewModel::onDialogAccepted,
        dialogState = dialogState,
        onDismissDialog = { /* state can be cleared here if desired */ }
    )
}
> If you want the dialog to be fully state-driven, add a `dialogState` inside the ViewModel and update it from effects; since you explicitly asked for dialog state and lifecycle collection, the next composable shows the dialog directly and can be extended to store it in the VM.
