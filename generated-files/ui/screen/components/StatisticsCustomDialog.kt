package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.StatisticsCustomDialog
import ifac.td.taxi.ui.screen.components.StatisticsUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 456-8: import androidx.compose.foundation.layout.Arrangement
@Composable
fun StatisticsCustomDialog(
    state: StatisticsDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = state.title)
        },
        text = {
            Text(text = state.message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = state.confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = state.dismissText)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
@Composable
fun StatisticsTabsContent(
    uiState: StatisticsUiState,
    onEvent: (StatisticsUiEvent) -> Unit,
) {
    val tabs = listOf(
        stringResource(R.string.week),
        stringResource(R.string.month),
        stringResource(R.string.year)
    )
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTab.ordinal,
        pageCount = { tabs.size }
    )
    LaunchedEffect(pagerState.currentPage) {
        val tab = when (pagerState.currentPage) {
            0 -> StatisticsTab.Week
            1 -> StatisticsTab.Month
            else -> StatisticsTab.Year
        }
        onEvent(StatisticsUiEvent.TabSelected(tab))
    }
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTab.ordinal == index,
                    onClick = { onEvent(StatisticsUiEvent.TabSelected(StatisticsTab.entries[index])) },
                    text = { Text(title) }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> StatisticsPage(
                    title = stringResource(R.string.week),
                    billingValues = uiState.billingValues,
                    timeValues = uiState.timeValues
                )
                1 -> StatisticsPage(
                    title = stringResource(R.string.month),
                    billingValues = uiState.billingValues,
                    timeValues = uiState.timeValues
                )
                2 -> StatisticsPage(
                    title = stringResource(R.string.year),
                    billingValues = uiState.billingValues,
                    timeValues = uiState.timeValues
                )
            }
        }
    }
}
