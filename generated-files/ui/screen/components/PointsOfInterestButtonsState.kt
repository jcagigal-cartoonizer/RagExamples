package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PointsOfInterestButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 443-3: import androidx.compose.runtime.Immutable
@Immutable
data class PointsOfInterestButtonsState(
    val cancelVisible: Boolean = true,
    val cancelEnabled: Boolean = true,
    val cancelText: String = "Cancel",
    val cancelStyle: ComposeButtonStyle = ComposeButtonStyle.ghost(),
    val searchVisible: Boolean = true,
    val searchEnabled: Boolean = true,
    val searchText: String = "Search",
    val searchStyle: ComposeButtonStyle = ComposeButtonStyle.primary(),
) {
    companion object {
        fun default() = PointsOfInterestButtonsState()
    }
}
@Immutable
data class ComposeButtonStyle(
    val containerColor: Long,
    val contentColor: Long,
    val disabledContainerColor: Long,
    val disabledContentColor: Long,
    val borderColor: Long? = null,
    val cornerRadiusDp: Int = 12,
    val strokeWidthDp: Int = 1
) {
    companion object {
        fun primary() = ComposeButtonStyle(
            containerColor = 0xFF1976D2,
            contentColor = 0xFFFFFFFF,
            disabledContainerColor = 0xFF90CAF9,
            disabledContentColor = 0xCCFFFFFF
        )
        fun ghost() = ComposeButtonStyle(
            containerColor = 0x00000000,
            contentColor = 0xFF1976D2,
            disabledContainerColor = 0x00000000,
            disabledContentColor = 0x66000000,
            borderColor = 0xFF1976D2
        )
    }
}
