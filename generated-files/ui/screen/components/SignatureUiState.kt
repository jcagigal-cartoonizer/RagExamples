package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SignatureUiState
import ifac.td.taxi.ui.screen.components.SignatureButtonsState = SignatureButtonsState
import ifac.td.taxi.ui.screen.components.SignatureButtonState
import ifac.td.taxi.ui.screen.components.SignatureBitmapReady
import ifac.td.taxi.ui.screen.components.SignatureDialogState
import ifac.td.taxi.ui.screen.components.SignatureChanged
import ifac.td.taxi.ui.screen.components.SignatureButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 4-1: import android.graphics.Bitmap
data class SignatureUiState(
    val serviceId: String? = null,
    val hasSigned: Boolean = false,
    val isSubmitting: Boolean = false,
    val showClearDialog: Boolean = false,
    val dialogMessageResId: Int? = null,
    val dialogType: SignatureDialogType? = null,
    val signatureButtonsState: SignatureButtonsState = SignatureButtonsState()
)
sealed interface SignatureUiEvent {
    data class OnSignatureChanged(val hasSigned: Boolean) : SignatureUiEvent
    data object OnClearClicked : SignatureUiEvent
    data object OnAcceptClicked : SignatureUiEvent
    data object OnDialogDismissed : SignatureUiEvent
    data class OnSignatureBitmapReady(val bitmap: Bitmap?) : SignatureUiEvent
    data class OnServiceIdResolved(val serviceId: String?) : SignatureUiEvent
    data object OnDialogConfirmClicked : SignatureUiEvent
}
sealed interface SignatureUiEffect {
    data class ShowToast(val messageResId: Int) : SignatureUiEffect
    data object NavigateBack : SignatureUiEffect
    data class NavigateTo(val direction: androidx.navigation.NavDirections) : SignatureUiEffect
    data class ShowDialog(val dialog: SignatureDialogState) : SignatureUiEffect
}
data class SignatureDialogState(
    val titleResId: Int,
    val messageResId: Int,
    val confirmResId: Int,
    val cancelResId: Int? = null,
    val type: SignatureDialogType = SignatureDialogType.INFO
)
enum class SignatureDialogType { INFO, WARNING, ERROR, SUCCESS }
data class SignatureButtonsState(
    val accept: SignatureButtonState = SignatureButtonState.Accept(),
    val clear: SignatureButtonState = SignatureButtonState.Clear()
)
sealed interface SignatureButtonState {
    val textResId: Int
    val enabled: Boolean
    val isLoading: Boolean
    val visible: Boolean
    val containerColor: Color
    val contentColor: Color
    val disabledContainerColor: Color
    val disabledContentColor: Color
    data class Accept(
        override val textResId: Int = ifac.td.taxi.R.string.accept,
        override val enabled: Boolean = true,
        override val isLoading: Boolean = false,
        override val visible: Boolean = true,
        override val containerColor: Color = Color(0xFF1E88E5),
        override val contentColor: Color = Color.White,
        override val disabledContainerColor: Color = Color(0xFF90A4AE),
        override val disabledContentColor: Color = Color(0xFFFFFFFF),
    ) : SignatureButtonState
    data class Clear(
        override val textResId: Int = ifac.td.taxi.R.string.clear,
        override val enabled: Boolean = true,
        override val isLoading: Boolean = false,
        override val visible: Boolean = true,
        override val containerColor: Color = Color(0xFFE53935),
        override val contentColor: Color = Color.White,
        override val disabledContainerColor: Color = Color(0xFFEF9A9A),
        override val disabledContentColor: Color = Color(0xFFFFFFFF),
    ) : SignatureButtonState
}
