package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.MeetingSignComposeViewModel
import ifac.td.taxi.ui.screen.components.MeetingSignUiEffect
import ifac.td.taxi.ui.screen.components.MeetingSignScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 278-5: import androidx.compose.foundation.background
@Composable
fun MeetingSignFabButton(
    state: MeetingSignFabButtonState,
    onClick: () -> Unit,
) {
    if (!state.visible) return
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .alpha(state.alpha),
        containerColor = Color.White,
        contentColor = Color.Black,
        elevation = FloatingActionButtonDefaults.elevation()
    ) {
        Icon(
            painter = painterResource(id = state.iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
@Composable
fun MeetingSignScreen(
    navController: NavController,
    viewModel: MeetingSignComposeViewModel,
    textColor: Int,
    backgroundColor: Int,
    fromDispatch: Boolean,
    isInSettings: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToInfoDispatch: () -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(textColor, backgroundColor, fromDispatch, isInSettings) {
        viewModel.setEnvironment(
            fromDispatch = fromDispatch,
            isInSettings = isInSettings,
            textColor = textColor,
            backgroundColor = backgroundColor
        )
    }
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    MeetingSignUiEffect.NavigateBack -> onNavigateBack()
                    MeetingSignUiEffect.NavigateToInfoDispatch -> onNavigateToInfoDispatch()
                    is MeetingSignUiEffect.ShowToast -> onShowToast(effect.messageRes)
                    is MeetingSignUiEffect.OpenEditDialog -> {
                        // handled by dialog state below
                    }
                }
            }
        }
    }
    val bg = if (uiState.backgroundColor != 0) Color(uiState.backgroundColor) else Color.Transparent
    val txt = if (uiState.textColor != 0) Color(uiState.textColor) else Color.Unspecified
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MeetingSignText(
                message = uiState.message,
                color = txt,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MeetingSignFabButton(
                    state = uiState.buttonsState.edit,
                    onClick = { viewModel.onEditClicked() }
                )
                MeetingSignFabButton(
                    state = uiState.buttonsState.dispatch,
                    onClick = { viewModel.onDispatchClicked() }
                )
                MeetingSignFabButton(
                    state = uiState.buttonsState.options,
                    onClick = { viewModel.onOptionsClicked() }
                )
            }
        }
    }
    if (uiState.message.isBlank()) {
        LaunchedEffect(uiState.message) {
            viewModel.onEditClicked()
        }
    }
    if (uiState.dialogState != null) {
        val d = uiState.dialogState!!
        MeetingSignCustomDialog(
            title = d.title,
            hint = d.hint,
            editText = d.editText,
            isCancellable = d.isCancellable,
            onDismiss = { viewModel.onDialogDismiss() },
            onCancel = { viewModel.onDialogDismiss() },
            onAccept = { input ->
                viewModel.onDialogConfirm(input)
                viewModel.onDialogDismiss()
            }
        )
    }
}
@Composable
fun MeetingSignText(
    message: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        style = MaterialTheme.typography.displayLarge,
        color = color,
        modifier = modifier
            .wrapContentSize()
            .padding(16.dp)
    )
}
In the fragment, navigation is done via `iMainActivity.navigateTo(...)` / `navigateBack()`. In Compose, preserve the same destination behavior by exposing callbacks from your hosting screen:
onNavigateBack = { navController.popBackStack() }
onNavigateToInfoDispatch = { navController.navigate(R.id.action_meetingSignFragment_to_infoDispatchFragment) }
That preserves the navigation contract.
That gives you one-off effect handling without leaking collectors.
For dialog handling:
The original XML/fragment uses:
In Compose, `Modifier.alpha` and `Modifier.offset` can mimic this visually, but Compose does not use `GONE` the same way. The `visible` flag in `MeetingSignFabButtonState` is the equivalent control. If you need animation parity, you can wrap buttons in `AnimatedVisibility`, `animateFloatAsState`, and `animateDpAsState`.
I can refine the Compose `MeetingSignCustomDialog` into a more exact `Dialog + Surface + Column` implementation that mirrors padding, shape, and typography more closely.
1. a full `@Composable` version with `AnimatedVisibility` for the FAB menu,
2. a `MeetingSignRoute()` example with Koin injection,
3. a more exact Material3 dialog styled to match your XML theme.
