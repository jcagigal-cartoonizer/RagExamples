package ifac.td.taxi.ui.screen

import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentMacroZoningBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.MacroZoneAdapter
import ifac.td.taxi.ui.custom.bar.CustomTopBar
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.dialog.bottomSheetDialog.ModalBottomSheetFilter
import ifac.td.taxi.ui.model.MacroZoneModel
import ifac.td.taxi.ui.model.ScrollModeEnum
import ifac.td.taxi.ui.util.ZoneUtils
import ifac.td.taxi.viewmodel.MacroZoningViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.model.FilterOptions
import ifac.td.taxi.viewmodel.model.OrderOptions
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MacroZoningFragment : BaseFragment<FragmentMacroZoningBinding, MacroZoningViewModel>(
    R.layout.fragment_macro_zoning
) {

    private val TAG = "MacroZoningFragment"

    private val vModel: MacroZoningViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private var pendingServicesButton: Boolean? = null
    private var pendingServicesHiredButton: Boolean? = null

    override fun onResume() {
        vModel.initViewModeldata()
        super.onResume()
    }

    private val macroZoneAdapter: MacroZoneAdapter? by lazy {
        MacroZoneAdapter(requireContext()) { id ->
            val macroZone = W2CLocation.getZoning().macrozones.find { it.idMacrozone == id }
            macroZone?.let { mZ ->
                if (mZ.zones.isEmpty()) {
                    iMainActivity.showToast(R.string.macro_zone_has_no_zones)
                } else {
                    sharedViewModel.emitManualZoningNavigation(true)
                    iMainActivity.navigateTo(
                        MacroZoningFragmentDirections.actionMacroZoningFragmentToZoningFragment(id)
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        vBinding.rvZones.adapter = null
        super.onDestroyView()
    }

    override fun getViewModel(): MacroZoningViewModel {
        return vModel
    }

    override fun getViewBinding(): FragmentMacroZoningBinding {
        return FragmentMacroZoningBinding.inflate(layoutInflater)
    }

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        vBinding.pbMacroZones?.visibility = View.VISIBLE
        vBinding.btnPreReservation.setButtonStyle(CustomButton.StyleButton.DISABLE)
        setButtons()
        setUpAdapter()
        setupListViewListener()
        vModel.checkPendingServiceOnForHirePermission()
        vModel.checkPendingServiceOnHiredPermission()
        vModel.checkHasZonesInFavourites()
        setupClickListeners()
    }

    private fun setUpAdapter() {
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())

        Logs.d(TAG, "settear adapter inicial")
        vBinding.rvZones.apply {
            adapter = macroZoneAdapter
            layoutManager = mLayoutManager
        }
    }

    private fun setupListViewListener() {
        vBinding.rvZones.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    vModel.setScrollMode(ScrollModeEnum.NO_SCROLL)
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //si es visible la primera pos
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisiblePosition: Int = layoutManager.findFirstVisibleItemPosition()
                if (firstVisiblePosition == 0) {
                    vModel.setScrollMode(ScrollModeEnum.JUMP_TO_TOP)
                }
            }
            }
        })
    }

    private fun setButtons() {
        vBinding.apply{
            btnPending.setAction {
                vModel.canOpenPendingTripsFragment(sharedViewModel._pendingTripsListFlow)
            }
        }
    }
