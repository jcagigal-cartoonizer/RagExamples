package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ShiftDetailButtonsState
import ifac.td.taxi.ui.screen.components.ShiftDetailButtonsState = copy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 50-2: import androidx.compose.ui.graphics.Color
data class ShiftDetailButtonsState(
    val export: ButtonState = ButtonState(label = "Export", enabled = true),
    val email: ButtonState = ButtonState(label = "Email", enabled = true),
    val print: ButtonState = ButtonState(label = "Print", enabled = true),
    val idSort: SortButtonState = SortButtonState(
        option = ShiftOrderOptions.ID,
        label = "ID"
    ),
    val amountSort: SortButtonState = SortButtonState(
        option = ShiftOrderOptions.AMOUNT,
        label = "Amount"
    ),
    val initHourSort: SortButtonState = SortButtonState(
        option = ShiftOrderOptions.START_DATE,
        label = "Init Hour"
    ),
    val distanceSort: SortButtonState = SortButtonState(
        option = ShiftOrderOptions.DISTANCE,
        label = "Distance"
    )
) {
    fun clearSortIndicators(): ShiftDetailButtonsState = copy(
        idSort = idSort.clear(),
        amountSort = amountSort.clear(),
        initHourSort = initHourSort.clear(),
        distanceSort = distanceSort.clear()
    )
    fun applySort(option: ShiftOrderOptions, ascending: Boolean?): ShiftDetailButtonsState {
        val reset = clearSortIndicators()
        return when (option) {
            ShiftOrderOptions.ID -> reset.copy(idSort = reset.idSort.withSortState(ascending))
            ShiftOrderOptions.AMOUNT -> reset.copy(amountSort = reset.amountSort.withSortState(ascending))
            ShiftOrderOptions.START_DATE -> reset.copy(initHourSort = reset.initHourSort.withSortState(ascending))
            ShiftOrderOptions.DISTANCE -> reset.copy(distanceSort = reset.distanceSort.withSortState(ascending))
            ShiftOrderOptions.NONE -> reset
        }
    }
}
data class ButtonState(
    val label: String,
    val enabled: Boolean = true,
    val backgroundColor: Long = 0xFF1E88E5,
    val contentColor: Long = 0xFFFFFFFF,
    val borderColor: Long? = null
)
data class SortButtonState(
    val option: ShiftOrderOptions,
    val label: String,
    val arrowVisible: Boolean = false,
    val ascending: Boolean? = null
) {
    fun clear() = copy(arrowVisible = false, ascending = null)
    fun withSortState(ascending: Boolean?): SortButtonState =
        copy(
            arrowVisible = ascending != null,
            ascending = ascending
        )
}
