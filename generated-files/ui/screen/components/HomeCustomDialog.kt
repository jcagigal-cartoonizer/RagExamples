package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// // import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun HomeButton(
    state: HomeButtonUiState,
    onClick: () -> Unit
) {
    if (!state.visible) return
    val (bg, content) = state.toComposeColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .alpha(if (state.enabled) 1f else 0.5f)
            .clickable(enabled = state.enabled && !state.loading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (state.loading) {
            CircularProgressIndicator(color = content, strokeWidth = 2.dp)
        } else {
            Text(
                text = if (state.textRes != 0) stringResource(state.textRes) else "",
                color = content,
                fontSize = 16.sp
            )
        }
    }
}
// // import androidx.compose.foundation.background
import androidx.compose.material3.*
@Composable
fun HomeHomeCustomDialog(
    dialog: HomeDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(text = stringResource(dialog.titleRes))
        },
        text = {
            dialog.descriptionRes?.let {
                Text(text = stringResource(it))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
@Composable
fun HomeRoute(
    state: HomeUiState,
    effects: SharedFlow<HomeUiEffect>,
    onEvent: (HomeUiEvent) -> Unit,
    navController: NavController,
    onShowToast: (Int) -> Unit,
    onBeep: (Int) -> Unit
) {
    HomeScreen(
        state = state,
        onEvent = onEvent,
        effects = effects,
        onNavigate = { nav ->
            when (nav) {
                HomeNavigation.Back -> navController.navigateUp()
                is HomeNavigation.Id -> navController.navigate(nav.destination)
                is HomeNavigation.DeepLink -> {
                    val request = NavDeepLinkRequest.Builder
                        .fromUri(nav.uri.toUri())
                        .build()
                    navController.navigate(request)
                }
            }
        },
        onShowToast = onShowToast,
        onBeep = onBeep
    )
}
Your fragment had several responsibilities:
In Compose:
To match the XML fully, you would need to reflect:
The provided code gives the correct architecture and close button styling helpers, but for pixel-perfect matching you should:
A practical migration is:
