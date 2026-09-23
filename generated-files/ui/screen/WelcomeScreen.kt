package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.WelcomeUiAction
import ifac.td.taxi.ui.screen.components.WelcomeScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 126-2: import androidx.compose.foundation.layout.*
@Composable
fun WelcomeScreen(
    uiState: WelcomeUiState,
    onAction: (WelcomeUiAction) -> Unit,
) {
    val buttons = uiState.buttons
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WelcomeButton(
            text = buttons.start.text,
            enabled = buttons.start.enabled,
            loading = buttons.start.loading,
            color = buttons.start.color,
            visible = buttons.start.visible,
            onClick = { onAction(WelcomeUiAction.StartPressed) }
        )
        WelcomeButton(
            text = buttons.settings.text,
            enabled = buttons.settings.enabled,
            loading = buttons.settings.loading,
            color = buttons.settings.color,
            visible = buttons.settings.visible,
            onClick = { onAction(WelcomeUiAction.SettingsPressed) }
        )
        WelcomeButton(
            text = buttons.shifts.text,
            enabled = buttons.shifts.enabled,
            loading = buttons.shifts.loading,
            color = buttons.shifts.color,
            visible = buttons.shifts.visible,
            onClick = { onAction(WelcomeUiAction.ShiftsPressed) }
        )
        WelcomeButton(
            text = buttons.statistics.text,
            enabled = buttons.statistics.enabled,
            loading = buttons.statistics.loading,
            color = buttons.statistics.color,
            visible = buttons.statistics.visible,
            onClick = { onAction(WelcomeUiAction.StatisticsPressed) }
        )
        WelcomeButton(
            text = buttons.configuration.text,
            enabled = buttons.configuration.enabled,
            loading = buttons.configuration.loading,
            color = buttons.configuration.color,
            visible = buttons.configuration.visible,
            onClick = { onAction(WelcomeUiAction.ConfigurationPressed) }
        )
        WelcomeButton(
            text = buttons.permissions.text,
            enabled = buttons.permissions.enabled,
            loading = buttons.permissions.loading,
            color = buttons.permissions.color,
            visible = buttons.permissions.visible,
            onClick = { onAction(WelcomeUiAction.PermissionsPressed) }
        )
        WelcomeButton(
            text = buttons.partials.text,
            enabled = buttons.partials.enabled,
            loading = buttons.partials.loading,
            color = buttons.partials.color,
            visible = buttons.partials.visible,
            onClick = { onAction(WelcomeUiAction.PartialsPressed) }
        )
        WelcomeButton(
            text = buttons.exit.text,
            enabled = buttons.exit.enabled,
            loading = buttons.exit.loading,
            color = buttons.exit.color,
            visible = buttons.exit.visible,
            onClick = { onAction(WelcomeUiAction.ExitPressed) }
        )
    }
}
