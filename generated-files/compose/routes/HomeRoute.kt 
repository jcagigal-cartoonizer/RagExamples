package ifac.td.taxi.compose.routes
import androidx.compose.runtime.Composable
// // ## 1) Routes + effects


sealed interface HomeRoute {
    val route: String

    data object Zoning : HomeRoute { override val route = "home/zoning" }
    data object PendingTrips : HomeRoute { override val route = "home/pendingTrips" }
    data object Message : HomeRoute { override val route = "home/messages" }
    data object ReceiptHistory : HomeRoute { override val route = "home/receipts" }
    data object ContactCentral : HomeRoute { override val route = "home/contactCentral" }
    data object Dashboard : HomeRoute { override val route = "home/dashboard" }
    data object FixedPrice : HomeRoute { override val route = "home/fixedPrice" }
    data object Welcome : HomeRoute { override val route = "welcome" }

    // Optional deep-link style route if needed
    data class ZoningDeepLink(val idMacroZone: Long) : HomeRoute {
        override val route: String = "android-app://ifac.td.taxi/zoningFragment/$idMacroZone"
    }
}


sealed interface HomeUiEffect {
    data class Navigate(val route: HomeRoute) : HomeUiEffect
    data class ShowToast(val messageRes: Int) : HomeUiEffect
    data class PlayBeep(val tone: Int) : HomeUiEffect
    data class KeepScreenOn(val keepOn: Boolean) : HomeUiEffect
    data class OpenDialog(val dialog: HomeDialogSpec) : HomeUiEffect
    data object CloseDialog : HomeUiEffect
}


