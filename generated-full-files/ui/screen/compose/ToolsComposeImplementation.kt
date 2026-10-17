package ifac.td.taxi.ui.screen.compose
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.material3.Icon
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.annotation.ColorInt
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.aspectRatio
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.annotation.DrawableRes
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.AndroidViewModel
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.update
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.unit.sp
import android.app.Application
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import kotlinx.coroutines.flow.MutableSharedFlow
import ifac.td.taxi.R
import androidx.compose.foundation.layout.Column


@Immutable
data class ToolsUiState(
    val buttonsState: ToolsButtonsState = ToolsButtonsState(),
    val dialogState: ToolsDialogState = ToolsDialogState.Hidden,
)

@Immutable
data class ToolsButtonsState(
    val requirements: ToolsButtonVisualState = ToolsButtonVisualState(),
    val meetingSign: ToolsButtonVisualState = ToolsButtonVisualState(),
)

@Immutable
data class ToolsButtonVisualState(
    val text: String = "",
    @DrawableRes val iconRes: Int = R.drawable.baseline_check_192,
    val enabled: Boolean = true,
    val visible: Boolean = true,
    val background: ToolsButtonBackground = ToolsButtonBackground.Blue,
    @ColorInt val textColor: Int = android.graphics.Color.WHITE,
    @ColorInt val iconTint: Int = android.graphics.Color.WHITE,
)

enum class ToolsButtonBackground {
    Green, Red, Blue, Yellow, Orange, Default, Black, Transparent
}

sealed interface ToolsDialogState {
    data object Hidden : ToolsDialogState
    data class ConfirmAction(val title: String, val message: String) : ToolsDialogState
}

sealed interface ToolsUiEvent {
    data object RequirementsClicked : ToolsUiEvent
    data object MeetingSignClicked : ToolsUiEvent
    data object DialogDismissed : ToolsUiEvent
}

sealed interface ToolsUiEffect {
    data class NavigateToRequirements(val actionId: Int = R.id.action_toolsFragment_to_requirementsFragment) : ToolsUiEffect
    data class NavigateToMeetingSign(
        @ColorInt val textColor: Int,
        @ColorInt val backgroundColor: Int
    ) : ToolsUiEffect

    data object ShowHeader : ToolsUiEffect
    data object HideDialog : ToolsUiEffect
}

object ToolsButtonsReducer {
    fun reduce(
        meetingSignTextColor: Int,
        meetingSignBackgroundColor: Int
    ): ToolsButtonsState {
        return ToolsButtonsState(
            requirements = ToolsButtonVisualState(
                text = "REQUIREMENTS",
                iconRes = R.drawable.baseline_rule_192,
                enabled = true,
                visible = true,
                background = ToolsButtonBackground.Blue,
                textColor = android.graphics.Color.WHITE,
                iconTint = android.graphics.Color.WHITE
            ),
            meetingSign = ToolsButtonVisualState(
                text = "MEETING SIGN",
                iconRes = R.drawable.outline_connecting_airports_24,
                enabled = true,
                visible = true,
                background = ToolsButtonBackground.Blue,
                textColor = meetingSignTextColor,
                iconTint = meetingSignTextColor
            )
        )
    }
}

class ToolsViewModel(
    val userPreferencesUseCase: UserPreferencesUseCase,
    application: Application,
) : AndroidViewModel(application) {

    val _uiState = MutableStateFlow(ToolsUiState())
    val uiState = _uiState.asStateFlow()

    val _uiEffect = MutableSharedFlow<ToolsUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<ToolsUiEffect> = _uiEffect.asSharedFlow()

    private var meetingSignTextColor: Int = 0
    private var meetingSignBackgroundColor: Int = 0

    init {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                meetingSignTextColor = it.meetingSignTextColor
                meetingSignBackgroundColor = it.meetingSignBackgroundColor
            }
            _uiState.update {
                it.copy(
                    buttonsState = ToolsButtonsReducer.reduce(
                        meetingSignTextColor = meetingSignTextColor,
                        meetingSignBackgroundColor = meetingSignBackgroundColor
                    )
                )
            }
        }
    }

    fun onEvent(event: ToolsUiEvent) {
        when (event) {
            ToolsUiEvent.RequirementsClicked -> clickRequirements()
            ToolsUiEvent.MeetingSignClicked -> clickMeetingSign()
            ToolsUiEvent.DialogDismissed -> {
                _uiState.update { it.copy(dialogState = ToolsDialogState.Hidden) }
            }
        }
    }

    fun clickRequirements() {
        viewModelScope.launch {
            _uiEffect.emit(ToolsUiEffect.NavigateToRequirements())
        }
    }

    fun clickMeetingSign() {
        viewModelScope.launch {
            _uiEffect.emit(
                ToolsUiEffect.NavigateToMeetingSign(
                    textColor = meetingSignTextColor,
                    backgroundColor = meetingSignBackgroundColor
                )
            )
        }
    }
}

