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
// # Block 598-6: import androidx.lifecycle.ViewModel
class MainActivityComposeStateHolder : ViewModel() {
    private val _pendingTripsListFlow = MutableStateFlow<ArrayList<Any>?>(null)
    val pendingTripsListFlow = _pendingTripsListFlow.asStateFlow()
    fun resetDispatchFlow() = Unit
    fun resetCurrentAmountFlow() = Unit
}
To match the original `CustomButton` more closely, the state model should keep:
That is already included in `HomeComposeButtonState`.
fun HomeComposeButtonState.asEmptyStyle() = copy(
    style = ButtonVisualStyle.Empty,
    background = ButtonColor.Gray,
    textColor = Color.Transparent
)
You’ll need to connect:
1. a `HomeRoute()` wrapper  
2. `rememberLauncherForActivityResult` if permission prompts are needed  
3. a more exact visual replica of `CustomButton` using Compose `Surface` + animated colors  
4. a migration of `MainActivityViewModel` into a Compose-friendly shared state holder.
