package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.framework.sdk.usecase.NavigatorUseCase
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DestinationMapViewModel(
    private val dispatchUseCase: DispatchUseCase,
    private val navigatorUseCase: NavigatorUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "DestinationMapViewModel"

    private val _navigatorFlow = MutableSharedFlow<Intent?>()
    val navigatorFlow = _navigatorFlow.asSharedFlow()

    fun openNavigatorApp(coordinates: String, street: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "openNavigatorApp: Coordinates = $coordinates, Street = $street")

            val navigatorIntent = when {
                !coordinates.isNullOrEmpty() && !street.isNullOrEmpty() -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with coordinates ($coordinates) and street: $street")
                    navigatorUseCase.openNavigatorWithCoordinatesAndStreetName(coordinates, street)
                }
                !coordinates.isNullOrEmpty() -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with coordinates ($coordinates)")
                    navigatorUseCase.openNavigatorWithCoordinates(coordinates)
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

            _navigatorFlow.emit(navigatorIntent)
        }
    }
}