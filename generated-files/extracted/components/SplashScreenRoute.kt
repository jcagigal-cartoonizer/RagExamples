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
import ifac.td.taxi.ui.screen.SplashScreenScreen
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
            SplashScreenHostSplashScreenCustomDialog(
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
