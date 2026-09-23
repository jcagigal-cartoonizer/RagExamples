package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementUiEffect
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementScreen
import ifac.td.taxi.compose.viewmodel.RequestStandReinforcementComposeViewModel
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementButtonState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 11-1: import androidx.compose.foundation.layout.*
@Composable
fun RequestStandReinforcementRoute(
    navController: NavController,
    viewModel: RequestStandReinforcementComposeViewModel,
    idMacrozone: Int,
    idZone: Int,
    isInFavourites: Boolean,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.init(
            idMacrozone = idMacrozone,
            idZone = idZone,
            isInFavourites = isInFavourites
        )
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RequestStandReinforcementUiEffect.NavigateBack -> {
                    navController.popBackStack()
                }
                is RequestStandReinforcementUiEffect.ShowToast -> {
                    onShowToast(effect.messageRes)
                }
                is RequestStandReinforcementUiEffect.ShowDialog -> {
                    viewModel.setDialogVisible(true)
                }
                RequestStandReinforcementUiEffect.HideDialog -> {
                    viewModel.setDialogVisible(false)
                }
            }
        }
    }
    RequestStandReinforcementScreen(
        uiState = uiState,
        onAction = viewModel::onAction
    )
}
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
            RequestStandReinforcementCustomDialog(
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
