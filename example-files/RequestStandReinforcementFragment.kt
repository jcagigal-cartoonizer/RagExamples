package ifac.td.taxi.ui.screen

import android.view.View
import androidx.navigation.fragment.navArgs
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentRequestStandReinforcementBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.RequestStandReinforcementViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RequestStandReinforcementFragment :
    BaseFragment<FragmentRequestStandReinforcementBinding, RequestStandReinforcementViewModel>(R.layout.fragment_request_stand_reinforcement) {

    private val vModel: RequestStandReinforcementViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()
    private val safeArgs: RequestStandReinforcementFragmentArgs by navArgs()

    override fun getViewModel(): RequestStandReinforcementViewModel {
        return vModel
    }

    override fun getViewBinding(): FragmentRequestStandReinforcementBinding {
        return FragmentRequestStandReinforcementBinding.inflate(layoutInflater)
    }

    override fun setupComponents() {
        iMainActivity.showHeader(false)
        setButtons()
    }

    override fun configureBottomBarVisibility() {
        iMainActivity.showBottomBar(false)
    }

    private fun setButtons() {
        vBinding.apply {
            btnCancel.setAction {
                iMainActivity.navigateBack()
            }

            btnAddFavourites.setAction {
                vModel.addZoneToFavourites(safeArgs.idMacrozone, safeArgs.idZone)
                iMainActivity.navigateBack()
            }

            btnRemoveFavourites.setAction {
                vModel.removeZoneFromFavourites(safeArgs.idMacrozone, safeArgs.idZone)
                iMainActivity.navigateBack()
            }

            if (safeArgs.isInFavourites) {
                btnRemoveFavourites.visibility = View.VISIBLE
                btnAddFavourites.visibility = View.GONE
            } else {
                btnRemoveFavourites.visibility = View.GONE
                btnAddFavourites.visibility = View.VISIBLE
            }

            btnReinforcement0.setAction {
                vModel.sendReinforcementRequest(safeArgs.idMacrozone, safeArgs.idZone, 0)
                iMainActivity.showToast(R.string.sending)
                iMainActivity.navigateBack()
            }

            btnReinforcement1.setAction {
                vModel.sendReinforcementRequest(safeArgs.idMacrozone, safeArgs.idZone, 1)
                iMainActivity.showToast(R.string.sending)
                iMainActivity.navigateBack()
            }

            btnReinforcement2.setAction {
                vModel.sendReinforcementRequest(safeArgs.idMacrozone, safeArgs.idZone, 2)
                iMainActivity.showToast(R.string.sending)
                iMainActivity.navigateBack()
            }

            btnReinforcement3.setAction {
                vModel.sendReinforcementRequest(safeArgs.idMacrozone, safeArgs.idZone, 3)
                iMainActivity.showToast(R.string.sending)
                iMainActivity.navigateBack()
            }

            btnReinforcement4.setAction {
                vModel.sendReinforcementRequest(safeArgs.idMacrozone, safeArgs.idZone, 4)
                iMainActivity.showToast(R.string.sending)
                iMainActivity.navigateBack()
            }

            btnReinforcement5.setAction {
                vModel.sendReinforcementRequest(safeArgs.idMacrozone, safeArgs.idZone, 5)
                iMainActivity.showToast(R.string.sending)
                iMainActivity.navigateBack()
            }

            btnReinforcement10.setAction {
                vModel.sendReinforcementRequest(safeArgs.idMacrozone, safeArgs.idZone, 10)
                iMainActivity.showToast(R.string.sending)
                iMainActivity.navigateBack()
            }

            btnReinforcement15.setAction {
                vModel.sendReinforcementRequest(safeArgs.idMacrozone, safeArgs.idZone, 15)
                iMainActivity.showToast(R.string.sending)
                iMainActivity.navigateBack()
            }

            btnReinforcement20.setAction {
                vModel.sendReinforcementRequest(safeArgs.idMacrozone, safeArgs.idZone, 20)
                iMainActivity.showToast(R.string.sending)
                iMainActivity.navigateBack()
            }

            btnReinforcement25.setAction {
                vModel.sendReinforcementRequest(safeArgs.idMacrozone, safeArgs.idZone, 25)
                iMainActivity.showToast(R.string.sending)
                iMainActivity.navigateBack()
            }

        }
    }
}