@Composable
fun PermissionsScreen(
    uiState: PermissionsUiState,
    safePermissionType: String? = null,
    onEvent: (PermissionsUiEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        onEvent(PermissionsUiEvent.OnScreenStarted)
        safePermissionType?.let { onEvent(PermissionsUiEvent.HighlightPermission(it)) }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Permissions",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Bluetooth",
                    checked = uiState.buttons.bluetooth.checked,
                    enabled = uiState.buttons.bluetooth.enabled,
                    visible = uiState.buttons.bluetooth.visible,
                    highlighted = uiState.buttons.bluetooth.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickBluetooth) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Camera",
                    checked = uiState.buttons.camera.checked,
                    enabled = uiState.buttons.camera.enabled,
                    visible = uiState.buttons.camera.visible,
                    highlighted = uiState.buttons.camera.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickCamera) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Phone calls",
                    checked = uiState.buttons.phoneCalls.checked,
                    enabled = uiState.buttons.phoneCalls.enabled,
                    visible = uiState.buttons.phoneCalls.visible,
                    highlighted = uiState.buttons.phoneCalls.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickPhoneCalls) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Notifications",
                    checked = uiState.buttons.notifications.checked,
                    enabled = uiState.buttons.notifications.enabled,
                    visible = uiState.buttons.notifications.visible,
                    highlighted = uiState.buttons.notifications.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickNotifications) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Location",
                    checked = uiState.buttons.location.checked,
                    enabled = uiState.buttons.location.enabled,
                    visible = uiState.buttons.location.visible,
                    highlighted = uiState.buttons.location.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickLocation) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Background location",
                    checked = uiState.buttons.backgroundLocation.checked,
                    enabled = uiState.buttons.backgroundLocation.enabled,
                    visible = uiState.buttons.backgroundLocation.visible,
                    highlighted = uiState.buttons.backgroundLocation.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickBackgroundLocation) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Ignore battery optimizations",
                    checked = uiState.buttons.battery.checked,
                    enabled = uiState.buttons.battery.enabled,
                    visible = uiState.buttons.battery.visible,
                    highlighted = uiState.buttons.battery.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickBattery) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Microphone",
                    checked = uiState.buttons.microphone.checked,
                    enabled = uiState.buttons.microphone.enabled,
                    visible = uiState.buttons.microphone.visible,
                    highlighted = uiState.buttons.microphone.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickMicrophone) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "Overlay system",
                    checked = uiState.buttons.overlay.checked,
                    enabled = uiState.buttons.overlay.enabled,
                    visible = uiState.buttons.overlay.visible,
                    highlighted = uiState.buttons.overlay.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickOverlay) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "System settings",
                    checked = uiState.buttons.settings.checked,
                    enabled = uiState.buttons.settings.enabled,
                    visible = uiState.buttons.settings.visible,
                    highlighted = uiState.buttons.settings.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickSystemSettings) }
                )
            }
            item {
                PermissionSwitchRow(
                    title = "RedSys password",
                    checked = uiState.buttons.redSysPassword.checked,
                    enabled = uiState.buttons.redSysPassword.enabled,
                    visible = uiState.buttons.redSysPassword.visible,
                    highlighted = uiState.buttons.redSysPassword.highlighted,
                    onCheckedChange = { onEvent(PermissionsUiEvent.ClickRedSysPassword) }
                )
            }
        }
        if (uiState.dialogState.visible) {
            PermissionsCustomDialog(
                state = uiState.dialogState,
                onDismiss = { onEvent(PermissionsUiEvent.DialogDismiss) },
                onUsernameChanged = { onEvent(PermissionsUiEvent.DialogUsernameChanged(it)) },
                onPasswordChanged = { onEvent(PermissionsUiEvent.DialogPasswordChanged(it)) },
                onAccept = { onEvent(PermissionsUiEvent.DialogAccept) },
                onChangePassword = { onEvent(PermissionsUiEvent.DialogChangePassword) }
            )
        }
    }
}
