package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
@Composable
fun CropImageButtons(
    state: CropImageButtonsState,
    onAccept: () -> Unit,
    onCrop: () -> Unit,
    onSelectImage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.acceptVisible) {
            CustomButton(
                text = "Accept",
                enabled = state.acceptEnabled,
                containerColor = state.acceptContainerColor,
                contentColor = state.acceptContentColor,
                disabledContainerColor = state.disabledContainerColor,
                disabledContentColor = state.disabledContentColor,
                onClick = onAccept
            )
        }
        if (state.cropVisible) {
            CustomButton(
                text = "Crop",
                enabled = state.cropEnabled,
                containerColor = state.cropContainerColor,
                contentColor = state.cropContentColor,
                disabledContainerColor = state.disabledContainerColor,
                disabledContentColor = state.disabledContentColor,
                onClick = onCrop
            )
        }
        if (state.selectImageVisible) {
            CustomButton(
                text = "Select image",
                enabled = state.selectImageEnabled,
                containerColor = state.selectContainerColor,
                contentColor = state.selectContentColor,
                disabledContainerColor = state.disabledContainerColor,
                disabledContentColor = state.disabledContentColor,
                onClick = onSelectImage
            )
        }
    }
}
@Composable
fun CustomButton(
    text: String,
    enabled: Boolean,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    disabledContainerColor: androidx.compose.ui.graphics.Color,
    disabledContentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = text)
    }
}
