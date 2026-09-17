package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.state.InfoDispatchButtons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # 6) Main content composable


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun InfoDispatchContent(
    modifier: Modifier = Modifier,
    state: InfoDispatchUiState,
    onEvent: (InfoDispatchUiEvent) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        InfoDispatchButtonsRow(
            buttons = state.buttons,
            onEvent = onEvent
        )

        if (state.tabs.isNotEmpty()) {
            InfoDispatchTabs(
                tabs = state.tabs,
                selectedIndex = state.selectedTabIndex,
                onTabSelected = { onEvent(InfoDispatchUiEvent.OnTabSelected(it)) }
            )
        }

        state.flightCode?.let {
            if (it.isNotBlank()) {
                Text(text = it)
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Dispatch fields here
        }
    }
}


