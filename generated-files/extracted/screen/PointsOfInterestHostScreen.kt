@Composable
fun PointsOfInterestHostScreen(
    viewModel: PointsOfInterestComposeViewModel,
    navigateBack: () -> Unit,
    launchIntent: (android.content.Intent?) -> Unit,
    saveHiredZone: (com.interfacom.sdk.taximeter.licensing.models.zoning.Zone?) -> Unit,
    showHeader: (Boolean) -> Unit,
) {
    PointsOfInterestRoute(
        viewModel = viewModel,
        onNavigateBack = navigateBack,
        onLaunchIntent = launchIntent,
        onSaveHiredZone = saveHiredZone,
        showHeader = showHeader
    )
}
sealed class PointsOfInterestDialogState {
    data class PoiDetails(val poi: Poi) : PointsOfInterestDialogState()
    data class ConfirmLocate(val poi: Poi, val navigateAfter: Boolean) : PointsOfInterestDialogState()
}
To preserve the original fragment behavior:
Those remain host responsibilities and should be passed into the composable.
1. a fully working `@Composable` screen,
2. a fully working `ViewModel`,
3. a navigation integration snippet using `NavController`,
4. and a refined dialog state machine that exactly handles `UBICAR_DESTINO` vs `UBICAR_DESTINO_NAVEGAR`.