/*
    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            sharedViewModel.shiftStatusFlow.value?.currentStatus?.let { currentStatus ->
                if (vModel.isHired(currentStatus)) {
                    sharedViewModel.tripFlow.value?.id?.let { id ->
                        val directions =
                            MacroZoningFragmentDirections.actionMacroZoningFragmentToOnTripFragment(
                                id
                            )
                        iMainActivity.navigateTo(directions)
                    }
                } else {
                    iMainActivity.navigateTo(R.id.homeFragment)
                }
            }
        }
    }

 */

    override fun updateTopBarIconRight() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.FILTER, true) {
            val currentFilter = vModel.listFilterFlow.value


            val modalFilterDialog = ModalBottomSheetFilter.newInstance(currentFilter) { selectedOption ->
                if (selectedOption == FilterOptions.FAVOURITES || selectedOption == FilterOptions.NEARNESS || selectedOption == FilterOptions.ID_NO_HIERARCHY || selectedOption == FilterOptions.HOT_ZONES || selectedOption == FilterOptions.MANUAL) {
                    if (selectedOption == FilterOptions.FAVOURITES && !vModel.hasZonesInFavourites()) {
                        iMainActivity.openDialog(
                            CustomDialog.CustomDialogModel(
                                title = resources.getString(R.string.no_favourite_zones),
                                description = resources.getString(R.string.how_to_favorite),
                                buttons = arrayListOf(ButtonType.ACCEPT)
                            ), {}
                        )
                    } else {
                        vModel.filterListBy(selectedOption)

                        val macroZone = if (W2CLocation.getLastIdMacrozone() == 0) {
                            1
                        } else {
                            W2CLocation.getLastIdMacrozone()
                        }

                        sharedViewModel.emitManualZoningNavigation(true)
                        iMainActivity.navigateTo(
                            MacroZoningFragmentDirections.actionMacroZoningFragmentToZoningFragment(macroZone)
                        )
                    }
                } else {
                    refreshHeaderImages()
                    //vModel.sortListBy(selectedOption, false)
                    vModel.filterListBy(selectedOption)
                }
            }


//
//            val modalDialog = ModalBottomSheet.newInstance(currentFilter) { selectedOption ->
//                if (selectedOption == OrderOptions.FAVOURITES || selectedOption == OrderOptions.NEARNESS || selectedOption == OrderOptions.ID_NO_HIERARCHY) {
//                    if (selectedOption == OrderOptions.FAVOURITES && !vModel.hasZonesInFavourites()) {
//                        Toast.makeText(context, requireContext().getString(R.string.no_favourite_zones), Toast.LENGTH_SHORT).show()
//                    } else {
//                        vModel.updateZoningSortConfigurationToFavouriteOrNearness(selectedOption)
//
//                        val macroZone = if (W2CLocation.getLastIdMacrozone() == 0) {
//                            1
//                        } else {
//                            W2CLocation.getLastIdMacrozone()
//                        }
//
//                        iMainActivity.navigateTo(
//                            MacroZoningFragmentDirections.actionMacroZoningFragmentToZoningFragment(macroZone)
//                        )
//                    }
//                } else {
//                    refreshHeaderImages()
//                    vModel.sortListBy(selectedOption, false)
//                }
//            }

            modalFilterDialog.show(parentFragmentManager, ModalBottomSheetFilter.TAG)
        }
    }


    private fun setupClickListeners() {
        vBinding.apply {
            tvOnStop.setOnClickListener {
                onHeaderClick(OrderOptions.IN_STAND)
                Logs.d("MacroZoningFragment", "tvOnStop onClick.")
            }

            tvOnZone.setOnClickListener {
                onHeaderClick(OrderOptions.IN_ZONE)
                Logs.d("MacroZoningFragment", "tvOnZone onClick")
            }

            tvHired.setOnClickListener {
                onHeaderClick(OrderOptions.HIRED)
                Logs.d("MacroZoningFragment", "tvHired onClick")
            }

            tvTrips.setOnClickListener {
                onHeaderClick(OrderOptions.TRIPS)
                Logs.d("MacroZoningFragment", "tvTrips onClick")
            }

            tvPlaceholder.setOnClickListener {
                onHeaderClick(OrderOptions.NAME)
                Logs.d("MacroZoningFragment", "tvPlaceholder onClick")
            }
        }
    }

    private fun onHeaderClick(newOrder: OrderOptions) {
        vModel.listOrderFlow.value?.let { lastOrder ->
            if (newOrder == lastOrder) {
                vModel.sortListBy(OrderOptions.NONE)
            } else {
                vModel.sortListBy(newOrder)
            }
        }
    }

    private fun refreshHeaderImages() {
        vBinding.apply {
            ivSortHired.visibility = View.INVISIBLE
            ivSortTrips.visibility = View.INVISIBLE
            ivSortOnStop.visibility = View.INVISIBLE
            ivSortOnZone.visibility = View.INVISIBLE
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.macroZonesListFlow.collect {
                        it?.let { macroZones ->
                            vModel.sortNewData(macroZones)
                        }
                    }
                }

                launch {
                    vModel.macroZonesListSortedFlow.collect {
                        it?.let { macroZones ->
                            Logs.d(TAG, "updateMacroZonesData")
                            if (vModel.hasSetDataTypes.value) {
                                updateMacroZonesData(macroZones)
                            } else {
                                Logs.d(TAG, "ignoring updateMacroZonesData - DataTypes not set")
                                vModel.setHasSetDataTypes()
                                Logs.d(TAG, "for not locking showing data, refresh variable")
                            }
                        }
                    }
                }

                launch {
                    vModel.pendingServicesButtonFlow.collect {
                        Logs.d(TAG, "taxisButtonFlow.value: $it")
                        pendingServicesButton = it
                        checkPendingButtonEnabled()
                    }
                }

                launch {
                    vModel.pendingServicesHiredButtonFlow.collect {
                        pendingServicesHiredButton = it
                        checkPendingButtonEnabled()
                    }
                }

                launch {
                    vModel.showPreReservationTripsButtonFlow.collect {
                        it?.let { showButton ->
                            if (showButton) {
                                vBinding.btnPreReservation.setButtonStyle(CustomButton.StyleButton.ENABLE)
                                vBinding.btnPreReservation.setAction {
                                    iMainActivity.navigateTo(R.id.action_macroZoningFragment_to_preReservationTripsFragment)
                                }
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.orangeBtnPendingFlow.collect { btnPendingShouldBeOrange ->
                        if (btnPendingShouldBeOrange == true) {
                            vBinding.btnPending.changeBackground(CustomButton.BackgroundButtonColor.ORANGE)
                        } else {
                            vBinding.btnPending.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
                        }
                    }
                }

                launch {
                    vModel.pendingServicesButtonPressedCallback.collect {
                        if (it) {
                            iMainActivity.navigateTo(R.id.action_macroZoningFragment_to_pendingTripsFragment)
                        } else {
                            if (sharedViewModel.pendingTripsListFlow.value?.isEmpty() == true) {
                                    //iMainActivity.showToast(getString(R.string.no_pending_services))
                                iMainActivity.showToast(R.string.toast_no_hay_pendientes)
                            } else {
                                iMainActivity.navigateTo(R.id.action_macroZoningFragment_to_pendingTripsFragment)
                            }
                        }
                    }
                }

                launch {
                    vModel.macroZoningListDataTypes.collect {
                        it?.let { dataTypes ->
                            if (dataTypes.isNotBlank()) {
                                updateHeader(dataTypes)
                                Logs.d(TAG, "setDataType")
                                macroZoneAdapter?.setDataType(dataTypes)
                                vModel.setHasSetDataTypes()

                                Logs.d(TAG, "resetting MacroZonesData to ensure data is shown")
                                vModel.macroZonesListSortedFlow.value?.let { data ->
                                    if (data.isNotEmpty()) {
                                        updateMacroZonesData(data)
                                    }
                                }
                            }
                        }
                    }
                }
                launch {
                    sharedViewModel.shortBreakStatus.collect {
                        checkPendingButtonEnabled()
                    }
                }
                launch {
                    vModel.listOrderFlow.collect {
                        refreshHeaderImages()
                        vBinding.apply {
                            when(it) {
                                OrderOptions.IN_STAND -> {
                                    ivSortOnStop.setImageResource(R.drawable.round_arrow_drop_up_24)
                                    ivSortOnStop.visibility = View.VISIBLE
                                    val stand = getString(R.string.stand)
                                    tvPlaceholder.text = getString(R.string.sort_by_placeholder, stand)
                                }
                                OrderOptions.IN_ZONE -> {
                                    ivSortOnZone.setImageResource(R.drawable.round_arrow_drop_up_24)
                                    ivSortOnZone.visibility = View.VISIBLE
                                    val zone = getString(R.string.zone_tts)
                                    tvPlaceholder.text = getString(R.string.sort_by_placeholder, zone)
                                }
                                OrderOptions.HIRED -> {
                                    ivSortHired.setImageResource(R.drawable.round_arrow_drop_up_24)
                                    ivSortHired.visibility = View.VISIBLE

                                    val values = vModel.macroZoningListDataTypes.value
                                    val hired = getString(R.string.hired)
                                    val trips = getString(R.string.trips)
                                    if (values != null) {
                                        val columns = ZoneUtils.parseStringDataTypes(values)
                                        if (!columns.contains(AvailableColumnsEnum.HIRED_VEHICLES)) {
                                            tvPlaceholder.text = getString(R.string.sort_by_placeholder, trips)
                                        } else {
                                            tvPlaceholder.text = getString(R.string.sort_by_placeholder, hired)
                                        }
                                    } else {
                                        tvPlaceholder.text = getString(R.string.sort_by_placeholder, hired)
                                    }
                                }
                                OrderOptions.TRIPS -> {
                                    val trips = getString(R.string.trips)
                                    ivSortTrips.setImageResource(R.drawable.round_arrow_drop_up_24)
                                    ivSortTrips.visibility = View.VISIBLE
                                    tvPlaceholder.text = getString(R.string.sort_by_placeholder, trips)
                                }

                                OrderOptions.NAME -> {
                                    tvPlaceholder.text = getString(R.string.sort_by_name)
                                }
                                else -> {
                                    tvPlaceholder.text = getString(R.string.sort_by_placeholder, "Id")
                                }
                            }
                        }

                    }
                }
                launch {
                    sharedViewModel.zoningChangedFlow.eventCollector {
                        it?.first?.let { macroZoneId ->
                            vModel.selectedZone?.let {
                                Log.d(TAG, "setupObservers: macroZoneId $macroZoneId - Selected ${vModel.selectedZone?.macroZone?.idMacrozone}")
                                vModel.setScrollMode(ScrollModeEnum.FOLLOW_SELECTED)
                                vModel.sortListBy(vModel.listOrderFlow.value)
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.zoneFlow.collect {
                        if (it.isNullOrBlank()) {
                            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        } else {
                            checkPendingButtonEnabled()
                        }
                    }
                }

                launch {
                    var refreshAnimator: android.animation.ObjectAnimator? = null
                    var emissionCount = 0
                    vModel.refreshProgressFlow.collect { progress ->
                        vBinding.pbRefreshRate.let { progressBar ->
                            refreshAnimator?.cancel()
                            if (emissionCount < 2 || progress == 1000) {
                                emissionCount++
                                progressBar.progress = progress
                            } else {
                                refreshAnimator = android.animation.ObjectAnimator.ofInt(
                                    progressBar, "progress", progressBar.progress, progress
                                ).apply {
                                    duration = 1000L
                                    interpolator = android.view.animation.LinearInterpolator()
                                    start()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkPendingButtonEnabled() {
        if (sharedViewModel.zoneFlow.value.isNullOrEmpty()) {
            Logs.d(TAG, "sharedViewModel.zoneFlow: NullOrEmpty")
            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
            return
        }

        val isInShortBreak = sharedViewModel.shortBreakStatus.value == ShortBreakStatus.IN_SHORT_BREAK ||
                sharedViewModel.shortBreakStatus.value == ShortBreakStatus.IN_SHORT_BREAK_FORCED

        // Si está en pausa corta, desactivar el botón
        if (isInShortBreak) {
            Logs.d(TAG, "checkPendingButtonEnabled: isInShortBreak")
            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
            return
        }

        // Si pendingServicesButton es false o no está permitido por ubicación, desactivar el botón
        if (!W2CLocation.isLocationAllowedByCentral()) {
            Logs.d(TAG, "isLocationAllowedByCentral: false")
            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
            return
        }

        // Si está en soonInZone activar el botón
        if (W2CLocation.getIsInSoonInZone()) {
            Logs.d(TAG, "isInSoonInZone: true")
            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.ENABLE)
            return
        }

        // Si hay un estado definido para pendingServicesHiredButton
        if (pendingServicesHiredButton != null) {
            Logs.d(TAG, "pendingServicesHiredButton: $pendingServicesHiredButton")
            if (!pendingServicesHiredButton!!) {
                Logs.d(TAG, "pendingServicesHiredButton: false")
                vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
            } else if (sharedViewModel.hiredZone.value != null) {
                Logs.d(TAG, "hiredZone != null")
                vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.ENABLE)
            }
            return
        }

        // Si pendingServicesButton es true, activar el botón
        if (pendingServicesButton == true) {
            Logs.d(TAG, "pendingServicesButton: true")
            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.ENABLE)
            return
        }

        Logs.d(TAG, "btnPending disable by default")
        // Caso por defecto
        vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
    }

    private fun updateHeader(dataTypes: String) {
        val columns = ZoneUtils.parseStringDataTypes(dataTypes)
        vBinding.apply {
            if (columns.contains(AvailableColumnsEnum.STAND_VEHICLES)) {
                vBinding.tvOnStop.visibility = View.VISIBLE
            } else {
                vBinding.tvOnStop.visibility = View.GONE
            }

            if (columns.contains(AvailableColumnsEnum.ZONE_VEHICLES)) {
                vBinding.tvOnZone.visibility = View.VISIBLE
            } else {
                vBinding.tvOnZone.visibility = View.GONE
            }

            if (columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) || columns.contains(AvailableColumnsEnum.BOOKED_TRIPS)){
                vBinding.tvHired.visibility = View.VISIBLE
                if (!columns.contains(AvailableColumnsEnum.HIRED_VEHICLES)) {
                    vBinding.tvHired.setText(R.string.abrevUbServiciosWithoutDots)
                }
            } else {
                vBinding.tvHired.visibility = View.GONE
            }

            if (columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS) || columns.contains(AvailableColumnsEnum.TOTAL_TRIPS) || (columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) && columns.contains(AvailableColumnsEnum.BOOKED_TRIPS))) {
                vBinding.tvTrips.visibility = View.VISIBLE
                if (columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS)) {
                    vBinding.tvTrips.setText(R.string.abbrevPreBookedTrips)
                } else if (columns.contains(AvailableColumnsEnum.TOTAL_TRIPS)) {
                    vBinding.tvTrips.setText(R.string.abbrevTotalTrips)
                }
            } else {
                vBinding.tvTrips.visibility = View.GONE
            }
        }
    }

    private fun updateMacroZonesData(macroZones: List<MacroZoneModel>) {
        try {
            if (vBinding.rvZones.adapter == null) {
                Logs.d(TAG, "initMacroZoneAdapter: initAdapter")
//                macroZoneAdapter = MacroZoneAdapter(requireContext()) { id ->
//                    val macroZone = W2CLocation.getZoning().macrozones.find { it.idMacrozone == id }
//                    macroZone?.let { mZ ->
//                        if (mZ.zones.isEmpty()) {
//                            iMainActivity.showToast(getString(R.string.macro_zone_has_no_zones))
//                        } else {
//                            iMainActivity.navigateTo(
//                                MacroZoningFragmentDirections.actionMacroZoningFragmentToZoningFragment(
//                                    id
//                                )
//                            )
//                        }
//                    }
//                }
                vBinding.rvZones.apply {
                    adapter = macroZoneAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                }
            }
            vBinding.pbMacroZones?.visibility = View.GONE
            vBinding.clRowZone.visibility = View.VISIBLE
            Logs.d(TAG, "clRow visible y pbZones gone")
            submitListMacroZones(macroZones)
        } catch (e: Exception) {
            Logs.d(TAG, "initMacroZoneAdapter: $e")
        }
    }

    private fun submitListMacroZones(macroZones: List<MacroZoneModel>) {
        Logs.d(TAG, "submitList:  macroZones.size-> " + macroZones.size)
        macroZoneAdapter?.submitList(macroZones) {
            Logs.d(TAG, "submitList finished")
            view ?: return@submitList
            vBinding.rvZones.apply {
                vModel.scrollPositionFlow.value.let { scrollMode ->
                    when(scrollMode) {
                        ScrollModeEnum.NO_SCROLL -> {}
                        ScrollModeEnum.JUMP_TO_TOP -> {
                            post {
                                view?.let {
                                    scrollToPosition(0)
                                }
                            }
                        }
                        ScrollModeEnum.FOLLOW_SELECTED -> {
                            post {
                                view?.let {
                                    val position =
                                        macroZones.indexOf(macroZones.firstOrNull {
                                            it.macroZone.idMacrozone == vModel.selectedZone?.macroZone?.idMacrozone
                                                    && it.macroZone.nombreMacrozone == vModel.selectedZone?.macroZone?.nombreMacrozone
                                        })
                                    if (position > -1) {
                                        Logs.d(TAG, "submitList scroll to position")
                                        scrollToPosition(position)
                                    }
                                }
                            }
                        }
                    }
                }
                post {
                    view?.let {
                        scrollToPosition(0)
                    }
                }
            }
        }
    }

    override fun onPause() {
        vModel.removeHandlerCallback()
        super.onPause()
    }
}