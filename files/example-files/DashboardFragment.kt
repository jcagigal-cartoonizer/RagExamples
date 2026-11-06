package ifac.td.taxi.ui.screen

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import com.interfacom.sdk.taximeter.log.Log
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentDashboardBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.SimplePendingTripsAdapter
import ifac.td.taxi.ui.adapter.ZoneAdapter
import ifac.td.taxi.ui.model.ZoneModel
import ifac.td.taxi.ui.util.ZoneUtils
import ifac.td.taxi.viewmodel.DashboardViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class DashboardFragment : BaseFragment<FragmentDashboardBinding, DashboardViewModel>(
    R.layout.fragment_dashboard
) {

    private val TAG = "DashboardFragment"

    private val sharedViewModel: MainActivityViewModel by activityViewModel()
    private val vModel: DashboardViewModel by viewModel()

    private val nearbyZoneAdapter: ZoneAdapter? by lazy {
        /*
        val click: (ZoneModel) -> Unit = { itemClick ->
            vModel.changeSelectedItem(itemClick)
            fromClick = true
        }

        val longClick: (Pair<Int, Int>) -> Unit = {
            vModel.isZoneInFavourites(it.first, it.second)
        }
 */
        val click: (ZoneModel) -> Unit = {
        }
        val longClick: (Pair<Int, Int>) -> Unit = {
        }
        ZoneAdapter(requireContext(), click, longClick)
    }

    private val farZoneAdapter: ZoneAdapter? by lazy {
        val click: (ZoneModel) -> Unit = {
        }
        val longClick: (Pair<Int, Int>) -> Unit = {
        }
        ZoneAdapter(requireContext(), click, longClick)
    }

    private val pendingTripsAdapter: SimplePendingTripsAdapter? by lazy {
        SimplePendingTripsAdapter(requireContext()) {

        }
    }

    private val actualZoneAdapter: ZoneAdapter? by lazy {
        val click: (ZoneModel) -> Unit = {
        }
        val longClick: (Pair<Int, Int>) -> Unit = {
        }
        ZoneAdapter(requireContext(), click, longClick)
    }

    override fun getViewModel(): DashboardViewModel {
        return vModel
    }

    override fun getViewBinding(): FragmentDashboardBinding {
        return FragmentDashboardBinding.inflate(layoutInflater)
    }

    override fun setupComponents() {
        iMainActivity.showHeader(true)
    }
/*
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
                Log.d("ZoningFragment", "tvOnZone onClick. Reverse: $reverse")
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
                Log.d("ZoningFragment", "tvHired onClick. Reverse: $reverse")
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
                Log.d("ZoningFragment", "tvTrips onClick. Reverse: $reverse")
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
        }
    }*/

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    vModel.nearbyZonesListSortedFlow.collect {
                        it?.let { zones ->
                            Log.d(TAG, "zonesListSortedFlow: updating recyclerView")
                            updateZonesData(zones)
                        }
                    }
                }

                launch {
                    vModel.farZonesListSortedFlow.collect {
                        it?.let { farZones ->
                            Log.d(TAG, "farZonesListSortedFlow: updating recyclerView")
                            updateFarZonesData(farZones)
                        }
                    }
                }

                launch {
                    sharedViewModel.pendingTripsListFlow.collect {
                        it?.let { pendingTrips ->
                            updatePendingTripsData(pendingTrips)
                        }
                    }
                }

                launch {
                    vModel.actualZoneListSortedFlow.collect {
                        it?.let { actualZone ->
                            updateActualZoneData(actualZone)
                        }
                    }
                }

                /*
                launch {
                    sharedViewModel.hiredZone.collect {
                        if (it == null) {
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
                        } else {
                            vBinding.btnLocateOnHired.changeText(getString(R.string.btn_delocate))
                            vBinding.btnLocateOnHired.changeButtonIcon(R.drawable.ubdesact)
                            vBinding.btnLocateOnHired.changeBackground(CustomButton.BackgroundButtonColor.RED)
                            vBinding.btnLocateOnHired.setAction {
                                openDelocateOnHiredDialog()
                            }
                        }
                    }
                }

                 */


                launch {
                    sharedViewModel.zoningChangedFlow.eventCollector {
                        it?.let { zone ->
                            vModel.refreshActualZoneData()
                            //vModel.checkZoningChanged(zone, sharedViewModel.backPressed)
                        }
                    }
                }

                launch {
                    vModel.zoningListDataTypes.collect {
                        it?.let { dataTypes ->
                            if (dataTypes.isNotBlank()) {
                                updateHeader(dataTypes)
                                nearbyZoneAdapter?.setDataType(dataTypes)
                                farZoneAdapter?.setDataType(dataTypes)
                                actualZoneAdapter?.setDataType(dataTypes)
                            }
                        }
                    }
                }


                launch {
                    sharedViewModel.bravoStateFlow.collect {
                        when(it.isInSoonInZone) {
                            true -> {
                                //TODO: Has soon in Zone. Add zone to zones
                            }
                            false -> {
                                //TODO: Does not have soon in Zone. Remove zone to zones
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateHeader(dataTypes: String) {
        val columns = ZoneUtils.parseStringDataTypes(dataTypes)
        vBinding.apply {
            if (columns.contains(AvailableColumnsEnum.STAND_VEHICLES)) {
                vBinding.tvOnStop.visibility = View.VISIBLE
                vBinding.tvOnStop2.visibility = View.VISIBLE
                vBinding.tvOnStop3.visibility = View.VISIBLE
            } else {
                vBinding.tvOnStop.visibility = View.GONE
                vBinding.tvOnStop2.visibility = View.GONE
                vBinding.tvOnStop3.visibility = View.GONE
            }

            if (columns.contains(AvailableColumnsEnum.ZONE_VEHICLES)) {
                vBinding.tvOnZone.visibility = View.VISIBLE
                vBinding.tvOnZone2.visibility = View.VISIBLE
                vBinding.tvOnZone3.visibility = View.VISIBLE
            } else {
                vBinding.tvOnZone.visibility = View.GONE
                vBinding.tvOnZone2.visibility = View.GONE
                vBinding.tvOnZone3.visibility = View.GONE
            }

            if (columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) || columns.contains(AvailableColumnsEnum.BOOKED_TRIPS)){
                vBinding.tvHired.visibility = View.VISIBLE
                vBinding.tvHired2.visibility = View.VISIBLE
                vBinding.tvHired3.visibility = View.VISIBLE
                if (!columns.contains(AvailableColumnsEnum.HIRED_VEHICLES)) {
                    vBinding.tvHired.setText(R.string.abrevUbServiciosWithoutDots)
                    vBinding.tvHired2.setText(R.string.abrevUbServiciosWithoutDots)
                    vBinding.tvHired3.setText(R.string.abrevUbServiciosWithoutDots)
                }
            } else {
                vBinding.tvHired.visibility = View.GONE
                vBinding.tvHired2.visibility = View.GONE
                vBinding.tvHired3.visibility = View.GONE
            }

            if (columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS) || columns.contains(AvailableColumnsEnum.TOTAL_TRIPS) || (columns.contains(AvailableColumnsEnum.HIRED_VEHICLES) && columns.contains(AvailableColumnsEnum.BOOKED_TRIPS))) {
                vBinding.tvTrips.visibility = View.VISIBLE
                vBinding.tvTrips2.visibility = View.VISIBLE
                vBinding.tvTrips3.visibility = View.VISIBLE
                if (columns.contains(AvailableColumnsEnum.PREBOOKED_TRIPS)) {
                    vBinding.tvTrips.setText(R.string.abbrevPreBookedTrips)
                    vBinding.tvTrips2.setText(R.string.abbrevPreBookedTrips)
                    vBinding.tvTrips3.setText(R.string.abbrevPreBookedTrips)
                } else if (columns.contains(AvailableColumnsEnum.TOTAL_TRIPS)) {
                    vBinding.tvTrips.setText(R.string.abbrevTotalTrips)
                    vBinding.tvTrips2.setText(R.string.abbrevTotalTrips)
                    vBinding.tvTrips3.setText(R.string.abbrevTotalTrips)
                }
            } else {
                vBinding.tvTrips.visibility = View.GONE
                vBinding.tvTrips2.visibility = View.GONE
                vBinding.tvTrips3.visibility = View.GONE
            }
        }
    }

    private fun updateActualZoneData(actualZoneData: List<ZoneModel>) {
        try {
            if (vBinding.rvActualZone.adapter == null) {
                Log.d(TAG, "initActualZoneAdapter: initAdapter")

                val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())

                vBinding.rvActualZone.apply {
                    adapter = actualZoneAdapter
                    layoutManager = mLayoutManager
                }
            }

            actualZoneAdapter?.submitList(actualZoneData)
        } catch (e: Exception) {
            Log.d(TAG, "initActualZoneData: $e")
        }
    }

    private fun updatePendingTripsData(pendingTrips: ArrayList<PendingTrip>) {
        try {
            if (vBinding.rvPendingTrips.adapter == null) {
                Log.d(TAG, "initZoneAdapter: initAdapter")

                val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())

                vBinding.rvPendingTrips.apply {
                    adapter = pendingTripsAdapter
                    layoutManager = mLayoutManager
                }
            }

            vBinding.tvPendingTripsSize.text = pendingTrips.size.toString()
            pendingTripsAdapter?.submitList(pendingTrips) {
                view?.let {
                    vBinding.rvPendingTrips.scrollToPosition(0)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "initMacroZoneAdapter: $e")
        }
    }

    private fun updateZonesData(zones: List<ZoneModel>) {
        try {
            if (vBinding.rvZones.adapter == null) {
                Log.d(TAG, "initZoneAdapter: initAdapter")

                val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())

                vBinding.rvZones.apply {
                    adapter = nearbyZoneAdapter
                    layoutManager = mLayoutManager
                }
            }
            vBinding.clRowZone.visibility = View.VISIBLE
            vBinding.lytInfoZones.visibility = View.VISIBLE
            vBinding.pbZones.visibility = View.GONE
            Log.d(TAG, "submitList:  zones.size-> " + zones.size)
            nearbyZoneAdapter?.submitList(zones) {
                view?.let {
                    vBinding.rvZones.scrollToPosition(0)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "initMacroZoneAdapter: $e")
        }
    }

    private fun updateFarZonesData(zones: List<ZoneModel>) {
        try {
            if (vBinding.rvZones2.adapter == null) {
                Log.d(TAG, "initZoneAdapter: initAdapter")

                val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())


                vBinding.rvZones2.apply {
                    adapter = farZoneAdapter
                    layoutManager = mLayoutManager
                }
            }
            vBinding.clRowZone.visibility = View.VISIBLE
            vBinding.lytInfoZones.visibility = View.VISIBLE
            vBinding.pbZones.visibility = View.GONE
            Log.d(TAG, "submitList:  farZones.size-> " + zones.size)
            farZoneAdapter?.submitList(zones)  {
                view?.let {
                    vBinding.rvZones2.scrollToPosition(0)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "initMacroZoneAdapter: $e")
        }
    }

    override fun onPause() {
        vModel.removeHandlerCallbacks()
        super.onPause()
    }

    override fun onResume() {
        vModel.initViewModelData()
        vModel.loadTrips(sharedViewModel._pendingTripsListFlow)
        super.onResume()
    }
}