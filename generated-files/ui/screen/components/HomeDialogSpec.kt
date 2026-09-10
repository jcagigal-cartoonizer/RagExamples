package ifac.td.taxi.ui.screen.components
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
// // ## 3) Dialog specs


import androidx.annotation.StringRes

sealed interface HomeDialogSpec {
    data object LocationConfirmDeactivate : HomeDialogSpec
    data object LocationConfirmActivate : HomeDialogSpec
    data object RoofLightConfirm : HomeDialogSpec
    data object ManualTripConfirm : HomeDialogSpec
    data object PendingTripsInfo : HomeDialogSpec

    data class Simple(
        @StringRes val title: Int,
        @StringRes val message: Int? = null
    ) : HomeDialogSpec
}


