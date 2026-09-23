package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 365-6: import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun MessagesButtons(
    state: MessagesButtonsState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    onMarkRead: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.showBack) {
            Button(
                onClick = onBack,
                enabled = state.backEnabled,
                colors = messagesButtonColors(isPrimary = false),
                shape = messagesButtonShape
            ) { Text("Back") }
        }
        if (state.showSelect) {
            Button(
                onClick = onSelect,
                enabled = state.selectEnabled,
                colors = messagesButtonColors(isPrimary = true),
                shape = messagesButtonShape
            ) { Text("Select") }
        }
        if (state.showDelete) {
            Button(
                onClick = onDelete,
                enabled = state.deleteEnabled,
                colors = messagesButtonColors(isPrimary = false, isDanger = true),
                shape = messagesButtonShape
            ) { Text("Delete") }
        }
        if (state.showMarkRead) {
            Button(
                onClick = onMarkRead,
                enabled = state.markReadEnabled,
                colors = messagesButtonColors(isPrimary = false),
                shape = messagesButtonShape
            ) { Text("Mark Read") }
        }
    }
}
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Color
@Composable
fun MessagesCustomDialog(
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
