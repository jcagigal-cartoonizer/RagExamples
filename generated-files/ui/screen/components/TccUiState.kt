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
// # Block 15-1: import ifac.td.taxi.domain.model.Dispatch
data class TccUiState(
    val isLoading: Boolean = false,
    val dispatch: Dispatch? = null,
    val buttonsState: TccButtonsState = TccButtonsState(),
    val dialogState: TccDialogState = TccDialogState.Hidden,
    val errorMessageRes: Int? = null
)
data class TccButtonsState(
    val accept: TccButtonState = TccButtonState(),
    val cancel: TccButtonState = TccButtonState(),
    val attributes1: AttributeSpinnerState = AttributeSpinnerState(),
    val attributes2: AttributeSpinnerState = AttributeSpinnerState(),
    val attributes3: AttributeSpinnerState = AttributeSpinnerState(),
    val attributes4: AttributeSpinnerState = AttributeSpinnerState(),
)
data class TccButtonState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val textRes: Int? = null
)
data class AttributeSpinnerState(
    val visible: Boolean = false,
    val title: String = "",
    val value: Int = 1
)
sealed interface TccDialogState {
    data object Hidden : TccDialogState
    data class ConfirmAccept(val dispatchTitle: String? = null) : TccDialogState
    data class Error(val message: String) : TccDialogState
}
sealed interface TccUiEffect {
    data object NavigateBack : TccUiEffect
    data class NavigateToSignature(val dispatchId: Long) : TccUiEffect
    data class NavigateToVoucher(val dispatchId: Long) : TccUiEffect
    data class NavigateToQr(val dispatchId: Long) : TccUiEffect
    data class FinishTcc(
        val tripId: Long?,
        val dispatch: InfoDispatchModel?,
    ) : TccUiEffect
    data class ToastRes(val resId: Int) : TccUiEffect
}
