package ifac.td.taxi.ui.screen.compose
import android.os.Looper
import androidx.annotation.RawRes
import kotlinx.coroutines.flow.MutableStateFlow
import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.asSharedFlow
import android.widget.VideoView
import androidx.compose.runtime.mutableStateOf
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.os.Handler
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import ifac.td.taxi.domain.usecase.SessionUseCase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.foundation.background
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Box
import android.os.Build
import android.graphics.Color
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import android.app.Application
import androidx.compose.runtime.remember
import android.view.View
import kotlinx.coroutines.flow.MutableSharedFlow
import ifac.td.taxi.R


/* -----------------------------------------------------------
 * UI STATE / EVENT / EFFECT
 * ----------------------------------------------------------- */

data class SplashScreenUiState(
    val reconnectionFlowValue: Boolean = false,
    val shouldSkipSplash: Boolean = false,
    val isSkipped: Boolean = false,
    val showVideo: Boolean = true,
    val dialogState: SplashScreenDialogState = SplashScreenDialogState.Hidden,
    val buttonsState: SplashScreenButtonsState = SplashScreenButtonsState.initial(),
)

sealed interface SplashScreenUiEvent {
    data class CheckReconnecting(val reconnectionFlowValue: Boolean) : SplashScreenUiEvent
    data object VideoCompleted : SplashScreenUiEvent
    data class VideoError(val what: Int, val extra: Int) : SplashScreenUiEvent
    data object TimeoutReached : SplashScreenUiEvent
    data object EndSplash : SplashScreenUiEvent
    data object EndSplashWithoutCheck : SplashScreenUiEvent
    data class UpdateReconnectionValue(val value: Boolean) : SplashScreenUiEvent
}

sealed interface SplashScreenUiEffect {
    data object NavigateToWelcome : SplashScreenUiEffect
    data object NavigateToLegalText : SplashScreenUiEffect
    data object EndSplashWithoutCheck : SplashScreenUiEffect
}

sealed interface SplashScreenDialogState {
    data object Hidden : SplashScreenDialogState
}

/* -----------------------------------------------------------
 * BUTTON VISUAL STATE
 * ----------------------------------------------------------- */

enum class SplashScreenStyleButton {
    ENABLE,
    DISABLE,
    LOADING,
}

enum class SplashScreenBackgroundButtonColor {
    GREEN,
    RED,
    BLUE,
    YELLOW,
    ORANGE,
    DEFAULT,
    BLACK,
    TRANSPARENT
}

data class SplashScreenButtonVisualState(
    val text: String = "",
    val iconRes: Int? = null,
    val background: SplashScreenBackgroundButtonColor = SplashScreenBackgroundButtonColor.BLUE,
    val style: SplashScreenStyleButton = SplashScreenStyleButton.ENABLE,
    val textVisible: Boolean = true,
    val iconVisible: Boolean = true,
    val onlyTextVisible: Boolean = false,
    val onlyTextValue: String = "",
)

data class SplashScreenButtonsState(
    val primary: SplashScreenButtonVisualState = SplashScreenButtonVisualState(),
    val secondary: SplashScreenButtonVisualState = SplashScreenButtonVisualState(),
) {
    companion object {
        fun initial() = SplashScreenButtonsState()
    }
}

/* -----------------------------------------------------------
 * EXACT REDUCER (one-by-one branch style)
 * ----------------------------------------------------------- */

