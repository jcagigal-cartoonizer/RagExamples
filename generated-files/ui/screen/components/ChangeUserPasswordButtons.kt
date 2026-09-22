package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.foundation.BorderStroke
// // // import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
@Composable
fun ChangeUserPasswordButtons(
    state: ChangeUserPasswordButtonsState,
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.showCancel) {
            CustomLikeButton(
                modifier = Modifier.weight(1f),
                text = "Cancel",
                appearance = state.cancel,
                enabled = state.cancelEnabled,
                onClick = onCancelClick
            )
        }
        if (state.showAccept) {
            CustomLikeButton(
                modifier = Modifier.weight(1f),
                text = "Accept",
                appearance = state.accept,
                enabled = state.acceptEnabled,
                onClick = onAcceptClick
            )
        }
    }
}
@Composable
fun CustomLikeButton(
    modifier: Modifier,
    text: String,
    appearance: ButtonAppearance,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg = Color(appearance.backgroundColor)
    val content = Color(appearance.contentColor)
    val stroke = appearance.strokeColor?.let { Color(it) }
    if (stroke != null && appearance.strokeWidthDp > 0) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(appearance.minHeightDp.dp),
            shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
            border = BorderStroke(appearance.strokeWidthDp.dp, stroke),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = bg,
                contentColor = content,
                disabledContainerColor = bg.copy(alpha = 0.5f),
                disabledContentColor = content.copy(alpha = 0.5f)
            )
        ) {
            Text(text)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(appearance.minHeightDp.dp),
            shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = bg,
                contentColor = content,
                disabledContainerColor = bg.copy(alpha = 0.5f),
                disabledContentColor = content.copy(alpha = 0.5f)
            )
        ) {
            Text(text)
        }
    }
}
// // # Block 428-8: import androidx.compose.foundation.layout.*
// // // import androidx.compose.foundation.layout.*
@Composable
fun ChangeUserPasswordDialog(
    state: ChangeUserPasswordDialogState,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = state.title) },
        text = {
            state.description?.let { Text(text = it) }
        },
        confirmButton = {
            if (state.buttons.contains(ChangeUserPasswordDialogButton.Accept)) {
                TextButton(onClick = onAccept) {
                    Text("ACCEPT")
                }
            }
        }
    )
}
data class ComposeCustomDialogModel(
    val title: String,
    val description: String? = null,
    val buttons: List<ChangeUserPasswordDialogButton> = listOf(ChangeUserPasswordDialogButton.Accept)
)
Then the dialog composable can render it directly.
class ChangeUserPasswordComposeFragment : Fragment() {
    private val viewModel: ChangeUserPasswordComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            ChangeUserPasswordRoute(
                viewModel = viewModel,
                onNavigateBack = {
                    findNavController().navigateUp()
                }
            )
        }
    }
}
Your fragment logic maps like this:
Your original code emits a `UserPresenter?` via `MutableSharedFlow<UserPresenter?>`. In Compose, a cleaner approach is to:
1. validate input
2. create presenter
3. invoke password change
4. receive callback success/failure
5. emit dialog effect
That avoids having to store a presenter in state.
