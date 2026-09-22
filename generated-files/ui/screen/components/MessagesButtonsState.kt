package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
@Immutable
data class MessagesButtonsState(
    val showBack: Boolean = true,
    val showDelete: Boolean = false,
    val showSelect: Boolean = true,
    val showMarkRead: Boolean = true,
    val backEnabled: Boolean = true,
    val deleteEnabled: Boolean = false,
    val selectEnabled: Boolean = true,
    val markReadEnabled: Boolean = true,
    val backColor: Color = Color(0xFF607D8B),
    val deleteColor: Color = Color(0xFFD32F2F),
    val selectColor: Color = Color(0xFF1976D2),
    val markReadColor: Color = Color(0xFF388E3C)
) {
    companion object {
        fun from(uiState: MessagesUiState): MessagesButtonsState {
            return MessagesButtonsState(
                showDelete = uiState.showDeleteButton,
                showSelect = uiState.showSelectButton,
                showMarkRead = uiState.showMarkReadAction,
                deleteEnabled = uiState.selectedMessageIds.isNotEmpty(),
                selectEnabled = true,
                markReadEnabled = true
            )
        }
    }
}
