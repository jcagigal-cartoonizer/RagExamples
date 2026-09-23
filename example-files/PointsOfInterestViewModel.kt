package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.rest.poi.response.Poi
import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
import com.interfacom.sdk.taximeter.licensing.rest.pois.POISDataModule
import com.interfacom.sdk.taximeter.licensing.rest.pois.POISListener
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.domain.usecase.LocationUseCase
import ifac.td.taxi.framework.sdk.usecase.NavigatorUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.ArrayList

class PointsOfInterestViewModel(
    context: Application,
    private val navigatorUseCase: NavigatorUseCase,
    private val locationUseCase: LocationUseCase
) :
    BaseViewModel(context) {

    private val _poiFlow = MutableSharedFlow<List<Poi>>()
    val poiFlow = _poiFlow.asSharedFlow()

    private val _locationOnHiredSentFlow = MutableSharedFlow<Zone?>()
    val locationOnHiredSentFlow = _locationOnHiredSentFlow.asSharedFlow()

    private val _navigatorFlow = MutableSharedFlow<Intent?>()
    val navigatorFlow = _navigatorFlow.asSharedFlow()

    val TAG = "PointsOfInterestViewModel"

    fun getPOIs(keyword: String) {
        viewModelScope.launch {
            val poiListener = object : POISListener {
                override fun getPOISSuccess(poiList: ArrayList<Poi>?) {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (poiList != null) {
                            Logs.d(TAG, "getPOISSuccess: $poiList")
                            _poiFlow.emit(poiList.toList())
                        }
                    }
                }

                override fun getPOISFailure() {
                    Logs.e(TAG, "Failed getting POI list")
                }
            }

            POISDataModule.providePOISPresenter(context, poiListener).getPois(keyword)
        }
    }

    fun openNavigatorApp(latitude: Double?, longitude: Double?, street: String? = null) {
        viewModelScope.launch(Dispatchers.Main) {
            Logs.d(TAG, "openNavigatorApp: Received params -> Latitude: $latitude, Longitude: $longitude, Street: $street")

            val navigatorIntent = when {
                latitude != null && longitude != null && !street.isNullOrEmpty() -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with coordinates ($latitude, $longitude) and street: $street")
                    navigatorUseCase.openNavigatorWithCoordinatesAndStreetName("$latitude,$longitude", street)
                }
                latitude != null && longitude != null -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with coordinates ($latitude, $longitude)")
                    navigatorUseCase.openNavigatorWithCoordinates("$latitude,$longitude")
                }
                !street.isNullOrEmpty() -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with street: $street")
                    navigatorUseCase.openNavigatorApp(street)
                }
                else -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator without specific parameters")
                    navigatorUseCase.openNavigatorApp()
                }
            }

            Logs.d(TAG, "openNavigatorApp: Emitting navigator intent: $navigatorIntent")
            _navigatorFlow.emit(navigatorIntent)
        }
    }





    fun locateOnHired(zone: Zone?) {
        viewModelScope.launch(Dispatchers.IO) {
            zone?.let { selectedZone ->
                var idMacroZone: Int? = null
                var index = 0
                do {
                    val hasTheZone =
                        W2CLocation.getZoning().macrozones[index].zones.find { zone -> selectedZone.idZone == zone.idZone && selectedZone.nombreZone == zone.nombreZone }
                    hasTheZone?.let {
                        idMacroZone = W2CLocation.getZoning().macrozones[index].idMacrozone
                    }
                    index++

                } while (idMacroZone == null && index < W2CLocation.getZoning().macrozones.size)

                idMacroZone?.let {
                    locationUseCase.locateOnHired(it, selectedZone.idZone)
                    _locationOnHiredSentFlow.emit(selectedZone)
                }
            }
        }
    }


}