sealed interface SplashScreenButtonType {
    data object ACCEPT : SplashScreenButtonType
    data object CANCEL : SplashScreenButtonType
    data object HIDDEN : SplashScreenButtonType
    data object EMPTY : SplashScreenButtonType
    data object MINUTE : SplashScreenButtonType
    data object REINFORCEMENT_0 : SplashScreenButtonType
    data object REINFORCEMENT_1 : SplashScreenButtonType
    data object REINFORCEMENT_2 : SplashScreenButtonType
    data object REINFORCEMENT_3 : SplashScreenButtonType
    data object REINFORCEMENT_4 : SplashScreenButtonType
    data object REINFORCEMENT_5 : SplashScreenButtonType
    data object REINFORCEMENT_10 : SplashScreenButtonType
    data object REINFORCEMENT_15 : SplashScreenButtonType
    data object REINFORCEMENT_20 : SplashScreenButtonType
    data object REINFORCEMENT_25 : SplashScreenButtonType
    data object LOGIN : SplashScreenButtonType
    data object SALIR : SplashScreenButtonType
    data object RETRY : SplashScreenButtonType
    data object SHOW_ALL : SplashScreenButtonType
    data object SHOW_RECENT : SplashScreenButtonType
    data object LOGIN_WITH_CENTRAL : SplashScreenButtonType
    data object LOGIN_WITHOUT_CENTRAL : SplashScreenButtonType
    data object LOGIN_REINFORCEMENT : SplashScreenButtonType
    data object OTHERS : SplashScreenButtonType
}

fun SplashScreenButtonsState.reduce(type: SplashScreenButtonType): SplashScreenButtonsState {
    val visual = when (type) {
        SplashScreenButtonType.ACCEPT -> SplashScreenButtonVisualState(
            text = "ACCEPT",
            iconRes = R.drawable.baseline_check_192,
            background = SplashScreenBackgroundButtonColor.GREEN,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = true,
            iconVisible = true
        )

        SplashScreenButtonType.CANCEL -> SplashScreenButtonVisualState(
            text = "CANCEL",
            iconRes = R.drawable.cerrar,
            background = SplashScreenBackgroundButtonColor.RED,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = true,
            iconVisible = true
        )

        SplashScreenButtonType.HIDDEN -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.TRANSPARENT,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false
        )

        SplashScreenButtonType.EMPTY -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLACK,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false
        )

        SplashScreenButtonType.MINUTE -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false,
            onlyTextVisible = true,
            onlyTextValue = "1"
        )

        SplashScreenButtonType.REINFORCEMENT_0 -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false,
            onlyTextVisible = true,
            onlyTextValue = "0"
        )

        SplashScreenButtonType.REINFORCEMENT_1 -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false,
            onlyTextVisible = true,
            onlyTextValue = "1"
        )

        SplashScreenButtonType.REINFORCEMENT_2 -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false,
            onlyTextVisible = true,
            onlyTextValue = "2"
        )

        SplashScreenButtonType.REINFORCEMENT_3 -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false,
            onlyTextVisible = true,
            onlyTextValue = "3"
        )

        SplashScreenButtonType.REINFORCEMENT_4 -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false,
            onlyTextVisible = true,
            onlyTextValue = "4"
        )

        SplashScreenButtonType.REINFORCEMENT_5 -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false,
            onlyTextVisible = true,
            onlyTextValue = "5"
        )

        SplashScreenButtonType.REINFORCEMENT_10 -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false,
            onlyTextVisible = true,
            onlyTextValue = "10"
        )

        SplashScreenButtonType.REINFORCEMENT_15 -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false,
            onlyTextVisible = true,
            onlyTextValue = "15"
        )

        SplashScreenButtonType.REINFORCEMENT_20 -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false,
            onlyTextVisible = true,
            onlyTextValue = "20"
        )

        SplashScreenButtonType.REINFORCEMENT_25 -> SplashScreenButtonVisualState(
            text = "",
            iconRes = null,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = false,
            iconVisible = false,
            onlyTextVisible = true,
            onlyTextValue = "25"
        )

        SplashScreenButtonType.LOGIN -> SplashScreenButtonVisualState(
            text = "LOGIN",
            iconRes = R.drawable.iniciocentral,
            background = SplashScreenBackgroundButtonColor.GREEN,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = true,
            iconVisible = true
        )

        SplashScreenButtonType.SALIR -> SplashScreenButtonVisualState(
            text = "SALIR",
            iconRes = R.drawable.baseline_exit_to_app_192,
            background = SplashScreenBackgroundButtonColor.RED,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = true,
            iconVisible = true
        )

        SplashScreenButtonType.RETRY -> SplashScreenButtonVisualState(
            text = "RETRY",
            iconRes = R.drawable.baseline_autorenew_192,
            background = SplashScreenBackgroundButtonColor.YELLOW,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = true,
            iconVisible = true
        )

        SplashScreenButtonType.SHOW_ALL -> SplashScreenButtonVisualState(
            text = "SHOW ALL",
            iconRes = R.drawable.baseline_add_192,
            background = SplashScreenBackgroundButtonColor.GREEN,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = true,
            iconVisible = true
        )

        SplashScreenButtonType.SHOW_RECENT -> SplashScreenButtonVisualState(
            text = "SHOW RECENT",
            iconRes = R.drawable.baseline_remove_192,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = true,
            iconVisible = true
        )

        SplashScreenButtonType.LOGIN_WITH_CENTRAL -> SplashScreenButtonVisualState(
            text = "LOGIN WITH CENTRAL",
            iconRes = R.drawable.iniciocentral,
            background = SplashScreenBackgroundButtonColor.GREEN,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = true,
            iconVisible = true
        )

        SplashScreenButtonType.LOGIN_WITHOUT_CENTRAL -> SplashScreenButtonVisualState(
            text = "LOGIN WITHOUT CENTRAL",
            iconRes = R.drawable.iniciosincentral,
            background = SplashScreenBackgroundButtonColor.BLUE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = true,
            iconVisible = true
        )

        SplashScreenButtonType.LOGIN_REINFORCEMENT -> SplashScreenButtonVisualState(
            text = "LOGIN REINFORCEMENT",
            iconRes = R.drawable.iniciocentral,
            background = SplashScreenBackgroundButtonColor.ORANGE,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = true,
            iconVisible = true
        )

        SplashScreenButtonType.OTHERS -> SplashScreenButtonVisualState(
            text = "OTHERS",
            iconRes = R.drawable.contado,
            background = SplashScreenBackgroundButtonColor.RED,
            style = SplashScreenStyleButton.ENABLE,
            textVisible = true,
            iconVisible = true
        )
    }

    return copy(primary = visual)
}

