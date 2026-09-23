package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 335-6: import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.LegalTextButtonsState
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
