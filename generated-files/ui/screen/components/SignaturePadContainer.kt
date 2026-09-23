package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 356-4: import android.graphics.Bitmap
@Composable
fun SignaturePadContainer(
    modifier: Modifier = Modifier,
    onSignatureChanged: (Boolean) -> Unit,
    onBitmapChanged: (Bitmap?) -> Unit,
    onCleared: () -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SignaturePad(context).apply {
                setOnSignedListener(object : SignaturePad.OnSignedListener {
                    override fun onStartSigning() = Unit
                    override fun onSigned() {
                        onSignatureChanged(true)
                        onBitmapChanged(signatureBitmap)
                    }
                    override fun onClear() {
                        onSignatureChanged(false)
                        onBitmapChanged(null)
                        onCleared()
                    }
                })
            }
        },
        update = { pad ->
            // no-op unless you want to clear/reset from state
        }
    )
}
