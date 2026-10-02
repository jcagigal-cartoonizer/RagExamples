@Composable
fun LegalTextScreen(
    uiState: ifac.td.taxi.viewmodel.LegalTextUiState,
    onAcceptClick: () -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDialog: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = uiState.legalText ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            )
            Spacer(modifier = Modifier.height(16.dp))
            LegalTextButtons(
                state = uiState.buttonsState,
                onAccept = onAcceptClick
            )
        }
        if (uiState.isDialogVisible) {
            LegalTextCustomFilledButtonColorsLegalTextCustomDialog(
                title = uiState.dialog.title,
                message = uiState.dialog.message,
                confirmText = uiState.dialog.confirmText,
                dismissText = uiState.dialog.dismissText,
                onConfirm = onConfirmDialog,
                onDismiss = onDismissDialog
            )
        }
    }
}