/* -----------------------------------------------------------
 * VIEWMODEL
 * ----------------------------------------------------------- */

class SplashScreenViewModel(
    context: Application,
    val sessionUseCase: SessionUseCase
) : BaseViewModel(context) {

    val TAG = "SplashScreenViewModel"

    val _serviceActiveFlow = MutableSharedFlow<Boolean>()
    val serviceActiveFlow: SharedFlow<Boolean> = _serviceActiveFlow.asSharedFlow()

    fun checkIsReconnecting(reconnectionFlowValue: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val serviceWasActive = sessionUseCase.getServiceWasActive()
            Logs.d(
                TAG,
                "checkIsReconnecting serviceWasActive: $serviceWasActive, reconnectionFlowValue: $reconnectionFlowValue"
            )
            delay(50)
            _serviceActiveFlow.emit(serviceWasActive)
        }
    }
}

class SplashScreenMainActivityViewModel : ViewModel() {
    val _reconnectionFlow = MutableStateFlow(false)
    val reconnectionFlow: StateFlow<Boolean> = _reconnectionFlow.asStateFlow()

    val _showLegalTextFlow = MutableStateFlow<Boolean?>(null)
    val showLegalTextFlow: StateFlow<Boolean?> = _showLegalTextFlow.asStateFlow()

    fun setReconnectionValue(value: Boolean) {
        _reconnectionFlow.value = value
    }

    fun showLegalTextWindow(showLegal: Boolean) {
        _showLegalTextFlow.value = showLegal
    }

    fun consumeShowLegalText() {
        _showLegalTextFlow.value = null
    }
}

/* -----------------------------------------------------------
 * COMPOSE SCREEN
 * ----------------------------------------------------------- */

