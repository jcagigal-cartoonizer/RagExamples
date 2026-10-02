@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    BackHandler(enabled = false)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsButton(
            text = stringResource(R.string.configuration),
            style = state.buttons.configuration.style,
            visible = state.buttons.configuration.visible,
            onClick = { onEvent(SettingsUiEvent.ConfigurationClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.device_settings),
            style = state.buttons.deviceSettings.style,
            visible = state.buttons.deviceSettings.visible,
            onClick = { onEvent(SettingsUiEvent.DeviceSettingsClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.gps),
            style = state.buttons.gps.style,
            visible = state.buttons.gps.visible,
            onClick = { onEvent(SettingsUiEvent.GpsClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.about),
            style = state.buttons.about.style,
            visible = state.buttons.about.visible,
            onClick = { onEvent(SettingsUiEvent.AboutClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.light),
            style = state.buttons.light.style,
            visible = state.buttons.light.visible,
            onClick = { onEvent(SettingsUiEvent.LightClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.requirements),
            style = state.buttons.requirements.style,
            visible = state.buttons.requirements.visible,
            onClick = { onEvent(SettingsUiEvent.RequirementsClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.preferencias),
            style = state.buttons.preferencias.style,
            visible = state.buttons.preferencias.visible,
            onClick = { onEvent(SettingsUiEvent.PreferenciasClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.bluetooth),
            style = state.buttons.bluetooth.style,
            visible = state.buttons.bluetooth.visible,
            onClick = { onEvent(SettingsUiEvent.BluetoothClicked) }
        )
        SettingsButton(
            text = stringResource(R.string.webview),
            style = state.buttons.webView.style,
            visible = state.buttons.webView.visible,
            onClick = { onEvent(SettingsUiEvent.WebViewClicked) }
        )
    }
    if (state.dialog != null) {
        SettingsCustomDialogCustomDialog(
            dialog = state.dialog,
            onDismiss = { onEvent(SettingsUiEvent.DialogDismissed) },
            onEvent = onEvent
        )
    }
}
