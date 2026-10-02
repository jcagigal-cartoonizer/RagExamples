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
