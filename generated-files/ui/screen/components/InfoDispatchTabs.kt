package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
The original fragment’s complex button state transitions are now represented by:
Your original fragment logic depends on many external flows:
In Compose, the cleanest approach is:
1. collect those flows in the screen
2. reduce them into one `InfoDispatchUiState`
3. render everything from that state
4. trigger `UiEffect`s for one-off actions
That is the intended architecture shown above.
