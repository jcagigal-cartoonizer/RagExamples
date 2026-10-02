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
// # Block 410-6: import androidx.compose.foundation.BorderStroke
@Composable
fun chooseOptionButtonColors(style: ChooseOptionButtonStyle): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = style.backgroundColor,
        contentColor = style.contentColor,
        disabledContainerColor = style.backgroundColor.copy(alpha = 0.4f),
        disabledContentColor = style.contentColor.copy(alpha = 0.4f)
    )
}
fun chooseOptionButtonBorder(style: ChooseOptionButtonStyle): BorderStroke? {
    return style.borderColor?.let { BorderStroke(1.dp, it) }
}
fun chooseOptionButtonShape() = RoundedCornerShape(12.dp)
class ChooseOptionComposeFragment : Fragment() {
    private val args: ChooseOptionFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                // Obtain your VM via Koin/Hilt/manual factory as needed.
                // Here only the Compose UI is shown.
                ChooseOptionScreen(
                    viewModel = TODO("Provide ChooseOptionViewModel"),
                    onNavigateBack = { parentFragmentManager.popBackStack() },
                    onCloseScreenAndGoBack = { parentFragmentManager.popBackStack() },
                    onSetFragmentActive = { /* call activity method here */ },
                    title = "Choose Option"
                )
            }
        }
    }
}
To match the original XML behavior exactly, you’ll want to copy over these details from your XML/custom views:
Old flow:
Compose flow:
1. a full `custom_dialog.xml`-matched Compose dialog with header image, rounded shadow, and XML-like spacing, or  
2. a `ChooseOptionFragment` using `ComposeView` plus Koin injection so you can drop it into your existing navigation graph with minimal changes.
