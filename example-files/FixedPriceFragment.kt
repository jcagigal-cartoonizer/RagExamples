package ifac.td.taxi.ui.screen

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.licensing.models.zoning.LatLong
import com.nexusgeographics.cercalia.maps.CameraUpdateFactory
import com.nexusgeographics.cercalia.maps.CercaliaMapView
import com.nexusgeographics.cercalia.maps.MapController
import com.nexusgeographics.cercalia.maps.features.Marker
import com.nexusgeographics.cercalia.maps.model.LatLng
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentFixedPriceMapBinding
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.utils.GeoUtils
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.rest.bravoRest.SuggestModel
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.SuggestionsAdapter
import ifac.td.taxi.viewmodel.FixedPriceViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toLatLng
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class FixedPriceMapFragment :
    BaseFragment<FragmentFixedPriceMapBinding, FixedPriceViewModel>(
        R.layout.fragment_fixed_price_map
    ) {

    private val vModel: FixedPriceViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private val TAG = "FixedPriceMapFragment"

    private lateinit var mapView: CercaliaMapView
    private var mapController: MapController? = null

    private var mapReady = false

    private var isChangingFromMapClick = false

    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    private val suggestionAdapter: SuggestionsAdapter? by lazy {
        val click: (SuggestModel) -> Unit = { itemClick ->
            submitList(emptyList())
            isChangingFromMapClick = true
            vModel.marker?.let {
                mapController?.removeFeature(it)
            }
            vModel.marker = null

            vModel.updateSelectedDropOff(itemClick)
        }

        SuggestionsAdapter(requireContext(), click)
    }

    private fun getCoordinatesFromString(input: String?): LatLng? {
        var latlng: LatLng? = null
        if (input == null) return null
        try {
            val data = input.split(",")
            latlng = LatLng(data[0], data[1])
        } catch (e: Exception) {
            Logs.d(TAG, "getCoordinatesFromString: $e")
        }
        return latlng
    }

    override fun getViewModel() = vModel

    override fun getViewBinding() = FragmentFixedPriceMapBinding.inflate(layoutInflater)

    override fun setupComponents() {
        //requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        iMainActivity.showHeader(true)
        setButtons()
        setUpMap()
        iMainActivity.requestedOrientationCustom(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        iMainActivity.showBottomBar(false)

        bottomSheetBehavior = BottomSheetBehavior.from(vBinding.cvEstimatePrice as View)
        setBottomSheetBehaviorListener()
        //bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

        if (sharedViewModel.tripFlow.value?.fromDispatch == true && (sharedViewModel.dispatchFlow.value?.dropOffCoordinates?.size ?: 0) > 0) {
            sendFixedPriceRequest()
            bottomSheetBehavior?.isDraggable = false
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            vBinding.ivClose.visibility = View.GONE
            vBinding.lytSuggest.visibility = View.GONE
            vBinding.tvDropOff.text = sharedViewModel.dispatchFlow.value?.destinyAdress?.last() ?: ""
        }
    }

    private fun setBottomSheetBehaviorListener() {
        bottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {

                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {
                        vModel.updateSelectedDropOff(null)
                        vModel.resetFixedPriceFlowFromDB()
                    }

                    else -> {
                        Logs.d(TAG, "onStateChanged: not defined")
                    }
                }
            }
        })
    }

    private fun setUpMap() {
        try {
            mapView = vBinding.map
            mapController = MapController.getInstance(requireActivity(), mapView)
            //mapController.isMyLocationEnable = true
            //val manager = com.nexusgeographics.cercalia.maps.location.LocationManager.getInstance(this.activity)
            //manager.enable = true
            mapController?.setOnMapReadyCallback {
                mapReady = true
                //mapController.isMyLocationEnable = true
                /*
                mapController.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            W2CLocation.getLastLatitude(),
                            W2CLocation.getLastLongitude()
                        ), 17f
                    )
                )
                 */
                val trip = sharedViewModel.tripFlow.value
                if (trip?.fromDispatch == true) {
                    vModel.getDispatchDestinationCoordinates(sharedViewModel.dispatchFlow.value)
                } else if (trip != null) {
                    setupMap(LatLong(trip.latitudePickup, trip.longitudePickup))
                } else {
                    setupMap(LatLong(W2CLocation.getLastLatitude(), W2CLocation.getLastLongitude()))
                }
                /*
                mapController.addOnMapClickListener { item ->
                    isChangingFromMapClick = true
                    marker?.let {
                        mapController.removeFeature(it)
                    }
                    marker =
                        Marker(requireContext(), "My Marker").setCoordinate(
                            LatLng(
                                item.lat,
                                item.lng
                            )
                        )
                    marker?.let {
                        mapController.putFeature(it)
                    }
                    startLoading()
                    vModel.getLocationStreetData(item.lat, item.lng)
                    true
                }
                 */

            }

        } catch (e: Exception) {
            Logs.e(TAG, "setupComponents: ${e.message}")
            iMainActivity.showToast(R.string.toast_error_mostrar_mapa)
        }
    }

    override fun onResume() {
        super.onResume()
        runCatching { mapView.onResume() }
            .onFailure { error ->
                Logs.e(TAG, "runCatching.onFailure onResume: ${error.message}")
                iMainActivity.showToast(R.string.toast_error_mostrar_mapa)
            }
    }

    override fun onPause() {
        super.onPause()
        vModel.marker?.let { marker ->
            mapController?.removeFeature(marker)
        }
        vModel.marker = null
        mapController = null
        mapReady = false
        runCatching { mapView.onDestroy() }
            .onFailure { error ->
                Logs.e(TAG, "runCatching.onFailure onPause: ${error.message}")
                iMainActivity.showToast(R.string.toast_error_mostrar_mapa)
            }
    }

    override fun onDestroyView() {
        mapView.onPause()
        runCatching { mapView.onDestroy() }
            .onFailure { error ->
                Logs.e(TAG, "runCatching.onFailure onDestroy: ${error.message}")
                iMainActivity.showToast(R.string.toast_error_mostrar_mapa)
            }
        mapController = null
        super.onDestroyView()
    }

    private fun setButtons() {
        vBinding.apply {
            vBinding.ivClose.setOnClickListener {
                vModel.updateSelectedDropOff(null)
            }


            etManual.doOnTextChanged { text, start, before, count ->
                if (isChangingFromMapClick) {
                    isChangingFromMapClick = false
                    return@doOnTextChanged
                }

                if (text.isNullOrBlank() || text.length < 3) {
                    submitList(emptyList())
                    return@doOnTextChanged
                }

                val isPoiSearch = vBinding.rbPOI.isChecked

                vModel.suggestAddresses(text.toString(), isPoiSearch)
            }
        }
    }

    private fun sendFixedPriceRequest(dropOffData: SuggestModel? = null) {
        if (sharedViewModel.tripFlow.value == null) {
            //From Vacant
            dropOffData?.let { suggest ->
                vModel.getFixedPriceInVacant(suggest)
            }
        } else {
            sharedViewModel.tripFlow.value?.let {
                if (it.fromDispatch) {
                    sharedViewModel.dispatchFlow.value?.let { dispatch ->
                        vModel.getFixedPriceFromDispatch(it, dispatch, dropOffData)
                    }
                } else {
                    dropOffData?.let { suggest ->
                        vModel.getFixedPriceFromTrip(it, suggest)
                    }
                }
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.suggestCallbackFlow.collect {
                        submitList(it)
                    }
                }
                launch {
                    vModel.selectDropOffFlow.collect {
                        manageSelectedDropOff(it)
                    }
                }
                launch {
                    vModel.errorCall.collect {
                        if (it == true) {
                            vBinding.ivVanError.visibility = View.VISIBLE
                            vBinding.ivBusinessError.visibility = View.VISIBLE
                            vBinding.ivNormalTaxiError.visibility = View.VISIBLE
                            vBinding.pbBusiness.visibility = View.GONE
                            vBinding.pbNormalTaxi.visibility = View.GONE
                            vBinding.pbVan.visibility = View.GONE
                            vBinding.tvPriceVan.text = ""
                            vBinding.tvPriceBusiness.text = ""
                            vBinding.tvNormalTaxiPrice.text = ""
                        } else {
                            vBinding.ivVanError.visibility = View.GONE
                            vBinding.ivBusinessError.visibility = View.GONE
                            vBinding.ivNormalTaxiError.visibility = View.GONE
                        }
                    }
                }
                launch {
                    vModel.fixedPriceData.collect {
                        it?.let { response ->
                            response.prices.forEach { price ->
                                val changingView = when (price.serviceType) {
                                    "taxi" -> {
                                        vBinding.pbNormalTaxi.visibility = View.GONE
                                        vBinding.tvNormalTaxiPrice
                                    }

                                    "largeCar" -> {
                                        vBinding.pbVan.visibility = View.GONE
                                        vBinding.tvPriceVan
                                    }

                                    "business" -> {
                                        vBinding.pbBusiness.visibility = View.GONE
                                        vBinding.tvPriceBusiness
                                    }

                                    else -> null
                                }
                                changingView?.visibility = View.VISIBLE
                                changingView?.text = price.finalPrice.toString()
                            }
                        }
                    }
                }

                launch {
                    vModel.dispatchDestinationCoordinates.collect {
                        val hasDestination = it.first != null
                        setupMap(LatLong(it.second.lat, it.second.lng), !hasDestination)
                        if (hasDestination) {
                            setUpMapDropOff(it.first!!, null, it.second)
                        }
                    }
                }
                launch {
                    vModel.fixedPriceDataFromDB.collect {
                        it?.let { data ->
                            if (data.first != null) {
                                vBinding.tvNormalTaxiPrice.text = data.first.toString()
                                vBinding.tvNormalTaxiPrice.visibility = View.VISIBLE
                                vBinding.pbNormalTaxi.visibility = View.GONE
                            } else {
                                vBinding.tvNormalTaxiPrice.text = ""
                                vBinding.ivNormalTaxiError.visibility = View.VISIBLE
                            }

                            if (data.second != null) {
                                vBinding.tvPriceVan.text = data.second.toString()
                                vBinding.tvPriceVan.visibility = View.VISIBLE
                                vBinding.pbVan.visibility = View.GONE
                            } else {
                                vBinding.tvPriceVan.text = ""
                                vBinding.ivVanError.visibility = View.VISIBLE
                            }

                            if (data.third != null) {
                                vBinding.tvPriceBusiness.text = data.third.toString()
                                vBinding.tvPriceBusiness.visibility = View.VISIBLE
                                vBinding.pbBusiness.visibility = View.GONE
                            } else {
                                vBinding.tvPriceBusiness.text = ""
                                vBinding.ivBusinessError.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun manageSelectedDropOff(selectedModel: SuggestModel?) {
        if (selectedModel == null) {
            if (sharedViewModel.tripFlow.value?.fromDispatch == false || sharedViewModel.tripFlow.value == null || (sharedViewModel.dispatchFlow.value?.dropOffCoordinates?.size ?: 0) <= 0) {
                if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                }

                resetFixedPriceData()
            }
        } else {
            hideKeyboard(requireActivity())
            getCoordinatesFromString(selectedModel.coord)?.let { coordinates ->
                val trip = sharedViewModel.tripFlow.value

                //Enviar peticion API Khonsu.
                sendFixedPriceRequest(selectedModel)

                setUpMapDropOff(coordinates, trip)
                vBinding.etManual.setText(selectedModel.getPrintableStreetTextShort())
                vBinding.tvDropOff.text = selectedModel.getPrintableStreetTextShort()
            }

            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun setUpMapDropOff(
        coordinates: LatLng,
        trip: Trip?,
        coordinatesPickUp: LatLng? = null,
    ) {
        if (vModel.marker == null) {
            vModel.marker =
                Marker(
                    requireContext(),
                    "DropOff"
                ).setCoordinate(coordinates)
            vModel.marker?.let {
                mapController?.putFeature(it)
            }
        }

        val tripLatitude: Double = if (trip != null) {
            trip.latitudePickup ?: 0.0
        } else W2CLocation.getLastLatitude()


        val tripLongitude: Double = if (trip != null) {
            trip.longitudePickup ?: 0.0
        } else W2CLocation.getLastLongitude()

        val midPoint = GeoUtils.calculateMidpoint(
            coordinates.lat,
            coordinates.lng,
            tripLatitude,
            tripLongitude
        )
        if (mapReady == false) {
            return
        }
        mapController?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    midPoint.first,
                    midPoint.second
                ),
                GeoUtils.calculateZoomLevel(
                    coordinates.lat,
                    coordinates.lng,
                    tripLatitude,
                    tripLongitude
                )
            )
        )
    }

    private fun resetFixedPriceData() {
        vBinding.tvPriceVan.visibility = View.GONE
        vBinding.tvPriceVan.text = ""
        vBinding.tvPriceBusiness.visibility = View.GONE
        vBinding.tvPriceBusiness.text = ""
        vBinding.tvNormalTaxiPrice.visibility = View.GONE
        vBinding.tvNormalTaxiPrice.text = ""
        vBinding.pbVan.visibility = View.VISIBLE
        vBinding.pbNormalTaxi.visibility = View.VISIBLE
        vBinding.pbBusiness.visibility = View.VISIBLE
        vBinding.tvDropOff.text = ""
        vBinding.etManual.setText("")
        vModel.resetErrorFlow()
    }

    private fun submitList(data: List<SuggestModel>) {
        try {
            if (vBinding.rvManualSuggestions.adapter == null) {
                Logs.d(TAG, "initZoneAdapter: initAdapter")

                val mLayoutManager = object : LinearLayoutManager(requireContext()) {
                    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
                        try {
                            super.onLayoutChildren(recycler, state)
                        } catch (e: IndexOutOfBoundsException) {
                            Logs.d(TAG, "onLayoutChildren: IndexOutOfBoundsException caught")
                        }
                    }
                }

                vBinding.rvManualSuggestions.apply {
                    adapter = suggestionAdapter
                    layoutManager = mLayoutManager
                }
            }

            if (data.isEmpty()) {
                vBinding.rvManualSuggestions.visibility = View.GONE
            } else {
                vBinding.rvManualSuggestions.visibility = View.VISIBLE
            }
            val dataSorted = data.sortedByDescending { it.score }.toList()
            suggestionAdapter?.submitList(dataSorted)
        } catch (e: Exception) {
            Logs.d(TAG, "initSuggestionAdapter: $e")
        }
    }

    private fun setupMap(pickUp: LatLong, moveCamera: Boolean = true) {
        try {
            val marker: Marker =
                Marker(requireContext(), "PickUp").setCoordinate(pickUp.toLatLng())
            marker.setDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_notification_png
                )
            )
            if (moveCamera) {
                mapController?.moveCamera(CameraUpdateFactory.newLatLngZoom(pickUp.toLatLng(), 17f))
            }
            mapController?.putFeature(marker)
        } catch (e: Exception) {
            Logs.e(TAG, "try-catch setupMap: ${e.message}")
        }
    }

    fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: View(activity) // Get current focused view or create one
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}