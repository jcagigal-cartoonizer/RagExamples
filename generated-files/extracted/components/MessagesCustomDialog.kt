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
// # Block 428-7: import androidx.compose.foundation.background
@Composable
fun MessagesCustomDialogCustomDialog(
    title: String,
    description: String,
    buttons: List<DialogButtonConfig>,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    buttons.forEach { button ->
                        when (button) {
                            DialogButtonConfig.Cancel -> {
                                TextButton(onClick = onCancel) { Text("Cancel") }
                            }
                            DialogButtonConfig.Accept -> {
                                Button(onClick = onAccept) { Text("Accept") }
                            }
                        }
                    }
                }
            }
        }
    }
}
class MessageComposeFragment : Fragment() {
    private val viewModel: MessagesViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MessagesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { findNavController().navigateUp() },
                    onNavigateToMessageDetail = { messageId, skipAutoClose ->
                        val action =
                            MessageFragmentDirections.actionMessageFragmentToMessageDetailFragment(
                                messageId,
                                skipAutoClose
                            )
                        findNavController().navigate(action)
                    }
                )
            }
        }
    }
}
Your old `DateItem`/`GeneralItem` adapter logic can also be ported to Compose by precomputing a grouped list state. If you want, I can provide a second version that exactly reproduces the date header rendering as a grouped lazy list.
Your adapter builds a mixed list with:
A Compose equivalent would use a sealed UI item list and render date headers + messages in a single `LazyColumn`. If you want, I can provide that too.
1. a **fully grouped Compose list with date headers**, or  
2. a **drop-in Navigation Compose destination setup** for this screen.
