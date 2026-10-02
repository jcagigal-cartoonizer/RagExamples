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
