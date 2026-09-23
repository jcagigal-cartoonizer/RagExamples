package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.ShiftsUiEffect
import ifac.td.taxi.ui.screen.components.ShiftsCustomDialog
import ifac.td.taxi.ui.screen.components.ShiftsScreen
import ifac.td.taxi.compose.viewmodel.ShiftsComposeViewModel
import ifac.td.taxi.ui.screen.components.ShiftsUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 298-4: import androidx.compose.animation.AnimatedVisibility
@Composable
fun ShiftsScreen(
    navController: NavController,
    viewModel: ShiftsComposeViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var dialogState by remember { mutableStateOf<DialogSpec?>(null) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ShiftsUiEffect.NavigateToShiftDetail -> {
                    val action =
                        ifac.td.taxi.ui.screen.ShiftsFragmentDirections
                            .actionShiftsFragmentToShiftDetailFragment(effect.shift)
                    navController.navigate(action)
                }
                is ShiftsUiEffect.OpenIntent -> {
                    runCatching { context.startActivity(effect.intent) }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.onEvent(ShiftsUiEvent.ScreenResumed)
    }
    dialogState = uiState.dialog
    if (dialogState != null) {
        ShiftsCustomDialog(
            dialog = dialogState!!,
            onDismiss = { viewModel.onEvent(ShiftsUiEvent.DialogDismiss) },
            onConfirmDelete = { viewModel.onEvent(ShiftsUiEvent.DialogConfirmDelete) }
        )
    }
    ShiftsContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onShiftClicked = { viewModel.onEvent(ShiftsUiEvent.ItemClicked(it)) },
        onToggleSelection = { viewModel.toggleSelection(it.id) }
    )
}
@Composable
fun ShiftsContent(
    state: ShiftsUiState,
    onEvent: (ShiftsUiEvent) -> Unit,
    onShiftClicked: (ifac.td.taxi.repository.room.entities.ShiftEntity) -> Unit,
    onToggleSelection: (ifac.td.taxi.repository.room.entities.ShiftEntity) -> Unit
) {
    val listState = rememberLazyListState()
    val reachedEnd by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.shifts.size - 1 && state.shifts.isNotEmpty()
        }
    }
    LaunchedEffect(reachedEnd, state.isLoading) {
        if (reachedEnd && !state.isLoading) {
            onEvent(ShiftsUiEvent.LoadNextPage)
        }
    }
    Scaffold(
        floatingActionButton = {
            ShiftsFloatingButtons(
                buttonsState = state.buttonsState,
                onMainClick = { onEvent(ShiftsUiEvent.ToggleMenu) },
                onExportClick = { onEvent(ShiftsUiEvent.ExportSelected) },
                onTrashClick = { onEvent(ShiftsUiEvent.DeleteSelected) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ShiftsSortHeader(
                sortState = state.sortState,
                onSortId = { onEvent(ShiftsUiEvent.SortById) },
                onSortAmount = { onEvent(ShiftsUiEvent.SortByAmount) }
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = state.shifts,
                    key = { it.id }
                ) { shift ->
                    ShiftRow(
                        shift = shift,
                        selected = state.selectedShiftIds.contains(shift.id),
                        onClick = { onShiftClicked(shift) },
                        onLongClick = { onToggleSelection(shift) }
                    )
                }
                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ShiftsSortHeader(
    sortState: ifac.td.taxi.viewmodel.model.SortState,
    onSortId: () -> Unit,
    onSortAmount: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SortButton(
            text = "ID",
            active = sortState.option == ifac.td.taxi.viewmodel.model.ShiftOrderOptions.ID,
            ascending = sortState.option == ifac.td.taxi.viewmodel.model.ShiftOrderOptions.ID && sortState.isAscending == true,
            descending = sortState.option == ifac.td.taxi.viewmodel.model.ShiftOrderOptions.ID && sortState.isAscending == false,
            onClick = onSortId
        )
        SortButton(
            text = "Amount",
            active = sortState.option == ifac.td.taxi.viewmodel.model.ShiftOrderOptions.AMOUNT,
            ascending = sortState.option == ifac.td.taxi.viewmodel.model.ShiftOrderOptions.AMOUNT && sortState.isAscending == true,
            descending = sortState.option == ifac.td.taxi.viewmodel.model.ShiftOrderOptions.AMOUNT && sortState.isAscending == false,
            onClick = onSortAmount
        )
    }
}
@Composable
fun SortButton(
    text: String,
    active: Boolean,
    ascending: Boolean,
    descending: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text)
        Spacer(modifier = Modifier.width(4.dp))
        when {
            ascending -> Icon(Icons.Default.ArrowDropUp, contentDescription = null)
            descending -> Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            else -> Spacer(modifier = Modifier.size(24.dp))
        }
    }
}
@Composable
fun ShiftRow(
    shift: ifac.td.taxi.repository.room.entities.ShiftEntity,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        tonalElevation = if (selected) 4.dp else 0.dp,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick, onLongClick = onLongClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "ID: ${shift.id}")
            Text(text = "Amount: ${shift.amount}")
        }
    }
}
@Composable
fun ShiftsCustomDialog(
    dialog: DialogSpec,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (dialog) {
                    is DialogSpec.Message -> dialog.title
                    is DialogSpec.ConfirmDelete -> dialog.title
                }
            )
        },
        text = {
            Text(
                text = when (dialog) {
                    is DialogSpec.Message -> dialog.message
                    is DialogSpec.ConfirmDelete -> dialog.message
                }
            )
        },
        confirmButton = {
            when (dialog) {
                is DialogSpec.Message -> {
                    TextButton(onClick = onDismiss) { Text("OK") }
                }
                is DialogSpec.ConfirmDelete -> {
                    TextButton(onClick = onConfirmDelete) { Text("Delete") }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
@Composable
fun StyledShiftsCustomDialog(
    title: String,
    message: String,
    confirmText: String = "OK",
    dismissText: String = "Cancel",
    onConfirm: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            if (onConfirm != null) {
                TextButton(onClick = onConfirm) { Text(confirmText) }
            } else {
                TextButton(onClick = onDismiss) { Text(confirmText) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText) }
        }
    )
}
@Composable
fun ShiftsActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val container = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text)
    }
}
@Composable
fun ShiftsSortActionButton(
    text: String,
    arrowState: ArrowState,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text)
            Spacer(Modifier.width(6.dp))
            when (arrowState) {
                ArrowState.Up -> Icon(Icons.Default.ArrowDropUp, contentDescription = null)
                ArrowState.Down -> Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                ArrowState.Hidden -> Spacer(Modifier.size(24.dp))
            }
        }
    }
}
In the fragment version, navigation happens here:
val direction =
    ShiftsFragmentDirections.actionShiftsFragmentToShiftDetailFragment(shift)
iMainActivity.navigateTo(direction)
In Compose, I preserved the same safe-args navigation by collecting this effect:
is ShiftsUiEffect.NavigateToShiftDetail -> {
    val action =
        ifac.td.taxi.ui.screen.ShiftsFragmentDirections
            .actionShiftsFragmentToShiftDetailFragment(effect.shift)
    navController.navigate(action)
}
So this is still your existing Navigation graph / Safe Args path.
Your fragment behavior includes:
The Compose version above mirrors these behaviors with state-driven animation.
@Composable
fun ShiftsRoute(
    navController: NavController,
    viewModel: ShiftsComposeViewModel
) {
    ShiftsScreen(
        navController = navController,
        viewModel = viewModel
    )
}