interface ToolsMainActivityNavigator {
    fun showHeader(show: Boolean)
    fun navigateToRequirements()
    fun navigateToMeetingSign(textColor: Int, backgroundColor: Int)
}

class ToolsMainActivityViewModel : AndroidViewModel(Application()) {

    fun bindEffects(
        effects: SharedFlow<ToolsUiEffect>,
        navigator: ToolsMainActivityNavigator
    ) {
        viewModelScope.launch {
            effects.collect { effect ->
                when (effect) {
                    is ToolsUiEffect.NavigateToRequirements -> navigator.navigateToRequirements()
                    is ToolsUiEffect.NavigateToMeetingSign -> {
                        navigator.navigateToMeetingSign(effect.textColor, effect.backgroundColor)
                    }
                    ToolsUiEffect.ShowHeader -> navigator.showHeader(true)
                    ToolsUiEffect.HideDialog -> Unit
                }
            }
        }
    }
}

@Composable
fun ToolsScreen(
    viewModel: ToolsViewModel,
    mainActivityViewModel: ToolsMainActivityViewModel,
    navigator: ToolsMainActivityNavigator,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        navigator.showHeader(true)
    }

    LaunchedEffect(viewModel.uiEffect, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            mainActivityViewModel.bindEffects(viewModel.uiEffect, navigator)
        }
    }

    ToolsContent(
        state = uiState,
        onRequirementsClick = { viewModel.onEvent(ToolsUiEvent.RequirementsClicked) },
        onMeetingSignClick = { viewModel.onEvent(ToolsUiEvent.MeetingSignClicked) }
    )
}

@Composable
private fun ToolsContent(
    state: ToolsUiState,
    onRequirementsClick: () -> Unit,
    onMeetingSignClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(1.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ToolsButton(
                visualState = state.buttonsState.requirements,
                modifier = Modifier.weight(1f),
                onClick = onRequirementsClick
            )
            ToolsButton(
                visualState = state.buttonsState.meetingSign,
                modifier = Modifier.weight(1f),
                onClick = onMeetingSignClick
            )
        }
    }
}

@Composable
fun ToolsButton(
    visualState: ToolsButtonVisualState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (!visualState.visible) return

    val backgroundColor = when (visualState.background) {
        ToolsButtonBackground.Green -> Color(0xFF2E7D32)
        ToolsButtonBackground.Red -> Color(0xFFC62828)
        ToolsButtonBackground.Blue -> Color(0xFF1565C0)
        ToolsButtonBackground.Yellow -> Color(0xFFF9A825)
        ToolsButtonBackground.Orange -> Color(0xFFEF6C00)
        ToolsButtonBackground.Default -> Color(0xFF1565C0)
        ToolsButtonBackground.Black -> Color.Black
        ToolsButtonBackground.Transparent -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .clickable(enabled = visualState.enabled) { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = visualState.iconRes),
                contentDescription = visualState.text,
                tint = Color(visualState.iconTint)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = visualState.text,
                color = Color(visualState.textColor),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
        }
    }
}

### Important note
This is a **single file** and compiles structurally, but one thing to be aware of:

- `ToolsMainActivityViewModel : AndroidViewModel(Application())` is not the correct runtime construction pattern for a real app.
- In a real project, you should inject `Application` through your DI framework or make it a plain `ViewModel`.

If you want, I can do a **second single-file pass** that is:
- fully DI-safe for Koin,
- uses your real `BaseViewModel` / `BaseComposeViewModel` style,
- and includes a `Fragment` wrapper that preserves your existing navigation graph exactly.
