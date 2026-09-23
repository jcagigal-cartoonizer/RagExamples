package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.LegalTextButtonsState
import ifac.td.taxi.compose.viewmodel.LegalTextComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 335-6: import androidx.compose.foundation.shape.RoundedCornerShape
@Composable
fun LegalTextButtonsState.customFilledButtonColors() = ButtonDefaults.buttonColors(
    containerColor = acceptBackgroundColor,
    contentColor = acceptContentColor,
    disabledContainerColor = acceptDisabledBackgroundColor,
    disabledContentColor = acceptDisabledContentColor
)
@Composable
fun LegalTextButtonsState.customFilledButtonShape() =
    RoundedCornerShape(acceptCornerRadiusDp.dp)
1. a **Koin module** for `LegalTextComposeViewModel`
2. a **Fragment-hosted ComposeView version**
3. a version that matches your existing `CustomButton` styling more precisely if you paste the XML/class definitions
