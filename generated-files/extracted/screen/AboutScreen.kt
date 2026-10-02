@Composable
fun AboutScreen(
    uiState: AboutUiState,
    onAcceptClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLogoClick: () -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = uiState.appInfo,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = uiState.privacyPolicyText,
            color = Color(0xFF1E88E5),
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onPrivacyClick() }
        )
        Spacer(Modifier.height(12.dp))
        if (uiState.isBluetoothInfoVisible) {
            Text(text = uiState.bluetoothInfoText)
        }
        Spacer(Modifier.height(24.dp))
        AboutButtons(
            state = uiState.buttons,
            onAcceptClick = onAcceptClick
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "LOGO",
            modifier = Modifier.clickable { onLogoClick() }
        )
    }
    when (val dialog = uiState.dialog) {
        AboutDialogState.Hidden -> Unit
        is AboutDialogState.Warning -> {
            AboutButtonStyle{AboutCustomDialog(
                title = "Warning",
                message = dialog.message,
                onConfirm = onDismissDialog,
                onDismiss = onDismissDialog
            )
        }
    }
}
