@Composable
fun RefundMoneiScreen(
    state: RefundMoneiUiState,
    onEvent: (RefundMoneiUiEvent) -> Unit
) {
    val buttonsState = remember(state) {
        RefundMoneiButtonsState(
            showCancel = true,
            showRefund = state.status != PaymentMoneiViewModel.StatusPayments.REFUNDED,
            cancelEnabled = true,
            refundEnabled = state.canRefund && state.status != PaymentMoneiViewModel.StatusPayments.REFUNDED,
            cancelText = "Cancel",
            refundText = "Refund",
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Amount")
        Spacer(Modifier.height(8.dp))
        Text(text = state.amountText.toCurrency())
        Spacer(Modifier.height(12.dp))
        Text(text = state.status.name)
        if (state.isLoading) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            if (buttonsState.showCancel) {
                RefundMoneiButton(
                    text = buttonsState.cancelText,
                    enabled = buttonsState.cancelEnabled,
                    background = RefundMoneiButtonStyle.secondary,
                    textColor = RefundMoneiButtonStyle.textOnSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(RefundMoneiUiEvent.OnCancelClicked) }
                )
            }
            if (buttonsState.showRefund) {
                RefundMoneiButton(
                    text = buttonsState.refundText,
                    enabled = buttonsState.refundEnabled,
                    background = RefundMoneiButtonStyle.primary,
                    textColor = RefundMoneiButtonStyle.textOnPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(RefundMoneiUiEvent.OnRefundClicked) }
                )
            }
        }
        if (state.showConfirmDialog) {
            RefundMoneiComposeFragmentRefundMoneiCustomDialog(
                title = "Refund",
                message = "Do you want to refund this payment?",
                confirmText = "Refund",
                dismissText = "Cancel",
                onConfirm = { onEvent(RefundMoneiUiEvent.OnConfirmRefund) },
                onDismiss = { onEvent(RefundMoneiUiEvent.OnDismissDialog) }
            )
        }
        if (state.showErrorDialog) {
            RefundMoneiComposeFragmentRefundMoneiCustomDialog(
                title = "Error",
                message = state.errorMessage ?: "Something went wrong",
                confirmText = "OK",
                dismissText = "Close",
                onConfirm = { onEvent(RefundMoneiUiEvent.OnDismissDialog) },
                onDismiss = { onEvent(RefundMoneiUiEvent.OnDismissDialog) }
            )
        }
    }
}
