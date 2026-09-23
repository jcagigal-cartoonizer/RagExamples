package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ZoningCarsUiState
import ifac.td.taxi.ui.screen.components.ZoningCarsButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 424-4: import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
data class ZoningCarsUiState(
    val idMacroZone: Int? = null,
    val idZone: Int? = null,
    val zoneName: String = "",
    val isLoadingCars: Boolean = false,
    val showProgress: Boolean = true,
    val refreshProgress: Int = 1000,
    val carRows: List<ZoneCarRowModel> = emptyList(),
)
sealed interface ZoningCarsUiEvent {
    data class Init(val idMacroZone: Int, val idZone: Int) : ZoningCarsUiEvent
    data object BackClicked : ZoningCarsUiEvent
    data object CloseClicked : ZoningCarsUiEvent
    data object PendingClicked : ZoningCarsUiEvent
    data object ShowLocateOnHiredDialog : ZoningCarsUiEvent
    data object ShowDelocateOnHiredDialog : ZoningCarsUiEvent
    data object DismissDialog : ZoningCarsUiEvent
    data class DialogButtonClicked(val buttonId: String) : ZoningCarsUiEvent
}
sealed interface ZoningCarsEffect {
    data object NavigateBack : ZoningCarsEffect
    data object NavigateToHome : ZoningCarsEffect
    data object NavigateToOnTrip : ZoningCarsEffect
    data object NavigateToPendingTrips : ZoningCarsEffect
    data object NavigateToPointsOfInterest : ZoningCarsEffect
    data class ShowToast(val messageRes: Int) : ZoningCarsEffect
}
enum class ZoningCarsDialogKind { LocateOnHired, DelocateOnHired }
data class ZoningCarsDialogState(
    val kind: ZoningCarsDialogKind,
    val title: String,
    val message: String,
    val buttons: List<String>,
) {
    companion object {
        fun locate(zone: Zone) = ZoningCarsDialogState(
            kind = ZoningCarsDialogKind.LocateOnHired,
            title = zone.nombreZone,
            message = "¿Ubicar en zona ocupada?",
            buttons = listOf(ButtonType.POI.name, ButtonType.ACCEPT.name)
                .takeIf { zone.allowedUbOcupado == true } ?: emptyList()
        )
        fun delocate(title: String) = ZoningCarsDialogState(
            kind = ZoningCarsDialogKind.DelocateOnHired,
            title = title,
            message = "¿Desea desubicar la zona?",
            buttons = listOf(ButtonType.CANCEL.name, ButtonType.ACCEPT.name)
        )
    }
}
data class ZoningCarsButtonsState(
    val showBackButton: Boolean = true,
    val showCloseButton: Boolean = true,
    val showPendingButton: Boolean = true,
    val backButtonStyle: CustomButtonStyle = CustomButtonStyle.ENABLE,
    val closeButtonStyle: CustomButtonStyle = CustomButtonStyle.ENABLE,
    val pendingButtonStyle: CustomButtonStyle = CustomButtonStyle.DISABLE,
    val backButtonColor: CustomButtonBackgroundColor = CustomButtonBackgroundColor.BLUE,
    val closeButtonColor: CustomButtonBackgroundColor = CustomButtonBackgroundColor.BLUE,
    val pendingButtonColor: CustomButtonBackgroundColor = CustomButtonBackgroundColor.BLUE,
    val pendingServicesButtonAllowed: Boolean? = null,
    val pendingServicesHiredButtonAllowed: Boolean? = null,
)
enum class CustomButtonStyle {
    ENABLE, DISABLE
}
enum class CustomButtonBackgroundColor {
    BLUE, ORANGE
}
