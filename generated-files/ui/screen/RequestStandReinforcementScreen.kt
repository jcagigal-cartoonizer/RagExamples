@Composable
fun RequestStandReinforcementScreen(
    uiState: RequestStandReinforcementUiState,
    onAction: (RequestStandReinforcementAction) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cancel
            CustomButton(
                text = "Cancel",
                style = uiState.buttons.cancel,
                onClick = { onAction(RequestStandReinforcementAction.CancelClicked) }
            )
            // Favorites buttons
            if (uiState.buttons.addFavourites.visible) {
                CustomButton(
                    text = "Add favourites",
                    style = uiState.buttons.addFavourites,
                    onClick = { onAction(RequestStandReinforcementAction.AddFavouriteClicked) }
                )
            }
            if (uiState.buttons.removeFavourites.visible) {
                CustomButton(
                    text = "Remove favourites",
                    style = uiState.buttons.removeFavourites,
                    onClick = { onAction(RequestStandReinforcementAction.RemoveFavouriteClicked) }
                )
            }
            // Reinforcement buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReinforcementButtonList(
                    buttons = listOf(
                        uiState.buttons.reinforcement0,
                        uiState.buttons.reinforcement1,
                        uiState.buttons.reinforcement2,
                        uiState.buttons.reinforcement3,
                        uiState.buttons.reinforcement4,
                        uiState.buttons.reinforcement5,
                    ),
                    onClick = { number ->
                        onAction(RequestStandReinforcementAction.ReinforcementClicked(number))
                    }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReinforcementButtonList(
                    buttons = listOf(
                        uiState.buttons.reinforcement10,
                        uiState.buttons.reinforcement15,
                        uiState.buttons.reinforcement20,
                        uiState.buttons.reinforcement25,
                    ),
                    onClick = { number ->
                        onAction(RequestStandReinforcementAction.ReinforcementClicked(number))
                    }
                )
            }
        }
        if (uiState.dialog.isVisible) {
            RequestStandReinforcementCustomDialogCustomDialog(
                dialogState = uiState.dialog,
                onConfirm = { onAction(RequestStandReinforcementAction.DialogConfirmed) },
                onDismiss = { onAction(RequestStandReinforcementAction.DialogDismissed) }
            )
        }
    }
}
@Composable
fun ReinforcementButtonList(
    buttons: List<RequestStandReinforcementButtonState>,
    onClick: (Int) -> Unit
) {
    buttons.forEach { buttonState ->
        CustomButton(
            text = buttonState.text,
            style = buttonState.style,
            visible = buttonState.visible,
            onClick = { onClick(buttonState.value) }
        )
    }
}
