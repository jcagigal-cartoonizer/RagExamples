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
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.MessageDetailScreen
// # Block 487-5: import androidx.compose.foundation.layout.*
@Composable
fun MessageDetailComposeFragmentMessageDetailCustomDialog(
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
                state.buttons.contains(MessageDetailComposeFragmentDialogButtonType.ACCEPT) -> {
                    TextButton(onClick = {
                        onResult(MessageDetailDialogResult.Accept(input.text))
                    }) { Text("Accept") }
                }
                state.buttons.contains(MessageDetailComposeFragmentDialogButtonType.SEND) -> {
                    TextButton(onClick = {
                        onResult(MessageDetailDialogResult.SendCustomMessage(input.text))
                    }) { Text("Send") }
                }
            }
        },
        dismissButton = {
            if (state.buttons.contains(MessageDetailComposeFragmentDialogButtonType.CANCEL)) {
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
