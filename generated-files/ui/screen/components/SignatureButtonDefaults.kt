package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SignatureButtonShape = RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 491-7: import androidx.compose.foundation.shape.RoundedCornerShape
@Composable
fun signatureButtonDefaults(
    enabled: Boolean,
    state: SignatureButtonState,
) = ButtonDefaults.buttonColors(
    containerColor = if (enabled) state.containerColor else state.disabledContainerColor,
    contentColor = if (enabled) state.contentColor else state.disabledContentColor,
    disabledContainerColor = state.disabledContainerColor,
    disabledContentColor = state.disabledContentColor
)
val SignatureButtonShape = RoundedCornerShape(12.dp)
fun signatureButtonHeight() = 52.dp
Example:
navController.navigate(PaymentDirections.goToScannerQRFragment(dispatchId))
That preserves your existing navigation action classes.
composable("signature/{serviceId}") { backStackEntry ->
    val serviceId = backStackEntry.arguments?.getString("serviceId")
    SignatureScreen(
        navController = navController,
        viewModel = signatureComposeViewModel,
        sharedViewModel = mainActivityViewModel,
        serviceId = serviceId,
        onShowHeader = { show -> mainActivityViewModel.showHeader(show) },
        onShowToast = { resId -> /* host activity toast */ }
    )
}
1. a `SignatureRoute(...)` wrapper,
2. Koin `viewModel()` injection in Compose,
3. exact `NavArgs` extraction in Compose,
4. and a more complete `MainActivityViewModel` interop example.
