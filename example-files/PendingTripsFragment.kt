package ifac.td.taxi.ui.screen

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.interfacom.sdk.taximeter.bravocomm.BravoCentral
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentPendingTripsBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.adapter.PendingTripsAdapter
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.PendingTripsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PendingTripsFragment : BaseFragment<FragmentPendingTripsBinding, PendingTripsViewModel>(
    R.layout.fragment_pending_trips
) {

    private val TAG = "PendingTripsFragment"

    private val vModel: PendingTripsViewModel by viewModel()
    private val sharedViewModel: MainActivityViewModel by activityViewModel()

    private lateinit var pendingTripsAdapter: PendingTripsAdapter

    val buttonsDialog = arrayListOf(
        ButtonType.CANCEL,
        ButtonType.ACCEPT
    )

    override fun getViewModel(): PendingTripsViewModel {
        return vModel
    }

    override fun getViewBinding(): FragmentPendingTripsBinding {
        return FragmentPendingTripsBinding.inflate(layoutInflater)
    }

    override fun setupComponents() {
        iMainActivity.showHeader(true)
    }

    override fun updateBackButton() {
        iMainActivity.customBackPressed {
            sharedViewModel.emitManualZoningNavigation(false)
            iMainActivity.navigateBack()
        }
    }

    override fun onResume() {
        super.onResume()
        vModel.checkAcceptDialogPermission()
        Logs.d(TAG, "onResume: current flow value has ${sharedViewModel._pendingTripsListFlow.value?.size ?: "null"} trips. Calling loadTrips(forced=true)")
        vModel.loadTrips(sharedViewModel._pendingTripsListFlow, forced = true)
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.pendingTripsListFlow.collect { arePending ->
                        Logs.d(TAG, "pendingTripsListFlow.collect: received ${arePending?.size ?: "null"} trips")
                        if (arePending.isNullOrEmpty()) {
                            Logs.d(TAG, "pendingTripsListFlow.collect: list is null or empty -> showing tvNoTrips")
                            vBinding.rvPendingTrips.visibility = View.GONE
                            vBinding.tvNoTrips.visibility = View.VISIBLE
                            vModel.checkClosePendingIfEmpty()
                        } else {
                            Logs.d(TAG, "pendingTripsListFlow.collect: list has ${arePending.size} trips -> calling setPendingTripsData")
                            vBinding.rvPendingTrips.visibility = View.VISIBLE
                            vBinding.tvNoTrips.visibility = View.GONE
                            setPendingTripsData(arePending)
                        }
                    }
                }

                launch {
                    sharedViewModel.eventPendingTrips.collect {
                        vModel.loadTrips(sharedViewModel._pendingTripsListFlow, forced = true)
                    }
                }
            }
        }
    }

    private fun setPendingTripsData(arePending: ArrayList<PendingTrip>) {
        Logs.d(TAG, "setPendingTripsData: submitting ${arePending.size} trips to adapter.")
        BravoCentral.setAuctionPeriodInSeconds(arePending[0].auctionPeriod)

        pendingTripsAdapter =
            PendingTripsAdapter(requireContext()) { tripId ->
                val trip = arePending.find { it.tripID == tripId }
                trip?.let {
                    if (vModel.showDialogOnAcceptFlow.value == true) {
                        requestTrip(it)
                    } else {
                        vModel.requestTrip(it)
                        try {
                            iMainActivity.showToast(R.string.strDatosTransmitidos)
                        } catch (e: IllegalStateException) {
                            Logs.d(TAG, "Couldn't show toast: ${e.message}")
                        }
                        iMainActivity.navigateBack()
                    }
                }
            }

        vBinding.rvPendingTrips.apply {
            adapter = pendingTripsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
        }

        pendingTripsAdapter.submitList(arePending)
    }

    fun requestTrip(pendingTrip: PendingTrip) {
        iMainActivity.openDialog(
            model = CustomDialog.CustomDialogModel(
                title = getString(R.string.auction_confirm),
                description = if (pendingTrip.pickupAddress.isNullOrBlank()) {
                    getString(R.string.confirm_auction_trip_no_address)
                } else {
                    getString(R.string.confirm_auction_trip, pendingTrip.pickupAddress)
                },
                buttons = buttonsDialog,
            ),
            response = {
                if (it.buttonPressed == ButtonType.ACCEPT) {
                    vModel.requestTrip(pendingTrip)
                    try {
                        iMainActivity.showToast(R.string.strDatosTransmitidos)
                    } catch (e: IllegalStateException) {
                        Logs.d(TAG, "Couldn't show toast: ${e.message}")
                    }
                    iMainActivity.navigateBack()
                }
            },
            fragmentManager = childFragmentManager
        )
    }

    override fun onPause() {
        vModel.removeHandlerCallback()
        sharedViewModel.finishPendingTripsTimer()
        super.onPause()
    }
}