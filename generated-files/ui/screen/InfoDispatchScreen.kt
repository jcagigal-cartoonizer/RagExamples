package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.InfoDispatchScreen
import ifac.td.taxi.ui.screen.state.InfoDispatchDialogState
import ifac.td.taxi.ui.screen.state.InfoDispatchButtonsState
import ifac.td.taxi.ui.screen.state.InfoDispatchButtons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import ifac.td.taxi.ui.screen.state.InfoDispatchUiEffect
import ifac.td.taxi.compose.viewmodel.InfoDispatchComposeViewModel
import ifac.td.taxi.ui.screen.state.InfoDispatchUiEvent
import ifac.td.taxi.ui.screen.state.InfoDispatchUiState
import ifac.td.taxi.ui.screen.state.ActionButtonState
import ifac.td.taxi.ui.screen.state.ButtonBackground
// // ## `InfoDispatchScreen.kt`


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

@Composable
fun InfoDispatchScreen(
    uiState: InfoDispatchUiState,
    onEvent: (InfoDispatchUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.topBarTitle) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            InfoDispatchHeader(uiState = uiState)

            Spacer(Modifier.height(12.dp))

            if (uiState.multiDispatchTabs.visible) {
                MultiDispatchTabs(
                    tabs = uiState.multiDispatchTabs.items,
                    selectedIndex = uiState.multiDispatchTabs.selectedIndex,
                    onTabSelected = { onEvent(InfoDispatchUiEvent.SelectDispatchTab(it)) }
                )
                Spacer(Modifier.height(12.dp))
            }

            InfoDispatchButtons(
                state = uiState.buttons,
                onNavigate = { onEvent(InfoDispatchUiEvent.NavigateClicked) },
                onVoiceCall = { onEvent(InfoDispatchUiEvent.VoiceCallClicked) },
                onPrint = { onEvent(InfoDispatchUiEvent.PrintClicked) },
                onNoClient = { onEvent(InfoDispatchUiEvent.NoClientClicked) },
                onReturn = { onEvent(InfoDispatchUiEvent.ReturnClicked) },
                onNotifications = { onEvent(InfoDispatchUiEvent.NotificationsClicked) }
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.details) { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


// # 3) UiState, UiEvent, UiEffect

// ## `InfoDispatchUiModels.kt`


import androidx.compose.runtime.Immutable
import ifac.td.taxi.viewmodel.model.InfoDispatchModel

@Immutable
data class InfoDispatchUiState(
    val topBarTitle: String = "",
    val dispatch: InfoDispatchModel? = null,
    val details: List<String> = emptyList(),
    val buttons: InfoDispatchButtonsState = InfoDispatchButtonsState(),
    val dialogState: InfoDispatchDialogState = InfoDispatchDialogState(),
    val multiDispatchTabs: MultiDispatchTabsState = MultiDispatchTabsState(),
    val flightCode: String? = null,
)

sealed interface InfoDispatchUiEvent {
    data object NavigateClicked : InfoDispatchUiEvent
    data object VoiceCallClicked : InfoDispatchUiEvent
    data object PrintClicked : InfoDispatchUiEvent
    data object NoClientClicked : InfoDispatchUiEvent
    data object ReturnClicked : InfoDispatchUiEvent
    data object NotificationsClicked : InfoDispatchUiEvent
    data object DismissDialog : InfoDispatchUiEvent
    data class SelectDispatchTab(val index: Int) : InfoDispatchUiEvent
    data class DialogAction(val action: InfoDispatchDialogAction) : InfoDispatchUiEvent
}

sealed interface InfoDispatchUiEffect {
    data object NavigateToDirections : InfoDispatchUiEffect
    data object NavigateToHome : InfoDispatchUiEffect
    data class NavigateToMeetingSign(val textColor: Int, val backgroundColor: Int) : InfoDispatchUiEffect
    data class ShowToast(val messageRes: Int) : InfoDispatchUiEffect
    data object RequestPhonePermission : InfoDispatchUiEffect
    data class OpenDialer(val phoneNumber: String) : InfoDispatchUiEffect
    data class ShowDialog(val dialog: InfoDispatchDialogState) : InfoDispatchUiEffect
}


// # 4) Full button state with exact visibility/color behavior

