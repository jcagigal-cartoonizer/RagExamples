package ifac.td.taxi.ui.screen.state
import androidx.compose.runtime.Composable
// // ## 2) UI state + button state

// This is the replacement for all button mutation logic from the fragment.


import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

enum class HomeButtonStyle { ENABLE, DISABLE, LOADING, EMPTY }

enum class HomeButtonColor { BLUE, RED, GREEN, ORANGE }

data class HomeButtonUiState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val style: HomeButtonStyle = HomeButtonStyle.ENABLE,
    val backgroundColor: HomeButtonColor = HomeButtonColor.BLUE,
    @StringRes val textRes: Int? = null,
    @StringRes val contentDescriptionRes: Int? = null,
)

data class HomeButtonsState(
    val zoning: HomeButtonUiState = HomeButtonUiState(
        textRes = null,
        backgroundColor = HomeButtonColor.BLUE,
        style = HomeButtonStyle.DISABLE
    ),
    val pending: HomeButtonUiState = HomeButtonUiState(
        backgroundColor = HomeButtonColor.BLUE,
        style = HomeButtonStyle.DISABLE
    ),
    val locateStand: HomeButtonUiState = HomeButtonUiState(
        backgroundColor = HomeButtonColor.RED,
        style = HomeButtonStyle.DISABLE
    ),
    val dashboard: HomeButtonUiState = HomeButtonUiState(
        visible = false,
        backgroundColor = HomeButtonColor.BLUE,
        style = HomeButtonStyle.ENABLE
    ),
    val location: HomeButtonUiState = HomeButtonUiState(
        backgroundColor = HomeButtonColor.RED,
        style = HomeButtonStyle.DISABLE
    ),
    val fixedPrice: HomeButtonUiState = HomeButtonUiState(
        visible = false,
        backgroundColor = HomeButtonColor.BLUE,
        style = HomeButtonStyle.ENABLE
    ),
    val receipts: HomeButtonUiState = HomeButtonUiState(
        backgroundColor = HomeButtonColor.BLUE,
        style = HomeButtonStyle.ENABLE
    ),
    val messages: HomeButtonUiState = HomeButtonUiState(
        backgroundColor = HomeButtonColor.BLUE,
        style = HomeButtonStyle.DISABLE
    ),
    val central: HomeButtonUiState = HomeButtonUiState(
        backgroundColor = HomeButtonColor.BLUE,
        style = HomeButtonStyle.ENABLE
    ),
    val roofLight: HomeButtonUiState = HomeButtonUiState(
        backgroundColor = HomeButtonColor.RED,
        style = HomeButtonStyle.DISABLE
    ),
) {
    val noLocationCentralMode: HomeButtonsState
        get() = copy(
            location = location.copy(style = HomeButtonStyle.EMPTY),
            zoning = zoning.copy(style = HomeButtonStyle.EMPTY),
            pending = pending.copy(style = HomeButtonStyle.EMPTY),
            locateStand = locateStand.copy(style = HomeButtonStyle.EMPTY),
            central = central.copy(style = HomeButtonStyle.EMPTY),
        )
}

// ### Compose styling helpers for the custom button look


// import androidx.compose.runtime.Composable
// import androidx.compose.ui.graphics.Color

@Composable
fun HomeButtonUiState.backgroundColor(): Color = when (backgroundColor) {
    HomeButtonColor.BLUE -> Color(0xFF1976D2)
    HomeButtonColor.RED -> Color(0xFFD32F2F)
    HomeButtonColor.GREEN -> Color(0xFF2E7D32)
    HomeButtonColor.ORANGE -> Color(0xFFF57C00)
}

@Composable
fun HomeButtonUiState.isClickable(): Boolean =
    visible && style != HomeButtonStyle.DISABLE && style != HomeButtonStyle.EMPTY

@Composable
fun HomeButtonUiState.renderText(): Int? = textRes


