package ifac.td.taxi.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.rest.poi.response.Poi
import com.interfacom.sdk.taximeter.bravocomm.rest.tsab_rest.FixedPriceCallback
import com.interfacom.sdk.taximeter.bravocomm.rest.tsab_rest.fixed_price.PriceEstimationDTO
import com.interfacom.sdk.taximeter.licensing.rest.pois.POISDataModule
import com.interfacom.sdk.taximeter.licensing.rest.pois.POISListener
import com.nexusgeographics.cercalia.maps.features.Marker
import com.nexusgeographics.cercalia.maps.model.LatLng
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.BravoRestApiUseCase
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.FixedPriceUseCase
import ifac.td.taxi.domain.usecase.LocationUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.rest.bravoRest.SuggestModel
import ifac.td.taxi.ui.util.CoordenatesUtil
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FixedPriceViewModel(
    private val locationUseCase: LocationUseCase,
    private val fixedPriceUseCase: FixedPriceUseCase,
    private val cercaliaUseCase: BravoRestApiUseCase,
    private val dispatchUseCase: DispatchUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "FixedPriceViewModel"

    private val _suggestCallbackFlow = MutableStateFlow<List<SuggestModel>>(emptyList())
    val suggestCallbackFlow = _suggestCallbackFlow.asStateFlow()

    private val _selectedDropOffFlow = MutableStateFlow<SuggestModel?>(null)
    val selectDropOffFlow = _selectedDropOffFlow.asStateFlow()

    private val _fixedPriceData = MutableStateFlow<PriceEstimationDTO?>(null)
    val fixedPriceData = _fixedPriceData.asStateFlow()

    private val _errorCall = MutableStateFlow<Boolean?>(null)
    val errorCall = _errorCall.asStateFlow()

    //DROPOFF FIRST --- PICK UP SECOND
    private val _dispatchDestinationCoordinates = MutableSharedFlow<Pair<LatLng?, LatLng>>()
    val dispatchDestinationCoordinates = _dispatchDestinationCoordinates.asSharedFlow()

    private val _fixedPriceDataFromDB = MutableStateFlow<Triple<Double?, Double?, Double?>?>(null)
    val fixedPriceDataFromDB = _fixedPriceDataFromDB.asStateFlow()

    private var lastRequestedTrip: Trip? = null

    var marker: Marker? = null

    private val callback: suspend (List<SuggestModel>) -> Unit = {
        _suggestCallbackFlow.emit(it)
    }

    private val fixedPriceCallback = object : FixedPriceCallback {
        override fun onCompleted(response: PriceEstimationDTO?) {
            response?.let {
                viewModelScope.launch {
                    lastRequestedTrip?.let { trip ->
                        fixedPriceUseCase.saveFixedPriceData(trip, response)
                    }
                    _fixedPriceData.emit(response)
                    _errorCall.emit(false)
                    resetFixedPriceFlowFromDB()
                }
            } ?: run {
                Logs.e(TAG, "onCompleted: error")
            }
        }

        override fun onError(p0: String?) {
            viewModelScope.launch {
                _errorCall.emit(true)
            }
            Toast.makeText(context, p0, Toast.LENGTH_SHORT).show()
        }
    }

    private val localCallback: (Triple<Double?, Double?, Double?>) -> Unit = {
        viewModelScope.launch {
            _fixedPriceDataFromDB.emit(it)
        }
    }



    private val poiListener = object : POISListener {
        override fun getPOISFailure() {
            Logs.e(TAG, "getPOISFailure: ")
        }

        override fun getPOISSuccess(data: ArrayList<Poi>?) {
            viewModelScope.launch {
                data?.let { poiData ->
                    callback.invoke(poiData.map { SuggestModel(
                        it.poi,
                        it.locality,
                        it.municipalyity,
                        it.province,
                        it.street,
                        it.coord,
                        null,
                        0.0f,
                    ) })
                }
            }
        }
    }

/*
    fun getLocationStreetData(lat: Double, lon: Double){
        val callback =
            com.nexusgeographics.cercalia.maps.service.geocoder.Geocoder.ReverseGeocodingCallback { addresses, nativeResponse, error ->
                if (error) {
                    return@ReverseGeocodingCallback
                }
                val address = locationUseCase.getValidAddressData(addresses)
                val addressString = "${address.dropOffStreetName}, ${address.dropOffStreetNumber} ${address.dropOffCity}"

                viewModelScope.launch {
                    _locationStreetDataCallbackFlow.emit(addressString)
                    _locationStreetDataFlow.emit(address)
                }
            }
        viewModelScope.launch {
            locationUseCase.getAddressFromLatLon(LatLong(lat, lon), callback)
        }
    }

 */

    fun suggestAddresses(text: String, isPoiSearch: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isPoiSearch) {
                POISDataModule.providePOISPresenter(context, poiListener).getPois(text)
            } else {
                cercaliaUseCase.suggest(text, callback)
            }
        }
    }

    fun updateSelectedDropOff(itemClick: SuggestModel?) {
        viewModelScope.launch {
            _selectedDropOffFlow.emit(itemClick)
        }
    }

    fun resetFixedPriceFlowFromDB() {
        viewModelScope.launch {
            _fixedPriceDataFromDB.emit(null)
        }
    }

    fun getFixedPriceInVacant(suggest: SuggestModel) {
        viewModelScope.launch {
            fixedPriceUseCase.getFixedPriceInVacant(suggest, fixedPriceCallback, localCallback)
        }
    }

    fun getFixedPriceFromTrip(trip: Trip, suggest: SuggestModel) {
        viewModelScope.launch {
            lastRequestedTrip = trip
            fixedPriceUseCase.getFixedPriceFromTrip(trip, suggest, fixedPriceCallback, localCallback)
        }
    }

    fun getFixedPriceFromDispatch(trip: Trip, dispatch: InfoDispatchModel, suggest: SuggestModel?) {
        viewModelScope.launch {
            lastRequestedTrip = trip
            fixedPriceUseCase.getFixedPriceFromDispatch(
                trip,
                dispatch,
                suggest,
                fixedPriceCallback,
                localCallback
            )
        }
    }

    fun getDispatchDestinationCoordinates(value: InfoDispatchModel?) {
        viewModelScope.launch {
            value?.let {
                val dispatchWithAddresses = dispatchUseCase.getDispatchWithAddressById(value.id)
                dispatchWithAddresses?.let { data ->
                    val lastDestination = data.addresses?.findLast { !it.isPickup }
                    val pickUp = data.addresses?.find { it.isPickup }
                    if (lastDestination != null) {
                        lastDestination?.let { destination ->
                            val coordinates = CoordenatesUtil.parseCoordinatesFromLatLong(destination.coordenadas ?: "")
                            coordinates?.Lat?.let { latitude ->
                                coordinates.Lng?.let { longitude ->
                                    pickUp?.let { pick ->
                                        val pickUpCoordinates = CoordenatesUtil.parseCoordinatesFromLatLong(pick.coordenadas ?: "")
                                        pickUpCoordinates?.Lat?.let { pickUpLatitude ->
                                            pickUpCoordinates.Lng?.let { pickUpLongitude ->
                                                _dispatchDestinationCoordinates.emit(Pair(LatLng(latitude, longitude), LatLng(pickUpLatitude, pickUpLongitude)))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        pickUp?.let { pick ->
                            val pickUpCoordinates = CoordenatesUtil.parseCoordinatesFromLatLong(pick.coordenadas ?: "")
                            pickUpCoordinates?.Lat?.let { pickUpLatitude ->
                                pickUpCoordinates.Lng?.let { pickUpLongitude ->
                                    _dispatchDestinationCoordinates.emit(Pair(null, LatLng(pickUpLatitude, pickUpLongitude)))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun resetErrorFlow() {
        viewModelScope.launch {
            _errorCall.emit(null)
        }
    }
}
