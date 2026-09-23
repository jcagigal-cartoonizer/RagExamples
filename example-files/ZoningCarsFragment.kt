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
import ifac.td.taxi.databinding.FragmentZoningCarsBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.ZoneCarsAdapter
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.model.ZoneCarRowModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.ZoningCarsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ZoningCarsFragment : BaseFragment<FragmentZoningCarsBinding, ZoningCarsViewModel>(
    R.layout.fragment_zoning_cars
) {

    private val TAG = this.javaClass.simpleName

    private val sharedViewModel: MainActivityViewModel by activityViewModel()
    private val vModel: ZoningCarsViewModel by viewModel()
    private val safeArgs: ZoningCarsFragmentArgs by navArgs()

    private var pendingServicesButton: Boolean? = null
    private var pendingServicesHiredButton: Boolean? = null

    private var zoneCarsAdapter: ZoneCarsAdapter? = null

    override fun getViewModel(): ZoningCarsViewModel {
        return vModel
    }

    override fun getViewBinding(): FragmentZoningCarsBinding {
        return FragmentZoningCarsBinding.inflate(layoutInflater)
    }

    override fun onDestroyView() {
        Logs.d(TAG, "LeakCanary adapter null")
        vBinding.rvZoningCars.adapter = null
        zoneCarsAdapter = null
        super.onDestroyView()
    }

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        Logs.d(TAG, "setupComponents: safeArgs -> idMacroZone: ${safeArgs.idMacroZone} idZone: ${safeArgs.idZone}")
        vBinding.pbZones.visibility = View.VISIBLE
        setViews()
        setButtons()
        vModel.getPOIsValue()
        vModel.checkPendingServiceOnForHirePermission()
        vModel.checkPendingServiceOnHiredPermission()
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            sharedViewModel.emitManualZoningNavigation(false)
            iMainActivity.navigateBack()
        }
    }

    private fun initViewModelData() {
        Logs.d(TAG, "initViewModelData: safeArgs -> idMacroZone: ${safeArgs.idMacroZone} idZone: ${safeArgs.idZone}")
        vModel.initViewModelData(safeArgs.idMacroZone, safeArgs.idZone)
    }

    override fun onResume() {
        initViewModelData()
        super.onResume()
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.carsListFlow.collect {
                        it?.let { list ->
                            Logs.d(TAG, "setupObservers: carsListFlow: $list")
                            updateData(vModel.getCarListByType(list))
                        }
                    }
                }

                launch {
                    vModel.locationOnHiredSentFlow.collect {
                        sharedViewModel.saveHiredZone(it)
                        sharedViewModel.tripFlow.value?.id?.let { tripId ->
                            iMainActivity.navigateTo(
                                ZoningCarsFragmentDirections.actionZoningCarsFragmentToHomeFragment()
                            )
                            // go to onTripFragment after homeFragment
                            iMainActivity.navigateTo(HomeDirections.goToOnTripFragment())
                        }
                    }
                }

                launch {
                    vModel.pendingServicesButtonPressedCallback.collect {
                        if (it) {
                            iMainActivity.navigateTo(R.id.action_zoningCarsFragment_to_pendingTripsFragment)
                        } else {
                            if (sharedViewModel.pendingTripsListFlow.value?.isEmpty() == true) {
                                //iMainActivity.showToast(getString(R.string.no_pending_services))
                                iMainActivity.showToast(R.string.toast_no_hay_pendientes)
                            } else {
                                iMainActivity.navigateTo(R.id.action_zoningCarsFragment_to_pendingTripsFragment)
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
                    vModel.pendingServicesButtonFlow.collect {
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
                    sharedViewModel.orangeBtnPendingFlow.collect { btnPendingShouldBeOrange ->
                        if (btnPendingShouldBeOrange == true) {
                            vBinding.btnPending.changeBackground(CustomButton.BackgroundButtonColor.ORANGE)
                        } else {
                            vBinding.btnPending.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
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
                    sharedViewModel.zoneFlow.collect {
                        Logs.d("ContactCentral", "zoneFlow collect: $it")
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

    private fun updateData(list: List<ZoneCarRowModel>) {
        try {
            vBinding.pbZones.visibility = View.GONE
            if (zoneCarsAdapter == null) {
                zoneCarsAdapter = ZoneCarsAdapter(requireContext())

                vBinding.rvZoningCars.apply {
                    adapter = zoneCarsAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                }
            }

            if (list.isEmpty()) {
                vBinding.tvNoCars.visibility = View.VISIBLE
            } else {
                vBinding.tvNoCars.visibility = View.GONE
            }

            Logs.d(TAG, "updateData: list: $list")
            submitList(list)

        } catch (e: Exception) {
            Logs.e(TAG, "updateData: $e")
        }
    }

    private fun submitList(list: List<ZoneCarRowModel>) {
        zoneCarsAdapter?.submitList(list)
    }

    private fun setViews() {
        vBinding.apply {
            tvZoneName.text = W2CLocation.getZoning().getMacrozoneById(safeArgs.idMacroZone)
                .getZoneById(safeArgs.idZone).nombreZone
        }
    }


    private fun setButtons() {
        vBinding.apply {

            btnBack.setAction {
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
                            iMainActivity.navigateTo(ZoningCarsFragmentDirections.actionZoningCarsFragmentToHomeFragment())
                            iMainActivity.navigateTo(HomeDirections.goToOnTripFragment())
                        }
                    }
                    else -> {
                        iMainActivity.navigateTo(ZoningCarsFragmentDirections.actionZoningCarsFragmentToHomeFragment())
                    }
                }
            }

            btnPending.setAction {
                vModel.canOpenPendingTripsFragment(sharedViewModel._pendingTripsListFlow)
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

        if (sharedViewModel.zoneFlow.value.isNullOrEmpty()) {
            Logs.d(TAG, "sharedViewModel.zoneFlow: NullOrEmpty")
            vBinding.btnPending.setButtonStyle(CustomButton.StyleButton.DISABLE)
        }

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

    private fun openLocateOnHiredDialog() {
        val zone = W2CLocation
            .getZoning()
            .getMacrozoneById(safeArgs.idMacroZone)
            .getZoneById(safeArgs.idZone)
        val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
            { response ->
                when (response.buttonPressed) {
                    ButtonType.ACCEPT -> vModel.locateOnHired(zone)
                    ButtonType.POI -> iMainActivity.navigateTo(ZoningFragmentDirections.actionZoningFragmentToPointsOfInterestFragment())
                    else -> Logs.d(TAG, "openLocateOnHiredDialog: callback dismiss")
                }
            }

        if (vModel.bravoConfiguration == null) {
            vModel.getPOIsValue()
        }

        val buttons = when {
            zone.allowedUbOcupado == true && vModel.bravoConfiguration?.isPoisEnabled == true ->
                arrayListOf(ButtonType.POI, ButtonType.ACCEPT)
            zone.allowedUbOcupado == true ->
                arrayListOf(ButtonType.ACCEPT)
            else ->
                arrayListOf(ButtonType.POI)
        }


        zone.let {
            if (it.allowedUbOcupado) {
                iMainActivity.openDialog(
                    CustomDialog.CustomDialogModel(
                        title = it.nombreZone,
                        description = getString(R.string.ask_locate),
                        buttons = buttons,
                    ),
                    callBack,
                    fragmentManager = childFragmentManager
                )
            } else {
                iMainActivity.showToast(R.string.locate_hired_zone_not_allowed)
            }
        }
    }

    private fun openDelocateOnHiredDialog() {
        val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
            { response ->
                when (response.buttonPressed) {
                    ButtonType.ACCEPT -> vModel.delocateOnHired()
                    else -> Logs.d(TAG, "openDelocateOnHiredDialog: callback dismiss")
                }
            }

        sharedViewModel.hiredZone.value?.let {
            if (it.allowedUbOcupado) {
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
    }

    override fun onPause() {
        vModel.removeHandlerCallbacks()
        super.onPause()
    }
}