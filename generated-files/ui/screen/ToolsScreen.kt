@Composable
fun ToolsScreen(
    uiState: ToolsUiState,
    onEvent: (ToolsUiEvent) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolsButton(
                text = "Requirements",
                colors = uiState.buttons.requirements,
                visible = uiState.buttons.requirements.visible,
                enabled = uiState.buttons.requirements.enabled,
                onClick = { onEvent(ToolsUiEvent.RequirementsClicked) }
            )
            ToolsButton(
                text = "Meeting sign",
                colors = uiState.buttons.meetingSign,
                visible = uiState.buttons.meetingSign.visible,
                enabled = uiState.buttons.meetingSign.enabled,
                onClick = { onEvent(ToolsUiEvent.MeetingSignClicked) }
            )
        }
        if (uiState.dialogState.visible) {
            ToolsFragmentComposeHostToolsCustomDialog(
                state = uiState.dialogState,
                onDismiss = { onEvent(ToolsUiEvent.DialogDismissed) },
                onConfirm = { onEvent(ToolsUiEvent.DialogConfirmed) }
            )
        }
    }
}
