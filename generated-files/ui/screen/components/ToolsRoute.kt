package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.ToolsComposeViewModel
import ifac.td.taxi.ui.screen.components.ToolsUiEvent
import ifac.td.taxi.ui.screen.components.ToolsScreen
import ifac.td.taxi.ui.screen.components.ToolsUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 6-1: import androidx.compose.foundation.layout.Arrangement
@Composable
fun ToolsRoute(
    viewModel: ToolsComposeViewModel,
    navController: NavController,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ToolsUiEffect.NavigateToRequirements -> {
                    navController.navigate(R.id.action_toolsFragment_to_requirementsFragment)
                }
                is ToolsUiEffect.NavigateToMeetingSign -> {
                    navController.navigate(
                        ToolsFragmentDirections.actionToolsFragmentToMeetingSignFragment(
                            effect.textColor,
                            effect.backgroundColor
                        )
                    )
                }
                is ToolsUiEffect.ShowDialog -> {
                    // UI state already controls the dialog; effect may be used as a trigger if needed
                }
            }
        }
    }
    ToolsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}
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
            ToolsCustomDialog(
                state = uiState.dialogState,
                onDismiss = { onEvent(ToolsUiEvent.DialogDismissed) },
                onConfirm = { onEvent(ToolsUiEvent.DialogConfirmed) }
            )
        }
    }
}
