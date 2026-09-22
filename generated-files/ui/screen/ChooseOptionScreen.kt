package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 217-3: import androidx.activity.compose.BackHandler
// import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.viewmodel.*
import kotlinx.coroutines.flow.collectLatest
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
