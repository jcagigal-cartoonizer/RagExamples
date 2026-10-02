@Composable
fun ReceiptHistoryScreen(
    uiState: ReceiptHistoryUiState,
    buttonsState: ReceiptHistoryButtonsState,
    dialogState: ReceiptHistoryDialogState,
    onEvent: (ReceiptHistoryUiEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ReceiptViewer(
                trip = uiState.trip,
                isVisible = uiState.trip != null,
                isEmpty = uiState.isReceiptEmpty,
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.trip == null || uiState.isReceiptEmpty) {
                Text(
                    text = "No hay viajes",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ReceiptHistoryButtons(
                state = buttonsState,
                onPrint = { onEvent(ReceiptHistoryUiEvent.PrintClicked) },
                onPrevious = { onEvent(ReceiptHistoryUiEvent.PreviousClicked) },
                onPartials = { onEvent(ReceiptHistoryUiEvent.PartialsClicked) },
                onCard = { onEvent(ReceiptHistoryUiEvent.CardClicked) },
                onBill = { onEvent(ReceiptHistoryUiEvent.BillClicked) },
                onFoto = { onEvent(ReceiptHistoryUiEvent.PhotoClicked) }
            )
        }
        if (dialogState.visible) {
            ReceiptHistoryButtonsCustomDialogCompose(
                state = dialogState,
                onDismiss = { onEvent(ReceiptHistoryUiEvent.DialogDismissed) },
                onButtonClick = { btn -> onEvent(ReceiptHistoryUiEvent.DialogButtonClicked(btn)) }
            )
        }
    }
}
@Composable
fun ReceiptViewer(
    trip: Trip?,
    isVisible: Boolean,
    isEmpty: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    Surface(modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(text = "Recibo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(text = "Trip: ${trip?.id ?: "-"}")
            Text(text = "Importe: ${trip?.totalAmount ?: 0.0}")
            Text(text = "Empty: $isEmpty")
        }
    }
}
@Composable
fun CollectReceiptHistoryEffects(
    effects: SharedFlow<ReceiptHistoryUiEffect>,
    navController: NavController,
    onShareImage: (android.net.Uri) -> Unit,
    onToast: (Int) -> Unit,
    onOpenDialog: (ReceiptHistoryDialogState) -> Unit
) {
    LaunchedEffect(Unit) {
        effects.collect { effect ->
            when (effect) {
                ReceiptHistoryUiEffect.NavigateToTripHistory -> {
                    navController.navigate(R.id.action_receiptHistoryFragment_to_tripHistoryFragment)
                }
                ReceiptHistoryUiEffect.NavigateToHome -> {
                    navController.navigate(R.id.action_receiptHistoryFragment_to_homeFragment)
                }
                ReceiptHistoryUiEffect.NavigateToOnTrip -> {
                    navController.navigate(R.id.action_receiptHistoryFragment_to_onTripFragment)
                }
                is ReceiptHistoryUiEffect.NavigateToOnlineInvoice -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToOnlineInvoiceFragment(
                            effect.tripId
                        )
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToOfflineInvoice -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToOfflineInvoiceFragment(
                            effect.tripId
                        )
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToRefundMonei -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToRefundMoneiFragment(
                            effect.tripId
                        )
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToReceiptRedSys -> {
                    navController.navigate(
                        ReceiptHistoryFragmentDirections.actionReceiptHistoryFragmentToReceiptRedSysFragment(
                            effect.operations
                        )
                    )
                }
                is ReceiptHistoryUiEffect.NavigateToCropImage -> {
                    navController.navigate(
                        HomeDirections.goToCropImageViewFragment(effect.dispatchNumber)
                    )
                }
                is ReceiptHistoryUiEffect.ShareTicketImage -> onShareImage(effect.uri)
                is ReceiptHistoryUiEffect.ToastRes -> onToast(effect.resId)
                is ReceiptHistoryUiEffect.ShowDialog -> onOpenDialog(effect.dialogState)
            }
        }
    }
}
