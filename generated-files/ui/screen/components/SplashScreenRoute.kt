package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.SplashScreenComposeViewModel
import ifac.td.taxi.ui.screen.components.SplashScreenUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 10-1: import android.graphics.Color
@Composable
fun SplashScreenRoute(
    viewModel: SplashScreenComposeViewModel,
    onShowHeader: (Boolean) -> Unit,
    onShowBottomBar: (Boolean) -> Unit,
    onNavigateToLegalText: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onNavigateBackBlocked: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        onShowHeader(false)
        onShowBottomBar(false)
        viewModel.onEvent(SplashScreenUiEvent.ScreenStarted)
    }
    // Collect one-off effects (navigation/dialog/skip)
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiEffect.collectLatest { effect ->
                    when (effect) {
                        SplashScreenUiEffect.NavigateToLegalText -> onNavigateToLegalText()
                        SplashScreenUiEffect.NavigateToWelcome -> onNavigateToWelcome()
                        SplashScreenUiEffect.HideSplash -> {
                            // The original fragment hid the video view; in Compose, we simply stop showing it.
                            // state is already controlling this.
                        }
                        SplashScreenUiEffect.ShowSkipDialog -> {
                            // dialog is handled by UI state; nothing else needed
                        }
                    }
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.White)
    ) {
        if (uiState.showSplashVideo) {
            SplashVideo(
                rawVideoRes = R.raw.new_splash,
                backgroundColor = R.color.splash_background,
                onPrepared = {
                    viewModel.onEvent(SplashScreenUiEvent.VideoPrepared)
                },
                onCompleted = {
                    viewModel.onEvent(SplashScreenUiEvent.VideoCompleted)
                },
                onError = {
                    viewModel.onEvent(SplashScreenUiEvent.VideoError)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        // Buttons area / dialog handling if needed
        SplashScreenButtons(
            state = uiState.buttonsState,
            onEvent = viewModel::onEvent
        )
        if (uiState.showDialog) {
            SplashScreenCustomDialog(
                state = uiState.dialogState,
                onConfirm = { viewModel.onEvent(SplashScreenUiEvent.DialogConfirm) },
                onDismiss = { viewModel.onEvent(SplashScreenUiEvent.DialogDismiss) }
            )
        }
    }
    // preserve back-blocking if needed
    if (onNavigateBackBlocked != null) {
        // hook this into your activity back handler if desired
    }
}
@Composable
fun SplashVideo(
    @RawRes rawVideoRes: Int,
    backgroundColor: Int,
    onPrepared: () -> Unit,
    onCompleted: () -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoUri = remember(rawVideoRes, context) {
        Uri.parse("android.resource://${context.packageName}/$rawVideoRes")
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            VideoView(ctx).apply {
                setZOrderOnTop(true)
                setBackgroundColor(ctx.getColor(backgroundColor))
                setVideoURI(videoUri)
                setOnPreparedListener { mediaPlayer ->
                    setBackgroundColor(Color.TRANSPARENT)
                    mediaPlayer.isLooping = false
                    mediaPlayer.start()
                    onPrepared()
                }
                setOnCompletionListener {
                    onCompleted()
                }
                setOnErrorListener { _, _, _ ->
                    onError()
                    true
                }
            }
        },
        update = { view ->
            if (!view.isPlaying) {
                view.start()
            }
        }
    )
}
