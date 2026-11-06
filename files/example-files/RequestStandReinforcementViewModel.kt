package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import kotlinx.coroutines.launch

class RequestStandReinforcementViewModel(
    private val zoningUseCase: ZoningUseCase,
    context: Application
    ) : BaseViewModel(context) {


    fun sendReinforcementRequest(idMacrozone: Int, idZone: Int, numCustomers: Int) {
        viewModelScope.launch {
            zoningUseCase.sendReinforcementRequest(idMacrozone, idZone, numCustomers)
        }
    }

    fun addZoneToFavourites(idMacroZone: Int, idZone: Int) {
        viewModelScope.launch {
            zoningUseCase.addFavouriteZone(idMacroZone, idZone)
        }
    }

    fun removeZoneFromFavourites(idMacroZone: Int, idZone: Int) {
        viewModelScope.launch {
            zoningUseCase.removeFavouriteZone(idMacroZone, idZone)
        }
    }
}