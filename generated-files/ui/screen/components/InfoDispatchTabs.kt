package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # 10) Tabs UI


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun InfoDispatchTabs(
    tabs: List<InfoDispatchTabUiModel>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(selectedTabIndex = selectedIndex, modifier = Modifier.fillMaxWidth()) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onTabSelected(index) },
                text = { Text(tab.title) },
                icon = { /* optional icon */ }
            )
        }
    }
}


// // # Notes on preserving behavior
