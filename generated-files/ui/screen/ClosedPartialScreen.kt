@Composable
fun ClosedPartialScreen(
    uiState: ClosedPartialUiState,
    onEvent: (ClosedPartialUiEvent) -> Unit,
) {
    val dialogState = uiState.dialogState
    val buttonsState = uiState.buttonsState
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = uiState.ticketContent,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            ClosedPartialButtonsRow(
                state = buttonsState,
                onCancel = { onEvent(ClosedPartialUiEvent.CancelClicked) },
                onPrint = { onEvent(ClosedPartialUiEvent.PrintClicked) },
                onTotalizers = { onEvent(ClosedPartialUiEvent.TotalizersClicked) }
            )
        }
        if (dialogState.visible) {
            ClosedPartialCustomDialogCustomDialog(
                title = dialogState.title,
                message = dialogState.message,
                confirmText = dialogState.confirmText,
                dismissText = dialogState.dismissText,
                onConfirm = { onEvent(ClosedPartialUiEvent.DialogConfirmed) },
                onDismiss = { onEvent(ClosedPartialUiEvent.DialogDismissed) }
            )
        }
    }
}
