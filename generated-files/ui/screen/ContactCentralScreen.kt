package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import kotlinx.coroutines.flow.collectLatest
@Composable
fun ContactCentralScreen(
    navController: NavController,
    viewModel: ContactCentralComposeViewModel,
    sharedUiState: SharedContactCentralState, // from activity/shared VM state holder
    onShortBreakButtonPressed: () -> Unit,      // preserves old iMainActivity.shortBreakButtonPressed()
    onShowHeader: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        onShowHeader(true)
        viewModel.initialize()
    }
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ContactCentralUiEffect.NavigateBack -> navController.popBackStack()
                is ContactCentralUiEffect.NavigateToPredefinedMessages ->
                    navController.navigate(R.id.action_contactCentralFragment_to_predefinedMessageFragment)
                is ContactCentralUiEffect.NavigateToInformationMessages ->
                    navController.navigate(R.id.action_contactCentralFragment_to_informationMessageFragment)
                is ContactCentralUiEffect.ShowShortBreakForcedToast ->
                    onShowToast(R.string.short_break_forced)
                is ContactCentralUiEffect.ShowVoiceRequestDialog -> {
                }
            }
        }
    }
    val dialogState = remember { mutableStateOf<ContactCentralDialogState?>(null) }
    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ContactCentralUiEffect.ShowVoiceRequestDialog -> dialogState.value =
                    ContactCentralDialogState(title = effect.title)
                else -> Unit
            }
        }
    }
    ContactCentralDialogHost(
        dialogState = dialogState.value,
        onDismiss = { dialogState.value = null },
        onAccept = {
            dialogState.value = null
            viewModel.onVoiceRequestDialogAccept()
        }
    )
    ContactCentralContent(
        state = uiState,
        sharedUiState = sharedUiState,
        onCancel = { viewModel.onCancelClick() },
        onShortBreak = {
            val status = sharedUiState.shortBreakStatus
            if (status == ShortBreakStatus.IN_SHORT_BREAK_FORCED) {
                onShowToast(R.string.short_break_forced)
            } else {
                viewModel.onShortBreakClick(status, !sharedUiState.zone.isNullOrBlank())
                onShortBreakButtonPressed()
            }
        },
        onVoiceCall = { viewModel.onVoiceCallClick(sharedUiState.voiceValue) },
        onMessages = { viewModel.onMessagesClick() },
        onInformation = { viewModel.onInformationClick() }
    )
}
@Composable
fun ContactCentralContent(
    state: ContactCentralUiState,
    sharedUiState: SharedContactCentralState,
    onCancel: () -> Unit,
    onShortBreak: () -> Unit,
    onVoiceCall: () -> Unit,
    onMessages: () -> Unit,
    onInformation: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ContactCentralButton(
            style = state.buttons.cancel,
            onClick = onCancel
        )
        Spacer(Modifier.height(12.dp))
        if (state.buttons.shortBreak.visible) {
            ContactCentralButton(
                style = state.buttons.shortBreak,
                onClick = onShortBreak
            )
        }
        Spacer(Modifier.height(12.dp))
        ContactCentralButton(
            style = state.buttons.voiceCall,
            onClick = onVoiceCall
        )
        Spacer(Modifier.height(12.dp))
        ContactCentralButton(
            style = state.buttons.messages,
            onClick = onMessages
        )
        Spacer(Modifier.height(12.dp))
        ContactCentralButton(
            style = state.buttons.information,
            onClick = onInformation
        )
    }
}
data class SharedContactCentralState(
    val shortBreakStatus: ShortBreakStatus? = null,
    val voiceValue: Boolean = false,
    val zone: String? = null,
)
