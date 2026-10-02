@Composable
fun SecurePinScreen(
    navController: NavController,
    viewModel: SecurePinComposeViewModel,
    onShowBottomBar: (Boolean) -> Unit,
    onShowHeader: (Boolean) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        onShowBottomBar(true)
        onShowHeader(true)
        viewModel.onEvent(SecurePinUiEvent.CheckSecurePin)
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SecurePinUiEffect.NavigateBack -> navController.popBackStack()
                SecurePinUiEffect.ShowSuccessDialog -> showDialog = true
            }
        }
    }
    BackHandler {
        navController.popBackStack()
    }
    if (showDialog) {
        ComposeSecurePinStyledButtonSecurePinCustomDialog(
            title = stringResource(R.string.dialog_success),
            description = null,
            buttons = listOf(ComposeDialogButton.Accept),
            onDismiss = { showDialog = false },
            onAccept = {
                showDialog = false
                navController.popBackStack()
            }
        )
    }
    SecurePinContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}
data class SecurePinUiState(
    val hasSecurePin: Boolean? = null,
    val pin: String = "",
    val pinRepeat: String = "",
    val buttonsState: SecurePinButtonsState = SecurePinButtonsState(),
    val isDialogVisible: Boolean = false
)
sealed interface SecurePinUiEvent {
    data object CheckSecurePin : SecurePinUiEvent
    data class PinChanged(val value: String) : SecurePinUiEvent
    data class PinRepeatChanged(val value: String) : SecurePinUiEvent
    data object AcceptClicked : SecurePinUiEvent
    data object CancelClicked : SecurePinUiEvent
    data object DialogDismissed : SecurePinUiEvent
    data object DialogAccepted : SecurePinUiEvent
}
sealed interface SecurePinUiEffect {
    data object NavigateBack : SecurePinUiEffect
    data object ShowSuccessDialog : SecurePinUiEffect
}
