package ifac.td.taxi.ui.screen

import android.view.View
import android.widget.GridLayout
import androidx.core.view.doOnLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
import com.interfacom.sdk.taximeter.licensing.models.zoning.Macrozone
import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
import ifac.td.taxi.HomeDirections
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentZoningBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.ZoneAdapter
import ifac.td.taxi.ui.custom.bar.CustomTopBar
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.dialog.bottomSheetDialog.ModalBottomSheetFilter
import ifac.td.taxi.ui.model.ScrollModeEnum
import ifac.td.taxi.ui.model.ZoneModel
import ifac.td.taxi.ui.util.ConfigurationUtils
import ifac.td.taxi.ui.util.ZoneUtils
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.ZoningViewModel
import ifac.td.taxi.viewmodel.model.FilterOptions
import ifac.td.taxi.viewmodel.model.LocationInHiredEnum
import ifac.td.taxi.viewmodel.model.OrderOptions
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class ZoningFragment : BaseFragment<FragmentZoningBinding, ZoningViewModel>(
    R.layout.fragment_zoning
) {

    private val TAG = "ZoningFragment"

    private val sharedViewModel: MainActivityViewModel by activityViewModel()
    private val vModel: ZoningViewModel by viewModel()
    private val safeArgs: ZoningFragmentArgs by navArgs()

    private var pendingServicesButton: Boolean? = null
    private var pendingServicesHiredButton: Boolean? = null

    private var refreshMenuPending = false

    private var lastShowLocatedOnHired: Boolean? = null
    private var lastShowSoonInZone: Boolean? = null
    private var lastOrientation: Int? = null
    private var lastIsTablet: Boolean? = null

    private val zoneAdapter: ZoneAdapter? by lazy {
        Logs.d(TAG, "iniciar adapter")
        val click: (ZoneModel) -> Unit = { itemClick ->
            vModel.changeSelectedItem(itemClick, sharedViewModel.hiredZone.value)
            fromClick = true
        }

        val longClick: (Pair<Int, Int>) -> Unit = {
            vModel.isZoneInFavourites(it.first, it.second)
        }

        ZoneAdapter(requireContext(), click, longClick)
    }

    private fun openLocateOnHiredDialog() {
        val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
            { response ->
                val hiredZone = sharedViewModel.hiredZone.value
                when (response.buttonPressed) {
                    ButtonType.WITH_ZONE -> {
                        if (hiredZone == null) {
                            vModel.locateOnHired()
                        } else {
                            vModel.swapOnHiredZone(hiredZone, true)
                        }
                    }
                    ButtonType.WITHOUT_ZONE -> {
                        if (hiredZone == null) {
                            locateOnHiredWithoutZone()
                        } else {
                            vModel.swapOnHiredZone(hiredZone, false)
                        }
                    }
                    ButtonType.POI -> iMainActivity.navigateTo(ZoningFragmentDirections.actionZoningFragmentToPointsOfInterestFragment())
                    else -> Logs.d(TAG, "openLocateOnHiredDialog: callback dismiss")
                }
            }

        if (vModel.bravoConfiguration == null) {
            vModel.getPOIsValue()
        }

        val buttons = mutableListOf<ButtonType>()
        val locationInHired = vModel.locationOnHiredParameter.value

        if (vModel.selectedZone?.zone?.allowedUbOcupado == true && (locationInHired == LocationInHiredEnum.BOTH_ZONE_OPTIONS.value || locationInHired == LocationInHiredEnum.WITH_ZONE_OPTION.value)) {
            buttons.add(ButtonType.WITH_ZONE)
        }

        if (locationInHired == LocationInHiredEnum.WITHOUT_ZONE_OPTION.value || locationInHired == LocationInHiredEnum.BOTH_ZONE_OPTIONS.value) {
            buttons.add(ButtonType.WITHOUT_ZONE)
        }

        if (vModel.bravoConfiguration?.isPoisEnabled == true) {
            buttons.add(ButtonType.POI)
        }

        vModel.selectedZone?.zone?.let {
            if (it.allowedUbOcupado) {
                iMainActivity.openDialog(
                    CustomDialog.CustomDialogModel(
                        title = it.nombreZone,
                        description = getString(R.string.ask_locate),
                        buttons = ArrayList(buttons),
                    ),
                    callBack,
                    fragmentManager = childFragmentManager
                )
            } else {
                iMainActivity.showToast(R.string.locate_hired_zone_not_allowed)
            }
        }
    }

    override fun onDestroyView() {
        vBinding.rvZones.adapter = null
        Logs.d(TAG, "adapter = null")
        super.onDestroyView()
    }

    private fun locateOnHiredWithoutZone() {
        sharedViewModel.saveHiredZone(Zone())
        vModel.sendPositionStatic()
        sharedViewModel.tripFlow.value?.id?.let { id ->
            // Navigate first to HomeGraph and later to OnTripFragment
            iMainActivity.navigateTo(ZoningFragmentDirections.actionZoningFragmentToHomeFragment())
            iMainActivity.navigateTo(HomeDirections.goToOnTripFragment())
        }
    }

    private fun openDelocateOnHiredDialog() {

        val callBackLocateWithZoneOption: (CustomDialog.CustomDialogResponse) -> Unit =
            { response ->
                when (response.buttonPressed) {
                    ButtonType.ACCEPT -> vModel.delocateOnHired()
                    else -> Logs.d(TAG, "openDelocateOnHiredDialog: callback dismiss")
                }
            }

        val callBackLocateWithoutZoneOption: (CustomDialog.CustomDialogResponse) -> Unit =
            { response ->
                when (response.buttonPressed) {
                    ButtonType.ACCEPT -> vModel.delocateOnHiredWithoutZone()
                    else -> Logs.d(TAG, "openDelocateOnHiredDialog: callback dismiss")
                }
            }
        val callBack = if (sharedViewModel.hiredZone.value?.allowedUbOcupado == true) {
            callBackLocateWithZoneOption
        } else {
            callBackLocateWithoutZoneOption
        }
        sharedViewModel.hiredZone.value?.let {
            iMainActivity.openDialog(
                CustomDialog.CustomDialogModel(
                    title = it.nombreZone,
                    description = getString(R.string.ask_delocate),
                    buttons = arrayListOf(
                        ButtonType.CANCEL,
                        ButtonType.ACCEPT,
                        ),
                    ),
                    callBack
                )
        }
    }


    override fun getViewModel(): ZoningViewModel {
        return vModel
    }

    override fun getViewBinding(): FragmentZoningBinding {
        return FragmentZoningBinding.inflate(layoutInflater)
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {

            if (vModel.listFilterFlow.value == FilterOptions.MANUAL ||
                vModel.listFilterFlow.value == FilterOptions.ID_NO_HIERARCHY ||
                vModel.listFilterFlow.value == FilterOptions.NEARNESS ||
                vModel.listFilterFlow.value == FilterOptions.HOT_ZONES ||
                vModel.listFilterFlow.value == FilterOptions.FAVOURITES
                ) {
                when(sharedViewModel.shiftStatusFlow.value?.currentStatus) {
                    ifConstants.STATE_HIRED,
                    ifConstants.STATE_DISPATCHED,
                    ifConstants.STATE_HIRED_DISPATCHED,
                    ifConstants.STATE_HIRED_NO_CENTRAL -> {
                        sharedViewModel.tripFlow.value?.id?.let { id ->
                            // Navigate first to HomeGraph and later to OnTripFragment
                            iMainActivity.navigateTo(ZoningFragmentDirections.actionZoningFragmentToHomeFragment())
                            iMainActivity.navigateTo(HomeDirections.goToOnTripFragment())
                        }
                    }
                    else -> {
                        iMainActivity.navigateTo(ZoningFragmentDirections.actionZoningFragmentToHomeFragment())
                    }
                }
            } else {
                iMainActivity.navigateTo(R.id.action_zoningFragment_to_macroZoningFragment)
            }
        }
    }

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        Logs.d(TAG, "safeArgs.idMacroZone = ${safeArgs.idMacroZone}")
        vBinding.pbZones?.visibility = View.VISIBLE
        setupClickListeners()
        setupListViewListener()
        setUpAdapter()
        vModel.getPOIsValue()
        vModel.checkPendingServiceOnForHirePermission()
        vModel.checkPendingServiceOnHiredPermission()
        vModel.checkServiceButton()
        vModel.checkTaxisButton()
    }

    private fun setUpAdapter() {
        val mLayoutManager: RecyclerView.LayoutManager = if (ConfigurationUtils.isTablet(resources) && ConfigurationUtils.isLandscape(resources)) {
            GridLayoutManager(context, 2)
        } else {
            LinearLayoutManager(requireContext())
        }

        Logs.d(TAG, "settear adapter inicial")
        vBinding.rvZones.apply {
            adapter = zoneAdapter
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

    private var firstChange = true

    private fun refreshMenuButtons() {
        Logs.d(TAG, "refreshMenuButtons called")
        val showLocatedOnHired = vBinding.btnLocateOnHired.visibility == View.VISIBLE
        val showSoonInZone = vBinding.btnSoonInZone.visibility == View.VISIBLE

        if (context == null) {
            return
        }

        val orientation = resources.configuration.orientation
        val isTablet = ConfigurationUtils.isTablet(resources)

        if (!firstChange && lastShowLocatedOnHired == showLocatedOnHired && lastShowSoonInZone == showSoonInZone && lastOrientation == orientation && lastIsTablet == isTablet) {
            return
        }

        lastShowLocatedOnHired = showLocatedOnHired
        lastShowSoonInZone = showSoonInZone
        lastOrientation = orientation
        lastIsTablet = isTablet
        firstChange = false

        if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            if (showLocatedOnHired || showSoonInZone) {
                if (showLocatedOnHired) {
                    //LocateOnHired
                    val paramsLocateOnHired =
                        vBinding.btnLocateOnHired.layoutParams as GridLayout.LayoutParams
                    paramsLocateOnHired.rowSpec = GridLayout.spec(0, 2, 1f)  // Ocupa las dos filas
                    paramsLocateOnHired.columnSpec = GridLayout.spec(0, 1, 1f)  // Primera columna
                    vBinding.btnLocateOnHired.layoutParams = paramsLocateOnHired
                } else {
                    //SoonInZone
                    val paramsSoonInZone =
                        vBinding.btnSoonInZone.layoutParams as GridLayout.LayoutParams
                    paramsSoonInZone.rowSpec = GridLayout.spec(0, 2, 1f)  // Ocupa las dos filas
                    paramsSoonInZone.columnSpec = GridLayout.spec(0, 1, 1f)  // Primera columna
                    vBinding.btnSoonInZone.layoutParams = paramsSoonInZone
                }

                //Pendings
                val paramsPending = vBinding.btnPending.layoutParams as GridLayout.LayoutParams
                paramsPending.rowSpec = GridLayout.spec(0, 2, 1f)  // Ocupa las dos filas
                paramsPending.columnSpec = GridLayout.spec(1, 1, 1f)  // Segunda columna
                vBinding.btnPending.layoutParams = paramsPending

                //Trips
                val paramsTrips = vBinding.btnTrips.layoutParams as GridLayout.LayoutParams
                paramsTrips.rowSpec = GridLayout.spec(0, 1, 0.5f)  // Primera fila, mitad de alto
                paramsTrips.columnSpec = GridLayout.spec(2, 1, 1f)  // Tercera columna
                vBinding.btnTrips.layoutParams = paramsTrips

                //Cars
                val paramsCars = vBinding.btnCars.layoutParams as GridLayout.LayoutParams
                paramsCars.rowSpec = GridLayout.spec(1, 1, 0.5f)  // Segunda fila, mitad de alto
                paramsCars.columnSpec = GridLayout.spec(2, 1, 1f)  // Tercera columna
                vBinding.btnCars.layoutParams = paramsCars
            }
        } else if (ConfigurationUtils.isTablet(resources)) {
            if (showLocatedOnHired || showSoonInZone) {
                if (showLocatedOnHired) {
                    //LocateOnHired
                    val paramsLocateOnHired =
                        vBinding.btnLocateOnHired.layoutParams as GridLayout.LayoutParams
                    paramsLocateOnHired.rowSpec = GridLayout.spec(2, 1, 1f)  // Ocupa las dos filas
                    paramsLocateOnHired.columnSpec = GridLayout.spec(0, 2, 1f)  // Primera columna
                    vBinding.btnLocateOnHired.layoutParams = paramsLocateOnHired
                } else {
                    //SoonInZone
                    val paramsSoonInZone =
                        vBinding.btnSoonInZone.layoutParams as GridLayout.LayoutParams
                    paramsSoonInZone.rowSpec = GridLayout.spec(2, 1, 1f)  // Ocupa las dos filas
                    paramsSoonInZone.columnSpec = GridLayout.spec(0, 2, 1f)  // Primera columna
                    vBinding.btnSoonInZone.layoutParams = paramsSoonInZone
                }

                //Pendings
                val paramsPending = vBinding.btnPending.layoutParams as GridLayout.LayoutParams
                paramsPending.rowSpec = GridLayout.spec(1, 1, 1f)  // Ocupa las dos filas
                paramsPending.columnSpec = GridLayout.spec(0, 2, 1f)  // Segunda columna
                vBinding.btnPending.layoutParams = paramsPending

                //Trips
                val paramsTrips = vBinding.btnTrips.layoutParams as GridLayout.LayoutParams
                paramsTrips.rowSpec = GridLayout.spec(0, 1, 1f)  // Primera fila, mitad de alto
                paramsTrips.columnSpec = GridLayout.spec(1, 1, 0.5f)  // Tercera columna
                vBinding.btnTrips.layoutParams = paramsTrips

                //Cars
                val paramsCars = vBinding.btnCars.layoutParams as GridLayout.LayoutParams
                paramsCars.rowSpec = GridLayout.spec(0, 1, 1f)  // Primera fila, mitad de alto
                paramsCars.columnSpec = GridLayout.spec(0, 1, 0.5f)  // Segunda columna
                vBinding.btnCars.layoutParams = paramsCars
            }
        }
        //Apply
        vBinding.btnLocateOnHired.requestLayout()
        vBinding.btnPending.requestLayout()
        vBinding.btnTrips.requestLayout()
        vBinding.btnCars.requestLayout()
    }

    private fun setButtons() {
        vBinding.apply {
            sharedViewModel.shiftStatusFlow.value?.currentStatus?.let {
                if (vModel.isHired(it) && (W2CLocation.isAllowedToLocateInHired() || vModel.isCurrentBluetoothItop())) {
                    btnLocateOnHired.visibility = View.VISIBLE
                } else {
                    btnLocateOnHired.visibility = View.GONE
                }
                prepareRefreshMenuButtons()
            }

            btnCars.setAction {
                val data = vModel.returnSelectedZoneData()
                Logs.d(TAG, "sending ZoneData Pair =  $data")
                if (data != null) {
                    iMainActivity.navigateTo(
                        ZoningFragmentDirections.actionZoningFragmentToZoningCarsFragment(
                            data.first,
                            data.second
                        )
                    )
                } else {
                    iMainActivity.showToast(R.string.select_zone_first)
                }
            }

            btnTrips.setAction {
                //iMainActivity.showToast("Navigate Trips Screen")
                val data = vModel.returnSelectedZoneData()
                Logs.d(TAG, "sending ZoneData Pair =  $data")
                if (data != null) {
                    iMainActivity.navigateTo(
                        ZoningFragmentDirections.actionZoningFragmentToZoningServicesFragment(
                            data.first,
                            data.second
                        )
                    )
                } else {
                    iMainActivity.showToast(R.string.select_zone_first)
                }
            }

            btnPending.setAction {
                vModel.canOpenPendingTripsFragment(sharedViewModel._pendingTripsListFlow)
            }

            btnLocateOnHired.setAction {
                vModel.selectedZone?.zone?.let {
                    vModel.getCanZoneStartSoonToClear(it)
                } ?: run {
                    //Select a Zone first
                }
            }

            btnSoonInZone.setAction {
                vModel.checkSoonInZone(sharedViewModel.bravoStateFlow.value.isInSoonInZone)
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

    override fun updateTopBarIconRight() {
        iMainActivity.configureIconsTopBar(CustomTopBar.IconType.FILTER, true) {
            val currentFilter: FilterOptions = vModel.listFilterFlow.value

            val modalFilterDialog = ModalBottomSheetFilter.newInstance(currentFilter) { selectedOptions ->
                if (selectedOptions == FilterOptions.MANUAL) {
                    openManualFilterDialog()
                } else if (vModel.zonesListFavouritesFlow.value?.isEmpty() == true && selectedOptions == FilterOptions.FAVOURITES) {
                    iMainActivity.openDialog(
                        CustomDialog.CustomDialogModel(
                            title = resources.getString(R.string.no_favourite_zones),
                            description = resources.getString(R.string.how_to_favorite),
                            buttons = arrayListOf(ButtonType.ACCEPT)
                        ), {}
                    )
                } else {
                    vModel.filterListBy(selectedOptions)
                }
            }

            modalFilterDialog.show(parentFragmentManager, ModalBottomSheetFilter.TAG)
        }
    }

    private fun openManualFilterDialog() {
        iMainActivity.openManualFilterDialog(mutableListOf()) { customFilterDialogResponse ->
            if (customFilterDialogResponse.dataSelected.isEmpty()) {
                try {
                    iMainActivity.showToast(R.string.manual_filter_error)
                } catch (e: IllegalStateException) {
                    Logs.d(TAG, "Couldn't show toast: ${e.message}")
                }
            } else {
                iMainActivity.saveManualFilterData(customFilterDialogResponse)
                vModel.filterListBy(FilterOptions.MANUAL)
            }
        }
    }

    private fun setupPlaceHolderText(macroZone: Macrozone) {
        vModel.listFilterFlow.value?.let {
            if (it == FilterOptions.ZONE_BY_MACROZONE) {
                vBinding.tvPlaceholder.text = macroZone.nombreMacrozone
            }
        }
    }

    private fun setupClickListeners() {
        vBinding.apply {
            tvOnStop.setOnClickListener {
                val hadTheSameOrder = onHeaderClick(OrderOptions.IN_STAND)
                hadTheSameOrder?.let {
                    refreshHeaderImages()
                    if (it) {
                        ivSortOnStop.visibility = View.INVISIBLE
                    } else {
                        ivSortOnStop.setImageResource(R.drawable.round_arrow_drop_up_24)
                        ivSortOnStop.visibility = View.VISIBLE
                    }
                }
            }

            tvOnZone.setOnClickListener {
                val reverse = onHeaderClick(OrderOptions.IN_ZONE)
                Logs.d("ZoningFragment", "tvOnZone onClick. Reverse: $reverse")
                reverse?.let { isReverse ->
                    refreshHeaderImages()
                    if (isReverse) {
                        ivSortOnZone.visibility = View.INVISIBLE
                    } else {
                        ivSortOnZone.setImageResource(R.drawable.round_arrow_drop_up_24)
                        ivSortOnZone.visibility = View.VISIBLE
                    }
                }
            }

            tvHired.setOnClickListener {
                val reverse = onHeaderClick(OrderOptions.HIRED)
                Logs.d("ZoningFragment", "tvHired onClick. Reverse: $reverse")
                reverse?.let { isReverse ->
                    refreshHeaderImages()
                    if (isReverse) {
                        ivSortHired.visibility = View.INVISIBLE
                    } else {
                        ivSortHired.setImageResource(R.drawable.round_arrow_drop_up_24)
                        ivSortHired.visibility = View.VISIBLE
                    }
                }
            }

            tvTrips.setOnClickListener {
                val reverse = onHeaderClick(OrderOptions.TRIPS)
                Logs.d("ZoningFragment", "tvTrips onClick. Reverse: $reverse")
                reverse?.let { isReverse ->
                    refreshHeaderImages()
                    if (isReverse) {
                        ivSortTrips.visibility = View.INVISIBLE
                    } else {
                        ivSortTrips.setImageResource(R.drawable.round_arrow_drop_up_24)
                        ivSortTrips.visibility = View.VISIBLE
                    }
                }
            }

            tvPlaceholder.setOnClickListener {
                if (vModel.isNumericZoneList.value && vModel.listOrderFlow.value != OrderOptions.NAME) {
                    Logs.d(TAG, "tvPlaceholder click - isNumericZoneList on preferences, ignoring click")
                    return@setOnClickListener
                }
                val reverse = onHeaderClick(OrderOptions.NAME)
                Logs.d("ZoningFragment", "tvPlaceholder onClick. Reverse: $reverse")
                reverse?.let { isReverse ->
                    refreshHeaderImages()
                    if (!isReverse) {
                        tvPlaceholder.text = getString(R.string.sort_by_name)
                    }
                }
            }
        }
    }

    private fun onHeaderClick(newOrder: OrderOptions): Boolean? {
        vModel.listOrderFlow.value?.let { lastOrder ->
            if (newOrder == lastOrder) {
                vModel.sortListBy(OrderOptions.NONE)
                return true
            } else {
                vModel.sortListBy(newOrder)
                return false
            }
        }
        return null
    }

    private fun refreshHeaderImages() {
        vBinding.apply {
            ivSortHired.visibility = View.INVISIBLE
            ivSortTrips.visibility = View.INVISIBLE
            ivSortOnStop.visibility = View.INVISIBLE
            ivSortOnZone.visibility = View.INVISIBLE
            refreshPlaceHolderText()
        }
    }

    private fun refreshPlaceHolderText() {
        vBinding.tvPlaceholder.text = vModel.macroZoneFlow.value?.nombreMacrozone
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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

                launch {
                    vModel.macroZoneFlow.collect {
                        it?.let { macroZone ->
                            Logs.d(TAG, "macrozoneFlow collect")
                            setupPlaceHolderText(macroZone)
                        }
                    }
                }

                launch {
                    vModel.listFilterFlow.collect {
                        Logs.d(TAG, "listFilter collect")
                        it?.let { order ->
                            Logs.d(TAG, "listFiler: $order")
                            when (order) {
                                FilterOptions.HOT_ZONES -> {
                                    vBinding.tvPlaceholder.text = getString(R.string.hot_zones)
                                }

                                FilterOptions.UPDATE_MANUAL,
                                FilterOptions.MANUAL -> {
                                    vBinding.tvPlaceholder.text = getString(R.string.manual)
                                }

                                FilterOptions.ZONE_BY_MACROZONE -> {
                                    refreshPlaceHolderText()
                                }

                                FilterOptions.ID_NO_HIERARCHY -> {
                                    vBinding.tvPlaceholder.text = getString(R.string.one_step_list)
                                }

                                FilterOptions.NEARNESS -> {
                                    vBinding.tvPlaceholder.text = getString(R.string.nearness)
                                }

                                FilterOptions.FAVOURITES -> {
                                    vBinding.tvPlaceholder.text = getString(R.string.favourites)
                                }

                                else -> {

                                }
                            }
                            if ((it == FilterOptions.MANUAL || it == FilterOptions.UPDATE_MANUAL)) {
                                if (iMainActivity.getManualFilterData().dataSelected.isEmpty()) {
                                    openManualFilterDialog()
                                } else {
                                    vModel.saveManualFilterData(iMainActivity.getManualFilterData())
                                }
                            }
                        }
                    }
                }

                launch {
                    vModel.zonesListSortedFlow.collect {
                        it?.let { zones ->
                            Logs.d(TAG, "zonesListSortedFlow: updating recyclerView")
                            updateZonesData(zones)
                        }
                    }
                }

                launch {
                    vModel.zoneIsInFavouriteListFlow.collect { zoneInFavourites ->
                        Logs.d(TAG, "zoneIsInFavourite collect")
                        val macroZone = W2CLocation.getZoning().macrozones.find { it.idMacrozone == zoneInFavourites.idMacroZone }
                        val zone =
                            macroZone?.zones?.find { zone -> zone.idZone == zoneInFavourites.idZone }
                        zone?.let { zoneNotNull ->
                            val hasDriverGoingHomeEnabled = vModel.driverGoingHomeCounter.value > 0

                            val canAskForReinforcement = zoneNotNull.allowedUbParada && vModel.isCustomersAtStand
                            if (zoneNotNull.allowedUbParada && vModel.isCustomersAtStand && !hasDriverGoingHomeEnabled) {
                                val directions =
                                    ZoningFragmentDirections.actionZoningFragmentToRequestZoneReinforcementFragment(
                                        zoneInFavourites.idMacroZone,
                                        zoneInFavourites.idZone,
                                        zoneInFavourites.isInFavourites
                                    )
                                iMainActivity.navigateTo(directions)
                            } else {
                                openAddToFavouriteDialog(zoneInFavourites.idMacroZone, zoneInFavourites.idZone, zoneNotNull.nombreZone, zoneNotNull, hasDriverGoingHomeEnabled, zoneInFavourites.isInFavourites, canAskForReinforcement)
                            }

//                                if (zoneInFavourites.isInFavourites) {
//                                openRemoveToFavouriteDialog(zoneInFavourites.idMacroZone, zoneInFavourites.idZone, zoneNotNull.nombreZone, zoneNotNull)
//                            } else {
//                                    openAddToFavouriteDialog(zoneInFavourites.idMacroZone, zoneInFavourites.idZone, zoneNotNull.nombreZone, zoneNotNull, hasDriverGoingHomeEnabled, zoneInFavourites.isInFavourites)
//
//                                }
                        }
                    }
                }

                launch {
                    vModel.locationOnHiredSentFlow.collect {
                        Logs.d(TAG, "locationHired collect")
                        iMainActivity.soonToClearStarted(it != null)
                        sharedViewModel.saveHiredZone(it)
                    }
                }

                launch {
                    sharedViewModel.hiredZone.collect {
                        Logs.d(TAG, "soonToClearBtn hiredZone value: ${it?.nombreZone}")
                        vModel.updateBtnStcOnZoneSelected(it)
                    }
                }

                launch {
                    vModel.soonToClearBtnEnabledFlow.collect {
                        if (it != null) {
                            Logs.d(TAG, "soonToClearBtn EnabledFlow.value: $it")
                            changeSoonToClearBtnState(btnStyle = it)
                        }
                    }
                }

                launch {
                    vModel.serviceButtonFlow.collect {
                        Logs.d(TAG, "serviceButtonFlow.value: $it")
                        if (it == false) {
                            vBinding.btnTrips.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }

                launch {
                    vModel.taxisButtonFlow.collect {
                        Logs.d(TAG, "taxisButtonFlow.value: $it")
                        if (it == false) {
                            vBinding.btnCars.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        }
                    }
                }

                launch {
                    vModel.pendingServicesButtonFlow.collect {
                        Logs.d(TAG, "pendingServices collect")
                        pendingServicesButton = it
                        checkPendingButtonEnabled()
                    }
                }

                launch {
                    vModel.pendingServicesHiredButtonFlow.collect {
                        Logs.d(TAG, "PendingServicesHired collect")
                        pendingServicesHiredButton = it
                        checkPendingButtonEnabled()
                    }
                }


                launch {
                    sharedViewModel.orangeBtnPendingFlow.collect { btnPendingShouldBeOrange ->
                        Logs.d(TAG, "btnPedningOrange collect")
                        if (btnPendingShouldBeOrange == true) {
                            vBinding.btnPending.changeBackground(CustomButton.BackgroundButtonColor.ORANGE)
                        } else {
                            vBinding.btnPending.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
                        }
                    }
                }

                launch {
                    vModel.pendingServicesButtonPressedCallback.collect {
                        Logs.d(TAG, "pendingPressed collect")
                        if (it) {
                            iMainActivity.navigateTo(R.id.action_zoningFragment_to_pendingTripsFragment)
                        } else {
                            if (sharedViewModel.pendingTripsListFlow.value?.isEmpty() == true) {
                                //iMainActivity.showToast(getString(R.string.no_pending_services))
                                iMainActivity.showToast(R.string.toast_no_hay_pendientes)
                            } else {
                                iMainActivity.navigateTo(R.id.action_zoningFragment_to_pendingTripsFragment)
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.zoningChangedFlow.eventCollector {
                        Logs.d(TAG, "zoningChanged collect")
                        it?.let { zone ->
                            Logs.d(TAG, "zoningChanged collect not null")
                            vModel.checkZoningChanged(
                                zone,
                                sharedViewModel.backPressed,
                                sharedViewModel.hiredZone.value
                            )
                        }
                    }
                }

                launch {
                    vModel.zoningListDataTypes.collect {
                        it?.let { dataTypes ->
                            if (dataTypes.isNotBlank()) {
                                dataTypes.forEach {
                                    Logs.d(TAG, "zoningDataTypes: ${it}")
                                }
                                updateHeader(dataTypes)
                                zoneAdapter?.setDataType(dataTypes)
                            }
                        }
                    }
                }

                launch {
                    vModel.hasSoonInZoneFlow.collect {
                        it?.let { hasSoonInZone ->
                            showSIZButton()
                            Logs.d(TAG, "hasSoonInZone: $hasSoonInZone")
                        }
                    }
                }

                launch {
                    vModel.soonInZoneConfirmationFlow.collect {
                        if (it) {
                            vModel.getCurrentSelectedIds()?.let { ids ->
                                iMainActivity.startSoonInZone(ids.first, ids.second)
                                vModel.filterData()
                            }
                            //Start confirmation
                            //startSoonInZoneConfirmationDialog()
                        } else {
                            //Cancel confirmation
                            val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
                                if (response.buttonPressed == ButtonType.ACCEPT) {
                                    //disable button for 3 seconds
                                    vModel.setCancelledSIZ()
                                    iMainActivity.cancelSoonInZone()
                                }
                            }
                            iMainActivity.openDialog(
                                CustomDialog.CustomDialogModel(
                                    title = getString(R.string.cancel_soon_in_zone),
                                    buttons = arrayListOf(
                                        ButtonType.CANCEL,
                                        ButtonType.ACCEPT,
                                    )
                                ), callback
                            )
                        }

                    }
                }

                launch {
                    sharedViewModel.bravoStateFlow.collect {
                        Logs.d(TAG, "bravoState collect")
                        vModel.saveIsInSoonInZone(it.isInSoonInZone)
                        when(it.isInSoonInZone) {
                            true -> {
                                vBinding.btnSoonInZone.changeBackground(CustomButton.BackgroundButtonColor.RED)
                            }
                            false -> {
                                vBinding.btnSoonInZone.changeBackground(CustomButton.BackgroundButtonColor.GREEN)
                            }
                        }
                    }
                }

                launch {
                    sharedViewModel.shortBreakStatus.collect {
                        Logs.d(TAG, "shortBreak collect")
                        showSIZButton()
                        checkPendingButtonEnabled()
                    }
                }

                launch {
                    sharedViewModel.zoneFlow.collect {
                        Logs.d(TAG, "zoneFlow collect")
                        if (it.isNullOrBlank()) {
                            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        } else {
                            checkPendingButtonEnabled()
                        }
                    }
                }

                launch {
                    sharedViewModel.iTopMeterBreakStatus.collect {
                        showSIZButton()
                    }
                }

                launch {
                    vModel.canStartSoonToClearFlow.collect {
                        Logs.d(TAG, "canStartSoonToClear collect")
                        if (it) {
                            openLocateOnHiredDialog()
                        } else {
                            try {
                                iMainActivity.showToast(R.string.stc_attempts_exhausted)
                            } catch (e: IllegalStateException) {
                                Logs.d(TAG, "Couldn't show toast: ${e.message}")
                            }
                        }
                    }
                }

                launch {
                    vModel.listOrderFlow.collect {
                        Logs.d(TAG, "listOrder collect")
                        refreshHeaderImages()
                        vBinding.apply {
                            when(it) {
                                OrderOptions.IN_STAND -> {
                                    Logs.d(TAG, "listOrderFlow.collect = IN_STAND")
                                    ivSortOnStop.setImageResource(R.drawable.round_arrow_drop_up_24)
                                    ivSortOnStop.visibility = View.VISIBLE
                                }
                                OrderOptions.IN_ZONE -> {
                                    Logs.d(TAG, "listOrderFlow.collect = IN_ZONE")
                                    ivSortOnZone.setImageResource(R.drawable.round_arrow_drop_up_24)
                                    ivSortOnZone.visibility = View.VISIBLE
                                }
                                OrderOptions.HIRED -> {
                                    Logs.d(TAG, "listOrderFlow.collect = HIRED")
                                    ivSortHired.setImageResource(R.drawable.round_arrow_drop_up_24)
                                    ivSortHired.visibility = View.VISIBLE
                                }
                                OrderOptions.TRIPS -> {
                                    Logs.d(TAG, "listOrderFlow.collect = TRIPS")
                                    ivSortTrips.setImageResource(R.drawable.round_arrow_drop_up_24)
                                    ivSortTrips.visibility = View.VISIBLE
                                }

                                OrderOptions.NAME -> {
                                    Logs.d(TAG, "listOrderFlow.collect = NAME")
                                    tvPlaceholder.text = getString(R.string.sort_by_name)
                                }

                                else -> {
                                    Logs.d(TAG, "listOrderFlow.collect = NONE")
                                }
                            }
                        }
                    }
                }

                launch {
                    vModel.swapOnHiredZoneFlow.collect { withZone ->
                        Logs.d(TAG, "swapOnHired collect")
                        if (withZone) {
                            vModel.locateOnHired()
                        } else {
                            locateOnHiredWithoutZone()
                        }
                    }
                }

                launch {
                    vModel.goingHomeCallback.collect {
                        iMainActivity.checkGoingHome()
                    }
                }

                //SIZ refresh show button
                launch {
                    sharedViewModel.locationType.collect {
                        Logs.d(TAG, "locationType collect")
                        vModel.saveLocationType(it)
                        showSIZButton()
                    }
                }
                launch {
                    vModel.hasCancelledSIZRecently.collect {
                        Logs.d(TAG, "hasCancelledSIZRecently Flow collect value: $it")
                        showSIZButton()
                    }
                }

                launch {
                    vModel.scrollPositionFlow.collect {
                        Logs.d(TAG, "scrollPositionFlow collected: $it")
                        sharedViewModel.saveZoningScrollPosition(it)
                    }
                }

                launch {
                    sharedViewModel.manualZoningNavigationEventFlow.eventCollector { isManualNavigation ->
                        Logs.d(TAG, "manualZoningNavigationEventFlow collected: $isManualNavigation")
                        if (isManualNavigation == false) {
                            val savedScrollMode = sharedViewModel.zoningScrollPositionFlow.value
                            Logs.d(TAG, "Restoring scroll position from back navigation: $savedScrollMode")
                            vModel.setScrollMode(savedScrollMode)
                        }
                    }
                }
            }
        }
    }

    private fun changeSoonToClearBtnState(btnStyle: CustomButton.StyleButton) {
        when (btnStyle) {
            CustomButton.StyleButton.ENABLE -> {
                vBinding.btnLocateOnHired.setButtonStyle(btnStyle)
                vBinding.btnLocateOnHired.changeText(getString(R.string.btn_soon_to_clear))
                vBinding.btnLocateOnHired.changeButtonIcon(R.drawable.ubactivar)
                vBinding.btnLocateOnHired.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
                vBinding.btnLocateOnHired.setAction {
                    vModel.selectedZone?.zone?.let { zone ->
                        vModel.getCanZoneStartSoonToClear(zone)
                    } ?: run {
                        //Select a Zone first
                    }
                }
            }
            CustomButton.StyleButton.DISABLE -> {
                vBinding.btnLocateOnHired.setButtonStyle(CustomButton.StyleButton.ENABLE)
                vBinding.btnLocateOnHired.changeText(getString(R.string.btn_delocate))
                vBinding.btnLocateOnHired.changeButtonIcon(R.drawable.ubdesact)
                vBinding.btnLocateOnHired.changeBackground(CustomButton.BackgroundButtonColor.RED)
                vBinding.btnLocateOnHired.setAction {
                    openDelocateOnHiredDialog()
                }
            }
            CustomButton.StyleButton.LOADING -> {
                vBinding.btnLocateOnHired.setButtonStyle(btnStyle)
            }
        }
    }



    private fun showSIZButton() {
        if (!vModel.isHired(sharedViewModel.shiftStatusFlow.value?.currentStatus ?: 0) && sharedViewModel.shiftStatusFlow.value?.currentStatus != ifConstants.STATE_DISPATCHED) {
            if (vModel.hasSoonInZoneFlow.value == true) {
                vBinding.btnSoonInZone.visibility = View.VISIBLE
                Logs.d(TAG, "showSIZButton")
            } else {
                vBinding.btnSoonInZone.visibility = View.GONE
                Logs.d(TAG, "DON'T showSIZButton")
            }

            val isNotLocatedInZone = sharedViewModel.locationType.value != "Z"
            val isInSoonInZone = sharedViewModel.bravoStateFlow.value.isInSoonInZone
            val hasCanceledSIZ = vModel.hasCancelledSIZRecently.value
            if ((!isInSoonInZone && (isNotLocatedInZone || sharedViewModel.shortBreakStatus.value == ShortBreakStatus.IN_SHORT_BREAK || sharedViewModel.shortBreakStatus.value == ShortBreakStatus.IN_SHORT_BREAK_FORCED || sharedViewModel.iTopMeterBreakStatus.value == true))
                || hasCanceledSIZ) {
                Logs.d(TAG, "DISABLE SIZButton because car is in short break or doesn't have location on")
                vBinding.btnSoonInZone.setButtonStyle(CustomButton.StyleButton.DISABLE)
            } else {
                vBinding.btnSoonInZone.setButtonStyle(CustomButton.StyleButton.ENABLE)
            }
            prepareRefreshMenuButtons()
        }
    }

    private fun startSoonInZoneConfirmationDialog() {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { response ->
            if (response.buttonPressed == ButtonType.ACCEPT) {
                vModel.getCurrentSelectedIds()?.let { ids ->
                    iMainActivity.startSoonInZone(ids.first, ids.second)
                }
            }
        }
        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(
                    R.string.start_soon_in_zone,
                    vModel.selectedZone?.zone?.nombreZone
                ),
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT,
                )
            ), callback
        )
    }

    private fun updateHeader(dataTypes: String) {
        Logs.d(TAG, "updateHeader")
        val columns = ZoneUtils.parseStringDataTypes(dataTypes)
        vBinding.apply {
            if (columns.contains(AvailableColumnsEnum.STAND_VEHICLES)) {
                vBinding.tvOnStop.visibility = View.VISIBLE
                vBinding.tvOnStop2?.visibility = View.VISIBLE
            } else {
                vBinding.tvOnStop.visibility = View.GONE
                vBinding.tvOnStop2?.visibility = View.GONE
            }

            if (columns.contains(AvailableColumnsEnum.ZONE_VEHICLES)) {
                vBinding.tvOnZone.visibility = View.VISIBLE
                vBinding.tvOnZone2?.visibility = View.VISIBLE
            } else {
                vBinding.tvOnZone.visibility = View.GONE
                vBinding.tvOnZone2?.visibility = View.GONE
            }

            if (columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) || columns.contains(AvailableColumnsEnum.BOOKED_TRIPS)){
                vBinding.tvHired.visibility = View.VISIBLE
                vBinding.tvHired2?.visibility = View.VISIBLE
                if (!columns.contains(AvailableColumnsEnum.HIRED_VEHICLES)) {
                    vBinding.tvHired.setText(R.string.abrevUbServiciosWithoutDots)
                    vBinding.tvHired2?.setText(R.string.abrevUbServiciosWithoutDots)
                }
            } else {
                vBinding.tvHired.visibility = View.GONE
                vBinding.tvHired2?.visibility = View.GONE
            }

            if (columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS) || columns.contains(AvailableColumnsEnum.TOTAL_TRIPS) || (columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) && columns.contains(AvailableColumnsEnum.BOOKED_TRIPS))) {
                vBinding.tvTrips.visibility = View.VISIBLE
                vBinding.tvTrips2?.visibility = View.VISIBLE
                if (columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS)) {
                    vBinding.tvTrips.setText(R.string.abbrevPreBookedTrips)
                    vBinding.tvTrips2?.setText(R.string.abbrevPreBookedTrips)
                } else if (columns.contains(AvailableColumnsEnum.TOTAL_TRIPS)) {
                    vBinding.tvTrips.setText(R.string.abbrevTotalTrips)
                    vBinding.tvTrips2?.setText(R.string.abbrevTotalTrips)
                }
            } else {
                vBinding.tvTrips.visibility = View.GONE
                vBinding.tvTrips2?.visibility = View.GONE
            }
        }
    }

    private fun openRemoveToFavouriteDialog(idMacroZone: Int, idZone: Int, zoneName: String, zone: Zone) {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit =
            { response ->
                if (response.buttonPressed == ButtonType.ACCEPT) {
                    vModel.removeZoneFromFavourites(
                        idMacroZone,
                        idZone,
                        zone
                    )
                }
            }

        iMainActivity.openDialog(
            model = CustomDialog.CustomDialogModel(
                title = getString(R.string.remove_from_favourite),
                description = getString(R.string.remove_from_favourite_msg, zoneName),
                buttons = arrayListOf(
                    ButtonType.CANCEL,
                    ButtonType.ACCEPT
                )
            ),
            response = callback
        )
    }

    private fun openAddToFavouriteDialog(idMacroZone: Int, idZone: Int, zoneName: String, zone: Zone, hasDriverGoingHomeEnabled: Boolean, alreadyInFavourites: Boolean, canAskForReinforcement: Boolean) {

        val callback: (CustomDialog.CustomDialogResponse) -> Unit =
            { response ->
                when(response.buttonPressed) {
                    ButtonType.ADD_FAVOURITES -> {
                        vModel.addZoneToFavourites(idMacroZone, idZone, zone)
                    }
                    ButtonType.REMOVE_FAVOURITES -> {
                        vModel.removeZoneFromFavourites(
                            idMacroZone,
                            idZone,
                            zone
                        )
                    }
                    ButtonType.GOING_HOME -> {
                        vModel.saveDriverGoingHomeData(idMacroZone, idZone, response.selectedOption?.id)
                    }
                    ButtonType.OPEN_REINFORCEMENT -> {
                        val directions =
                            ZoningFragmentDirections.actionZoningFragmentToRequestZoneReinforcementFragment(
                                idMacroZone,
                                idZone,
                                alreadyInFavourites
                            )
                        iMainActivity.navigateTo(directions)
                    }
                    else -> {

                    }
                }
//                if (response.buttonPressed == ButtonType.ADD_FAVOURITES) {
//                    vModel.addZoneToFavourites(idMacroZone, idZone, zone)
//                }
            }

        val buttons = mutableListOf<ButtonType>()

        var listOption: Map<Int, String>? = null
        var description: String? = null

        if (hasDriverGoingHomeEnabled) {

            listOption = mapOf(
                75 to "75 min.",
            )

            buttons.add(ButtonType.GOING_HOME)
            vModel.driverGoingHomeValue.value?.let { value ->
                try {
                    if (value.isNotBlank() && context != null) {
                        listOption = value.split("|").associate {
                            val key = it.toInt()
                            key to requireContext().getString(R.string.driver_going_home_item, it)
                        }
                    }
                } catch (e: Exception) {
                    Logs.e(TAG, "Error parsing driver going home values: $e")
                }
            }
            description = getString(R.string.driver_going_home_description)
        } else {
            buttons.add(ButtonType.CANCEL)
        }

        if (canAskForReinforcement) {
            buttons.add(ButtonType.OPEN_REINFORCEMENT)
        }

        if (alreadyInFavourites) {
            buttons.add(ButtonType.REMOVE_FAVOURITES)
        } else {
            buttons.add(ButtonType.ADD_FAVOURITES)
        }
        iMainActivity.openDialog(
            model = CustomDialog.CustomDialogModel(
                title = getString(R.string.zone_and_name, zoneName),
                buttons = ArrayList(buttons),
                listOptions = listOption,
                description = description
            ),
            response = callback,
            fragmentManager = childFragmentManager
        )

    }

    private var fromClick = false

    private fun updateZonesData(zones: List<ZoneModel>) {
        try {
            if (vBinding.rvZones.adapter == null) {
                Logs.d(TAG, "initZoneAdapter: initAdapter")

//                val click: (ZoneModel) -> Unit = { itemClick ->
//                    vModel.changeSelectedItem(itemClick)
//                    fromClick = true
//                }
//
//                val longClick: (Pair<Int, Int>) -> Unit = {
//                    vModel.isZoneInFavourites(it.first, it.second)
//                }
//                zoneAdapter = ZoneAdapter(requireContext(), click, longClick)

                val mLayoutManager: RecyclerView.LayoutManager = if (ConfigurationUtils.isTablet(resources) && ConfigurationUtils.isLandscape(resources)) {
                    GridLayoutManager(context, 2)
                } else {
                    LinearLayoutManager(requireContext())
                }

                Logs.d(TAG, "settear adapter")
                vBinding.rvZones.apply {
                    adapter = zoneAdapter
                    layoutManager = mLayoutManager
                }
            }
            vBinding.clRowZone.visibility = View.VISIBLE
            vBinding.pbZones?.visibility = View.GONE
            Logs.d(TAG, "clRow visible y pbZones gone")
            submitListZones(zones)
        } catch (e: Exception) {
            Logs.d(TAG, "initZoneAdapter catch: $e")
        }
    }

    private fun submitListZones(zones: List<ZoneModel>) {
        Logs.d(TAG, "submitList:  zones.size-> " + zones.size)
        zoneAdapter?.submitList(zones) {
            view ?: return@submitList
            Logs.d(TAG, "submitList: select finished")
            //vBinding.rvZones.scrollBarSize
            vModel.scrollPositionFlow.value.let {
                vBinding.rvZones.apply {
                    if (!fromClick) {
                        when(it) {
                            ScrollModeEnum.JUMP_TO_TOP -> {
                                post {
                                    view?.let {
                                        scrollToPosition(0)
                                    }
                                }
                            }
                            ScrollModeEnum.FOLLOW_SELECTED -> {
                                post {
                                    val position =
                                        zones.indexOf(zones.firstOrNull { it.zone.idZone == vModel.selectedZone?.zone?.idZone
                                                && it.zone.nombreZone == vModel.selectedZone?.zone?.nombreZone})
                                    if (position > -1) {
                                        view?.let {
                                            scrollToPosition(position)
                                        }
                                    }
                                }
                            }
                            ScrollModeEnum.NO_SCROLL -> {
                                //No scroll Mode, do nothing
                            }
                        }
                    } else {
                        fromClick = false
                    }
                }
            }
        }
    }

    override fun onPause() {
        vModel.removeHandlerCallbacks()
        firstChange = true
        super.onPause()
    }

    override fun onResume() {
        vModel.initViewModelData(safeArgs.idMacroZone, sharedViewModel.hiredZone.value)
        super.onResume()
    }

    private fun prepareRefreshMenuButtons() {
        if (refreshMenuPending) return
        refreshMenuPending = true

        vBinding.root.doOnLayout {
            refreshMenuPending = false
            refreshMenuButtons()
        }
    }
}