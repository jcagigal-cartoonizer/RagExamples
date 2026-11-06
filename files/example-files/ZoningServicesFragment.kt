package ifac.td.taxi.ui.screen

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.HomeDirections
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentZoningServicesBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.ZoneServicesAdapter
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.model.ZoneTripModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.ZoningServicesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ZoningServicesFragment : BaseFragment<FragmentZoningServicesBinding, ZoningServicesViewModel>(
    R.layout.fragment_zoning_services
) {

    private val TAG = this.javaClass.simpleName

    private val sharedViewModel: MainActivityViewModel by activityViewModel()
    private val vModel: ZoningServicesViewModel by viewModel()
    private val safeArgs: ZoningServicesFragmentArgs by navArgs()

    private var zoneServicesAdapter: ZoneServicesAdapter? = null

    override fun getViewModel(): ZoningServicesViewModel {
        return vModel
    }

    override fun getViewBinding(): FragmentZoningServicesBinding {
        return FragmentZoningServicesBinding.inflate(layoutInflater)
    }

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        setButtons()
        vBinding.pbZones.visibility = View.VISIBLE
        setViews()
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            sharedViewModel.emitManualZoningNavigation(false)
            iMainActivity.navigateBack()
        }
    }

    private fun iniViewModelData() {
        Logs.d(TAG, "initViewModelData: safeArgs -> idMacroZone: ${safeArgs.idMacroZone} idZone: ${safeArgs.idZone}")
        vModel.initViewModelData(safeArgs.idMacroZone, safeArgs.idZone)
    }


    override fun onResume() {
        iniViewModelData()
        super.onResume()
    }

    private fun setButtons() {
        vBinding.apply {
            btnShowAll.setButtonStyle(CustomButton.StyleButton.DISABLE)
            btnShowAll.setAction {
                vModel.setShowAllTrips(true, safeArgs.idMacroZone, safeArgs.idZone)
                btnShowAll.visibility = View.GONE
                btnShowRecent.visibility = View.VISIBLE
            }
            btnShowRecent.setAction {
                vModel.setShowAllTrips(false, safeArgs.idMacroZone, safeArgs.idZone)
                btnShowAll.visibility = View.VISIBLE
                btnShowRecent.visibility = View.GONE
            }
            btnCancel.setAction {
                sharedViewModel.emitManualZoningNavigation(false)
                iMainActivity.navigateBack()
            }

            btnClose.setAction {
                when(sharedViewModel.shiftStatusFlow.value?.currentStatus) {
                    ifConstants.STATE_HIRED,
                    ifConstants.STATE_DISPATCHED,
                    ifConstants.STATE_HIRED_DISPATCHED,
                    ifConstants.STATE_HIRED_NO_CENTRAL -> {
                        sharedViewModel.tripFlow.value?.id?.let { id ->
                            // Navigate first to HomeGraph and later to OnTripFragment
                            iMainActivity.navigateTo(ZoningServicesFragmentDirections.actionZoningServicesFragmentToHomeFragment())
                            iMainActivity.navigateTo(HomeDirections.goToOnTripFragment())
                        }
                    }
                    else -> {
                        iMainActivity.navigateTo(ZoningServicesFragmentDirections.actionZoningServicesFragmentToHomeFragment())
                    }
                }
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.zoneTripsListFlow.collect {
                        Logs.d(TAG, "setupObservers: collected zoneTripsListFlow")
                        updateData(it)
                    }
                }

                launch {
                    vModel.enableShowAllButtonFlow.collect {
                        Logs.d(TAG, "setupObservers: collected enableShowAllButtonFlow -> $it")
                        it?.let { showButton ->
                            if (showButton) {
                                vBinding.btnShowAll.setButtonStyle(CustomButton.StyleButton.ENABLE)
                            }
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

    private fun updateData(list: List<ZoneTripModel>?) {
        try {
            Logs.d(TAG, "updateData: updating data with ${list?.size ?: 0} items")
            vBinding.pbZones.visibility = View.GONE
            if (zoneServicesAdapter == null) {
                zoneServicesAdapter = ZoneServicesAdapter(requireContext(), vModel.companyNumber)

                vBinding.rvZoningCars.apply {
                    adapter = zoneServicesAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                }
            }

            if (list?.isEmpty() == true) {
                vBinding.tvNoCars.visibility = View.VISIBLE
            } else {
                vBinding.tvNoCars.visibility = View.GONE
            }

            submitList(list)

        } catch (e: Exception) {
            Logs.e(TAG, "updateData: $e")
        }
    }

    private fun submitList(list: List<ZoneTripModel>?) {
        Logs.d(TAG, "submitList: submitting list with ${list?.size ?: 0} items")
        if (list == null) {
            zoneServicesAdapter?.submitList(listOf())
        } else {
            zoneServicesAdapter?.submitList(list)
        }
    }

    private fun setViews() {
        vBinding.apply {
            tvZoneName.text = W2CLocation.getZoning().getMacrozoneById(safeArgs.idMacroZone)
                .getZoneById(safeArgs.idZone).nombreZone
        }
    }

    override fun onPause() {
        super.onPause()
        vModel.removeHandlerCallbacks()
    }
}