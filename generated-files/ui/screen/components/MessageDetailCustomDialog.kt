package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 487-5: import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
@Composable
fun MessageDetailCustomDialog(
    state: MessageDetailDialogState,
    onDismiss: () -> Unit,
    onResult: (MessageDetailDialogResult) -> Unit
) {
    var input by remember(state) { mutableStateOf(TextFieldValue("")) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.description?.let { Text(it) }
                state.message?.let { Text(it) }
                state.messageOptions?.let { options ->
                    Column {
                        options.forEach { option ->
                            TextButton(
                                onClick = {
                                    input = TextFieldValue(option)
                                }
                            ) {
                                Text(option)
                            }
                        }
                    }
                }
                if (state.hint != null) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text(state.hint) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            when {
                state.buttons.contains(MessageDetailDialogButtonType.ACCEPT) -> {
                    TextButton(onClick = {
                        onResult(MessageDetailDialogResult.Accept(input.text))
                    }) { Text("Accept") }
                }
                state.buttons.contains(MessageDetailDialogButtonType.SEND) -> {
                    TextButton(onClick = {
                        onResult(MessageDetailDialogResult.SendCustomMessage(input.text))
                    }) { Text("Send") }
                }
            }
        },
        dismissButton = {
            if (state.buttons.contains(MessageDetailDialogButtonType.CANCEL)) {
                TextButton(onClick = { onDismiss() }) { Text("Cancel") }
            }
        }
    )
}
sealed interface MessageDetailDialogResult {
    data object Dismiss : MessageDetailDialogResult
    data class Accept(val text: String) : MessageDetailDialogResult
    data class SendCustomMessage(val text: String) : MessageDetailDialogResult
    data object DeleteAccepted : MessageDetailDialogResult
}
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ifac.td.taxi.ui.screen.compose.MessageDetailRoute
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
class MessageDetailComposeFragment : Fragment(R.layout.fragment_message_detail_compose) {
    private val vModel: ifac.td.taxi.viewmodel.MessageDetailComposeViewModel by viewModel()
    private val sharedViewModel: ifac.td.taxi.viewmodel.MainActivityViewModel by activityViewModel()
    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        val args = MessageDetailFragmentArgs.fromBundle(requireArguments())
        view.findViewById<ComposeView>(R.id.composeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MessageDetailRoute(
                    navController = findNavController(),
                    viewModel = vModel,
                    sharedViewModel = sharedViewModel,
                    messageId = args.messageId,
                    skipAutoClose = args.skipAutoClose,
                    onNavigateToPredefinedMessages = { messageId ->
                        findNavController().navigate(
                            MessageDetailComposeFragmentDirections.actionMessageDetailFragmentToPredefinedMessageFragment(
                                messageId,
                                true
                            )
                        )
                    }
                )
            }
        }
    }
}
`fragment_message_detail_compose.xml` can be as simple as:
<?xml version="1.0" encoding="utf-8"?>
<androidx.compose.ui.platform.ComposeView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/composeView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
The code above is intentionally close to the fragment logic. In a production Compose migration, you would likely also want:
