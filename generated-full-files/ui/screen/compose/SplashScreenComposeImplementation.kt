package ifac.td.taxi.ui.screen.compose

import android.app.Application
import android.graphics.Color
import android.net.Uri
import android.widget.VideoView
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import ifac.td.taxi.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// -----------------------------------------------------------------------------
// 1) SHARED VM ADAPTER CONTRACT
// -----------------------------------------------------------------------------

/**
 * Adapter contract for your existing shared activity VM.
 * In your app, implement this by delegating to your real MainActivityViewModel
 * or ComposeSharedViewModel.
 */
interface SplashScreenSharedVmContract {
    val reconnectionFlow: StateFlow<Boolean>
    val showLegalTextFlow: StateFlow<Boolean?>

    fun showHeader(visible: Boolean)
    fun showBottomBar(visible: Boolean)
    fun showLegalTextWindow(show: Boolean)
    fun getBravoService(): Any?
    fun customBackPressed(block: () -> Unit)
    fun navigateTo(directions: NavDirections)
}

// -----------------------------------------------------------------------------
// 2) SPLASH UI STATE / EVENT / EFFECT
// -----------------------------------------------------------------------------

@Immutable
data class SplashScreenUiState(
    val isVideoVisible: Boolean = true,
    val isSkipped: Boolean = false,
)

sealed interface SplashScreenUiEvent {
    data class CheckIsReconnecting(val reconnectionFlowValue: Boolean) : SplashScreenUiEvent
    data object VideoPrepared : SplashScreenUiEvent
    data object VideoCompleted : SplashScreenUiEvent
    data class VideoError(val what: Int, val extra: Int) : SplashScreenUiEvent
    data object TimeoutReached : SplashScreenUiEvent
}

sealed interface SplashScreenUiEffect {
    data object EndSplash : SplashScreenUiEffect
    data object EndSplashWithoutCheck : SplashScreenUiEffect
}

// -----------------------------------------------------------------------------
// 3) REDUCER-STYLE STATE
// -----------------------------------------------------------------------------

@Immutable
data class SplashScreenButtonsState(
    val splashVisible: Boolean = true,
    val isSkipped: Boolean = false,

    val showLegalTextFlowValue: Boolean? = null,
    val navigateToLegalText: Boolean = false,
    val navigateToWelcome: Boolean = false,

    val serviceActiveFlowValue: Boolean? = null,
    val shouldSkipSplash: Boolean = false,
    val shouldEndSplashWithoutCheck: Boolean = false,

    val showLegalTextWindowValue: Boolean? = null,
    val hideVideoView: Boolean = false,
)

sealed interface SplashScreenReducerAction {
    data class ShowLegalTextFlowCollected(val value: Boolean?) : SplashScreenReducerAction
    data class ServiceActiveFlowCollected(val value: Boolean) : SplashScreenReducerAction
    data object EndSplash : SplashScreenReducerAction
    data object EndSplashWithoutCheck : SplashScreenReducerAction
    data object ResetNavigationFlags : SplashScreenReducerAction
}

fun reduceSplashScreenButtonsState(
    current: SplashScreenButtonsState,
    action: SplashScreenReducerAction
): SplashScreenButtonsState {
    return when (action) {
        is SplashScreenReducerAction.ShowLegalTextFlowCollected -> {
            when (action.value) {
                null -> current.copy(
                    showLegalTextFlowValue = null,
                    navigateToLegalText = false,
                    navigateToWelcome = false
                )
                true -> current.copy(
                    showLegalTextFlowValue = true,
                    navigateToLegalText = true,
                    navigateToWelcome = false
                )
                false -> current.copy(
                    showLegalTextFlowValue = false,
                    navigateToLegalText = false,
                    navigateToWelcome = true
                )
            }
        }

        is SplashScreenReducerAction.ServiceActiveFlowCollected -> current.copy(
            serviceActiveFlowValue = action.value,
            shouldSkipSplash = action.value,
            shouldEndSplashWithoutCheck = action.value
        )

        SplashScreenReducerAction.EndSplash -> current.copy(
            splashVisible = false,
            hideVideoView = true,
            showLegalTextWindowValue = false
        )

        SplashScreenReducerAction.EndSplashWithoutCheck -> current.copy(
            splashVisible = false,
            isSkipped = true,
            hideVideoView = true,
            showLegalTextWindowValue = true,
            shouldEndSplashWithoutCheck = true
        )

        SplashScreenReducerAction.ResetNavigationFlags -> current.copy(
            navigateToLegalText = false,
            navigateToWelcome = false
        )
    }
}

// -----------------------------------------------------------------------------
// 4) SPLASH VIEWMODEL
// -----------------------------------------------------------------------------

class SplashScreenViewModel(
    application: Application,
    private val sessionUseCase: SessionUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SplashScreenUiState())
    val uiState: StateFlow<SplashScreenUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SplashScreenUiEffect>()
    val effects: SharedFlow<SplashScreenUiEffect> = _effects.asSharedFlow()

    private val _serviceActiveFlow = MutableSharedFlow<Boolean>()
    val serviceActiveFlow: SharedFlow<Boolean> = _serviceActiveFlow.asSharedFlow()

    fun onEvent(event: SplashScreenUiEvent) {
        when (event) {
            is SplashScreenUiEvent.CheckIsReconnecting -> checkIsReconnecting(event.reconnectionFlowValue)
            SplashScreenUiEvent.VideoPrepared -> {
                _uiState.value = _uiState.value.copy(isVideoVisible = true)
            }
            SplashScreenUiEvent.VideoCompleted -> {
                viewModelScope.launch { _effects.emit(SplashScreenUiEffect.EndSplash) }
            }
            is SplashScreenUiEvent.VideoError -> {
                viewModelScope.launch { _effects.emit(SplashScreenUiEffect.EndSplash) }
            }
            SplashScreenUiEvent.TimeoutReached -> {
                viewModelScope.launch { _effects.emit(SplashScreenUiEffect.EndSplash) }
            }
        }
    }

    fun checkIsReconnecting(reconnectionFlowValue: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val serviceWasActive = sessionUseCase.getServiceWasActive()
            delay(50)
            _serviceActiveFlow.emit(serviceWasActive)
        }
    }
}

// -----------------------------------------------------------------------------
// 5) MAIN ACTIVITY SHARED VM ADAPTER
// -----------------------------------------------------------------------------

class SplashScreenMainActivityViewModel(
    application: Application
) : AndroidViewModel(application), SplashScreenSharedVmContract {

    private val _reconnectionFlow = MutableStateFlow(false)
    override val reconnectionFlow: StateFlow<Boolean> = _reconnectionFlow.asStateFlow()

    private val _showLegalTextFlow = MutableStateFlow<Boolean?>(null)
    override val showLegalTextFlow: StateFlow<Boolean?> = _showLegalTextFlow.asStateFlow()

    private val _navigationDirectionsFlow = MutableSharedFlow<NavDirections>()
    val navigationDirectionsFlow: SharedFlow<NavDirections> = _navigationDirectionsFlow.asSharedFlow()

    private var bravoService: Any? = null

    override fun showHeader(visible: Boolean) {
        // delegate to your real activity VM or UI controller
    }

    override fun showBottomBar(visible: Boolean) {
        // delegate to your real activity VM or UI controller
    }

    override fun showLegalTextWindow(show: Boolean) {
        _showLegalTextFlow.value = show
    }

    override fun getBravoService(): Any? = bravoService

    fun setBravoService(service: Any?) {
        bravoService = service
    }

    fun setReconnectionValue(value: Boolean) {
        _reconnectionFlow.value = value
    }

    override fun customBackPressed(block: () -> Unit) {
        block()
    }

    override fun navigateTo(directions: NavDirections) {
        _navigationDirectionsFlow.tryEmit(directions)
    }
}

// -----------------------------------------------------------------------------
// 6) COMPOSE SCREEN
// -----------------------------------------------------------------------------

@Composable
fun SplashScreenRoute(
    navController: androidx.navigation.NavController,
    splashViewModel: SplashScreenViewModel,
    sharedViewModel: SplashScreenSharedVmContract,
    onNavigateToWelcome: () -> Unit,
    onNavigateToLegalText: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by splashViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        sharedViewModel.showHeader(false)
        sharedViewModel.showBottomBar(false)
        splashViewModel.onEvent(
            SplashScreenUiEvent.CheckIsReconnecting(
                reconnectionFlowValue = sharedViewModel.reconnectionFlow.value
            )
        )
    }

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                sharedViewModel.showLegalTextFlow.collectLatest { showLegal ->
                    showLegal?.let {
                        if (it) onNavigateToLegalText() else onNavigateToWelcome()
                    }
                }
            }

            launch {
                splashViewModel.serviceActiveFlow.collectLatest { shouldSkipSplash ->
                    if (shouldSkipSplash && sharedViewModel.getBravoService() == null) {
                        sharedViewModel.showLegalTextWindow(true)
                        onNavigateToWelcome()
                    }
                }
            }

            launch {
                splashViewModel.effects.collectLatest { effect ->
                    when (effect) {
                        SplashScreenUiEffect.EndSplash -> {
                            sharedViewModel.showLegalTextWindow(false)
                        }
                        SplashScreenUiEffect.EndSplashWithoutCheck -> {
                            sharedViewModel.showLegalTextWindow(true)
                            onNavigateToWelcome()
                        }
                    }
                }
            }

            launch {
                if (sharedViewModel is SplashScreenMainActivityViewModel) {
                    sharedViewModel.navigationDirectionsFlow.collectLatest { directions ->
                        navController.navigate(directions)
                    }
                }
            }
        }
    }

    SplashScreenContent(
        splashVisible = uiState.isVideoVisible,
        splashBackgroundRes = R.color.splash_background,
        splashVideoRes = R.raw.new_splash,
        onVideoPrepared = { splashViewModel.onEvent(SplashScreenUiEvent.VideoPrepared) },
        onVideoCompleted = { splashViewModel.onEvent(SplashScreenUiEvent.VideoCompleted) },
        onVideoError = { what, extra ->
            splashViewModel.onEvent(SplashScreenUiEvent.VideoError(what, extra))
        },
        onTimeoutReached = { splashViewModel.onEvent(SplashScreenUiEvent.TimeoutReached) }
    )
}

// -----------------------------------------------------------------------------
// 7) COMPOSE CONTENT
// -----------------------------------------------------------------------------

@Composable
private fun SplashScreenContent(
    splashVisible: Boolean,
    @RawRes splashVideoRes: Int,
    splashBackgroundRes: Int,
    onVideoPrepared: () -> Unit,
    onVideoCompleted: () -> Unit,
    onVideoError: (what: Int, extra: Int) -> Unit,
    onTimeoutReached: () -> Unit,
) {
    val context = LocalContext.current
    val backgroundColor = androidx.compose.ui.res.colorResource(id = splashBackgroundRes)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (splashVisible) {
            AndroidView(
                factory = {
                    VideoView(it).apply {
                        setZOrderOnTop(true)
                        setBackgroundColor(Color.TRANSPARENT)

                        setOnPreparedListener { mediaPlayer ->
                            setBackgroundColor(Color.TRANSPARENT)
                            mediaPlayer.isLooping = false
                            mediaPlayer.start()
                            onVideoPrepared()
                        }

                        setOnCompletionListener {
                            onVideoCompleted()
                        }

                        setOnErrorListener { _, what, extra ->
                            onVideoError(what, extra)
                            true
                        }

                        setVideoURI(
                            Uri.parse("android.resource://${context.packageName}/$splashVideoRes")
                        )
                    }
                },
                update = { }
            )
        }

        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(4800L)
            onTimeoutReached()
        }
    }
}

// -----------------------------------------------------------------------------
// 8) CUSTOM BUTTON API MATCHING YOUR PATTERN
// -----------------------------------------------------------------------------

@Immutable
data class SplashScreenButtonStyle(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val textRes: Int = 0,
)

@Composable
fun SplashScreenButton(
    style: SplashScreenButtonStyle,
    onClick: () -> Unit
) {
    if (!style.visible) return

    // Replace this with your exact custom button component implementation.
    // This is intentionally shaped like your existing API:
    //   ContactCentralButton(style = ..., onClick = ...)
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = style.enabled
    ) {
        androidx.compose.material3.Text(text = "Button")
    }
}

// -----------------------------------------------------------------------------
// 9) MINIMAL SESSION USE CASE CONTRACT
// -----------------------------------------------------------------------------

interface SessionUseCase {
    fun getServiceWasActive(): Boolean
}


