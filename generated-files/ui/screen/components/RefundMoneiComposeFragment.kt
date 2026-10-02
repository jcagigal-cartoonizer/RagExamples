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
// # Block 457-6: import android.os.Bundle
class RefundMoneiComposeFragment : Fragment() {
    private val args: RefundMoneiFragmentArgs by navArgs()
    private val viewModel: RefundMoneiComposeViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Scaffold {
                    RefundMoneiRoute(
                        tripId = args.tripId,
                        viewModel = viewModel,
                        onNavigateBack = { findNavController().navigateUp() },
                        onShowSnackbar = { /* hook into your snackbar host if needed */ }
                    )
                }
            }
        }
    }
}
then the `RefundMoneiButton` can be upgraded to a `Surface`-based implementation. Example pattern:
@Composable
fun RefundMoneiButton(
    text: String,
    enabled: Boolean,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        color = if (enabled) background else RefundMoneiButtonStyle.disabled,
        shape = RefundMoneiButtonStyle.shape,
        modifier = modifier
            .heightIn(min = 48.dp)
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
    ) {
        androidx.compose.foundation.layout.Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = text, color = textColor)
        }
    }
}
1. a **full Koin module setup** for the Compose ViewModel  
2. a **Compose version of the status badge** matching your `tvStatus` behavior exactly  
3. a **more exact translation of `custom_dialog.xml`** if you share that XML or `RefundMoneiComposeFragmentCustomDialog.kt` source
