package ifac.td.taxi.ui.screen
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        ShiftsScreenShiftsCustomDialog(
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
fun ShiftsFloatingButtons(
    buttonsState: ShiftsButtonsState,
    onMainClick: () -> Unit,
    onExportClick: () -> Unit,
    onTrashClick: () -> Unit
) {
    val mainRotation by animateFloatAsState(targetValue = buttonsState.mainRotation, label = "mainRotation")
    val exportAlpha by animateFloatAsState(targetValue = buttonsState.exportAlpha, label = "exportAlpha")
    val trashAlpha by animateFloatAsState(targetValue = buttonsState.trashAlpha, label = "trashAlpha")
    val exportY by animateFloatAsState(targetValue = buttonsState.exportTranslationY, label = "exportY")
    val trashY by animateFloatAsState(targetValue = buttonsState.trashTranslationY, label = "trashY")
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(horizontalAlignment = Alignment.End) {
            AnimatedVisibility(visible = buttonsState.exportVisible || exportAlpha > 0f) {
                FloatingActionButton(
                    onClick = onExportClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .offset(y = exportY.dp)
                        .padding(bottom = 12.dp)
                        .alpha(exportAlpha)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Export")
                }
            }
            AnimatedVisibility(visible = buttonsState.trashVisible || trashAlpha > 0f) {
                FloatingActionButton(
                    onClick = onTrashClick,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    modifier = Modifier
                        .offset(y = trashY.dp)
                        .padding(bottom = 12.dp)
                        .alpha(trashAlpha)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            FloatingActionButton(
                onClick = onMainClick,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.rotate(mainRotation)
            ) {
                Icon(Icons.Default.ExpandLess, contentDescription = "Menu")
            }
        }
    }
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
