package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // ## 6) Compose custom button styling helpers

// This aims to mimic your `CustomButton` behavior more closely.

@Composable
fun DashboardCustomButton(
    text: String,
    type: DashboardButtonType,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = when (type) {
        DashboardButtonType.Blue -> ButtonDefaults.buttonColors(
            containerColor = DashboardButtonColors.BlueContainer,
            contentColor = DashboardButtonColors.BlueContent,
            disabledContainerColor = DashboardButtonColors.DisabledContainer,
            disabledContentColor = DashboardButtonColors.DisabledContent
        )
        DashboardButtonType.Red -> ButtonDefaults.buttonColors(
            containerColor = DashboardButtonColors.RedContainer,
            contentColor = DashboardButtonColors.RedContent,
            disabledContainerColor = DashboardButtonColors.DisabledContainer,
            disabledContentColor = DashboardButtonColors.DisabledContent
        )
        DashboardButtonType.Gray -> ButtonDefaults.buttonColors(
            containerColor = DashboardButtonColors.GrayContainer,
            contentColor = DashboardButtonColors.GrayContent,
            disabledContainerColor = DashboardButtonColors.DisabledContainer,
            disabledContentColor = DashboardButtonColors.DisabledContent
        )
        DashboardButtonType.Disable -> ButtonDefaults.buttonColors(
            containerColor = DashboardButtonColors.DisabledContainer,
            contentColor = DashboardButtonColors.DisabledContent,
            disabledContainerColor = DashboardButtonColors.DisabledContainer,
            disabledContentColor = DashboardButtonColors.DisabledContent
        )
    }

    Button(
        onClick = onClick,
        enabled = enabled && type != DashboardButtonType.Disable,
        colors = colors,
        modifier = modifier.height(48.dp)
    ) {
        Text(text = text)
    }
}


