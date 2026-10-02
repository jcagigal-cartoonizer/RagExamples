@Composable
fun ChangePasswordRedSysScreen(
    uiState: ifac.td.taxi.viewmodel.ChangePasswordRedSysUiState,
    dialogState: ChangePasswordRedSysDialogState?,
    onDismissDialog: () -> Unit,
    onDialogAccepted: () -> Unit,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    onUserChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onRepeatNewPasswordChanged: (String) -> Unit,
) {
    val buttonsState = ChangePasswordRedSysButtonsStateProvider.fromUiState(uiState.isLoading)
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Change password")
        Spacer(modifier = Modifier.height(16.dp))
        // Replace these with your actual text fields.
        // These are here to show state flow from ViewModel to composable.
        // You can wire them to OutlinedTextField etc.
        Spacer(modifier = Modifier.height(24.dp))
        ChangePasswordRedSysActionButton(
            state = buttonsState.accept,
            onClick = onAccept
        )
        Spacer(modifier = Modifier.height(12.dp))
        ChangePasswordRedSysActionButton(
            state = buttonsState.cancel,
            onClick = onCancel
        )
        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
    dialogState?.let { dialog ->
        ChangePasswordRedSysRouteChangePasswordRedSysCustomDialog(
            model = ChangePasswordRedSysRouteCustomDialogModel(
                title = dialog.title,
                description = dialog.description,
                buttons = listOf(ChangePasswordRedSysRouteDialogButtonType.ACCEPT)
            ),
            onDismissRequest = onDismissDialog,
            onResponse = { response ->
                when (response.buttonPressed) {
                    ChangePasswordRedSysRouteDialogButtonType.ACCEPT -> onDialogAccepted()
                    ChangePasswordRedSysRouteDialogButtonType.CANCEL -> onDismissDialog()
                }
            }
        )
    }
}
@Composable
fun ChangePasswordRedSysDestination(
    viewModel: ChangePasswordRedSysComposeViewModel,
    onNavigateBack: () -> Unit
) {
    ChangePasswordRedSysRoute(
        viewModel = viewModel,
        navigateBack = onNavigateBack,
        showHeader = { visible ->
            // call your scaffold/header state here
        }
    )
}
navigateBack = { iMainActivity.navigateBack() }
sealed interface ChangePasswordRedSysRouteButtonVisualState {
    data object Enabled : ChangePasswordRedSysRouteButtonVisualState
    data object Disabled : ChangePasswordRedSysRouteButtonVisualState
    data object Loading : ChangePasswordRedSysRouteButtonVisualState
}
