package ifac.td.taxi.ui.screen

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.nexusgeographics.cercalia.maps.CercaliaMapView
import com.nexusgeographics.cercalia.maps.MapController
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentDispatchReceivedMapBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.entities.BravoConfigurationVariableEntity
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.map.DispatchMapRenderer
import ifac.td.taxi.viewmodel.DispatchReceivedViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.model.DispatchReceivedModel
import ifac.td.taxi.viewmodel.model.UtilsModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.showButtonCancelDispatch
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toBoolean1or0
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class DispatchReceivedMapFragment :
    BaseFragment<FragmentDispatchReceivedMapBinding, DispatchReceivedViewModel>(
        R.layout.fragment_dispatch_received_map
    ) {

    private val vModel: DispatchReceivedViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModels()
    private val safeArgs: DispatchReceivedMapFragmentArgs by navArgs()

    private val TAG = "DispatchReceivedMapFragment"

    private var mapView: CercaliaMapView? = null
    private var mapController: MapController? = null

    private var renderer: DispatchMapRenderer? = null

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentDispatchReceivedMapBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        iMainActivity.showBottomBar(false)
        vModel.getDispatch(safeArgs.idDispatch)
        setButtons()

        try {
            mapView = vBinding.map

            if (mapController == null) {
                mapView?.let {
                    mapController = MapController.getInstance(requireActivity(), it)
                }
            }
        } catch (e: Exception) {
            Logs.e(TAG, "setupComponents: ${e.message}")
            iMainActivity.showToast(R.string.toast_error_mostrar_mapa)
        }

        vModel.checkCancelDialogPermission()
        vModel.checkIsDirRecogidaAceptaDespacho()

        iMainActivity.closeDialog(CustomDialog.CustomDialogTAG.LOCATE_ON_STAND_DIALOG)
    }

    override fun onResume() {
        super.onResume()
        runCatching { mapView?.onResume() }
            .onFailure { error ->
                Logs.e(TAG, "runCatching.onFailure onResume: ${error.message}")
                iMainActivity.showToast(R.string.toast_error_mostrar_mapa)
            }
    }

    override fun onPause() {
        vBinding.btnAccept.stopTimer()
        vBinding.btnCancel.stopTimer()
        runCatching { mapView?.onPause() }
            .onFailure { error ->
                Logs.e(TAG, "runCatching.onFailure onPause: ${error.message}")
                iMainActivity.showToast(R.string.toast_error_mostrar_mapa)
            }
        super.onPause()
    }

    override fun onDestroyView() {

        vBinding.btnAccept.stopTimer()
        vBinding.btnCancel.stopTimer()
        runCatching {
            vBinding.map.removeCallbacks(null)
            renderer?.setMapNotReady()
            mapView?.onDestroy()
        }.onFailure { error ->
            Logs.e(TAG, "runCatching.onFailure onDestroy: ${error.message}")
            iMainActivity.showToast(R.string.toast_error_mostrar_mapa)
        }
        mapView = null
        mapController = null
        renderer = null
        super.onDestroyView()
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                iMainActivity.acceptDispatch(safeArgs.idDispatch)
            }

            btnCancel.setAction {
                iMainActivity.rejectDispatch(safeArgs.idDispatch)
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.dispatchFlow.collect {
                        it?.let { dispatch ->
                            setUpDispatchData(dispatch, vModel.bravoConfigurationLoginFlow.value)
                        }
                    }
                }

                launch {
                    vModel.showDialogOnCancelFlow.collect {
                        if (it) {
                            vBinding.btnCancel.setAction {
                                val callBack: (CustomDialog.CustomDialogResponse) -> Unit =
                                    { response ->
                                        if (response.buttonPressed == ButtonType.ACCEPT) {
                                            iMainActivity.rejectDispatch(safeArgs.idDispatch)
                                        }
                                    }

                                iMainActivity.openDialog(
                                    CustomDialog.CustomDialogModel(
                                        title = getString(R.string.warning),
                                        description = getString(R.string.dialog_reject_dispatch_desc),
                                        buttons = arrayListOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                                    ),
                                    callBack,
                                    fragmentManager = childFragmentManager
                                )
                            }
                        }
                    }
                }

                launch {
                    vModel.bravoConfigurationLoginFlow.collect {
                        it?.let {
                            setUpDispatchData(vModel.dispatchFlow.value, it)
                        }
                    }
                }

                launch {
                    sharedViewModel.buttonTimerState.collect { timerState ->
                        if (timerState != null) {
                            when (timerState.buttonType) {
                                ButtonType.ACCEPT -> {
                                    vBinding.btnAccept.showTimerButton(
                                        maxSeconds = timerState.maxSeconds,
                                        leftSeconds = timerState.remainingSeconds,
                                        countdownStartTime = timerState.startTime
                                    )
                                    vBinding.btnCancel.stopTimer()
                                    vBinding.btnCancel.hideTimerButton()

                                }

                                ButtonType.CANCEL -> {
                                    vBinding.btnCancel.showTimerButton(
                                        maxSeconds = timerState.maxSeconds,
                                        leftSeconds = timerState.remainingSeconds,
                                        countdownStartTime = timerState.startTime
                                    )
                                    vBinding.btnAccept.stopTimer()
                                    vBinding.btnAccept.hideTimerButton()
                                }

                                else -> {}
                            }
                        } else {
                            vBinding.btnAccept.stopTimer()
                            vBinding.btnAccept.hideTimerButton()
                            vBinding.btnCancel.stopTimer()
                            vBinding.btnCancel.hideTimerButton()
                        }
                    }
                }

                launch {
                    sharedViewModel.disableDispatchButtonFlow.collect {
                        vBinding.btnCancel.setAction {}
                        vBinding.btnAccept.setAction {}
                        vBinding.btnCancel.setButtonStyle(CustomButton.StyleButton.DISABLE)
                        vBinding.btnAccept.setButtonStyle(CustomButton.StyleButton.DISABLE)
                    }
                }
            }
        }

    }

    var viewIsLoaded = false

    private fun setUpDispatchData(dispatch: DispatchReceivedModel?, configuration: BravoConfigurationVariableEntity?) {
        if (dispatch == null || configuration == null) {
            Logs.d(TAG, "setUpDispatchData not succeed. Dispatch is null ${dispatch == null}. Configuration is null ${configuration == null}")
            return
        }

        if (viewIsLoaded) {
            Logs.d(TAG, "setUpDispatchData view is already loading")
            return
        }

        viewIsLoaded = true

        Logs.d(TAG, "setUpDispatchData succeed - Setting up data to views")

        //Iniciar el mapa cuando el dispatchFlow esté disponible
        launchMap(dispatch)

        if (configuration.showPickupTime) {
            dispatch.pickUpTime?.let { time ->
                vBinding.txHour.visibility = View.VISIBLE
                vBinding.txHour.text = time
            }
        }

        when (dispatch.canReject?.showButtonCancelDispatch()) {
            UtilsModel.Companion.NOT_ACCEPT_DISPATCH.NOT_SHOW -> {
                vBinding.btnCancel.setButtonStyle(CustomButton.StyleButton.DISABLE)
                vBinding.btnCancel.changeBackground(CustomButton.BackgroundButtonColor.DEFAULT)
            }
            UtilsModel.Companion.NOT_ACCEPT_DISPATCH.SHOW_RED -> {
                vBinding.btnCancel.setButtonStyle(CustomButton.StyleButton.ENABLE)
                vBinding.btnCancel.changeBackground(CustomButton.BackgroundButtonColor.RED)
            }
            UtilsModel.Companion.NOT_ACCEPT_DISPATCH.SHOW_BLUE -> {
                vBinding.btnCancel.setButtonStyle(CustomButton.StyleButton.ENABLE)
                vBinding.btnCancel.changeBackground(CustomButton.BackgroundButtonColor.BLUE)
            }
            null -> {
                vBinding.btnCancel.setButtonStyle(CustomButton.StyleButton.DISABLE)
                vBinding.btnCancel.changeBackground(CustomButton.BackgroundButtonColor.DEFAULT)
            }
        }

        if (dispatch.autoAccept?.toBoolean1or0() == true) {
            vBinding.btnCancel.setButtonStyle(CustomButton.StyleButton.DISABLE)
            vBinding.btnAccept.setAction {
                iMainActivity.cancelAutoAcceptDispatch()
                iMainActivity.acceptDispatch(safeArgs.idDispatch)
            }
            iMainActivity.autoAcceptDispatch(safeArgs.idDispatch)
        }

        when (configuration.showCustomerName) {
            1 -> {
                dispatch.passengerName?.let { passagerName ->
                    vBinding.txPassengerName.text = passagerName
                    vBinding.txPassengerName.visibility = View.VISIBLE
                }
            }

            2 -> {
                dispatch.passengerName?.let { passagerName ->
                    dispatch.subscriberUserName?.let { subscriberUserName ->
                        vBinding.txPassengerName.visibility = View.VISIBLE
                        if (subscriberUserName.isNotBlank()) {
                            vBinding.txPassengerName.text =
                                "$passagerName - $subscriberUserName"
                        } else {
                            vBinding.txPassengerName.text = passagerName
                        }
                    }
                }
            }
        }
        if (!dispatch.extra.isNullOrBlank()) {
            vBinding.txExtra.text = dispatch.extra
            vBinding.txExtra.visibility = View.VISIBLE
        }

        Logs.d(TAG, "setUpDispatchData showing destination? (externalinfo ${dispatch.externalInfo}, (from dispatch: ${dispatch.showDropOff}, from configuration (IGNORED AFTER 3.1.2 AND ONLY USING FROM DISPATCH): ${configuration.showDestination}) ")
        if (dispatch.externalInfo.isNotBlank()) {
            vBinding.txDestination.text = dispatch.externalInfo
            vBinding.txDestination.visibility = View.VISIBLE
        } else if (dispatch.showDropOff) {
            dispatch.dropOffAddress?.let { dropOff ->
                vBinding.txDestination.text = dropOff
                vBinding.txDestination.visibility = View.VISIBLE
            }
        }

        if (vModel.showPickupAddress()) {
            if (dispatch.smallPickUpAdress != null && dispatch.smallPickUpAdress?.isNotBlank() == true) {
                vBinding.txSimpleAdress.text = dispatch.smallPickUpAdress
                vBinding.txSimpleAdress.visibility = View.VISIBLE
            }

            if (dispatch.city != null && dispatch.city?.isNotBlank() == true) {
                vBinding.txCity.text = dispatch.city
                vBinding.txCity.visibility = View.VISIBLE
            }
        }

        if (dispatch.pickUpZone != null && dispatch.pickUpZone?.isNotBlank() == true) {
            vBinding.txPickUpZone.text = dispatch.pickUpZone
            vBinding.txPickUpZone.visibility = View.VISIBLE
        }

        dispatch.observations?.let { observationList ->
            vModel.getDispatchProvider(dispatch.id, observationList)
        }

        Logs.d(TAG, "setUpDispatchData view loaded")
    }

    private fun launchMap(dispatch: DispatchReceivedModel) {
        try {
            mapView?.visibility = View.VISIBLE
            vBinding.imgNoMap.visibility = View.GONE
            Logs.d(TAG, "launchMap: Map view set to visible")

            renderer?.let { render ->
                val rendered = render.renderDispatch(
                    dispatch = dispatch,
                    showPickupAddress = vModel.showPickupAddress(),
                    showStreetNumber = dispatch.showstreetNumber == "1"
                )
                if (!rendered) {
                    mapView?.visibility = View.GONE
                    vBinding.imgNoMap.visibility = View.VISIBLE
                    Logs.e(TAG, "launchMap: Nothing to render -> hiding map")
                }
                return
            }

            mapController?.setOnMapReadyCallback { map ->
                Logs.d(TAG, "launchMap: onMapReady called")

                val mapView = mapView
                if (mapView == null) {
                    Logs.e(TAG, "launchMap: mapView is null on onMapReady")
                    return@setOnMapReadyCallback
                }

                renderer = DispatchMapRenderer(requireContext(), map, mapView).also { it.setMapReady() }

                val rendered = renderer?.renderDispatch(
                    dispatch = dispatch,
                    showPickupAddress = vModel.showPickupAddress(),
                    showStreetNumber = dispatch.showstreetNumber == "1"
                )

                if (rendered == false) {
                    mapView.visibility = View.GONE
                    vBinding.imgNoMap.visibility = View.VISIBLE
                    Logs.e(TAG, "launchMap: Nothing to render -> hiding map")
                }
            } ?: Logs.e(TAG, "launchMap: mapController is null, cannot set callback")

        } catch (e: Exception) {
            Logs.e(TAG, "launchMap: Exception initializing map -> ${e.message}")
            iMainActivity.showToast(R.string.toast_error_mostrar_mapa)
        }
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            //Don't let the user to navigate back
        }
    }
}