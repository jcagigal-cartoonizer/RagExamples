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
import  androidx.compose.ui.window.Dialog
// # Block 476-6: import androidx.compose.foundation.layout.*
@Composable
fun GPSTestCustomDialogCustomDialog(
    title: String,
    message: String,
    confirmText: String = "OK",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(dismissText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onConfirm) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
That’s the Compose equivalent of your fragment’s `repeatOnLifecycle` collectors.
Your fragment used:
In Compose, preserve it by passing callbacks from the hosting fragment/activity:
GPSTestScreen(
    viewModel = viewModel,
    onNavigateBack = { navController.popBackStack() },
    launchIntent = { intent -> context.startActivity(intent) }
)
From the fragment:
class GPSTestComposeFragment : Fragment() {
    private val viewModel: GPSTestComposeViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            GPSTestScreen(
                viewModel = viewModel,
                onNavigateBack = { requireActivity().onBackPressedDispatcher.onBackPressed() },
                launchIntent = { intent -> startActivity(intent) }
            )
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.checkExternalGPS()
        viewModel.initViewModel()
    }
    override fun onPause() {
        viewModel.removeHandlerCallback()
        super.onPause()
    }
}
1. Your original ViewModel uses `Handler + Runnable` polling.  
   I preserved that to keep behavior identical. In a full Compose refactor, you may later want to replace it with a coroutine ticker loop.
2. `SharedFlow<GPSTestUiEffect>` is used for one-time events, as requested.
3. The dialog implementation here is generic because the exact `custom_dialog.xml` and `GPSTestCustomDialogCustomDialog.kt` weren’t included. If you provide them, I can match spacing, typography, button arrangement, and icons exactly.
4. The sample code shows the architecture and wiring. You’ll likely want to:
1. a **fully theme-aware Material3 version**,
2. a **closer pixel-match to your XML layout**,
3. or a **complete migration with Koin DI definitions for the new Compose ViewModel**.