@Composable
fun SplashScreenRoute(
    viewModel: SplashScreenViewModel,
    sharedViewModel: SplashScreenMainActivityViewModel,
    onNavigateToWelcome: () -> Unit,
    onNavigateToLegalText: () -> Unit,
    onShowHeader: (Boolean) -> Unit = {},
    onShowBottomBar: (Boolean) -> Unit = {},
    onDisableBack: () -> Unit = {},
) {
    val uiState by remember { viewModel.uiStateHolder() }.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onShowHeader(false)
        onShowBottomBar(false)
        onDisableBack()
        viewModel.checkIsReconnecting(sharedViewModel.reconnectionFlow.value)
    }

    LaunchedEffect(Unit) {
        viewModel.serviceActiveFlow.collect { shouldSkipSplash ->
            Logs.d("SplashScreenFragment", "checkIsReconnecting shouldSkipSplash: $shouldSkipSplash")
            if (shouldSkipSplash) {
                endSplashWithoutCheck(
                    sharedViewModel = sharedViewModel,
                    onNavigateToWelcome = onNavigateToWelcome
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        sharedViewModel.showLegalTextFlow.collect { showLegal ->
            showLegal?.let {
                if (it) onNavigateToLegalText() else onNavigateToWelcome()
                sharedViewModel.consumeShowLegalText()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor(android.graphics.Color.parseColor("#000000")))
    ) {
        SplashVideoPlayer(
            rawResId = R.raw.new_splash,
            splashTimeout = 4800L,
            onPrepared = {
                Logs.d("SplashScreenFragment", "setUpVideo -> Video prepared, starting")
            },
            onVideoEnded = {
                Logs.d("SplashScreenFragment", "setUpVideo -> Video ended")
                endSplash(sharedViewModel)
            },
            onVideoError = { what, extra ->
                Logs.e("SplashScreenFragment", "Error during video playback: what=$what, extra=$extra")
                endSplash(sharedViewModel)
            }
        )
    }
}

@Composable
private fun SplashVideoPlayer(
    @RawRes rawResId: Int,
    splashTimeout: Long,
    onPrepared: () -> Unit,
    onVideoEnded: () -> Unit,
    onVideoError: (what: Int, extra: Int) -> Unit,
) {
    val context = LocalContext.current
    val handler = remember { Handler(Looper.getMainLooper()) }
    var ended by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            handler.removeCallbacksAndMessages(null)
        }
    }

    AndroidView(
        factory = {
            VideoView(it).apply {
                setZOrderOnTop(true)
                setBackgroundColor(it.resources.getColor(R.color.splash_background, null))

                setOnPreparedListener { mediaPlayer ->
                    setBackgroundColor(Color.TRANSPARENT)
                    mediaPlayer.isLooping = false
                    mediaPlayer.start()
                    onPrepared()
                }

                setOnCompletionListener {
                    if (!ended) {
                        ended = true
                        onVideoEnded()
                    }
                }

                setOnErrorListener { _, what, extra ->
                    if (!ended) {
                        ended = true
                        onVideoError(what, extra)
                    }
                    true
                }

                try {
                    val videoUri = Uri.parse("android.resource://${context.packageName}/$rawResId")
                    setVideoURI(videoUri)
                    handler.postDelayed({
                        if (!ended) {
                            ended = true
                            onVideoEnded()
                        }
                    }, splashTimeout)
                } catch (e: Exception) {
                    if (!ended) {
                        ended = true
                        onVideoEnded()
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

/* -----------------------------------------------------------
 * FRAGMENT PARITY HELPERS
 * ----------------------------------------------------------- */

private fun endSplash(sharedViewModel: SplashScreenMainActivityViewModel) {
    sharedViewModel.showLegalTextWindow(false)
}

private fun endSplashWithoutCheck(
    sharedViewModel: SplashScreenMainActivityViewModel,
    onNavigateToWelcome: () -> Unit
) {
    sharedViewModel.showLegalTextWindow(true)
    onNavigateToWelcome()
}

/* -----------------------------------------------------------
 * OPTIONAL STATE HOLDER ACCESSOR
 * ----------------------------------------------------------- */

private fun SplashScreenViewModel.uiStateHolder(): StateFlow<SplashScreenUiState> {
    // Minimal bridge for a single-file compile-friendly example.
    // If your real ViewModel already exposes uiState, replace this with that property.
    return MutableStateFlow(SplashScreenUiState())
}

