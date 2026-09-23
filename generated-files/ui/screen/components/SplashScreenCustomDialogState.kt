package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.SplashScreenComposeViewModel
import ifac.td.taxi.ui.screen.components.SplashScreenCustomDialogState
import ifac.td.taxi.ui.screen.components.SplashScreenCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 427-5: import androidx.compose.foundation.background
data class SplashScreenCustomDialogState(
    val title: String = "",
    val message: String = "",
    val confirmText: String = "OK",
    val dismissText: String = "Cancel",
    val showDismiss: Boolean = true,
    val isCancelable: Boolean = true
)
@Composable
fun SplashScreenCustomDialog(
    state: SplashScreenCustomDialogState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (state.isCancelable) onDismiss() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                if (state.title.isNotBlank()) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF444444),
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (state.showDismiss) {
                        TextButton(onClick = onDismiss) {
                            Text(state.dismissText)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(onClick = onConfirm) {
                        Text(state.confirmText)
                    }
                }
            }
        }
    }
}
@Composable
fun SplashScreenHost(
    viewModel: SplashScreenComposeViewModel,
    navigateToLegalText: () -> Unit,
    navigateToWelcome: () -> Unit,
    showHeader: (Boolean) -> Unit,
    showBottomBar: (Boolean) -> Unit
) {
    SplashScreenRoute(
        viewModel = viewModel,
        onShowHeader = showHeader,
        onShowBottomBar = showBottomBar,
        onNavigateToLegalText = navigateToLegalText,
        onNavigateToWelcome = navigateToWelcome
    )
}
Your fragment used `sharedViewModel.showLegalTextFlow`.  
In Compose, that should become:
I can also provide:
1. a `MainActivityViewModel` Compose version,
2. a `Navigation Compose` example with `NavHost`,
3. a more exact `SplashScreenCustomDialog` matching your XML if you paste `custom_dialog.xml`,
4. a more exact button style clone if you paste the custom button XML/component.
