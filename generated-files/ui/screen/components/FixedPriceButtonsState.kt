package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.FixedPriceButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 341-2: import androidx.compose.foundation.BorderStroke
@Immutable
data class FixedPriceButtonsState(
    val closeVisible: Boolean,
    val suggestionsVisible: Boolean,
    val taxi: PriceButtonState,
    val van: PriceButtonState,
    val business: PriceButtonState,
    val isExpanded: Boolean,
) {
    companion object {
        fun fromUiState(
            uiState: ifac.td.taxi.viewmodel.FixedPriceUiState,
        ): FixedPriceButtonsState {
            return FixedPriceButtonsState(
                closeVisible = uiState.showCloseButton,
                suggestionsVisible = uiState.showSuggestions && uiState.isSuggestionsVisible,
                taxi = PriceButtonState.from(
                    text = uiState.priceTaxi,
                    loading = uiState.isTaxiLoading,
                    error = uiState.isTaxiError
                ),
                van = PriceButtonState.from(
                    text = uiState.priceVan,
                    loading = uiState.isVanLoading,
                    error = uiState.isVanError
                ),
                business = PriceButtonState.from(
                    text = uiState.priceBusiness,
                    loading = uiState.isBusinessLoading,
                    error = uiState.isBusinessError
                ),
                isExpanded = uiState.isBottomSheetExpanded
            )
        }
    }
}
@Immutable
data class PriceButtonState(
    val text: String,
    val loading: Boolean,
    val error: Boolean,
    val visible: Boolean = true,
) {
    val showProgress: Boolean get() = loading && !error && text.isBlank()
    val showText: Boolean get() = text.isNotBlank() && !loading
    val showError: Boolean get() = error
    companion object {
        fun from(text: String, loading: Boolean, error: Boolean) = PriceButtonState(
            text = text,
            loading = loading,
            error = error
        )
    }
}
/**
 * Helpers that mirror a custom button component more closely.
 * Replace colors with your XML colors to match exactly.
 */
object FixedPriceButtonStyle {
    val contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    val border: BorderStroke = BorderStroke(1.dp, Color(0xFFB0B0B0))
    val disabledBorder: BorderStroke = BorderStroke(1.dp, Color(0xFFE0E0E0))
    fun primaryColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF1E88E5),
        contentColor = Color.White,
        disabledContainerColor = Color(0xFFE0E0E0),
        disabledContentColor = Color(0xFF9E9E9E)
    )
    fun errorColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFFFDECEA),
        contentColor = Color(0xFFD32F2F),
        disabledContainerColor = Color(0xFFF5F5F5),
        disabledContentColor = Color(0xFFBDBDBD)
    )
    fun neutralColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color.White,
        contentColor = Color(0xFF212121),
        disabledContainerColor = Color(0xFFF5F5F5),
        disabledContentColor = Color(0xFF9E9E9E)
    )
}
