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
// # Block 254-4: import ifac.td.taxi.ui.custom.button.ButtonType
sealed interface ChangeDriverPinUiEvent {
    data class DriverNumberChanged(val value: String) : ChangeDriverPinUiEvent
    data class CurrentPinChanged(val value: String) : ChangeDriverPinUiEvent
    data class NewPinChanged(val value: String) : ChangeDriverPinUiEvent
    data class RepeatPinChanged(val value: String) : ChangeDriverPinUiEvent
    data object AcceptClicked : ChangeDriverPinUiEvent
    data object CancelClicked : ChangeDriverPinUiEvent
    data class DialogButtonClicked(val buttonType: ButtonType) : ChangeDriverPinUiEvent
}
sealed interface ChangeDriverPinUiEffect {
    data class ShowDialog(val dialogState: ChangeDriverPinCustomDialogStateCustomDialogState) : ChangeDriverPinUiEffect
    data class ShowToast(val message: String) : ChangeDriverPinUiEffect
    data object NavigateBack : ChangeDriverPinUiEffect
}
