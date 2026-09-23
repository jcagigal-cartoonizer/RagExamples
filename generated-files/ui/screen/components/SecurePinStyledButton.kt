package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.compose.viewmodel.SecurePinComposeViewModel
import ifac.td.taxi.ui.screen.components.SecurePinButtonStyle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 538-9: import androidx.compose.material3.*
@Composable
fun SecurePinStyledButton(
    state: SecurePinButtonState,
    onClick: () -> Unit
) {
    val colors = securePinButtonColors(state.style)
    if (state.style == SecurePinButtonStyle.Secondary) {
        OutlinedButton(
            onClick = onClick,
            enabled = state.enabled,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.container,
                contentColor = colors.content,
                disabledContentColor = colors.disabledContent
            )
        ) {
            Text(text = stringResource(state.textRes))
        }
    } else {
        Button(
            onClick = onClick,
            enabled = state.enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.container,
                contentColor = colors.content,
                disabledContainerColor = colors.disabledContainer,
                disabledContentColor = colors.disabledContent
            )
        ) {
            Text(text = stringResource(state.textRes))
        }
    }
}
In the fragment version, navigation is handled by:
In Compose, that is preserved via:
composable("secure_pin") {
    val viewModel: SecurePinComposeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    SecurePinScreen(
        navController = navController,
        viewModel = viewModel,
        onShowBottomBar = { visible -> /* call host */ },
        onShowHeader = { visible -> /* call host */ }
    )
}
Because the XML files and the original `CustomButton` / `SecurePinCustomDialog` implementations were not included, I matched behavior based on the fragment logic:
1. a **more exact Material 2 / Material 3 custom button recreation**
2. a **fully themed dialog matching your XML margins, corners, and colors**
3. a **Navigation Compose version with argument support**
4. a **Koin module for the Compose ViewModel**
5. a **migration version that keeps your existing domain/usecase classes unchanged**
