package ifac.td.taxi.ui.screen.components
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
// # Block 502-9: import androidx.compose.foundation.layout.Column
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
