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
// # Block 470-6: import androidx.compose.ui.graphics.Color
object RequirementsButtonStyleHelper {
    val PrimaryEnabledBackground = Color(0xFF1E88E5)
    val PrimaryDisabledBackground = Color(0xFFE0E0E0)
    val PrimaryEnabledContent = Color.White
    val PrimaryDisabledContent = Color(0xFF9E9E9E)
    fun primary(enabled: Boolean): RequirementsButtonStyleHelperButtonVisualState {
        return RequirementsButtonStyleHelperButtonVisualState(
            visible = true,
            enabled = enabled,
            background = if (enabled) PrimaryEnabledBackground else PrimaryDisabledBackground,
            contentColor = if (enabled) PrimaryEnabledContent else PrimaryDisabledContent
        )
    }
}
Then inside your state builder:
buttonsState = RequirementsButtonsState(
    driverPrimary = RequirementsButtonStyleHelper.primary(driver.isNotEmpty()),
    vehiclePrimary = RequirementsButtonStyleHelper.primary(vehicle.isNotEmpty())
)
class RequirementsComposeFragment : Fragment() {
    private val viewModel: RequirementsComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RequirementsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { requireActivity().onBackPressedDispatcher.onBackPressed() },
                    onShowToast = { /* Toast.makeText(...) */ },
                    onShowHeader = { /* iMainActivity.showHeader(it) */ },
                    onShowBottomBar = { /* iMainActivity.showBottomBar(it) */ }
                )
            }
        }
    }
}
