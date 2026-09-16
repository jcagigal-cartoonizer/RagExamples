package ifac.td.taxi.ui.routes
import ifac.td.taxi.compose.viewmodel.InfoDispatchComposeViewModel
import ifac.td.taxi.ui.screen.components.InfoDispatchCustomDialog
import ifac.td.taxi.ui.screen.InfoDispatchScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // ## `InfoDispatchRoute.kt`


import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.InfoDispatchViewModel
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun InfoDispatchRoute(
    viewModel: InfoDispatchComposeViewModel,
    navController: NavController,
    onNavigateToHome: () -> Unit,
    onNavigateToDirections: () -> Unit,
    onNavigateToMeetingSign: (Int, Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is InfoDispatchUiEffect.ShowToast -> {
                    Toast.makeText(context, context.getString(effect.messageRes), Toast.LENGTH_SHORT).show()
                }
                is InfoDispatchUiEffect.NavigateToDirections -> onNavigateToDirections()
                is InfoDispatchUiEffect.NavigateToHome -> onNavigateToHome()
                is InfoDispatchUiEffect.NavigateToMeetingSign -> onNavigateToMeetingSign(effect.textColor, effect.backgroundColor)
                is InfoDispatchUiEffect.RequestPhonePermission -> {
                    // handled by host/fragment/activity
                }
                is InfoDispatchUiEffect.OpenDialer -> {
                    // handled by host
                }
                is InfoDispatchUiEffect.ShowDialog -> Unit
            }
        }
    }

    InfoDispatchScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )

    if (uiState.dialogState.visible) {
        InfoDispatchCustomDialog(
            state = uiState.dialogState,
            onDismiss = { viewModel.onEvent(InfoDispatchUiEvent.DismissDialog) },
            onAction = { action ->
                viewModel.onDialogAction(action)
            }
        )
    }
}


// # 2) Composable screen

