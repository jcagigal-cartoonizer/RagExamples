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
// # Block 532-6: import androidx.compose.foundation.layout.Arrangement
enum class ComposePredefinedMessageComposeDialogButtonType{DialogButtonType {
    CANCEL, EDIT, SEND
}
@Composable
fun ComposePredefinedMessageComposeDialogButtonType{PredefinedMessageCustomDialog(
    title: String,
    description: String? = null,
    editText: String? = null,
    hint: String? = null,
    buttons: List<ComposePredefinedMessageComposeDialogButtonType{DialogButtonType>,
    onDismiss: () -> Unit,
    onButtonClick: (ComposePredefinedMessageComposeDialogButtonType{DialogButtonType, String?) -> Unit,
) {
    val textState = remember(editText) { mutableStateOf(editText.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!description.isNullOrBlank()) {
                    Text(text = description)
                }
                if (editText != null || hint != null) {
                    OutlinedTextField(
                        value = textState.value,
                        onValueChange = { textState.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = hint?.let { { Text(it) } }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        confirmButton = {
            DialogButtons(
                buttons = buttons,
                currentText = textState.value,
                onButtonClick = onButtonClick,
                onDismiss = onDismiss
            )
        }
    )
}
@Composable
fun DialogButtons(
    buttons: List<ComposePredefinedMessageComposeDialogButtonType{DialogButtonType>,
    currentText: String,
    onButtonClick: (ComposePredefinedMessageComposeDialogButtonType{DialogButtonType, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    Column {
        buttons.forEach { button ->
            TextButton(
                onClick = {
                    when (button) {
                        ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.CANCEL -> onDismiss()
                        else -> onButtonClick(button, currentText)
                    }
                }
            ) {
                Text(
                    text = when (button) {
                        ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.CANCEL -> "Cancel"
                        ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.EDIT -> "Edit"
                        ComposePredefinedMessageComposeDialogButtonType{DialogButtonType.SEND -> "Send"
                    }
                )
            }
        }
    }
}
class PredefinedMessageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext())
        composeView.setContent {
            val navController = findNavController()
            val viewModel: PredefinedMessageComposeViewModel = /* inject */
            val args: PredefinedMessageFragmentArgs by navArgs()
            PredefinedMessageScreen(
                navController = navController,
                viewModel = viewModel,
                messageId = args.messageId
            )
        }
        return composeView
    }
}
I can make the Compose implementation match almost 1:1.
