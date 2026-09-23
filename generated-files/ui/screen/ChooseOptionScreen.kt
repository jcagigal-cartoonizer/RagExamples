package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.ChooseOptionButtons
import ifac.td.taxi.ui.screen.components.ChooseOptionScreen
import ifac.td.taxi.compose.viewmodel.ChooseOptionComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 217-3: import androidx.activity.compose.BackHandler
@Composable
fun ChooseOptionScreen(
    viewModel: ChooseOptionComposeViewModel,
    onNavigateBack: () -> Unit,
    onShowBottomBar: (Boolean) -> Unit,
    onShowHeader: (Boolean) -> Unit,
    onSetChooseOptionFragmentActive: (Boolean) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        onShowBottomBar(true)
        onShowHeader(true)
        onSetChooseOptionFragmentActive(true)
    }
    BackHandler {
        onSetChooseOptionFragmentActive(false)
        viewModel.cancelSelectionAndLogoff()
    }
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                ChooseOptionUiEffect.NavigateBack -> {
                    onSetChooseOptionFragmentActive(false)
                    onNavigateBack()
                }
                ChooseOptionUiEffect.RequestCloseDialog -> Unit
                ChooseOptionUiEffect.RequestOpenDialog -> Unit
                ChooseOptionUiEffect.FinishSelectionAndLogin -> Unit
            }
        }
    }
    ChooseOptionContent(
        uiState = uiState,
        onOptionClick = viewModel::onOptionClick,
        onDialogDismiss = viewModel::onDialogDismiss,
        onDialogAccept = viewModel::onDialogAccept
    )
}
@Composable
fun ChooseOptionContent(
    uiState: ChooseOptionUiState,
    onOptionClick: (ChooseOptionItem) -> Unit,
    onDialogDismiss: () -> Unit,
    onDialogAccept: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.options) { item ->
                OptionRow(item = item, onClick = { onOptionClick(item) })
            }
        }
        ChooseOptionButtons(
            state = uiState.buttonsState,
            options = uiState.options,
            onOptionClick = onOptionClick
        )
    }
    if (uiState.dialogState is ChooseOptionDialogState.ConfirmSelection) {
        val dialog = uiState.dialogState as ChooseOptionDialogState.ConfirmSelection
        ChooseOptionDialog(
            title = "Warning",
            description = "Do you want to send option: ${dialog.selectedLabel} ?",
            onDismiss = onDialogDismiss,
            onAccept = onDialogAccept,
        )
    }
}
@Composable
fun OptionRow(item: ChooseOptionItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        tonalElevation = 1.dp
    ) {
        Text(
            text = item.label,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
@Composable
fun ChooseOptionButtons(
    state: ChooseOptionButtonsState,
    options: List<ChooseOptionItem>,
    onOptionClick: (ChooseOptionItem) -> Unit,
) {
    val visibleOptions = options.take(3)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        visibleOptions.getOrNull(0)?.let {
            if (state.primaryVisible) StyledButton(it, state.primaryStyle, onOptionClick)
        }
        visibleOptions.getOrNull(1)?.let {
            if (state.secondaryVisible) StyledButton(it, state.secondaryStyle, onOptionClick)
        }
        visibleOptions.getOrNull(2)?.let {
            if (state.tertiaryVisible) StyledButton(it, state.tertiaryStyle, onOptionClick)
        }
    }
}
@Composable
fun StyledButton(
    item: ChooseOptionItem,
    style: ChooseOptionButtonStyle,
    onOptionClick: (ChooseOptionItem) -> Unit,
) {
    val colors = ButtonDefaults.buttonColors(
        containerColor = style.backgroundColor,
        contentColor = style.contentColor
    )
    val border = style.borderColor?.let { BorderStroke(1.dp, it) }
    Button(
        onClick = { onOptionClick(item) },
        enabled = style.visible,
        colors = colors,
        border = border,
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
    ) {
        Text(item.label)
    }
}
