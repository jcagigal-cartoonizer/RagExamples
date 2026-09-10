package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.Composable
// // ## 8) Button composable

@Composable
private fun HomeTile(
    state: HomeButtonUiState,
    onClick: () -> Unit,
    label: String
) {
    if (!state.visible) {
        Box(modifier = Modifier.height(0.dp).fillMaxWidth())
        return
    }

    val bg = when (state.backgroundColor) {
        HomeButtonColor.BLUE -> Color(0xFF1976D2)
        HomeButtonColor.RED -> Color(0xFFD32F2F)
        HomeButtonColor.GREEN -> Color(0xFF2E7D32)
        HomeButtonColor.ORANGE -> Color(0xFFF57C00)
    }

    val clickable = state.style != HomeButtonStyle.DISABLE && state.style != HomeButtonStyle.EMPTY

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(if (state.style == HomeButtonStyle.EMPTY) Color.Transparent else bg)
            .then(
                if (clickable) Modifier
                    .padding(1.dp)
                    .wrapContentSize()
                else Modifier
            )
    ) {
        Button(
            onClick = onClick,
            enabled = clickable,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = bg,
                disabledContainerColor = Color.DarkGray
            )
        ) {
            Text(
                text = label,
                color = Color.White
            )
        }
    }
}


