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
// # Block 13-1: import android.content.Intent
data class ShiftDetailUiState(
    val shiftId: Long = 0L,
    val trips: List<Trip> = emptyList(),
    val sortState: SortStateUi = SortStateUi(),
    val buttons: ShiftDetailButtonsState = ShiftDetailButtonsState(),
    val dialog: ShiftDetailComposeFragmentCustomDialogState? = null
)
data class SortStateUi(
    val option: ShiftOrderOptions = ShiftOrderOptions.NONE,
    val isAscending: Boolean? = null
)
data class ShiftDetailDialogState(
    val title: String,
    val message: String,
    val positiveText: String = "OK",
    val negativeText: String? = null
)
data class ShiftDetailComposeFragmentCustomDialogState(
    val title: String,
    val message: String,
    val positiveButtonText: String,
    val negativeButtonText: String? = null
)
sealed interface ShiftDetailUiEffect {
    data class OpenIntent(val intent: Intent) : ShiftDetailUiEffect
    data class ShowDialog(val dialog: ShiftDetailComposeFragmentCustomDialogState) : ShiftDetailUiEffect
    data class ShowToast(val messageRes: Int) : ShiftDetailUiEffect
}
